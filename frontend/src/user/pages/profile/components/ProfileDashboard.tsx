import {
  Button,
  Card,
  CardContent,
  CardHeader,
  Dialog,
  DialogContent,
  DialogTitle,
  Grid,
  Stack,
} from "@mui/material";
import {GlobalRole} from "../../../../authentication/dto/userInfo/GlobalRole.ts";
import ProfileMainCard from "./ProfileMainCard.tsx";
import PasswordUpdateForm from "./PasswordUpdateForm.tsx";
import useLocalized from "../../../../common/localization/hooks/useLocalized.tsx";

interface ProfileDashboardProps {
  username: string,
  roles: GlobalRole[],
  onApplicationUserDelete: () => unknown,
  applicationUserDeleteLoading: boolean,
  onRequestsClick: () => void,
  handleUserPasswordUpdate: (event: any) => Promise<void>,
  passwordFormOpen: boolean,
  setPasswordFormOpen: (value: (((prevState: boolean) => boolean) | boolean)) => void,
}

export default function ProfileDashboard(props: ProfileDashboardProps) {
  const localized = useLocalized();
  return (
    <Grid container justifyContent={"center"}>
      <Grid item xs={10} sm={5} mb={4} lg={3}>
        <Stack spacing={2}>
          <ProfileMainCard username={props.username}
                           roles={props.roles}
                           onRequestsClick={props.onRequestsClick}/>
          <Card>
            <CardHeader title={localized("pages.user.profile.groups_and_projects")}
                        titleTypographyProps={{variant: "h6"}}/>
            <CardContent> <Button sx={{maxWidth: "fit-content"}}
                                  onClick={props.onRequestsClick}
                                  variant={"text"}>
              {localized("pages.user.profile.manage_join_requests")}
            </Button> </CardContent>
          </Card>
          <Card>
            <CardHeader title={localized("pages.user.profile.user_details")}
                        titleTypographyProps={{variant: "h6"}}/>
            <CardContent>
              <Stack spacing={2}>
                <Button type={"button"} sx={{maxWidth: "fit-content"}}
                        onClick={() => props.setPasswordFormOpen(true)}>
                  {localized("pages.user.profile.change_password")}
                </Button>
                <Button type={"button"} sx={{maxWidth: "fit-content"}}
                        disabled={props.applicationUserDeleteLoading}
                        onClick={props.onApplicationUserDelete}
                        variant={"contained"} color={"error"}>
                  {localized("pages.user.profile.remove_user")}
                </Button>
              </Stack>
            </CardContent>
          </Card>
          <Dialog open={props.passwordFormOpen} onClose={() => props.setPasswordFormOpen(false)}>
            <DialogTitle>{localized("pages.user.profile.change_password")}</DialogTitle>
            <DialogContent>
              <PasswordUpdateForm handleUserPasswordUpdate={props.handleUserPasswordUpdate}/>
            </DialogContent>
          </Dialog>
        </Stack></Grid></Grid>
  )
}
