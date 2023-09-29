// Copy pasted from: https://github.com/InseeFrLab/keycloakify/blob/main/src/login/Template.tsx

import { clsx } from "keycloakify/tools/clsx";
import { usePrepareTemplate } from "keycloakify/lib/usePrepareTemplate";
import { type TemplateProps } from "keycloakify/login/TemplateProps";
import { useGetClassName } from "keycloakify/login/lib/useGetClassName";
import AuthPageLayout from "src/components/Layout/PublicPageLayout/AuthPageLayout";
import { Alert } from "@mui/material";
import type { KcContext } from "./kcContext";
import type { I18n } from "./i18n";
import PublicPageLayout from "../components/Layout/PublicPageLayout";

export const Template = (props: TemplateProps<KcContext, I18n>) => {
  const {
    displayInfo = false,
    displayMessage = true,
    displayRequiredFields = false,
    displayWide = false,
    showAnotherWayIfPresent = true,
    headerNode,
    showUsernameNode = null,
    infoNode = null,
    kcContext,
    i18n,
    doUseDefaultCss,
    classes,
    children
  } = props;

  const { getClassName } = useGetClassName({ doUseDefaultCss, classes });

  const { msg } = i18n;

  const { realm, auth, url, message, isAppInitiatedAction } = kcContext;

  const { isReady } = usePrepareTemplate({
    "doFetchDefaultThemeResources": doUseDefaultCss,
    "styles": [
    ],
    "htmlClassName": getClassName("kcHtmlClass"),
    "bodyClassName": undefined
  });

  if (!isReady) {
    return null;
  }

  return (
    <PublicPageLayout infoNode={infoNode}>
      <AuthPageLayout title={headerNode}>
        {/* 
        <header className={getClassName("kcFormHeaderClass")}>
          {!(auth !== undefined && auth.showUsername && !auth.showResetCredentials) ? (
            displayRequiredFields ? (
              <div className={getClassName("kcContentWrapperClass")}>
                <div className={clsx(getClassName("kcLabelWrapperClass"), "subtitle")}>
                  <span className="subtitle">
                    <span className="required">*</span>
                    {msg("requiredFields")}
                  </span>
                </div>
                <div className="col-md-10">
                  <h1 id="kc-page-title">{headerNode}</h1>
                </div>
              </div>
            ) : (
              <h1 id="kc-page-title">{headerNode}</h1>
            )
          ) : displayRequiredFields ? (
            <div className={getClassName("kcContentWrapperClass")}>
              <div className={clsx(getClassName("kcLabelWrapperClass"), "subtitle")}>
                <span className="subtitle">
                  <span className="required">*</span> {msg("requiredFields")}
                </span>
              </div>
              <div className="col-md-10">
                {showUsernameNode}
                <div className={getClassName("kcFormGroupClass")}>
                  <div id="kc-username">
                    <label id="kc-attempted-username">{auth?.attemptedUsername}</label>
                    <a id="reset-login" href={url.loginRestartFlowUrl}>
                      <div className="kc-login-tooltip">
                        <i className={getClassName("kcResetFlowIcon")} />
                        <span className="kc-tooltip-text">{msg("restartLoginTooltip")}</span>
                      </div>
                    </a>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <>
              {showUsernameNode}
              <div className={getClassName("kcFormGroupClass")}>
                <div id="kc-username">
                  <label id="kc-attempted-username">{auth?.attemptedUsername}</label>
                  <a id="reset-login" href={url.loginRestartFlowUrl}>
                    <div className="kc-login-tooltip">
                      <i className={getClassName("kcResetFlowIcon")} />
                      <span className="kc-tooltip-text">{msg("restartLoginTooltip")}</span>
                    </div>
                  </a>
                </div>
              </div>
            </>
          )}
        </header>
        */}
        {displayMessage && message !== undefined && (message.type !== "warning" || !isAppInitiatedAction) && (
          <Alert severity={message.type} sx={{mb: 2}}>
            {message.summary}
          </Alert>
        )}
        {children}
        {auth !== undefined && auth.showTryAnotherWayLink && showAnotherWayIfPresent && (
          <form
            id="kc-select-try-another-way-form"
            action={url.loginAction}
            method="post"
            className={clsx(displayWide && getClassName("kcContentWrapperClass"))}
          >
            <div
              className={clsx(
                displayWide && [getClassName("kcFormSocialAccountContentClass"), getClassName("kcFormSocialAccountClass")]
              )}
            >
              <div className={getClassName("kcFormGroupClass")}>
                <input type="hidden" name="tryAnotherWay" value="on" />
                {/* eslint-disable-next-line jsx-a11y/anchor-is-valid */}
                <a
                  href="#"
                  id="try-another-way"
                  onClick={() => {
                    document.forms["kc-select-try-another-way-form" as never].submit();
                    return false;
                  }}
                >
                  {msg("doTryAnotherWay")}
                </a>
              </div>
            </div>
          </form>
        )}
      </AuthPageLayout>
    </PublicPageLayout >
  );
}

export default Template;
