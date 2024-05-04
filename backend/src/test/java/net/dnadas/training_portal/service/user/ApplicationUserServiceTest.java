package net.dnadas.training_portal.service.user;

import net.dnadas.training_portal.dto.email.EmailRequestDto;
import net.dnadas.training_portal.dto.user.*;
import net.dnadas.training_portal.dto.verification.VerificationTokenDto;
import net.dnadas.training_portal.exception.auth.InvalidCredentialsException;
import net.dnadas.training_portal.exception.auth.PasswordVerificationFailedException;
import net.dnadas.training_portal.exception.auth.UserAlreadyExistsException;
import net.dnadas.training_portal.exception.auth.UserNotFoundException;
import net.dnadas.training_portal.model.user.ApplicationUser;
import net.dnadas.training_portal.model.user.ApplicationUserDao;
import net.dnadas.training_portal.model.verification.EmailChangeVerificationToken;
import net.dnadas.training_portal.model.verification.EmailChangeVerificationTokenDao;
import net.dnadas.training_portal.model.verification.RegistrationTokenDao;
import net.dnadas.training_portal.service.utils.converter.UserConverter;
import net.dnadas.training_portal.service.utils.email.EmailService;
import net.dnadas.training_portal.service.utils.email.EmailTemplateService;
import net.dnadas.training_portal.service.verification.VerificationTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Service
class ApplicationUserServiceTest {
  private final List<ApplicationUser> testUsers = new ArrayList<>();
  @Mock
  private ApplicationUserDao applicationUserDao;
  @Mock
  private UserConverter userConverter;
  @Mock
  private UserProvider userProvider;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Mock
  private EmailChangeVerificationTokenDao emailChangeVerificationTokenDao;
  @Mock
  private EmailService emailService;
  @Mock
  private EmailTemplateService emailTemplateService;
  @Mock
  private VerificationTokenService verificationTokenService;
  @Mock
  private RegistrationTokenDao registrationTokenDao;

