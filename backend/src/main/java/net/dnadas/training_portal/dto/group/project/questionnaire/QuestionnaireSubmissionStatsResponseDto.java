package net.dnadas.training_portal.dto.group.project.questionnaire;

/**
 * DTO for Questionnaire Statistics
 *
 * @param questionnaireName
 * @param questionnaireMaxPoints           NULLABLE
 * @param maxPointSubmissionId             NULLABLE
 * @param maxPointSubmissionCreatedAt      NULLABLE
 * @param maxPointSubmissionReceivedPoints NULLABLE
 * @param userId
 * @param username
 * @param coordinatorUsername              NULLABLE
 * @param dataPreparatorUsername           NULLABLE
 * @param hasExternalTestQuestionnaire
 * @param hasExternalTestFailure
 * @param submissionCount
 */
public record QuestionnaireSubmissionStatsResponseDto(
  String questionnaireName, Integer questionnaireMaxPoints, Long maxPointSubmissionId,
  String maxPointSubmissionCreatedAt,
  Integer maxPointSubmissionReceivedPoints,
  Long userId, String username,
  String coordinatorUsername,
  String dataPreparatorUsername,
  Boolean hasExternalTestQuestionnaire,
  Boolean hasExternalTestFailure,
  Long submissionCount) {
}
