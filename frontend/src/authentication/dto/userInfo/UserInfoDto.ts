import {GlobalRole} from "./GlobalRole.ts";

export interface UserInfoDto {
  readonly username: string;
  readonly roles: Array<GlobalRole>;
}
