import {useNotification} from "../../notification/context/NotificationProvider.tsx";
import {useAuthentication} from "../../../authentication/hooks/useAuthentication.ts";
import useRefresh from "../../../authentication/hooks/useRefresh.ts";
import useLogout from "../../../authentication/hooks/useLogout.ts";
import useLocaleContext from "../../localization/hooks/useLocaleContext.tsx";
import {ApiRequestDto} from "../dto/ApiRequestDto.ts";
import {ApiResponseDto} from "../dto/ApiResponseDto.ts";
import {getRequestConfig, verifyHttpResponse} from "../utils/apiUtils.ts";
import useLocalized from "../../localization/hooks/useLocalized.tsx";

export default function useAuthJsonFetch() {
  const notification = useNotification();
  const authentication = useAuthentication();
  const refresh = useRefresh();
  const logout = useLogout();
  const {locale} = useLocaleContext();
  const localized = useLocalized();

  const notifyAndLogout = async (
    httpResponse: Response, errorMessage: string | undefined = undefined) => {
    notification.openNotification({
      type: "error", vertical: "top", horizontal: "center",
      message:
        errorMessage
        ?? httpResponse.status === 403
          ? "Forbidden" : "Unauthorized"
    })
    return await logout();
  }
  const authJsonFetch = async (request: ApiRequestDto) => {
    try {
      const requestConfig = getRequestConfig(request, locale, "application/json");
      const accessToken = authentication.getAccessToken();
      if (!accessToken) {
        throw new Error("Unauthorized");
      }
      const baseUrl = import.meta.env.VITE_API_BASE_URL;
      let httpResponse: Response = await authenticatedFetch(
        `${baseUrl}/${request.path}`, requestConfig, accessToken);
      verifyHttpResponse(httpResponse);
      let responseObject = await httpResponse?.json();

      // Refresh if the access token is expired, and try to re-fetch
      if (responseObject?.isAccessTokenExpired) {
        const refreshResponseDto = await refresh();

        // If the refresh token is also expired:
        if (!refreshResponseDto?.newAuthentication?.accessToken) {
          return await notifyAndLogout(httpResponse, refreshResponseDto.error);
        }
        httpResponse = await authenticatedFetch(
          `${baseUrl}/${request.path}`, requestConfig,
          refreshResponseDto.newAuthentication.accessToken);
        responseObject = await httpResponse?.json();
      }

      // If Unauthorized or Forbidden:
      if (httpResponse.status === 401) {
        return await notifyAndLogout(httpResponse, responseObject?.error);
      }

      const apiResponse: ApiResponseDto = {
        ...responseObject,
        status: httpResponse.status
      };
      return apiResponse;
    } catch (e) {
      console.error(e);
      return {
        status: 500,
        error: localized("common.error.fetch.unknown")
      };
    }
  };
  return authJsonFetch;
}

async function authenticatedFetch(path: string, requestConfig: RequestInit, accessToken: string): Promise<Response> {
  return await fetch(path, {
    ...requestConfig,
    headers: {
      ...requestConfig.headers,
      "Authorization": `Bearer ${accessToken}`
    }
  });
}
