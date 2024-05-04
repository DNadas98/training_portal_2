package net.dnadas.training_portal.dto.group.project.questionnaire;

import java.time.Instant;

public record QuestionnaireSubmissionStatsInternalDto(
  String questionnaireName, Integer questionnaireMaxPoints, Long maxPointSubmissionId,
  Instant maxPointSubmissionCreatedAt,
  Integer maxPointSubmissionReceivedPoints,
  Long userId, String username,
  String coordinatorUsername,
  String dataPreparatorUsername,
  Boolean hasExternalTestQuestionnaire,
  Boolean hasExternalTestFailure,
  Long submissionCount) {
}
