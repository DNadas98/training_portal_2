package net.dnadas.training_portal.dto.user;

import jakarta.validation.constraints.NotNull;

public record UserResponsePrivateDto(
  @NotNull Long userId,
  @NotNull String username
) {
}
