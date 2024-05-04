import {Card, CardActions, CardContent, CardHeader, Stack, Typography} from "@mui/material";
import {AccountBoxRounded} from "@mui/icons-material";
import {GlobalRole} from "../../../../authentication/dto/userInfo/GlobalRole.ts";
import useLocalized from "../../../../common/localization/hooks/useLocalized.tsx";

interface ProfileMainCardProps {
  username: string,
  roles: GlobalRole[],
  onRequestsClick: () => void
}

export default function ProfileMainCard(props: ProfileMainCardProps) {
  const localized = useLocalized();
  return (
    <Card>
      <CardHeader avatar={<AccountBoxRounded color={"secondary"} sx={{height: 40, width: 40}}/>}
                  title={props.username} titleTypographyProps={{"variant": "h5"}}>
      </CardHeader>
      <CardContent>
        <Stack spacing={2}>
          <Typography variant={"body1"}>
            {localized("inputs.username")}: {props.username}
          </Typography>
          {props.roles?.length > 1 ?
            <Typography variant={"body1"}>
              {localized("pages.user.profile.roles")}: {props.roles.join(", ")}
            </Typography> : <></>}
        </Stack>
      </CardContent>

      <CardActions>
      </CardActions>
    </Card>
  )
}
