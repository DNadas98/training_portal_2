package net.dnadas.training_portal.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.dto.user.UserResponsePrivateDto;
import net.dnadas.training_portal.dto.user.UserResponsePublicDto;
import net.dnadas.training_portal.service.user.ApplicationUserService;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class GlobalAdminUserController {
  private final ApplicationUserService applicationUserService;
  private final MessageSource messageSource;

  @GetMapping
  public ResponseEntity<?> getAllApplicationUsers() {
    List<UserResponsePublicDto> users = applicationUserService.getAllApplicationUsers();
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", users));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getApplicationUserById(@PathVariable @Min(1) Long id) {
    UserResponsePrivateDto user = applicationUserService.getApplicationUserById(id);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", user));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteApplicationUserById(
    @PathVariable @Min(
      1) Long id, Locale locale) {
    applicationUserService.archiveApplicationUserById(id);
    return ResponseEntity.status(HttpStatus.OK).body(
      Map.of("message", messageSource.getMessage("user.delete.success", null, locale)));
  }
}
