export interface ProjectResponseDetailsDto {
  readonly groupId: number;
  readonly projectId: number;
  readonly name: string;
  readonly description: string;
  readonly detailedDescription: string;
  readonly startDate: Date;
  readonly deadline: Date;
}
