package net.dnadas.training_portal.dto.auth;

import jakarta.validation.constraints.NotNull;
import net.dnadas.training_portal.model.auth.GlobalRole;

import java.util.Set;

public record UserInfoDto(
  @NotNull String username,
  @NotNull Set<GlobalRole> roles) {
}
