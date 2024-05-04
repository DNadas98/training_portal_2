import {Avatar, Button, Card, CardContent, Grid, Stack, Typography} from "@mui/material";
import {Lock} from "@mui/icons-material";
import UsernameInput from "../../../components/inputs/UsernameInput.tsx";
import PasswordInput from "../../../components/inputs/PasswordInput.tsx";
import {FormEvent} from "react";
import LegalPolicyCheckbox from "../../../../common/utils/components/LegalPolicyCheckbox.tsx";
import useLocalized from "../../../../common/localization/hooks/useLocalized.tsx";

interface RegisterCardProps {
  onSubmit: (event: FormEvent<HTMLFormElement>) => Promise<void>;
}

export default function RegisterCard({onSubmit}: RegisterCardProps) {
  const localized = useLocalized();
  return (
    <Grid container justifyContent={"center"}>
      <Grid item xs={10} sm={8} md={7} lg={6}>
        <Card sx={{
          paddingTop: 4, textAlign: "center",
          maxWidth: 500, width: "100%",
          marginLeft: "auto", marginRight: "auto"
        }}>
          <Stack
            spacing={2}
            alignItems={"center"}
            justifyContent={"center"}>
            <Avatar variant={"rounded"}
                    sx={{backgroundColor: "secondary.main"}}>
              <Lock/>
            </Avatar>
            <Typography variant="h5" gutterBottom>
              {localized("pages.sign_up.title")}
            </Typography>
          </Stack>
          <CardContent sx={{justifyContent: "center", textAlign: "center"}}>
            <Stack spacing={2}>
              <Typography variant={"body2"}
                          textAlign={"left"}>{localized("pages.sign_up.info")}</Typography>
              <form onSubmit={onSubmit}>
                <Stack spacing={2}>
                  <LegalPolicyCheckbox/>
                  <UsernameInput/>
                  <PasswordInput autoComplete={"new-password"}/>
                  <PasswordInput confirm={true}/>
                  <Button type={"submit"}
                          variant={"contained"}>
                    {localized("pages.sign_up.submit_button")}
                  </Button>
                </Stack>
              </form>
            </Stack>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  )
}
