package net.dnadas.training_portal.exception.group.project;

public class ProjectJoinRequestNotFoundException extends RuntimeException {
  private final Long id;

  public ProjectJoinRequestNotFoundException(Long id) {
    super("Project join request was not found");
    this.id = id;
  }

  public Long getId() {
    return id;
  }
}
