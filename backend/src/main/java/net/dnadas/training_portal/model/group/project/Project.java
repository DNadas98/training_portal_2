package net.dnadas.training_portal.model.group.project;

import jakarta.persistence.*;
import lombok.*;
import net.dnadas.training_portal.model.group.UserGroup;
import net.dnadas.training_portal.model.group.project.questionnaire.Questionnaire;
import net.dnadas.training_portal.model.group.project.task.Task;
import net.dnadas.training_portal.model.request.ProjectJoinRequest;
import net.dnadas.training_portal.model.user.ApplicationUser;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(length = 255, nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private String description;

  @Column(length = 10000, nullable = false)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private String detailedDescription;

  @Column(nullable = false)
  private Instant startDate;

  @Column(nullable = false)
  private Instant deadline;

  @ManyToOne
  @JoinColumn(name = "group_id")
  private UserGroup userGroup;

  @OneToMany(mappedBy = "project", orphanRemoval = true, cascade = CascadeType.REMOVE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OrderBy("startDate ASC")
  private List<Task> tasks = new ArrayList<>();

  @OneToMany(mappedBy = "project", orphanRemoval = true, cascade = CascadeType.REMOVE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OrderBy("createdAt DESC")
  private List<Questionnaire> questionnaires = new ArrayList<>();

  @OneToMany(mappedBy = "project", orphanRemoval = true, cascade = CascadeType.REMOVE)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OrderBy("updatedAt ASC")
  private List<ProjectJoinRequest> joinRequests = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "project_admins", joinColumns = @JoinColumn(name = "project_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OrderBy("username ASC")
  private List<ApplicationUser> admins = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "project_coordinators", joinColumns = @JoinColumn(name = "project_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OrderBy("username ASC")
  private List<ApplicationUser> coordinators = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "project_editors", joinColumns = @JoinColumn(name = "project_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OrderBy("username ASC")
  private List<ApplicationUser> editors = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "project_assigned_members", joinColumns = @JoinColumn(name = "project_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OrderBy("username ASC")
  private List<ApplicationUser> assignedMembers = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "project_inactive_members", joinColumns = @JoinColumn(name = "project_id"),
    inverseJoinColumns = @JoinColumn(name = "user_id"))
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  @OrderBy("username ASC")
  private List<ApplicationUser> inactiveMembers = new ArrayList<>();

  public Project(
    String name, String description, String detailedDescription, Instant startDate,
    Instant deadline,
    ApplicationUser projectCreator, UserGroup userGroup) {
    this.name = name;
    this.description = description;
    this.detailedDescription = detailedDescription;
    this.startDate = startDate;
    this.deadline = deadline;
    admins.add(projectCreator);
    editors.add(projectCreator);
    assignedMembers.add(projectCreator);
    this.userGroup = userGroup;
  }

  public List<Task> getTasks() {
    return List.copyOf(tasks);
  }

  public List<ApplicationUser> getAdmins() {
    return List.copyOf(admins);
  }

  public void addAdmin(ApplicationUser applicationUser) {
    if (!assignedMembers.contains(applicationUser)) {
      assignedMembers.add(applicationUser);
    }
    if (!admins.contains(applicationUser)) {
      admins.add(applicationUser);
    }
  }

  public void removeAdmin(ApplicationUser applicationUser) {
    this.admins.remove(applicationUser);
  }

  public List<ApplicationUser> getEditors() {
    return List.copyOf(editors);
  }

  public void addEditor(ApplicationUser applicationUser) {
    if (!assignedMembers.contains(applicationUser)) {
      assignedMembers.add(applicationUser);
    }
    if (!editors.contains(applicationUser)) {
      editors.add(applicationUser);
    }
  }

  public void removeEditor(ApplicationUser applicationUser) {
    this.editors.remove(applicationUser);
  }

  public List<ApplicationUser> getCoordinators() {
    return List.copyOf(coordinators);
  }

  public void addCoordinator(ApplicationUser applicationUser) {
    if (!coordinators.contains(applicationUser)) {
      coordinators.add(applicationUser);
    }
  }

  public void removeCoordinator(ApplicationUser applicationUser) {
    this.coordinators.remove(applicationUser);
  }

  public List<ApplicationUser> getAssignedMembers() {
    return List.copyOf(assignedMembers);
  }

  public void assignMember(ApplicationUser applicationUser) {
    if (!assignedMembers.contains(applicationUser)) {
      assignedMembers.add(applicationUser);
    }
  }

  public void removeMember(ApplicationUser applicationUser) {
    this.inactiveMembers.add(applicationUser);
    deleteMember(applicationUser);
  }

  public void deleteMember(ApplicationUser applicationUser) {
    this.assignedMembers.remove(applicationUser);
    if (this.editors.contains(applicationUser)) {
      this.editors.remove(applicationUser);
    }
    if (this.coordinators.contains(applicationUser)) {
      this.coordinators.remove(applicationUser);
    }
    if (this.admins.contains(applicationUser)) {
      this.admins.remove(applicationUser);
    }
  }
}
