package net.dnadas.training_portal.dto.group;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GroupResponsePublicDTO(
  @NotNull @Min(1) Long groupId,
  @NotNull String name,
  @NotNull String description) {
}
