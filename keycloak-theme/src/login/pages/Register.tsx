// ejected using 'npx eject-keycloak-page'
import { useState, type FormEventHandler } from "react";
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import { useFormik } from "formik";
import { useGetClassName } from "keycloakify/login/lib/useGetClassName";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import AuthActionButton from "../../components/Buttons/AuthActionButton";
import CarpInput from "../../components/CarpInput";
import { AuthInfoText } from "../../components/Layout/PublicPageLayout/AuthPageLayout/styles";
import BannerLogin from "../../components/Layout/PublicPageLayout/BannerLogin";
import StyledLink from "../../components/StyledLink";
import * as yup from "yup";
import type { I18n } from "../i18n";
import type { KcContext } from "../kcContext";

const validationSchema = yup.object({
  firstName: yup.string().required("First name is required"),
  lastName: yup.string().required("Last name is required"),
  email: yup
    .string()
    .email("Enter a valid email")
    .required("Email is required"),
  password: yup
    .string()
    .min(8, "Password has to be at least 8 characters long")
    .required("Password is required"),
  "password-confirm": yup
    .string()
    .min(8, "Password has to be at least 8 characters long")
    .required("Password is required")
    .oneOf([yup.ref("password"), null], "Passwords must match"),
});

const Register = (
  props: PageProps<Extract<KcContext, { pageId: "register.ftl" }>, I18n>,
) => {
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

  const { getClassName } = useGetClassName({
    doUseDefaultCss,
    classes,
  });

  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>((e) => {
    e.preventDefault();
    setIsLoading(true);
    const formElement = e.target as HTMLFormElement;
    formElement.submit();
  });

  const {
    url,
    register,
    passwordRequired,
    recaptchaRequired,
    recaptchaSiteKey,
  } = kcContext;

  const formik = useFormik({
    initialValues: {
      firstName: register.formData.firstName ?? "",
      lastName: register.formData.lastName ?? "",
      email: register.formData.email ?? "",
      password: "",
      "password-confirm": "",
    },
    validationSchema,
    onSubmit: () => {},
  });

  const { msg, msgStr } = i18n;

  return (
    <Template
      {...{ kcContext, i18n, doUseDefaultCss, classes }}
      headerNode={msg("registerTitle")}
      infoNode={<BannerLogin loginUrl={url.loginUrl} msgStr={msgStr} />}
    >
      <form
        id="kc-register-form"
        action={url.registrationAction}
        method="post"
        onSubmit={onSubmit}
      >
        <CarpInput
          name="firstName"
          label={msgStr("firstName")}
          type="text"
          formikConfig={formik}
          autoComplete="given-name"
          variant="outlined"
        />
        <CarpInput
          name="lastName"
          label={msgStr("lastName")}
          type="text"
          formikConfig={formik}
          autoComplete="family-name"
          variant="outlined"
        />
        <CarpInput
          name="email"
          label={msgStr("email")}
          type="text"
          formikConfig={formik}
          autoComplete="email"
          variant="outlined"
        />
        {passwordRequired && (
          <>
            <CarpInput
              name="password"
              label={msgStr("password")}
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
          </>
        )}
        {recaptchaRequired && (
          <div className="form-group">
            <div className={getClassName("kcInputWrapperClass")}>
              <div
                className="g-recaptcha"
                data-size="compact"
                data-sitekey={recaptchaSiteKey}
              />
            </div>
          </div>
        )}

        <AuthActionButton text={msgStr("doRegister")} loading={isLoading} />
        <AuthInfoText variant="h4_web" hideOnMobile>
          {msgStr("byRegisteringYouAgree")}{" "}
          <StyledLink to="https://carp.cachet.dk/privacy-policy-service/">
            {msgStr("carpPrivacyPolicy")}
          </StyledLink>{" "}
          {msgStr("and")}{" "}
          <StyledLink to="/forgot-password">
            {msgStr("termsOfService")}
          </StyledLink>
          .
        </AuthInfoText>
      </form>
    </Template>
  );
};

export default Register;
