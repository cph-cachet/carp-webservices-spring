import { useFormik } from "formik";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import { useState, type FormEventHandler } from "react";
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import CarpInput from "../../components/CarpInput";
import { AuthInfoText } from "../../components/Layout/PublicPageLayout/AuthPageLayout/styles";
import * as yup from "yup";
import AuthActionButton from "../../components/Buttons/AuthActionButton";
import BannerLogin from "../../components/Layout/PublicPageLayout/BannerLogin";
import type { KcContext } from "../kcContext";
import type { I18n } from "../i18n";

const validationSchema = yup.object({
  username: yup
    .string()
    .email("Enter a valid email")
    .required("Email is required"),
});

const LoginResetPassword = (
  props: PageProps<
    Extract<KcContext, { pageId: "login-reset-password.ftl" }>,
    I18n
  >,
) => {
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

  const [isLoading, setIsLoading] = useState(false);
  const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>((e) => {
    e.preventDefault();

    setIsLoading(true);

    const formElement = e.target as HTMLFormElement;

    // NOTE: Even if we login with email Keycloak expect username and password in
    // the POST request.
    formElement
      .querySelector("input[name='email']")
      ?.setAttribute("name", "username");

    formElement.submit();
  });

  const formik = useFormik({
    initialValues: {
      username: "",
    },
    validationSchema,
    onSubmit: () => {},
  });

  const { url } = kcContext;

  const { msg, msgStr } = i18n;

  return (
    <Template
      {...{ kcContext, i18n, doUseDefaultCss, classes }}
      displayMessage={false}
      headerNode={msg("emailForgotTitle")}
      infoNode={<BannerLogin loginUrl={url.loginUrl} msgStr={msgStr} />}
    >
      <AuthInfoText variant="h4_web">
        {msgStr("forgotPasswordInfo")}
      </AuthInfoText>
      <form
        id="kc-reset-password-form"
        action={url.loginAction}
        method="post"
        onSubmit={onSubmit}
      >
        <CarpInput
          name="username"
          type="email"
          label={msgStr("email")}
          formikConfig={formik}
          autoComplete="email"
          variant="outlined"
        />
        <AuthActionButton loading={isLoading} text={msgStr("doSubmit")} />
      </form>
    </Template>
  );
};

export default LoginResetPassword;
