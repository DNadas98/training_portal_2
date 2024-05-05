package net.dnadas.training_portal.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import net.dnadas.training_portal.model.auth.PermissionType;
import org.hibernate.validator.constraints.Length;

import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
/**
 * Represents a pre-registered user.
 @param username the username of the user
 @param groupPermissions the permissions of the user
 @param projectPermissions the permissions of the user
 @param coordinatorName the name of the coordinator - nullable
 @param dataPreparatorName the name of the data preparator - nullable
 @param hasExternalTestQuestionnaire whether the user has an external test questionnaire - nullable
 @param hasExternalTestFailure whether the user has an external test failure - nullable
 */
public final class PreRegisterUserInternalDto {
  private final @NotNull @Length(min = 1, max = 50) String username;
  private final @NotNull Set<PermissionType> groupPermissions;
  private final @NotNull Set<PermissionType> projectPermissions;
  private final @Length(min = 1, max = 100) String coordinatorName;
  private final @Length(min = 1, max = 100) String dataPreparatorName;
  private final Boolean hasExternalTestQuestionnaire;
  private final Boolean hasExternalTestFailure;

  @ToString.Exclude
  private String invitationCode;
}
