/* eslint-disable no-nested-ternary */
import { Box, Typography } from "@mui/material";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import { assert } from "keycloakify/tools/assert";
import type { I18n } from "../i18n";
import type { KcContext } from "../kcContext";
import CarpButton from "../../components/Buttons/AuthActionButton/styles";

const Info = (
  props: PageProps<Extract<KcContext, { pageId: "info.ftl" }>, I18n>,
) => {
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

  const { msgStr } = i18n;

  assert(
    kcContext.message !== undefined,
    "No message in kcContext.message, there will always be a message in production context, add it in your mock",
  );

  const {
    messageHeader,
    message,
    requiredActions,
    skipLink,
    pageRedirectUri,
    actionUri,
    client,
  } = kcContext;

  return (
    <Template
      {...{ kcContext, i18n, doUseDefaultCss, classes }}
      displayMessage={false}
      headerNode={
        messageHeader !== undefined ? (
          <Typography variant="h1">{messageHeader}</Typography>
        ) : (
          <Typography variant="h1">{message.summary}</Typography>
        )
      }
    >
      <div id="kc-info-message">
        <Typography variant="body1" className="instruction">
          {message.summary}{" "}
          {requiredActions !== undefined && (
            <b>
              {requiredActions
                .map((requiredAction) =>
                  msgStr(`requiredAction.${requiredAction}` as const),
                )
                .join(", ")}
            </b>
          )}
        </Typography>
        <Box display="flex" justifyContent="center" mt={4}>
          {!skipLink && pageRedirectUri !== undefined ? (
            <CarpButton href={pageRedirectUri} variant="contained">
              {msgStr("backToApplication")}
            </CarpButton>
          ) : actionUri !== undefined ? (
            <CarpButton href={actionUri} variant="contained">
              {msgStr("proceedWithAction")}
            </CarpButton>
          ) : (
            client.baseUrl !== undefined && (
              <CarpButton href={client.baseUrl} variant="contained">
                {msgStr("backToApplication")}
              </CarpButton>
            )
          )}
        </Box>
      </div>
    </Template>
  );
};

export default Info;
