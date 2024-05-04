import {createContext, ReactNode, useState} from "react";
import {AuthenticationDto} from "../dto/AuthenticationDto.ts";
import {IAuthenticationContext} from "./IAuthenticationContext.ts";
import {GlobalRole} from "../dto/userInfo/GlobalRole.ts";

interface AuthenticationProviderProps {
  children: ReactNode;
}

export const AuthenticationContext = createContext<IAuthenticationContext>({
  authenticate: () => {
  },
  logout: () => {
  },
  getUsername: () => undefined,
  getRoles: () => undefined,
  getAccessToken: () => undefined
});

export function AuthenticationProvider({children}: AuthenticationProviderProps) {
  const [authentication, setAuthentication] = useState<AuthenticationDto>({});

  const authenticate = (authentication: AuthenticationDto) => {
    if (!authentication.accessToken || !authentication.userInfo
      || !authentication.userInfo.username?.length
      || !authentication.userInfo.roles?.length
      || !authentication.userInfo?.roles?.includes(GlobalRole.USER)) {
      throw new Error();
    }
    setAuthentication(authentication);
  };

  const logout = () => {
    setAuthentication({});
  };

  const getUsername = () => {
    return authentication.userInfo?.username;
  };

  const getRoles = () => {
    return authentication.userInfo?.roles;
  };

  const getAccessToken = () => {
    return authentication.accessToken;
  };

  return (
    <AuthenticationContext.Provider
      value={{authenticate, logout, getUsername, getRoles, getAccessToken}}>
      {children}
    </AuthenticationContext.Provider>
  );
}
