package net.dnadas.training_portal.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.model.auth.PermissionType;
import net.dnadas.training_portal.service.group.GroupAdminService;
import net.dnadas.training_portal.service.group.project.ProjectService;
import net.dnadas.training_portal.service.group.project.task.TaskRoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/api/v1/user/permissions")
@RequiredArgsConstructor
public class UserPermissionController {
  private final GroupAdminService groupAdminService;
  private final ProjectService projectService;
  private final TaskRoleService taskRoleService;

  @GetMapping("/groups/{groupId}")
  public ResponseEntity<?> getOwnPermissionsForGroup(@PathVariable @Min(1) Long groupId) {
    Set<PermissionType> permissions = groupAdminService.getUserPermissionsForGroup(groupId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", permissions));
  }

  @GetMapping("/groups/{groupId}/projects/{projectId}")
  public ResponseEntity<?> getOwnPermissionsForProject(
    @PathVariable @Min(1) Long groupId, @PathVariable @Min(1) Long projectId
  ) {
    Set<PermissionType> permissions = projectService.getUserPermissionsForProject(
      groupId, projectId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", permissions));
  }

  @GetMapping("/groups/{groupId}/projects/{projectId}/tasks/{taskId}")
  public ResponseEntity<?> getOwnPermissionsForTask(
    @PathVariable @Min(1) Long groupId, @PathVariable @Min(1) Long projectId,
    @PathVariable @Min(1) Long taskId
  ) {
    Set<PermissionType> permissions = taskRoleService.getUserPermissionsForTask(
      groupId, projectId, taskId);
    return ResponseEntity.status(HttpStatus.OK).body(Map.of("data", permissions));
  }
}