  @InjectMocks
  private ApplicationUserService applicationUserService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    ApplicationUser testUser1 = new ApplicationUser(
      "test1", "test1@test.test", "testpassword1", "Test 1");
    testUser1.setId(1L);
    ApplicationUser testUser2 = new ApplicationUser("test2", "test2@test.test", "testpassword2",
      "Test 2");
    testUser2.setId(2L);
    testUsers.add(testUser1);
    testUsers.add(testUser2);
  }

  @AfterEach
  void tearDown() {
    testUsers.clear();
  }

  @Test
  void getOwnUserDetails_returns_correct_user() {
    ApplicationUser authenticatedUser = testUsers.get(0);
    UserResponsePrivateDto expectedResponse = new UserResponsePrivateDto(testUsers.get(0).getId(),
      testUsers.get(0).getUsername(), testUsers.get(0).getEmail(), testUsers.get(0).getFullName());

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(userConverter.toUserResponsePrivateDto(authenticatedUser)).thenReturn(expectedResponse);
    UserResponsePrivateDto actualResponse = applicationUserService.getOwnUserDetails();

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  void getAllApplicationUsers_returns_list_of_ApplicationUsers() {
    when(applicationUserDao.findAll()).thenReturn(testUsers);
    List<UserResponsePublicDto> expectedResponse = new ArrayList<>();
    for (ApplicationUser user : testUsers) {
      expectedResponse.add(
        new UserResponsePublicDto(user.getId(), user.getUsername(), user.getFullName()));
    }
    when(userConverter.toUserResponsePublicDtos(testUsers)).thenReturn(expectedResponse);
    List<UserResponsePublicDto> actualResponse = applicationUserService.getAllApplicationUsers();

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  void getAllApplicationUsers_returns_empty_list() {
    when(applicationUserDao.findAll()).thenReturn(new ArrayList<>());
    List<UserResponsePublicDto> expectedResponse = new ArrayList<>();
    when(userConverter.toUserResponsePublicDtos(new ArrayList<>())).thenReturn(expectedResponse);
    List<UserResponsePublicDto> actualResponse = applicationUserService.getAllApplicationUsers();

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  void getApplicationUserById_returns_correct_user() {
    Long userId = 1L;
    ApplicationUser user = testUsers.get(0);
    UserResponsePrivateDto expectedResponse = new UserResponsePrivateDto(testUsers.get(0).getId(),
      testUsers.get(0).getUsername(), testUsers.get(0).getEmail(), testUsers.get(0).getFullName());

    when(applicationUserDao.findById(userId)).thenReturn(java.util.Optional.of(user));
    when(userConverter.toUserResponsePrivateDto(user)).thenReturn(expectedResponse);
    UserResponsePrivateDto actualResponse = applicationUserService.getApplicationUserById(userId);

    assertEquals(expectedResponse, actualResponse);
  }

  @Test
  void getApplicationUserById_throws_UserNotFoundException() {
    Long userId = 3L;
    when(applicationUserDao.findById(userId)).thenReturn(java.util.Optional.empty());

    assertThrows(
      UserNotFoundException.class, () -> applicationUserService.getApplicationUserById(userId));
  }

  @Test
  void updateFullName_updates_fullName_when_password_correct() {
    ApplicationUser authenticatedUser = testUsers.get(0);
    UserFullNameUpdateDto updateDto = new UserFullNameUpdateDto("New Name", "password");

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(passwordEncoder.matches(updateDto.password(), authenticatedUser.getPassword())).thenReturn(
      true);
    applicationUserService.updateFullName(updateDto);

    assertEquals(updateDto.fullName(), authenticatedUser.getFullName());
  }

  @Test
  void updateFullName_throws_PasswordVerificationFailedException_when_password_incorrect() {
    ApplicationUser authenticatedUser = testUsers.get(0);
    UserFullNameUpdateDto updateDto = new UserFullNameUpdateDto("New Name", "password");

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(passwordEncoder.matches(updateDto.password(), authenticatedUser.getPassword())).thenReturn(
      false);

    assertThrows(
      PasswordVerificationFailedException.class,
      () -> applicationUserService.updateFullName(updateDto));
  }

  @Test
  void updatePassword_updates_password_to_new_hashed_password() {
    ApplicationUser authenticatedUser = testUsers.get(0);
    UserPasswordUpdateDto updateDto = new UserPasswordUpdateDto(
      authenticatedUser.getPassword(), "newPassword");

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(passwordEncoder.matches(anyString(), anyString())).thenReturn(
      true);
    applicationUserService.updatePassword(updateDto);

    verify(passwordEncoder).encode(updateDto.newPassword());
    assertNotEquals(authenticatedUser.getPassword(), updateDto.newPassword());
    verify(applicationUserDao).save(authenticatedUser);
  }

  @Test
  void updatePassword_throws_PasswordVerificationFailedException_when_password_incorrect() {
    ApplicationUser authenticatedUser = testUsers.get(0);
    UserPasswordUpdateDto updateDto = new UserPasswordUpdateDto("password", "newPassword");

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(passwordEncoder.matches(updateDto.password(), authenticatedUser.getPassword())).thenReturn(
      false);

    assertThrows(
      PasswordVerificationFailedException.class,
      () -> applicationUserService.updatePassword(updateDto));
  }

  @Test
  void archiveOwnApplicationUser_archives_user() {
    ApplicationUser authenticatedUser = testUsers.get(0);

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    applicationUserService.archiveOwnApplicationUser();

    assertTrue(authenticatedUser.getActualUsername().contains("archived"));
    assertTrue(authenticatedUser.getEmail().contains("archived"));
    assertTrue(authenticatedUser.getFullName().toLowerCase().contains("archived"));
    assertEquals("", authenticatedUser.getPassword());
    assertFalse(authenticatedUser.isEnabled());
  }


  @Test
  void archiveApplicationUserById_archives_user() {
    ApplicationUser user = testUsers.get(0);
    Long userId = user.getId();

    when(applicationUserDao.findById(userId)).thenReturn(java.util.Optional.of(user));
    applicationUserService.archiveApplicationUserById(userId);

    assertTrue(user.getUsername().contains("archived"));
    assertTrue(user.getEmail().contains("archived"));
    assertEquals("", user.getPassword());
    assertFalse(user.isEnabled());
  }

  @Test
  void changeEmail_changes_email_when_verification_token_is_valid() {
    VerificationTokenDto verificationTokenDto = new VerificationTokenDto(1L, UUID.randomUUID());
    EmailChangeVerificationToken verificationToken = new EmailChangeVerificationToken(
      "newEmail@test.test", 1L, "hashedVerificationCode");
    ApplicationUser user = testUsers.get(0);

    when(verificationTokenService.findVerificationToken(verificationTokenDto)).thenReturn(
      verificationToken);
    when(applicationUserDao.findById(verificationToken.getUserId())).thenReturn(
      java.util.Optional.of(user));
    applicationUserService.changeEmail(verificationTokenDto);

    assertEquals("newEmail@test.test", user.getEmail());
  }

  @Test
  void changeEmail_throws_InvalidCredentialsException_when_user_not_found() {
    VerificationTokenDto verificationTokenDto = new VerificationTokenDto(1L, UUID.randomUUID());
    EmailChangeVerificationToken verificationToken = new EmailChangeVerificationToken(
      "newEmail@test.test", 1L, "hashedVerificationCode");

    when(verificationTokenService.findVerificationToken(verificationTokenDto)).thenReturn(
      verificationToken);
    when(applicationUserDao.findById(verificationToken.getUserId())).thenReturn(
      java.util.Optional.empty());

    assertThrows(
      InvalidCredentialsException.class,
      () -> applicationUserService.changeEmail(verificationTokenDto));
  }

  @Test
  void changeEmail_deletes_verification_token_after_successful_email_change() {
    VerificationTokenDto verificationTokenDto = new VerificationTokenDto(1L, UUID.randomUUID());
    EmailChangeVerificationToken verificationToken = new EmailChangeVerificationToken(
      "newEmail@test.test", 1L, "hashedVerificationCode");
    ApplicationUser user = testUsers.get(0);

    when(verificationTokenService.findVerificationToken(verificationTokenDto)).thenReturn(
      verificationToken);
    when(applicationUserDao.findById(verificationToken.getUserId())).thenReturn(
      java.util.Optional.of(user));
    applicationUserService.changeEmail(verificationTokenDto);

    verify(verificationTokenService).deleteVerificationToken(verificationToken.getId());
  }

  @Test
  void sendEmailChangeVerificationEmail_sends_verification_email_when_password_is_correct_and_email_is_not_taken()
    throws Exception {
    UserEmailUpdateDto updateDto = new UserEmailUpdateDto("newEmail@test.test", "testpassword1");
    ApplicationUser authenticatedUser = testUsers.get(0);
    UUID verificationCode = UUID.randomUUID();
    EmailChangeVerificationToken savedVerificationToken = new EmailChangeVerificationToken(
      "newEmail@test.test", 1L,
      verificationTokenService.getHashedVerificationCode(verificationCode));
    VerificationTokenDto verificationTokenDto = new VerificationTokenDto(
      savedVerificationToken.getId(), verificationCode);
    EmailRequestDto emailRequestDto = emailTemplateService.getEmailChangeVerificationEmailDto(
      verificationTokenDto, updateDto.email(), authenticatedUser.getActualUsername(),
      Locale.of("hu", "HU"));

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(passwordEncoder.matches(updateDto.password(), authenticatedUser.getPassword())).thenReturn(
      true);
    when(registrationTokenDao.findByEmailOrUsername(anyString(), anyString())).thenReturn(
      Optional.empty());
    when(applicationUserDao.findByEmail(updateDto.email())).thenReturn(Optional.empty());
    when(emailChangeVerificationTokenDao.findByNewEmail(updateDto.email())).thenReturn(
      Optional.empty());
    when(emailChangeVerificationTokenDao.findByUserId(authenticatedUser.getId())).thenReturn(
      Optional.empty());
    when(emailChangeVerificationTokenDao.save(any(EmailChangeVerificationToken.class))).thenReturn(
      savedVerificationToken);
    doNothing().when(emailService).sendMailToUserAddress(emailRequestDto);

    applicationUserService.sendEmailChangeVerificationEmail(updateDto, Locale.of("hu", "HU"));

    verify(emailService).sendMailToUserAddress(emailRequestDto);
  }

  @Test
  void sendEmailChangeVerificationEmail_throws_PasswordVerificationFailedException_when_password_incorrect() {
    UserEmailUpdateDto updateDto = new UserEmailUpdateDto("newEmail@test.test", "wrongpassword");
    ApplicationUser authenticatedUser = testUsers.get(0);

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(passwordEncoder.matches(updateDto.password(), authenticatedUser.getPassword())).thenReturn(
      false);

    assertThrows(
      PasswordVerificationFailedException.class,
      () -> applicationUserService.sendEmailChangeVerificationEmail(updateDto,
        Locale.of("hu", "HU")));
  }

  @Test
  void sendEmailChangeVerificationEmail_throws_UserAlreadyExistsException_when_email_already_taken() {
    UserEmailUpdateDto updateDto = new UserEmailUpdateDto("test1@test.test", "testpassword1");
    ApplicationUser authenticatedUser = testUsers.get(0);

    when(userProvider.getAuthenticatedUser()).thenReturn(authenticatedUser);
    when(passwordEncoder.matches(updateDto.password(), authenticatedUser.getPassword())).thenReturn(
      true);
    when(applicationUserDao.findByEmail(updateDto.email())).thenReturn(
      Optional.of(authenticatedUser));

    assertThrows(
      UserAlreadyExistsException.class,
      () -> applicationUserService.sendEmailChangeVerificationEmail(updateDto,
        Locale.of("hu", "HU")));
  }
}