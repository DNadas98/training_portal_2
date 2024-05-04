package net.dnadas.training_portal.dto.auth;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record TokenPayloadDto(@NotNull @Length(min = 1, max = 50) String username) {
}
