package net.dnadas.training_portal.service.utils.converter;

import lombok.RequiredArgsConstructor;
import net.dnadas.training_portal.dto.group.project.questionnaire.*;
import net.dnadas.training_portal.model.group.project.questionnaire.Questionnaire;
import net.dnadas.training_portal.model.group.project.questionnaire.QuestionnaireSubmission;
import net.dnadas.training_portal.model.group.project.questionnaire.SubmittedAnswer;
import net.dnadas.training_portal.model.group.project.questionnaire.SubmittedQuestion;
import net.dnadas.training_portal.service.utils.datetime.DateTimeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionnaireSubmissionConverter {
  private final DateTimeService dateTimeService;

  public QuestionnaireSubmissionResponseDto toQuestionnaireSubmissionResponseDto(
    QuestionnaireSubmission questionnaireSubmission, Questionnaire questionnaire) {
    return new QuestionnaireSubmissionResponseDto(questionnaireSubmission.getId(),
      questionnaire.getName(), questionnaire.getDescription(),
      questionnaireSubmission.getReceivedPoints(),
      questionnaireSubmission.getMaxPoints(),
      dateTimeService.toDisplayedDate(questionnaireSubmission.getCreatedAt()));
  }

  public QuestionnaireSubmissionResponseDetailsDto toQuestionnaireSubmissionResponseDetailsDto(
    QuestionnaireSubmission questionnaireSubmission, Questionnaire questionnaire) {
    return new QuestionnaireSubmissionResponseDetailsDto(questionnaireSubmission.getId(),
      questionnaire.getName(), questionnaire.getDescription(),
      questionnaireSubmission.getSubmittedQuestions().stream()
        .map(this::toSubmittedQuestionResponseDto).toList(),
      questionnaireSubmission.getReceivedPoints(),
      questionnaireSubmission.getMaxPoints(),
      dateTimeService.toDisplayedDate(questionnaireSubmission.getCreatedAt()));
  }

  @Transactional(readOnly = true)
  public QuestionnaireSubmissionResponseEditorDto toQuestionnaireSubmissionResponseEditorDto(
    QuestionnaireSubmission questionnaireSubmission, Questionnaire questionnaire) {
    return new QuestionnaireSubmissionResponseEditorDto(questionnaireSubmission.getId(),
      questionnaire.getName(), questionnaire.getDescription(),
      questionnaireSubmission.getReceivedPoints(),
      questionnaireSubmission.getMaxPoints(),
      dateTimeService.toDisplayedDate(questionnaireSubmission.getCreatedAt()),
      questionnaireSubmission.getStatus());
  }

  public QuestionnaireSubmissionStatsResponseDto toQuestionnaireSubmissionStatsResponseDto(
    QuestionnaireSubmissionStatsInternalDto dto) {
    Instant maxPointSubmissionCreatedAt = dto.maxPointSubmissionCreatedAt();
    String maxPointSubmissionCreatedAtDate;
    if (maxPointSubmissionCreatedAt == null) {
      maxPointSubmissionCreatedAtDate = null;
    } else {
      maxPointSubmissionCreatedAtDate = dateTimeService.toDisplayedDate(
        dto.maxPointSubmissionCreatedAt());
    }
    return new QuestionnaireSubmissionStatsResponseDto(
      dto.questionnaireName(), dto.questionnaireMaxPoints(), dto.maxPointSubmissionId(),
      maxPointSubmissionCreatedAtDate,
      dto.maxPointSubmissionReceivedPoints(),
      dto.userId(), dto.username(),
      dto.hasExternalTestQuestionnaire(), dto.hasExternalTestFailure(),
      dto.submissionCount());
  }

  private SubmittedQuestionResponseDto toSubmittedQuestionResponseDto(
    SubmittedQuestion submittedQuestion) {
    return new SubmittedQuestionResponseDto(submittedQuestion.getId(),
      submittedQuestion.getText(), submittedQuestion.getType(),
      submittedQuestion.getReceivedPoints(), submittedQuestion.getMaxPoints(),
      submittedQuestion.getQuestionOrder(),
      submittedQuestion.getSubmittedAnswers().stream().map(
        this::toSubmittedAnswerResponseDto
      ).collect(Collectors.toList()));
  }

  private SubmittedAnswerResponseDto toSubmittedAnswerResponseDto(
    SubmittedAnswer submittedAnswer) {
    return new SubmittedAnswerResponseDto(submittedAnswer.getId(),
      submittedAnswer.getText(), submittedAnswer.getAnswerOrder(),
      submittedAnswer.getStatus());
  }
}
