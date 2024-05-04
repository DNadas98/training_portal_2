package net.dnadas.training_portal.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.dnadas.training_portal.model.auth.PermissionType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Invitation {
  @Column(unique = true, nullable = false)
  String username;
  @Column(nullable = false)
  Long groupId;
  @Column(nullable = false)
  Long projectId;
  @Column(nullable = false)
  Long questionnaireId;
  Set<PermissionType> groupPermissions;
  Set<PermissionType> projectPermissions;
  String coordinatorName;
  String dataPreparatorName;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private UUID invitationCode;
  @CreationTimestamp
  private Instant createdAt;
  @Column(nullable = false)
  private Instant expiresAt;
  @Column(nullable = true)
  @ToString.Exclude
  private Boolean hasExternalTestQuestionnaire;

  @Column(nullable = true)
  @ToString.Exclude
  private Boolean hasExternalTestFailure;

  public Invitation(
    UUID invitationCode, Instant expiresAt, String username, Long groupId, Long projectId,
    Long questionnaireId, Set<PermissionType> groupPermissions,
    Set<PermissionType> projectPermissions, String coordinatorName, String dataPreparatorName,
    Boolean hasExternalTestQuestionnaire, Boolean hasExternalTestFailure) {
    this.invitationCode = invitationCode;
    this.expiresAt = expiresAt;
    this.username = username;
    this.groupId = groupId;
    this.projectId = projectId;
    this.questionnaireId = questionnaireId;
    this.groupPermissions = groupPermissions;
    this.projectPermissions = projectPermissions;
    this.coordinatorName = coordinatorName;
    this.dataPreparatorName = dataPreparatorName;
    this.hasExternalTestQuestionnaire = hasExternalTestQuestionnaire;
    this.hasExternalTestFailure = hasExternalTestFailure;
  }
}
