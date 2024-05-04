export interface PreRegisterUsersReportDto {
  totalUsers: number;
  updatedUsers: PreRegisterUserInternalDto[];
  invitedUsers: PreRegisterUserInternalDto[];
  failedUsers: Map<string, string>;
}

interface PreRegisterUserInternalDto {
  username: string;
}
