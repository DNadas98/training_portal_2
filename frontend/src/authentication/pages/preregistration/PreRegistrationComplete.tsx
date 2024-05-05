import {useNotification} from "../../../common/notification/context/NotificationProvider.tsx";
import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import LoadingSpinner from "../../../common/utils/components/LoadingSpinner.tsx";
import {ApiResponseDto} from "../../../common/api/dto/ApiResponseDto.ts";
import {Box, Button, Dialog, DialogContent, Stack, Typography} from "@mui/material";
import usePublicJsonFetch from "../../../common/api/hooks/usePublicJsonFetch.tsx";
import {AuthenticationDto} from "../../dto/AuthenticationDto.ts";
import {useAuthentication} from "../../hooks/useAuthentication.ts";
import SuccessfulLoginRedirect from "../../components/SuccessfulLoginRedirect.tsx";
import LegalPolicyCheckbox from "../../../common/utils/components/LegalPolicyCheckbox.tsx";
import SiteInformation from "../../../common/utils/components/SiteInformation.tsx";
import useLocalized from "../../../common/localization/hooks/useLocalized.tsx";
import {passwordRegex} from "../../../common/utils/regex.ts";
import PasswordInput from "../../components/inputs/PasswordInput.tsx";
import LocaleMenu from "../../../common/menu/LocaleMenu.tsx";
import HomeList from "../../../public/pages/home/components/HomeList.tsx";
import IsSmallScreen from "../../../common/utils/IsSmallScreen.tsx";
import UsernameInput from "../../components/inputs/UsernameInput.tsx";

export default function PreRegistrationComplete() {
  const [loading, setLoading] = useState<boolean>(true);
  const [processError, setProcessError] = useState<null | string>(null);
  const notification = useNotification();
  const navigate = useNavigate();
  const publicJsonFetch = usePublicJsonFetch();
  const authentication = useAuthentication();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [activeGroupId, setActiveGroupId] = useState<number | null>(null);
  const [activeProjectId, setActiveProjectId] = useState<number | null>(null);
  const [activeQuestionnaireId, setActiveQuestionnaireId] = useState<number | null>(null);
  const localized = useLocalized();
  const isSmallScreen = IsSmallScreen();
  const invitationCode = new URLSearchParams(window.location.search).get("code") ?? "";

  const handleProcessError = (error: string | undefined = undefined) => {
    const message = error ?? localized("common.error.redirect.unknown");
    setProcessError(message);
  };

  const checkVerification = async () => {
    try {
      setLoading(true);
      const response: ApiResponseDto = await publicJsonFetch({
        path: `auth/preregistration-check?code=${invitationCode}`, method: "GET"
      });
      if (response.error || response?.status > 399) {
        return handleProcessError(response?.error ?? undefined);
      }
    } catch (e) {
      handleProcessError();
      setLoading(false);
    }
  }

  const handleVerification = async (event: any) => {
    try {
      event.preventDefault();
      const formData = new FormData(event.target);
      const password = formData.get("password") as string;
      const username = formData.get("username") as string | undefined;
      const confirmPassword = formData.get("confirmPassword") as string;
      if (!passwordRegex.test(password)) {
        notification.openNotification({
          type: "error", vertical: "top", horizontal: "center",
          message: localized("inputs.password_invalid")
        });
        return;
      }
      if (password !== confirmPassword) {
        return notification.openNotification({
          type: "error",
          vertical: "top",
          horizontal: "center",
          message: localized("inputs.confirm_password_invalid")
        });
      }
      const response: ApiResponseDto = await publicJsonFetch({
        path: `auth/preregistration-complete?code=${invitationCode}`, method: "POST", body: {
          username, password
        }
      });
      if (response.error || response?.status > 399 || !response.data) {
        return handleProcessError(response.error);
      }
      const {userInfo, accessToken} = response.data as AuthenticationDto;
      authentication.authenticate({userInfo, accessToken});
      const groupId = response.data.groupId;
      const projectId = response.data.projectId;
      const questionnaireId = response.data.questionnaireId;
      setActiveGroupId(groupId);
      setActiveProjectId(projectId);
      setActiveQuestionnaireId(questionnaireId);
      setIsLoggedIn(true);
    } catch (e) {
      handleProcessError();
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    if (!invitationCode?.length) {
      handleProcessError(localized("common.error.redirect.code_invalid"));
      setLoading(false);
    } else {
      checkVerification().then();
    }
  }, [invitationCode]);

  if (processError) {
    notification.openNotification({
      type: "error", vertical: "top", horizontal: "center", message: processError
    });
    navigate("/login");
    return <></>;
  }

  return (
    loading
      ? <LoadingSpinner/>
      : isLoggedIn
        ? <SuccessfulLoginRedirect groupId={activeGroupId} projectId={activeProjectId}
                                   questionnaireId={activeQuestionnaireId}/>
        : <Dialog open={true} maxWidth={"lg"} fullScreen={isSmallScreen}>
          <DialogContent><Stack spacing={1}>
            <Stack spacing={1} direction={"row"} flexWrap={"wrap"} mb={2} alignItems={"flex-start"}
                   justifyContent={"space-between"}>
              <Typography variant={"h6"}>{localized("pages.pre_registration.title")}</Typography>
              <LocaleMenu/>
            </Stack>
            <HomeList/>
            <Stack spacing={0.5} sx={{pl: 2, pr: 2}}>
              {localized("pages.pre_registration.info").split("\n").map((row, i) => <Typography
                key={i}>
                {row}
              </Typography>)}
            </Stack>
            <SiteInformation/>
            <Box sx={{pl: 2, pr: 2}} component={"form"} onSubmit={handleVerification}><Stack
              spacing={2}>
              <Typography>{localized("inputs.password_invalid")}</Typography>
              <LegalPolicyCheckbox/>
              <UsernameInput/>
              <PasswordInput/>
              <PasswordInput confirm={true}/>
              <Button type={"submit"}>{localized("pages.pre_registration.submit_button")}</Button>
            </Stack></Box>
          </Stack></DialogContent>
        </Dialog>
  );
}
