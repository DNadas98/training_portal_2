package net.dnadas.training_portal.model.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.dnadas.training_portal.model.auth.GlobalRole;
import net.dnadas.training_portal.model.group.UserGroup;
import net.dnadas.training_portal.model.group.project.Project;
import net.dnadas.training_portal.model.group.project.questionnaire.Questionnaire;
import net.dnadas.training_portal.model.group.project.questionnaire.QuestionnaireSubmission;
import net.dnadas.training_portal.model.group.project.task.Task;
import net.dnadas.training_portal.model.request.ProjectJoinRequest;
import net.dnadas.training_portal.model.request.UserGroupJoinRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "application_user")
public class ApplicationUser implements UserDetails {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @Column(nullable = true)
  @ToString.Exclude
  private String coordinatorUsername;

  @Column(nullable = true)
  @ToString.Exclude
  private String dataPreparatorUsername;

  @Column(nullable = true)
  @ToString.Exclude
  private Boolean hasExternalTestQuestionnaire;

  @Column(nullable = true)
  @ToString.Exclude
  private Boolean hasExternalTestFailure;

  @Column(nullable = false)
  private boolean expired;

  @Column(nullable = false)
  private boolean locked;

  @Column(nullable = false)
  private boolean credentialsExpired;

  @Column(nullable = false)
  private boolean enabled;

  @Enumerated(EnumType.STRING)
  private Set<GlobalRole> globalRoles = new HashSet<>();

  @ManyToMany(mappedBy = "admins", fetch = FetchType.LAZY)
  @OrderBy("name ASC")
  private List<UserGroup> adminUserGroups = new ArrayList<>();

  @ManyToMany(mappedBy = "editors", fetch = FetchType.LAZY)
  @OrderBy("name ASC")
  private List<UserGroup> editorUserGroups = new ArrayList<>();

  @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
  @OrderBy("name ASC")
  private List<UserGroup> memberUserGroups = new ArrayList<>();

  @ManyToMany(mappedBy = "admins", fetch = FetchType.LAZY)
  @OrderBy("startDate ASC, name ASC")
  private List<Project> adminProjects = new ArrayList<>();

  @ManyToMany(mappedBy = "coordinators", fetch = FetchType.LAZY)
  @OrderBy("startDate ASC, name ASC")
  private List<Project> coordinatorProjects = new ArrayList<>();

  @ManyToMany(mappedBy = "editors", fetch = FetchType.LAZY)
  @OrderBy("startDate ASC, name ASC")
  private List<Project> editorProjects = new ArrayList<>();

  @ManyToMany(mappedBy = "assignedMembers", fetch = FetchType.LAZY)
  @OrderBy("startDate ASC, name ASC")
  private List<Project> assignedProjects = new ArrayList<>();


  @ManyToMany(mappedBy = "assignedMembers", fetch = FetchType.LAZY)
  @OrderBy("startDate ASC, name ASC")
  private List<Task> assignedTasks = new ArrayList<>();

  @OneToMany(mappedBy = "applicationUser", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @OrderBy("updatedAt DESC")
  private List<UserGroupJoinRequest> joinRequests = new ArrayList<>();

  @OneToMany(mappedBy = "applicationUser", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @OrderBy("updatedAt DESC")
  private List<ProjectJoinRequest> projectJoinRequests = new ArrayList<>();

  @OneToMany(mappedBy = "createdBy", fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
  @OrderBy("updatedAt DESC")
  private List<Questionnaire> createdQuestionnaires = new ArrayList<>();

  @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
  @OrderBy("updatedAt DESC")
  private List<Questionnaire> lastUpdatedQuestionnaires = new ArrayList<>();

  @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  @OrderBy("createdAt DESC")
  private List<QuestionnaireSubmission> questionnaireSubmissions = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.DETACH)
  @JoinColumn(name = "active_questionnaire_id")
  private Questionnaire activeQuestionnaire = null;

  public ApplicationUser(String username, String password) {
    this.username = username.trim();
    this.password = password;
    this.enabled = true;
    this.expired = false;
    this.locked = false;
    this.credentialsExpired = false;
    globalRoles.add(GlobalRole.USER);
  }

  public Set<GlobalRole> getGlobalRoles() {
    return Set.copyOf(globalRoles);
  }

  public void addGlobalRole(GlobalRole globalRole) {
    this.globalRoles.add(globalRole);
  }

  // UserDetails

  /**
   * Returns the authorities granted to the user. Cannot return <code>null</code>.
   *
   * @return the authorities, sorted by natural key (never <code>null</code>)
   */
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return this.globalRoles.stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(
      Collectors.toSet());
  }

  /**
   * Returns the password used to authenticate the user.
   *
   * @return the password
   */
  @Override
  public String getPassword() {
    return this.password;
  }

  @Override
  public String getUsername() {
    return this.username;
  }

  /**
   * Indicates whether the user's account has expired. An expired account cannot be
   * authenticated.
   *
   * @return <code>true</code> if the user's account is valid (ie non-expired),
   * <code>false</code> if no longer valid (ie expired)
   */
  @Override
  public boolean isAccountNonExpired() {
    return !this.expired;
  }

  /**
   * Indicates whether the user is locked or unlocked. A locked user cannot be
   * authenticated.
   *
   * @return <code>true</code> if the user is not locked, <code>false</code> otherwise
   */
  @Override
  public boolean isAccountNonLocked() {
    return !this.locked;
  }

  /**
   * Indicates whether the user's credentials (newPassword) has expired. Expired
   * credentials prevent authentication.
   *
   * @return <code>true</code> if the user's credentials are valid (ie non-expired),
   * <code>false</code> if no longer valid (ie expired)
   */
  @Override
  public boolean isCredentialsNonExpired() {
    return !this.credentialsExpired;
  }

  /**
   * Indicates whether the user is enabled or disabled. A disabled user cannot be
   * authenticated.
   *
   * @return <code>true</code> if the user is enabled, <code>false</code> otherwise
   */
  @Override
  public boolean isEnabled() {
    return this.enabled;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username, globalRoles);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ApplicationUser applicationUser)) {
      return false;
    }
    return Objects.equals(id, applicationUser.id) &&
      Objects.equals(
        username, applicationUser.username) && Objects.equals(
      globalRoles, applicationUser.globalRoles);
  }

  @Override
  public String toString() {
    return "ApplicationUser{" +
      "id=" + id +
      ", username='" + username + '\'' +
      ", globalRoles=" + globalRoles +
      '}';
  }

  @PreRemove
  private void preRemove() {
    // Disassociate from UserGroup entities
    for (UserGroup userGroup : new ArrayList<>(memberUserGroups)) {
      userGroup.deleteMember(this);
    }

    // Disassociate from Project entities
    for (Project project : new ArrayList<>(assignedProjects)) {
      project.deleteMember(this);
    }

    // Disassociate from Task entities
    for (Task task : new ArrayList<>(assignedTasks)) {
      task.removeMember(this);
    }

    // Disassociate from Questionnaire entities, but keep the Questionnaires
    for (Questionnaire questionnaire : new ArrayList<>(createdQuestionnaires)) {
      questionnaire.setCreatedBy(null);
    }
    for (Questionnaire questionnaire : new ArrayList<>(lastUpdatedQuestionnaires)) {
      questionnaire.setUpdatedBy(null);
    }
  }
}

