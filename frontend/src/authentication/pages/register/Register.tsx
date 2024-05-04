import RegisterCard from "./components/RegisterCard.tsx";
import {useNotification} from "../../../common/notification/context/NotificationProvider.tsx";
import {FormEvent} from "react";
import {RegisterRequestDto} from "../../dto/RegisterRequestDto.ts";
import usePublicJsonFetch from "../../../common/api/hooks/usePublicJsonFetch.tsx";
import useLocalized from "../../../common/localization/hooks/useLocalized.tsx";
import {passwordRegex} from "../../../common/utils/regex.ts";
import {AuthenticationDto} from "../../dto/AuthenticationDto.ts";
import {ApiResponseDto} from "../../../common/api/dto/ApiResponseDto.ts";
import {useAuthentication} from "../../hooks/useAuthentication.ts";
import {useNavigate} from "react-router-dom";

export default function Register() {
  const authentication = useAuthentication();
  const notification = useNotification();
  const publicJsonFetch = usePublicJsonFetch();
  const localized = useLocalized();
  const navigate = useNavigate();
  const validatePassword = (password: string, confirmPassword: string) => {
    if (!passwordRegex.test(password)) {
      notification.openNotification({
        type: "error", vertical: "top", horizontal: "center",
        message: localized("inputs.password_invalid")
      });
      return false;
    }
    if (password !== confirmPassword) {
      notification.openNotification({
        type: "error", vertical: "top", horizontal: "center",
        message: localized("inputs.confirm_password_invalid"),
      });
      return false;
    }
    return true;
  };

  const registerUser = async (registerRequestDto: RegisterRequestDto) => {
    return await publicJsonFetch({
      path: "auth/register", method: "POST", body: registerRequestDto
    });
  };

  const handleError = (error: string | undefined = undefined) => {
    notification.openNotification({
      type: "error",
      vertical: "top",
      horizontal: "center",
      message: error ?? localized("pages.sign_up.error.default"),
    });
  };

  const handleSuccess = (response: ApiResponseDto) => {
    const {userInfo, accessToken} = response.data as AuthenticationDto;
    authentication.authenticate({userInfo, accessToken});
    navigate("/groups");
    notification.openNotification({
      type: "success",
      vertical: "top",
      horizontal: "center",
      message: localized("pages.sign_up.success")
    })

  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    try {
      event.preventDefault();
      const formData = new FormData(event.currentTarget);
      const username = formData.get("username") as string;
      const password = formData.get("password") as string;
      const confirmPassword = formData.get("confirmPassword") as string;

      const passwordIsValid = validatePassword(password, confirmPassword);
      if (!passwordIsValid) {
        return;
      }
      const registerRequestDto: RegisterRequestDto = {username, password};
      const response = await registerUser(registerRequestDto);

      if (response.error || response?.status > 399 || !response.data) {
        handleError(response.error);
        return;
      }

      handleSuccess(response);
    } catch (e) {
      const errorMessage = localized("pages.sign_up.error.default");
      handleError(errorMessage);
    }
  };

  return <RegisterCard onSubmit={handleSubmit}/>
}
