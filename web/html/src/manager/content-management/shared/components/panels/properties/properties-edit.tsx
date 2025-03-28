import * as React from "react";

import { isOrgAdmin } from "core/auth/auth.utils";
import useRoles from "core/auth/use-roles";

import CreatorPanel from "components/panels/CreatorPanel";
import { showErrorToastr, showSuccessToastr } from "components/toastr";
import { Loading } from "components/utils/loading/Loading";

import produce from "utils/produce";

import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import { ProjectHistoryEntry, ProjectMessageType, ProjectPropertiesType } from "../../../type";
import getRenderedMessages from "../../messages/messages";
import PropertiesForm from "./properties-form";
import PropertiesView from "./properties-view";

type Props = {
  projectId: string;
  properties: ProjectPropertiesType;
  showDraftVersion: boolean;
  onChange: Function;
  currentHistoryEntry?: ProjectHistoryEntry;
  messages?: Array<ProjectMessageType>;
};

const PropertiesEdit = (props: Props) => {
  const { onAction, cancelAction, isLoading } = useLifecycleActionsApi({
    resource: "projects",
    nestedResource: "properties",
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  const defaultDraftHistory = {
    version: props.currentHistoryEntry ? props.currentHistoryEntry.version + 1 : 1,
    message: t("(draft - not built) - Check the changes below"),
  };

  const messages = getRenderedMessages(props.messages || []);

  let propertiesToShow = produce(props.properties, (draftProperties) => {
    if (props.showDraftVersion) {
      draftProperties.historyEntries.unshift(defaultDraftHistory);
    }
  });

  return (
    <CreatorPanel
      id="properties"
      creatingText={t("Edit Properties")}
      className={messages.panelClass}
      panelLevel="2"
      disableEditing={!hasEditingPermissions}
      title={t("Project Properties")}
      collapsible
      icon="fa-pencil"
      customIconClass="fa-small"
      onOpen={({ setItem, setErrors }) => {
        setItem(props.properties);
        setErrors(null);
      }}
      disableOperations={isLoading}
      onSave={({ item, closeDialog, setErrors }) => {
        return onAction(item, "update", props.projectId)
          .then((editedProject) => {
            closeDialog();
            showSuccessToastr(t("Project properties updated successfully"));
            props.onChange(editedProject);
          })
          .catch((error) => {
            setErrors(error.errors);
            showErrorToastr(error.messages, { autoHide: false });
          });
      }}
      onCancel={() => cancelAction()}
      renderContent={() => (
        <React.Fragment>
          {messages.messages}
          <PropertiesView properties={propertiesToShow} />
        </React.Fragment>
      )}
      renderCreationContent={({ open, item, setItem, errors }) => {
        if (isLoading) {
          return <Loading text={t("Editing properties..")} />;
        }

        return (
          <PropertiesForm
            properties={{ ...item }}
            errors={errors}
            onChange={(editedProperties) => setItem(editedProperties)}
            editing
          />
        );
      }}
    />
  );
};

export default PropertiesEdit;
