package net.dnadas.training_portal.dto.group.project.questionnaire;

import java.util.List;

public record QuestionnaireSubmissionResponseDetailsDto(
  Long id, String name, String description,
  List<SubmittedQuestionResponseDto> questions,
  Integer receivedPoints, Integer maxPoints, String createdAt) {
}
