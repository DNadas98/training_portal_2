package net.dnadas.training_portal.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.dto.auth.*;
import net.dnadas.training_portal.exception.auth.InvalidCredentialsException;
import net.dnadas.training_portal.exception.auth.UnauthorizedException;
import net.dnadas.training_portal.service.auth.AuthenticationService;
import net.dnadas.training_portal.service.user.PreRegistrationService;
import net.dnadas.training_portal.service.utils.security.CookieService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
  private final AuthenticationService authenticationService;
  private final PreRegistrationService preRegistrationService;
  private final CookieService cookieService;
  private final MessageSource messageSource;

//  @PostMapping("/register")
//  public ResponseEntity<?> register(
//    @RequestBody @Valid RegisterRequestDto dto, HttpServletResponse response) {
//    authenticationService.register(dto);
//    return loginExistingUser(response, dto.username(), dto.password(), HttpStatus.CREATED);
//  }

  @GetMapping("/preregistration-check")
  public ResponseEntity<?> checkPreRegistration(@RequestParam String code) {
    final UUID invitationCodeUuid;
    try {
      invitationCodeUuid = UUID.fromString(code);
    } catch (IllegalArgumentException e) {
      throw new InvalidCredentialsException();
    }
    preRegistrationService.checkPreRegistration(invitationCodeUuid);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("message","OK"));
  }

  @PostMapping("/preregistration-complete")
  public ResponseEntity<?> completePreRegistration(
    @RequestBody @Valid PreRegisterCompleteRequestDto dto, HttpServletResponse response,
    @RequestParam String code) {
    final UUID invitationCodeUuid;
    try {
      invitationCodeUuid = UUID.fromString(code);
    } catch (IllegalArgumentException e) {
      throw new InvalidCredentialsException();
    }
    preRegistrationService.completePreRegistration(dto, invitationCodeUuid);
    return loginExistingUser(response, dto.username().trim(), dto.password(), HttpStatus.CREATED);

  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
    @RequestBody @Valid LoginRequestDto loginRequest, HttpServletResponse response) {
    return loginExistingUser(response, loginRequest.username(), loginRequest.password(),
      HttpStatus.OK);
  }

  private ResponseEntity<?> loginExistingUser(
    HttpServletResponse response, String username, String password, HttpStatus status) {
    LoginResponseDto loginResponse = authenticationService.login(
      new LoginRequestDto(username, password));
    String refreshToken = authenticationService.getNewRefreshToken(
      new TokenPayloadDto(loginResponse.getUserInfo().username()));
    cookieService.addRefreshCookie(refreshToken, response);
    return ResponseEntity.status(status).body(Map.of("data", loginResponse));
  }

  @GetMapping("/refresh")
  public ResponseEntity<?> refresh(@CookieValue(required = false) String jwt) {
    try {
      RefreshResponseDto refreshResponse = authenticationService.refresh(
        new RefreshRequestDto(jwt));
      return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", refreshResponse));
    } catch (ValidationException e) {
      throw new UnauthorizedException();
    }
  }

  @GetMapping("/logout")
  public ResponseEntity<?> logout(
    @CookieValue(required = false) String jwt, HttpServletResponse response, Locale locale) {
    if (jwt == null) {
      return ResponseEntity.noContent().build();
    }
    cookieService.clearRefreshCookie(response);
    return ResponseEntity.status(HttpStatus.OK).body(
      Map.of("message", messageSource.getMessage("auth.logout.success", null, locale)));
  }
}
