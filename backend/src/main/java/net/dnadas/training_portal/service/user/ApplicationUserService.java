package net.dnadas.training_portal.service.user;

import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.dto.user.UserPasswordUpdateDto;
import net.dnadas.training_portal.dto.user.UserResponsePrivateDto;
import net.dnadas.training_portal.dto.user.UserResponsePublicDto;
import net.dnadas.training_portal.exception.auth.PasswordVerificationFailedException;
import net.dnadas.training_portal.exception.auth.UnauthorizedException;
import net.dnadas.training_portal.exception.auth.UserNotFoundException;
import net.dnadas.training_portal.model.auth.GlobalRole;
import net.dnadas.training_portal.model.user.ApplicationUser;
import net.dnadas.training_portal.model.user.ApplicationUserDao;
import net.dnadas.training_portal.service.utils.converter.UserConverter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationUserService {
  private final ApplicationUserDao applicationUserDao;
  private final UserConverter userConverter;
  private final UserProvider userProvider;
  private final PasswordEncoder passwordEncoder;

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

  private void archiveUser(ApplicationUser user) {
    String archived = UUID.randomUUID() + "archived";
    user.setUsername(archived);
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
}
