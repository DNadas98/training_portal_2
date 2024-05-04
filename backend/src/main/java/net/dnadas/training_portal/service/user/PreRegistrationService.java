package net.dnadas.training_portal.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dnadas.training_portal.dto.auth.PreRegisterCompleteRequestDto;
import net.dnadas.training_portal.dto.user.PreRegisterUserInternalDto;
import net.dnadas.training_portal.dto.user.PreRegisterUsersReportDto;
import net.dnadas.training_portal.exception.auth.InvalidCredentialsException;
import net.dnadas.training_portal.exception.auth.UserAlreadyExistsException;
import net.dnadas.training_portal.exception.group.GroupNotFoundException;
import net.dnadas.training_portal.exception.group.project.ProjectNotFoundException;
import net.dnadas.training_portal.exception.group.project.questionnaire.QuestionnaireNotFoundException;
import net.dnadas.training_portal.exception.user.ExpirationDateNotWithinSpecifiedException;
import net.dnadas.training_portal.exception.user.InvitationExpiredException;
import net.dnadas.training_portal.exception.user.PastDateExpirationDateException;
import net.dnadas.training_portal.model.auth.PermissionType;
import net.dnadas.training_portal.model.group.UserGroup;
import net.dnadas.training_portal.model.group.UserGroupDao;
import net.dnadas.training_portal.model.group.project.Project;
import net.dnadas.training_portal.model.group.project.ProjectDao;
import net.dnadas.training_portal.model.group.project.questionnaire.Questionnaire;
import net.dnadas.training_portal.model.group.project.questionnaire.QuestionnaireDao;
import net.dnadas.training_portal.model.user.ApplicationUser;
import net.dnadas.training_portal.model.user.ApplicationUserDao;
import net.dnadas.training_portal.model.user.Invitation;
import net.dnadas.training_portal.model.user.InvitationDao;
import net.dnadas.training_portal.service.utils.datetime.DateTimeService;
import net.dnadas.training_portal.service.utils.file.CsvUtilsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreRegistrationService {
  private static final Integer RECEIVED_CSV_MAX_SIZE = 400000;
  private static final String RECEIVED_CSV_CONTENT_TYPE = "text/csv";
  private static final String CSV_DELIMITER = ",";
  private static final List<String> CSV_HEADERS = List.of("Username",
    "Group Permissions: available: " + PermissionType.GROUP_ADMIN.name() + " " +
      PermissionType.GROUP_EDITOR.name() + " and " + PermissionType.GROUP_MEMBER.name() +
      " default: " + PermissionType.GROUP_MEMBER.name(),
    "Project Permissions: available: " + PermissionType.PROJECT_ADMIN.name() + " " +
      PermissionType.PROJECT_COORDINATOR.name() + " " + PermissionType.PROJECT_EDITOR.name() +
      " and " + PermissionType.PROJECT_ASSIGNED_MEMBER.name() + " default: " +
      PermissionType.PROJECT_ASSIGNED_MEMBER.name(), "Current Coordinator Username or NULL",
    "Current Data Preparator Username or NULL",
    "Has External Test Questionnaire: TRUE FALSE or NULL",
    "Has External Test Failure: TRUE FALSE or NULL");
  private static final long MAX_EXPIRATION_DATE_SECONDS = 31536000; // 1 year
  private final ApplicationUserDao applicationUserDao;
  private final PasswordEncoder passwordEncoder;
  private final InvitationDao invitationDao;
  private final UserGroupDao userGroupDao;
  private final ProjectDao projectDao;
  private final QuestionnaireDao questionnaireDao;
  private final CsvUtilsService csvUtilsService;
  private final DateTimeService dateTimeService;

  public void getPreRegisterUsersCsvTemplate(OutputStream outputStream) throws IOException {
    List<List<String>> exampleData = List.of(
      List.of("exampleUser1", "GROUP_EDITOR",
        "PROJECT_ASSIGNED_MEMBER&PROJECT_EDITOR&PROJECT_ADMIN", "NULL", "NULL", "NULL", "NULL"),
      List.of("exampleUser2", "GROUP_MEMBER", "PROJECT_ASSIGNED_MEMBER&PROJECT_COORDINATOR", "NULL",
        "NULL", "NULL", "NULL"),
      List.of("exampleUser3", "GROUP_MEMBER", "PROJECT_ASSIGNED_MEMBER", "Example Coordinator",
        "Example Data Preparator", "TRUE", "FALSE"));
    csvUtilsService.writeCsvToStream(exampleData, CSV_DELIMITER, CSV_HEADERS, outputStream);
  }

  @Transactional(rollbackFor = Error.class)
  @PreAuthorize("hasPermission(#groupId, 'UserGroup', 'GROUP_ADMIN')")
  public PreRegisterUsersReportDto preRegisterUsers(
    Long groupId, Long projectId, Long questionnaireId, MultipartFile usersCsv, String expiration) {
    Instant expirationDate = dateTimeService.toStoredDate(expiration);
    if (expirationDate.isBefore(Instant.now())) {
      throw new PastDateExpirationDateException();
    }
    if (expirationDate.isAfter(Instant.now().plusSeconds(MAX_EXPIRATION_DATE_SECONDS))) {
      throw new ExpirationDateNotWithinSpecifiedException();
    }
    Questionnaire questionnaire = questionnaireDao.findByGroupIdAndProjectIdAndId(
      groupId, projectId, questionnaireId).orElseThrow(QuestionnaireNotFoundException::new);
    Project project = projectDao.findByIdAndGroupId(projectId, groupId).orElseThrow(
      () -> new ProjectNotFoundException(projectId));
    UserGroup group = userGroupDao.findById(groupId).orElseThrow(
      () -> new GroupNotFoundException(groupId));

    if (!group.getId().equals(groupId)) {
      throw new GroupNotFoundException(groupId);
    }
    if (!project.getId().equals(projectId)) {
      throw new ProjectNotFoundException(projectId);
    }

    return processPreRegistrationRequest(usersCsv, group, project, questionnaire, expirationDate);
  }

  @Transactional(rollbackFor = Exception.class)
  public void completePreRegistration(PreRegisterCompleteRequestDto dto, UUID invitationCode) {
    Optional<ApplicationUser> existingUser = applicationUserDao.findByUsername(dto.username());
    if (existingUser.isPresent()) {
      throw new UserAlreadyExistsException();
    }
    Invitation invitation = invitationDao.findByInvitationCodeAndUsername(
      invitationCode, dto.username()).orElseThrow(InvalidCredentialsException::new);
    if (invitation.getExpiresAt().isBefore(Instant.now())) {
      invitationDao.delete(invitation);
      throw new InvitationExpiredException();
    }
    ApplicationUser user = new ApplicationUser(
      invitation.getUsername(), passwordEncoder.encode(dto.password()));
    applicationUserDao.save(user);
    UserGroup group = userGroupDao.findById(invitation.getGroupId()).orElseThrow(
      () -> new GroupNotFoundException(invitation.getGroupId()));
    Project project = projectDao.findByIdAndGroupId(
      invitation.getProjectId(), invitation.getGroupId()).orElseThrow(
      () -> new ProjectNotFoundException(invitation.getProjectId()));
    Questionnaire questionnaire = questionnaireDao.findByGroupIdAndProjectIdAndId(
        invitation.getGroupId(), invitation.getProjectId(), invitation.getQuestionnaireId())
      .orElseThrow(QuestionnaireNotFoundException::new);
    updateExistingUser(group, project, questionnaire, user, invitation);
    invitationDao.delete(invitation);
  }

  private PreRegisterUsersReportDto processPreRegistrationRequest(
    MultipartFile usersCsv, UserGroup group, Project project, Questionnaire questionnaire,
    Instant expirationDate) {
    Map<String, PreRegisterUserInternalDto> updatedUsers = new HashMap<>();
    Map<String, PreRegisterUserInternalDto> invitedUsers = new HashMap<>();
    Map<String, String> failedUsers = new HashMap<>();
    List<PreRegisterUserInternalDto> userRequests = parsePreRegistrationCsv(usersCsv);

    for (PreRegisterUserInternalDto userRequest : userRequests) {
      try {
        Optional<ApplicationUser> existingUser = applicationUserDao.findByUsername(
          userRequest.getUsername());

        if (existingUser.isPresent()) {
          updateExistingUser(group, project, questionnaire, existingUser.get(), userRequest);
          updatedUsers.put(userRequest.getUsername(), userRequest);
        } else {
          UUID invitationCode = handlePreRegistrationRequest(
            group, project, questionnaire, userRequest, expirationDate);
          userRequest.setInvitationCode(invitationCode.toString());
          invitedUsers.put(userRequest.getUsername(), userRequest);
        }
      } catch (Exception e) {
        failedUsers.put(userRequest.getUsername(), e.getMessage());
      }
    }
    List<PreRegisterUserInternalDto> updatedUsersList = updatedUsers.values().stream().toList();
    List<PreRegisterUserInternalDto> invitedUsersList = invitedUsers.values().stream().toList();
    return new PreRegisterUsersReportDto(
      updatedUsersList.size() + invitedUsersList.size() + failedUsers.size(), updatedUsersList,
      invitedUsersList, failedUsers);
  }

  public List<PreRegisterUserInternalDto> parsePreRegistrationCsv(MultipartFile usersCsv) {
    csvUtilsService.verifyCsv(usersCsv, RECEIVED_CSV_CONTENT_TYPE, RECEIVED_CSV_MAX_SIZE);
    List<List<String>> csvRecords = csvUtilsService.parseCsv(usersCsv, CSV_DELIMITER, CSV_HEADERS);
    return csvRecords.stream().map(record -> new PreRegisterUserInternalDto(record.get(0).trim(),
      parseGroupPermissions(record.get(1).trim()), parseProjectPermissions(record.get(2).trim()),
      parseNullable(record.get(3)), parseNullable(record.get(4)),
      parseNullableBoolean(record.get(5)), parseNullableBoolean(record.get(6)))).toList();
  }

  private Set<PermissionType> parseGroupPermissions(String permissions) {
    List<String> permissionStrings = Arrays.asList(permissions.split("&"));
    Set<PermissionType> permissionTypes = new HashSet<>();
    permissionTypes.add(PermissionType.GROUP_MEMBER);
    if (permissionStrings.contains(PermissionType.GROUP_ADMIN.name())) {
      permissionTypes.add(PermissionType.GROUP_ADMIN);
    }
    if (permissionStrings.contains(PermissionType.GROUP_EDITOR.name())) {
      permissionTypes.add(PermissionType.GROUP_EDITOR);
    }
    return permissionTypes;
  }

  private Set<PermissionType> parseProjectPermissions(String permissions) {
    List<String> permissionStrings = Arrays.asList(permissions.split("&"));
    Set<PermissionType> permissionTypes = new HashSet<>();
    permissionTypes.add(PermissionType.PROJECT_ASSIGNED_MEMBER);
    if (permissionStrings.contains(PermissionType.PROJECT_ADMIN.name())) {
      permissionTypes.add(PermissionType.PROJECT_ADMIN);
    }
    if (permissionStrings.contains(PermissionType.PROJECT_EDITOR.name())) {
      permissionTypes.add(PermissionType.PROJECT_EDITOR);
    }
    if (permissionStrings.contains(PermissionType.PROJECT_COORDINATOR.name())) {
      permissionTypes.add(PermissionType.PROJECT_COORDINATOR);
    }
    return permissionTypes;
  }

  private String parseNullable(String value) {
    if (value == null || value.isEmpty() || value.equalsIgnoreCase("null")) {
      return null;
    }
    return value.trim();
  }

  private Boolean parseNullableBoolean(String value) {
    if (value == null || value.isEmpty() || value.equalsIgnoreCase("null")) {
      return null;
    }
    return value.equalsIgnoreCase("TRUE");
  }


  private UUID handlePreRegistrationRequest(
    UserGroup group, Project project, Questionnaire questionnaire,
    PreRegisterUserInternalDto userRequest, Instant expirationDate) {
    String username = userRequest.getUsername();
    Optional<Invitation> existingInvitation = invitationDao.findByUsername(username);
    if (existingInvitation.isPresent()) {
      return updateExistingInvitation(userRequest, expirationDate, existingInvitation.get());
    }
    return createInvitation(group, project, questionnaire, userRequest, expirationDate, username);
  }

  private UUID createInvitation(
    UserGroup group, Project project, Questionnaire questionnaire,
    PreRegisterUserInternalDto userRequest, Instant expirationDate, String username) {
    UUID invitationCode = UUID.randomUUID();
    Invitation invitation = new Invitation(invitationCode, expirationDate, username, group.getId(),
      project.getId(), questionnaire.getId(), userRequest.getGroupPermissions(),
      userRequest.getProjectPermissions(), userRequest.getCoordinatorName(),
      userRequest.getDataPreparatorName(), userRequest.getHasExternalTestQuestionnaire(),
      userRequest.getHasExternalTestFailure());
    invitationDao.save(invitation);
    return invitationCode;
  }

  private UUID updateExistingInvitation(
    PreRegisterUserInternalDto userRequest, Instant expirationDate,
    Invitation invitation) {
    invitation.setExpiresAt(expirationDate);
    invitation.setGroupPermissions(userRequest.getGroupPermissions());
    invitation.setProjectPermissions(userRequest.getProjectPermissions());
    invitation.setCoordinatorName(userRequest.getCoordinatorName());
    invitation.setDataPreparatorName(userRequest.getDataPreparatorName());
    invitation.setHasExternalTestQuestionnaire(userRequest.getHasExternalTestQuestionnaire());
    invitation.setHasExternalTestFailure(userRequest.getHasExternalTestFailure());
    invitationDao.save(invitation);
    return invitation.getInvitationCode();
  }

  private void updateExistingUser(
    UserGroup group, Project project, Questionnaire questionnaire, ApplicationUser user,
    PreRegisterUserInternalDto userRequest) {
    group.addMember(user);
    Set<PermissionType> groupPermissions = userRequest.getGroupPermissions();
    Set<PermissionType> projectPermissions = userRequest.getProjectPermissions();
    String coordinatorName = userRequest.getCoordinatorName();
    String dataPreparatorName = userRequest.getDataPreparatorName();
    Boolean hasExternalTestQuestionnaire = userRequest.getHasExternalTestQuestionnaire();
    Boolean hasExternalTestFailure = userRequest.getHasExternalTestFailure();

    updateUser(group, project, questionnaire, user, groupPermissions, projectPermissions,
      coordinatorName, dataPreparatorName, hasExternalTestQuestionnaire, hasExternalTestFailure);
  }

  private void updateExistingUser(
    UserGroup group, Project project, Questionnaire questionnaire, ApplicationUser user,
    Invitation invitation) {
    Set<PermissionType> groupPermissions = invitation.getGroupPermissions();
    Set<PermissionType> projectPermissions = invitation.getProjectPermissions();
    String coordinatorName = invitation.getCoordinatorName();
    String dataPreparatorName = invitation.getDataPreparatorName();
    Boolean hasExternalTestQuestionnaire = invitation.getHasExternalTestQuestionnaire();
    Boolean hasExternalTestFailure = invitation.getHasExternalTestFailure();

    updateUser(group, project, questionnaire, user, groupPermissions, projectPermissions,
      coordinatorName, dataPreparatorName, hasExternalTestQuestionnaire, hasExternalTestFailure);
  }

  private void updateUser(
    UserGroup group, Project project, Questionnaire questionnaire, ApplicationUser user,
    Set<PermissionType> groupPermissions, Set<PermissionType> projectPermissions,
    String coordinatorName, String dataPreparatorName, Boolean hasExternalTestQuestionnaire,
    Boolean hasExternalTestFailure) {
    group.addMember(user);
    if (groupPermissions.contains(PermissionType.GROUP_ADMIN)) {
      group.addAdmin(user);
    }
    if (groupPermissions.contains(PermissionType.GROUP_EDITOR)) {
      group.addEditor(user);
    }
    userGroupDao.save(group);

    project.assignMember(user);
    if (projectPermissions.contains(PermissionType.PROJECT_ADMIN)) {
      project.addAdmin(user);
    }
    if (projectPermissions.contains(PermissionType.PROJECT_EDITOR)) {
      project.addEditor(user);
    }
    if (projectPermissions.contains(PermissionType.PROJECT_COORDINATOR)) {
      project.addCoordinator(user);
    }
    projectDao.save(project);

    user.setActiveQuestionnaire(questionnaire);
    if (coordinatorName != null) {
      user.setCoordinatorUsername(coordinatorName);
    }
    if (dataPreparatorName != null) {
      user.setDataPreparatorUsername(dataPreparatorName);
    }
    if (hasExternalTestQuestionnaire != null) {
      user.setHasExternalTestQuestionnaire(hasExternalTestQuestionnaire);
    }
    if (hasExternalTestFailure != null) {
      user.setHasExternalTestFailure(hasExternalTestFailure);
    }
    applicationUserDao.save(user);
  }
}
