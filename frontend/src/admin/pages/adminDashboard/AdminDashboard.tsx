import {ChangeEvent, useEffect, useState} from "react";
import {
  QuestionnaireResponseEditorDto
} from "../../../questionnaires/dto/QuestionnaireResponseEditorDto.ts";
import {GroupResponsePublicDto} from "../../../groups/dto/GroupResponsePublicDto.ts";
import {ProjectResponsePublicDto} from "../../../projects/dto/ProjectResponsePublicDto.ts";
import {useNotification} from "../../../common/notification/context/NotificationProvider.tsx";
import {useNavigate} from "react-router-dom";
import UserPreRegistrationForm from "./components/UserPreRegistrationForm.tsx";
import useAuthJsonFetch from "../../../common/api/hooks/useAuthJsonFetch.tsx";
import LoadingSpinner from "../../../common/utils/components/LoadingSpinner.tsx";
import {formatISO} from "date-fns";
import useAuthFetch from "../../../common/api/hooks/useAuthFetch.tsx";
import {Grid} from "@mui/material";
import useLocalizedDateTime from "../../../common/localization/hooks/useLocalizedDateTime.tsx";

//TODO: add backend search filtering, pagination if necessary

export default function AdminDashboard() {
  const [loading, setLoading] = useState<boolean>(false);
  const [downloadLoading, setDownloadLoading] = useState<boolean>(false);
  const [groupsLoading, setGroupsLoading] = useState<boolean>(true);
  const [groups, setGroups] = useState<GroupResponsePublicDto[]>([]);
  const [selectedGroup, setSelectedGroup] = useState<GroupResponsePublicDto | null | undefined>(null);
  const [groupFilterValue, setGroupFilterValue] = useState<string>("");

  const [projectsLoading, setProjectsLoading] = useState<boolean>(false);
  const [projects, setProjects] = useState<ProjectResponsePublicDto[]>([]);
  const [selectedProject, setSelectedProject] = useState<ProjectResponsePublicDto | null | undefined>(null);
  const [projectFilterValue, setProjectFilterValue] = useState<string>("");

  const [questionnairesLoading, setQuestionnairesLoading] = useState<boolean>(false);
  const [questionnaires, setQuestionnaires] = useState<QuestionnaireResponseEditorDto[]>([]);
  const [selectedQuestionnaire, setSelectedQuestionnaire] = useState<QuestionnaireResponseEditorDto | null | undefined>(null);
  const [questionnaireFilterValue, setQuestionnaireFilterValue] = useState<string>("");

  const [selectedFile, setSelectedFile] = useState<File | undefined | null>(null);
  const MAX_FILE_SIZE = 400000; // 400 KB

  const [expiresAt, setExpiresAt] = useState<Date>(new Date());

  const notification = useNotification();
  const navigate = useNavigate();
  const authJsonFetch = useAuthJsonFetch();
  const authFetch = useAuthFetch();
  const getLocalizedDateTime = useLocalizedDateTime();

  const openErrorNotification = (message: string) => notification.openNotification({
    type: "error", vertical: "top", horizontal: "center", message: message
  });

  const trimAndLowercase = (input: any) => {
    if (!input || !input.toString()) {
      return "";
    }
    return input.toString().trim().toLowerCase();
  }

  const loadAllGroups = async () => {
    const defaultError = "Failed to load groups";
    try {
      setGroupsLoading(true);
      setGroupFilterValue("");
      setSelectedGroup(null);
      const response = await authJsonFetch({
        path: "admin/groups"
      });
      if (!response || !response.data || response.status > 399) {
        openErrorNotification(response?.error ?? defaultError);
        setGroups([]);
        return;
      }
      setGroups(response.data);
    } catch (e) {
      openErrorNotification(defaultError);
      setGroups([]);
    } finally {
      setGroupsLoading(false);
    }
  }

  const handleGroupSearchInputChange = (e: any) => {
    setGroupFilterValue(trimAndLowercase(e.target.value));
  }

  const handleGroupSelect = (_event: ChangeEvent<NonNullable<unknown>>, newValue: GroupResponsePublicDto | null) => {
    setSelectedGroup(newValue);
  };

  useEffect(() => {
    loadAllGroups().then();
  }, []);

  const loadProjects = async () => {
    const defaultError = "Failed to load projects";
    try {
      setProjectsLoading(true);
      setSelectedProject(null);
      setProjectFilterValue("");
      if (!selectedGroup) {
        setProjects([]);
        return;
      }
      const response = await authJsonFetch({
        path: `admin/groups/${selectedGroup.groupId}/projects`
      });
      if (!response || !response.data || response.status > 399) {
        openErrorNotification(response?.error ?? defaultError);
        setProjects([]);
        return;
      }
      setProjects(response.data);
    } catch (e) {
      openErrorNotification(defaultError);
      setProjects([]);
    } finally {
      setProjectsLoading(false);
    }
  }

  const handleProjectSearchInputChange = (e: any) => {
    setProjectFilterValue(trimAndLowercase(e.target.value));
  }

  const handleProjectSelect = (_event: ChangeEvent<NonNullable<unknown>>, newValue: ProjectResponsePublicDto | null) => {
    setSelectedProject(newValue);
  };

  useEffect(() => {
    loadProjects().then();
  }, [selectedGroup]);

  const loadQuestionnaires = async () => {
    const defaultError = "Failed to load questionnaires";
    try {
      setQuestionnairesLoading(true);
      setSelectedQuestionnaire(null);
      setQuestionnaireFilterValue("");
      if (!selectedGroup || !selectedProject) {
        setQuestionnaires([]);
        return;
      }
      const response = await authJsonFetch({
        path: `admin/groups/${selectedGroup.groupId}/projects/${selectedProject.projectId}/questionnaires`
      });
      if (!response || !response.data || response.status > 399) {
        openErrorNotification(response?.error ?? defaultError);
        setQuestionnaires([]);
        return;
      }
      setQuestionnaires(response.data);
    } catch (e) {
      openErrorNotification(defaultError);
      setQuestionnaires([]);
    } finally {
      setQuestionnairesLoading(false);
    }
  }

  const handleQuestionnaireSearchInputChange = (e: any) => {
    setQuestionnaireFilterValue(trimAndLowercase(e.target.value));
  }

  const handleQuestionnaireSelect = (_event: ChangeEvent<NonNullable<unknown>>, newValue: QuestionnaireResponseEditorDto | null) => {
    setSelectedQuestionnaire(newValue);
  };

  const handleFileSelect = (event: ChangeEvent<HTMLInputElement>) => {
    const files = event.target.files;
    if (files && files.length > 0) {
      const selectedFile = files[0];
      if (!selectedFile.name.toLowerCase().endsWith('.csv')) {
        openErrorNotification("The selected file must have .csv extension");
        setSelectedFile(null);
        return;
      }
      if (selectedFile.size > MAX_FILE_SIZE) {
        openErrorNotification("The selected file is too large. Maximum allowed size is 400 KB");
        setSelectedFile(null);
        return;
      }
      setSelectedFile(selectedFile);
    }
  };

  const handleSubmit = async (e: any) => {
    e.preventDefault();
    const defaultError = "Failed to upload user data";
    if (!selectedFile || !selectedGroup || !selectedProject || !selectedQuestionnaire) {
      openErrorNotification("All fields are required");
      return;
    }
    try {
      setLoading(true);
      const userTimezone = encodeURIComponent(Intl.DateTimeFormat().resolvedOptions().timeZone);
      let fileName =
        `pre-registration-${selectedProject?.name}-${getLocalizedDateTime(new Date())}`;
      // @ts-expect-error replaceAll does in fact exist here on type string
      fileName = fileName.trim().toLowerCase().replaceAll(" ", "_").replaceAll(".", "")
        .concat(".xlsx");
      const formData = new FormData();
      formData.append("file", selectedFile);
      formData.append("groupId", selectedGroup.groupId.toString());
      formData.append("projectId", selectedProject.projectId.toString());
      formData.append("questionnaireId", selectedQuestionnaire.id.toString());
      formData.append("expiresAt", formatISO(expiresAt));
      const response = await authFetch({
        path: `admin/pre-register/users?timeZone=${userTimezone}`,
        method: 'POST',
        body: formData
      });
      if (!response || response.status > 399) {
        openErrorNotification(response?.error ?? defaultError);
        return;
      }
      const blob = await response?.blob();
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();
      link.parentNode?.removeChild(link);
    } catch (error) {
      openErrorNotification(defaultError);
    } finally {
      setLoading(false);
    }
  }

  const handleBackClick = () => {
    navigate("/user");
  }
  const handleDownloadTemplate = async () => {
    try {
      if (downloadLoading) {
        return;
      }
      setDownloadLoading(true);
      const response = await authFetch({
        path: "admin/pre-register/users/csv-template",
        contentType: "text/csv"
      });
      const blob = await response.blob();
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.setAttribute("download", "user_pre_registration_template.csv");
      document.body.appendChild(link);
      link.click();
      link.parentNode?.removeChild(link);
    } catch (e) {
      openErrorNotification("Failed to download template");
    } finally {
      setDownloadLoading(false);
    }
  };

  useEffect(() => {
    loadQuestionnaires().then();
  }, [selectedGroup, selectedProject]);

  function handleExpirationChange(newValue: Date) {
    setExpiresAt(newValue);
  }

  return loading ? <LoadingSpinner/> :
    <Grid container justifyContent={"center"} alignItems={"center"} spacing={2}>
      <Grid item xs={10} sm={8}>
        <UserPreRegistrationForm
          groups={groups.filter(group => group.name.trim().toLowerCase().includes(groupFilterValue))}
          groupsLoading={groupsLoading}
          selectedGroup={selectedGroup}
          onGroupSelect={handleGroupSelect}
          onGroupSearchInputChange={handleGroupSearchInputChange}
          projects={projects.filter(project => project.name.trim().toLowerCase().includes(projectFilterValue))}
          projectsLoading={projectsLoading}
          selectedProject={selectedProject}
          onProjectSelect={handleProjectSelect}
          onProjectSearchInputChange={handleProjectSearchInputChange}
          questionnaires={questionnaires.filter(questionnaire => questionnaire.name.trim().toLowerCase().includes(questionnaireFilterValue))}
          questionnairesLoading={questionnairesLoading}
          selectedQuestionnaire={selectedQuestionnaire}
          onQuestionnaireSelect={handleQuestionnaireSelect}
          onQuestionnaireSearchInputChange={handleQuestionnaireSearchInputChange}
          selectedFile={selectedFile}
          onFileSelect={handleFileSelect}
          onSubmit={handleSubmit}
          onBackClick={handleBackClick}
          onDownloadTemplate={handleDownloadTemplate}
          expiresAt={expiresAt}
          onExpiresAtChange={handleExpirationChange}/>
      </Grid>
    </Grid>;
}
