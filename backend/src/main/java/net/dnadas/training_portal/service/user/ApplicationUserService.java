package net.dnadas.training_portal.service.user;

import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.dto.email.EmailRequestDto;
import net.dnadas.training_portal.dto.user.*;
import net.dnadas.training_portal.dto.verification.VerificationTokenDto;
import net.dnadas.training_portal.exception.auth.*;
import net.dnadas.training_portal.model.auth.GlobalRole;
import net.dnadas.training_portal.model.user.ApplicationUser;
import net.dnadas.training_portal.model.user.ApplicationUserDao;
import net.dnadas.training_portal.model.verification.EmailChangeVerificationToken;
import net.dnadas.training_portal.service.utils.converter.UserConverter;
import net.dnadas.training_portal.service.utils.email.EmailService;
import net.dnadas.training_portal.service.utils.email.EmailTemplateService;
import net.dnadas.training_portal.service.verification.VerificationTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationUserService {
  private final ApplicationUserDao applicationUserDao;
  private final UserConverter userConverter;
  private final UserProvider userProvider;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final EmailTemplateService emailTemplateService;
  private final VerificationTokenService verificationTokenService;

  public UserResponsePrivateDto getOwnUserDetails() throws UnauthorizedException {
    ApplicationUser applicationUser = userProvider.getAuthenticatedUser();
    return userConverter.toUserResponsePrivateDto(applicationUser);
  }

  public List<UserResponsePublicDto> getAllApplicationUsers() {
    List<ApplicationUser> users = applicationUserDao.findAll();
    return userConverter.toUserResponsePublicDtos(users);
  }

  public UserResponsePrivateDto getApplicationUserById(Long userId) throws UserNotFoundException {
    ApplicationUser user = applicationUserDao.findById(userId).orElseThrow(
      () -> new UserNotFoundException(userId));
    return userConverter.toUserResponsePrivateDto(user);
  }

  @Transactional(rollbackFor = Exception.class)
  public void updateFullName(UserFullNameUpdateDto updateDto) {
    ApplicationUser applicationUser = userProvider.getAuthenticatedUser();
    verifyPassword(updateDto.password(), applicationUser);
    applicationUser.setFullName(updateDto.fullName());
    applicationUserDao.save(applicationUser);
  }

  @Transactional(rollbackFor = Exception.class)
  public void updatePassword(UserPasswordUpdateDto updateDto) {
    ApplicationUser applicationUser = userProvider.getAuthenticatedUser();
    verifyPassword(updateDto.password(), applicationUser);
    applicationUser.setPassword(passwordEncoder.encode(updateDto.newPassword()));
    applicationUserDao.save(applicationUser);
  }

  @Transactional(rollbackFor = Exception.class)
  public void archiveOwnApplicationUser() {
    ApplicationUser user = userProvider.getAuthenticatedUser();
    archiveUser(user);
  }

  @Transactional(rollbackFor = Exception.class)
  public void archiveApplicationUserById(Long id) {
    ApplicationUser user = applicationUserDao.findById(id).orElseThrow(
      () -> new UserNotFoundException(id));
    if (user.getGlobalRoles().contains(GlobalRole.ADMIN)) {
      throw new UnauthorizedException();
    }
    archiveUser(user);
  }

  @Transactional(rollbackFor = Exception.class)
  public void sendEmailChangeVerificationEmail(UserEmailUpdateDto updateDto, Locale locale)
    throws Exception {
    VerificationTokenDto verificationTokenDto = null;
    try {
      ApplicationUser applicationUser = userProvider.getAuthenticatedUser();
      verifyPassword(updateDto.password(), applicationUser);

      verifyChangedEmail(updateDto, applicationUser);
      verifyEmailNotTaken(updateDto);
      verificationTokenService.verifyNoEmailChangeTokenWithId(applicationUser.getId());

      verificationTokenDto = verificationTokenService.saveEmailChangeVerificationToken(
        updateDto, applicationUser);

      EmailRequestDto emailRequestDto = emailTemplateService.getEmailChangeVerificationEmailDto(
        verificationTokenDto, updateDto.email(), applicationUser.getFullName(), locale);

      emailService.sendMailToUserAddress(emailRequestDto);
    } catch (Exception e) {
      verificationTokenService.cleanupVerificationToken(verificationTokenDto);
      throw e;
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void changeEmail(VerificationTokenDto verificationTokenDto) {
    EmailChangeVerificationToken verificationToken =
      (EmailChangeVerificationToken) verificationTokenService.findVerificationToken(
        verificationTokenDto);
    ApplicationUser user = applicationUserDao.findById(verificationToken.getUserId()).orElseThrow(
      InvalidCredentialsException::new);
    user.setEmail(verificationToken.getNewEmail());
    applicationUserDao.save(user);
    verificationTokenService.deleteVerificationToken(verificationToken.getId());
  }

  private void archiveUser(ApplicationUser user) {
    String archived = UUID.randomUUID() + "archived";
    user.setUsername(archived);
    user.setEmail(archived + "@" + archived + ".net");
    user.setFullName("Archived User");
    user.setPassword("");
    user.setEnabled(false);
    applicationUserDao.save(user);
  }

  private void verifyPassword(String password, ApplicationUser applicationUser) {
    if (password == null || !passwordEncoder.matches(
      password, applicationUser.getPassword())) {
      throw new PasswordVerificationFailedException();
    }
  }

  private void verifyEmailNotTaken(UserEmailUpdateDto updateDto) {
    applicationUserDao.findByEmail(updateDto.email()).ifPresent(user -> {
      throw new UserAlreadyExistsException();
    });
    verificationTokenService.verifyTokenDoesNotExistWith(updateDto.email());
  }

  private void verifyChangedEmail(
    UserEmailUpdateDto updateDto, ApplicationUser applicationUser) {
    if (applicationUser.getEmail().equals(updateDto.email())) {
      throw new UserAlreadyExistsException();
    }
  }
}
