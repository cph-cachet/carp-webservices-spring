import { Box, Button, Typography } from "@mui/material";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import { assert } from "keycloakify/tools/assert";
import type { I18n } from "../i18n";
import type { KcContext } from "../kcContext";

export default function Info(
  props: PageProps<Extract<KcContext, { pageId: "info.ftl" }>, I18n>,
) {
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
          <>{messageHeader}</>
        ) : (
          <>{message.summary}</>
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
                .join(",")}
            </b>
          )}
        </Typography>
        <Box display="flex" justifyContent="center" mt={4}>
          {!skipLink && pageRedirectUri !== undefined ? (
            <Button href={pageRedirectUri} variant="contained">
              {msgStr("backToApplication").replace("&raquo;", "").replace("&laquo;", "")}
            </Button>
          ) : actionUri !== undefined ? (
            <Button href={actionUri} variant="contained">
              {msgStr("proceedWithAction").replace("&raquo;", "").replace("&laquo;", "")}
            </Button>
          ) : (
            client.baseUrl !== undefined && (
              <Button href={client.baseUrl} variant="contained">
              {msgStr("backToApplication").replace("&raquo;", "").replace("&laquo;", "")}
              </Button>
            )
          )}
        </Box>
      </div>
    </Template>
  );
}
