package net.dnadas.training_portal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.dto.user.UserPasswordUpdateDto;
import net.dnadas.training_portal.dto.user.UserResponsePrivateDto;
import net.dnadas.training_portal.service.user.ApplicationUserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
  private final ApplicationUserService applicationUserService;
  private final MessageSource messageSource;

  @GetMapping
  public ResponseEntity<?> getOwnApplicationUser() {
    UserResponsePrivateDto userDetails = applicationUserService.getOwnUserDetails();
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", userDetails));
  }

  @PatchMapping("/password")
  public ResponseEntity<?> updatePassword(
    @RequestBody @Valid UserPasswordUpdateDto updateDto, Locale locale) {
    applicationUserService.updatePassword(updateDto);
    return ResponseEntity.status(HttpStatus.OK).body(
      Map.of("message", messageSource.getMessage(
        "user.details.update.success", null, locale)));
  }

  @DeleteMapping
  public ResponseEntity<?> deleteOwnApplicationUser(Locale locale) {
    applicationUserService.archiveOwnApplicationUser();
    return ResponseEntity.status(HttpStatus.OK).body(
      Map.of("message", messageSource.getMessage("user.delete.success", null, locale)));
  }
}
