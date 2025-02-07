import type { PageProps } from "keycloakify/account";
import { lazy, Suspense } from "react";
import { useI18n } from "./i18n";
import type { KcContext } from "./kcContext";

const Template = lazy(() => import("./Template"));

const Password = lazy(() => import("./pages/Password"));
const Fallback = lazy(() => import("keycloakify/account"));

const classes: PageProps<any, any>["classes"] = {
  kcBodyClass: "my-root-account-class",
};

const KcApp = (props: { kcContext: KcContext }) => {
  const { kcContext } = props;

  const i18n = useI18n({ kcContext });

  if (i18n === null) {
    return null;
  }

  return (
    <Suspense>
      {(() => {
        switch (kcContext.pageId) {
          case "password.ftl":
            return (
              <Password
                {...{ kcContext, i18n, Template, classes }}
                doUseDefaultCss
              />
            );
          default:
            return (
              <Fallback
                {...{ kcContext, i18n, classes }}
                Template={Template}
                doUseDefaultCss
              />
            );
        }
      })()}
    </Suspense>
  );
};

export default KcApp;
