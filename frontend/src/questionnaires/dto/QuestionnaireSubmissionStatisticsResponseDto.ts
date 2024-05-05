export interface QuestionnaireSubmissionStatisticsResponseDto {
  questionnaireName: string;
  questionnaireMaxPoints: number;
  maxPointSubmissionId: number;
  maxPointSubmissionCreatedAt: string;
  maxPointSubmissionReceivedPoints: number;
  userId: number;
  username: string;
  coordinatorUsername: string;
  dataPreparatorUsername: string;
  hasExternalTestQuestionnaire: boolean;
  hasExternalTestFailure: boolean;
  submissionCount: number;
}
