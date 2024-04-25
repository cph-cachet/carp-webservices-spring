import { lazy, Suspense } from "react";
import Fallback, { type PageProps } from "keycloakify/login";
import type { KcContext } from "./kcContext";
import { useI18n } from "./i18n";
import LoginResetPassword from "./pages/LoginResetPassword";
import { CssBaseline, StyledEngineProvider, ThemeProvider } from "@mui/material";
import LoginUpdatePassword from "./pages/LoginUpdatePassword";
import LoginUpdateProfile from "./pages/LoginUpdateProfile";
import { themeInstance } from "../utils/theme";

const Template = lazy(() => import("./Template"));

// You can uncomment this to see the values passed by the main app before redirecting.  
// import { foo, bar } from "./valuesTransferredOverUrl";
// console.log(`Values passed by the main app in the URL parameter:`, { foo, bar });

const Login = lazy(() => import("./pages/Login"));
// If you can, favor register-user-profile.ftl over register.ftl, see: https://docs.keycloakify.dev/realtime-input-validation
const Register = lazy(() => import("./pages/Register"));
const RegisterUserProfile = lazy(() => import("./pages/RegisterUserProfile"));
const Terms = lazy(() => import("./pages/Terms"));
const MyExtraPage1 = lazy(() => import("./pages/MyExtraPage1"));
const MyExtraPage2 = lazy(() => import("./pages/MyExtraPage2"));
const Info = lazy(() => import("./pages/Info"));

// This is like adding classes to theme.properties 
// https://github.com/keycloak/keycloak/blob/11.0.3/themes/src/main/resources/theme/keycloak/login/theme.properties
const classes: PageProps<any, any>["classes"] = {
  // NOTE: The classes are defined in ./KcApp.css
  "kcHtmlClass": "my-root-class",
  "kcHeaderWrapperClass": "my-color my-font"
};

export default function KcApp(props: { kcContext: KcContext; }) {

  const { kcContext } = props;

  const i18n = useI18n({ kcContext });

  if (i18n === null) {
    // NOTE: Text resources for the current language are still being downloaded, we can't display anything yet.
    // We could display a loading progress but it's usually a matter of milliseconds.
    return null;
  }

  /* 
  * Examples assuming i18n.currentLanguageTag === "en":
  * i18n.msg("access-denied") === <span>Access denied</span>
  * i18n.msg("foo") === <span>foo in English</span>
  */

  return (
    <Suspense>
      <StyledEngineProvider injectFirst>
        <ThemeProvider theme={themeInstance}>
          <CssBaseline />
      {(() => {
        switch (kcContext.pageId) {
          case "login.ftl": return <Login {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />;
          case "register.ftl": return <Register {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />;
          case "register-user-profile.ftl": return <RegisterUserProfile {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />
          case "login-update-profile.ftl": return <LoginUpdateProfile {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />;
          case "login-update-password.ftl": return <LoginUpdatePassword {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />;
          case "login-reset-password.ftl": return <LoginResetPassword {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />;
          case "terms.ftl": return <Terms {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />;
          case "my-extra-page-1.ftl": return <MyExtraPage1 {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />;
          case "my-extra-page-2.ftl": return <MyExtraPage2 {...{ kcContext, i18n, Template, classes }} doUseDefaultCss />;
          case "info.ftl": return <Info {...{ kcContext, i18n, classes, Template }} doUseDefaultCss />;
          default: return <Fallback {...{ kcContext, i18n, classes }} Template={Template} doUseDefaultCss />;
        }
      })()}
      </ThemeProvider>
      </StyledEngineProvider>
    </Suspense>
  );

}
