import { useState, type FormEventHandler } from "react";
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import { useFormik } from "formik";
import { useGetClassName } from "keycloakify/login/lib/useGetClassName";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import BannerLogin from "../../components/Layout/PublicPageLayout/BannerLogin";
import * as yup from "yup";
import AuthActionButton from "../../components/Buttons/AuthActionButton";
import type { I18n } from "../i18n";
import type { KcContext } from "../kcContext";
import CarpInput from "../../components/CarpInput";

const LoginUpdatePassword = (
  props: PageProps<
    Extract<KcContext, { pageId: "login-update-password.ftl" }>,
    I18n
  >,
) => {
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

  const { getClassName } = useGetClassName({
    doUseDefaultCss,
    classes,
  });

  const { msg, msgStr } = i18n;

  const { url, isAppInitiatedAction, username } = kcContext;

  const validationSchema = yup.object({
    "password-new": yup
      .string()
      .min(8, msgStr("passwordMinLength"))
      .required(msgStr('passwordRequired')),
    "password-confirm": yup
      .string()
      .min(8, msgStr('passwordMinLength'))
      .required(msgStr('passwordRequired'))
      .oneOf([yup.ref("password-new"), null], msgStr("passwordsDontMatch")),
  });

  const formik = useFormik({
    initialValues: {
      "password-new": "",
      "password-confirm": "",
    },
    validationSchema,
    onSubmit: () => {},
  });

  const [isLoading, setIsLoading] = useState(false);
  const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>((e) => {
    e.preventDefault();
    setIsLoading(true);
    const formElement = e.target as HTMLFormElement;
    formElement.submit();
  });

  return (
    <Template
      {...{ kcContext, i18n, doUseDefaultCss, classes }}
      headerNode={msg("updatePasswordTitle")}
      infoNode={<BannerLogin loginUrl={url.loginUrl} msgStr={msgStr} />}
    >
      <form
        id="kc-passwd-update-form"
        action={url.loginAction}
        method="post"
        onSubmit={onSubmit}
      >
        <input
          type="text"
          id="username"
          name="username"
          value={username}
          readOnly
          autoComplete="username"
          style={{ display: "none" }}
        />
        <input
          type="password"
          id="password"
          name="password"
          autoComplete="current-password"
          style={{ display: "none" }}
        />

        <CarpInput
          name="password-new"
          label={msgStr("passwordNew")}
          type="password"
          formikConfig={formik}
          autoComplete="new-password"
          variant="outlined"
        />
        <CarpInput
          name="password-confirm"
          label={msgStr("passwordConfirm")}
          type="password"
          formikConfig={formik}
          autoComplete="new-password"
          variant="outlined"
        />

        <div className={getClassName("kcFormGroupClass")}>
          <div
            id="kc-form-options"
            className={getClassName("kcFormOptionsClass")}
          >
            <div className={getClassName("kcFormOptionsWrapperClass")}>
              {isAppInitiatedAction && (
                <div className="checkbox">
                  <label>
                    <input
                      type="checkbox"
                      id="logout-sessions"
                      name="logout-sessions"
                      value="on"
                      checked
                    />
                    {msgStr("logoutOtherSessions")}
                  </label>
                </div>
              )}
            </div>
          </div>

          <div
            id="kc-form-buttons"
            className={getClassName("kcFormButtonsClass")}
          >
            <AuthActionButton text={msgStr("doSubmit")} loading={isLoading} />
          </div>
        </div>
      </form>
    </Template>
  );
};

export default LoginUpdatePassword;
