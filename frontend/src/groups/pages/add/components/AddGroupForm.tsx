import React from "react";
import {Avatar, Button, Card, CardContent, Grid, Stack, Typography} from "@mui/material";
import {DomainAddOutlined} from "@mui/icons-material";
import GroupNameInput from "../../../components/GroupNameInput.tsx";
import GroupDescriptionInput from "../../../components/GroupDescriptionInput.tsx";
import RichTextEditorUncontrolled
  from "../../../../common/richTextEditor/RichTextEditorUncontrolled.tsx";

interface AddGroupFormProps {
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => Promise<void>
}

export default function AddGroupForm(props: AddGroupFormProps) {
  return (
    <Grid container justifyContent={"center"}>
      <Grid item xs={11}>
        <Card sx={{
          paddingTop: 4, textAlign: "center",
          marginLeft: "auto", marginRight: "auto"
        }}>
          <Stack
            spacing={2}
            alignItems={"center"}
            justifyContent={"center"}>
            <Avatar variant={"rounded"}
                    sx={{backgroundColor: "secondary.main"}}>
              <DomainAddOutlined/>
            </Avatar>
            <Typography variant="h5" gutterBottom>
              Add new group
            </Typography>
          </Stack>
          <CardContent sx={{justifyContent: "center", textAlign: "center"}}>
            <Grid container sx={{
              justifyContent: "center",
              alignItems: "center",
              textAlign: "center",
              gap: "2rem"
            }}>
              <Grid item xs={12} sx={{borderColor: "secondary.main"}}>
                <form onSubmit={props.onSubmit}>
                  <Stack spacing={2}>
                    <GroupNameInput/>
                    <GroupDescriptionInput/>
                    <RichTextEditorUncontrolled name={"detailedDescription"}/>
                    <Button type={"submit"}
                            variant={"contained"}>
                      Add Group
                    </Button>
                  </Stack>
                </form>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </Grid>
    </Grid>
  )
}
