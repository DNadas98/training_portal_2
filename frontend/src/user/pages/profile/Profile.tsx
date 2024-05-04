import {useAuthentication} from "../../../authentication/hooks/useAuthentication.ts";
import ProfileDashboard from "./components/ProfileDashboard.tsx";
import {useState} from "react";
import LoadingSpinner from "../../../common/utils/components/LoadingSpinner.tsx";
import {useNotification} from "../../../common/notification/context/NotificationProvider.tsx";
import useLogout from "../../../authentication/hooks/useLogout.ts";
import {useDialog} from "../../../common/dialog/context/DialogProvider.tsx";
import {useNavigate} from "react-router-dom";
import {UserPasswordUpdateDto} from "../../dto/UserPasswordUpdateDto.ts";
import useRefresh from "../../../authentication/hooks/useRefresh.ts";
import useAuthJsonFetch from "../../../common/api/hooks/useAuthJsonFetch.tsx";
import {passwordRegex} from "../../../common/utils/regex.ts";
import useLocalized from "../../../common/localization/hooks/useLocalized.tsx";

export default function Profile() {
  const [applicationUserDeleteLoading, setApplicationUserDeleteLoading] = useState<boolean>(false);
  const authJsonFetch = useAuthJsonFetch();
  const authentication = useAuthentication();
  const dialog = useDialog();
  const notification = useNotification();
  const username = authentication.getUsername();
  const roles = authentication.getRoles();
  const logout = useLogout();
  const refresh = useRefresh();
  const navigate = useNavigate();
  const [userDetailsUpdateLoading, setUserDetailsUpdateLoading] = useState<boolean>(false);
  const localized = useLocalized();

  async function deleteApplicationUser(): Promise<void> {
    const defaultError =
      localized("pages.user.profile.error.remove_user_default");
    try {
      setApplicationUserDeleteLoading(true);
      const response = await authJsonFetch({
        path: `user`, method: "DELETE"
      });
      if (response?.status !== 200 || !response.message) {
        return notifyOnError(response?.error ?? defaultError);
      }
      await logout(true);
      notification.openNotification({
        type: "success", vertical: "top", horizontal: "center", message: response.message
      });
    } catch (e) {
      notifyOnError(defaultError);
    } finally {
      setApplicationUserDeleteLoading(false);
    }
  }

  function openDeleteApplicationUserDialog() {
    return dialog.openDialog({
      content: localized("pages.user.profile.archive_user_confirmation"),
      onConfirm: deleteApplicationUser
    });
  }

  const [passwordFormOpen, setPasswordFormOpen] = useState<boolean>(false);

  async function handleUserPasswordUpdate(event: any) {
    const defaultError = localized("pages.user.profile.error.update_password_default");
    try {
      event.preventDefault();
      setUserDetailsUpdateLoading(true);
      const formData = new FormData(event.target);
      const dto: UserPasswordUpdateDto = {
        password: formData.get("password") as string,
        newPassword: formData.get("newPassword") as string,
      }
      if (!passwordRegex.test(dto.password) || !passwordRegex.test(dto.newPassword)) {
        return notifyOnError(localized("inputs.password_invalid"));
      }
      const confirmNewPassword = formData.get("confirmNewPassword") as any;
      if (dto.newPassword !== confirmNewPassword) {
        return notifyOnError(localized("inputs.confirm_password_invalid"));
      }
      const response = await authJsonFetch({
        path: `user/password`, method: "PATCH", body: dto
      });
      if (response?.status !== 200 || !response.message) {
        return notifyOnError(response?.error ?? defaultError);
      }
      notification.openNotification({
        type: "success", vertical: "top", horizontal: "center", message: response.message
      });
      setPasswordFormOpen(false);
      await refresh();
    } catch (e) {
      return notifyOnError(defaultError);
    } finally {
      setUserDetailsUpdateLoading(false);
    }
  }

  function notifyOnError(message: string) {
    notification.openNotification({
      type: "error", vertical: "top", horizontal: "center",
      message: message
    });
    return;
  }

  return userDetailsUpdateLoading || applicationUserDeleteLoading
    ? <LoadingSpinner/>
    : username && roles ? (
      <ProfileDashboard username={username}
                        roles={roles}
                        onApplicationUserDelete={openDeleteApplicationUserDialog}
                        applicationUserDeleteLoading={applicationUserDeleteLoading}
                        handleUserPasswordUpdate={handleUserPasswordUpdate}
                        passwordFormOpen={passwordFormOpen}
                        setPasswordFormOpen={setPasswordFormOpen}
                        onRequestsClick={() => {
                          navigate("/user/requests")
                        }}
      />
    ) : <></>
}
