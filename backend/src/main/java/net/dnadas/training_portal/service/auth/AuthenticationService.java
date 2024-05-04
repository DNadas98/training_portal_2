package net.dnadas.training_portal.service.auth;


import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.dto.auth.*;
import net.dnadas.training_portal.exception.auth.InvalidCredentialsException;
import net.dnadas.training_portal.exception.auth.UnauthorizedException;
import net.dnadas.training_portal.exception.auth.UserAlreadyExistsException;
import net.dnadas.training_portal.model.group.UserGroup;
import net.dnadas.training_portal.model.group.project.Project;
import net.dnadas.training_portal.model.group.project.questionnaire.Questionnaire;
import net.dnadas.training_portal.model.user.ApplicationUser;
import net.dnadas.training_portal.model.user.ApplicationUserDao;
import net.dnadas.training_portal.model.user.Invitation;
import net.dnadas.training_portal.model.user.InvitationDao;
import net.dnadas.training_portal.service.utils.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
  private final ApplicationUserDao applicationUserDao;
  private final InvitationDao invitationDao;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  @Transactional(rollbackFor = Exception.class)
  public void register(RegisterRequestDto dto) {
    Optional<ApplicationUser> existingUser = applicationUserDao.findByUsername(dto.username());
    if (existingUser.isPresent()) {
      throw new UserAlreadyExistsException();
    }
    Optional<Invitation> invitation = invitationDao.findByUsername(dto.username());
    if (invitation.isPresent()) {
      //invitation by admin is prioritized over standard registration
      throw new UserAlreadyExistsException();
    }
    ApplicationUser user = new ApplicationUser(
      dto.username(), passwordEncoder.encode(dto.password()));
    user.setPassword(passwordEncoder.encode(dto.password()));
    applicationUserDao.save(user);
  }

  @Transactional(readOnly = true)
  public LoginResponseDto login(LoginRequestDto loginRequest) {
    try {
      authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password()));
    } catch (Exception e) {
      throw new InvalidCredentialsException();
    }
    ApplicationUser user = applicationUserDao.findByUsername(loginRequest.username()).orElseThrow(
      InvalidCredentialsException::new);
    TokenPayloadDto payloadDto = new TokenPayloadDto(user.getUsername());
    String accessToken = jwtService.generateAccessToken(payloadDto);

    Optional<Questionnaire> activeQuestionnaire = Optional.ofNullable(
      user.getActiveQuestionnaire());
    if (activeQuestionnaire.isPresent()) {
      return getLoginResponseWithActiveQuestionnaire(activeQuestionnaire.get(), accessToken, user);
    }

    return new LoginResponseDto(
      accessToken,
      new UserInfoDto(
        user.getUsername(), user.getGlobalRoles()));
  }

  private LoginResponseDto getLoginResponseWithActiveQuestionnaire(
    Questionnaire questionnaire, String accessToken, ApplicationUser user) {
    Project project = questionnaire.getProject();
    UserGroup group = project.getUserGroup();
    return new LoginResponseDto(
      accessToken,
      new UserInfoDto(
        user.getUsername(), user.getGlobalRoles()),
      group.getId(), project.getId(), questionnaire.getId());
  }


  public String getNewRefreshToken(TokenPayloadDto payloadDto) {
    return jwtService.generateRefreshToken(payloadDto);
  }

  @Transactional(readOnly = true)
  public RefreshResponseDto refresh(RefreshRequestDto refreshRequest) {
    String refreshToken = refreshRequest.refreshToken();
    TokenPayloadDto payload = jwtService.verifyRefreshToken(refreshToken);
    ApplicationUser user = applicationUserDao.findByUsername(payload.username()).orElseThrow(
      UnauthorizedException::new);
    String accessToken = jwtService.generateAccessToken(payload);
    return new RefreshResponseDto(
      accessToken,
      new UserInfoDto(
        user.getUsername(), user.getGlobalRoles()));
  }
}