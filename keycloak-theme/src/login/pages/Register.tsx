import { useEffect, type FormEventHandler } from "react";
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../kcContext";
import type { I18n } from "../i18n";
import * as yup from "yup";
import { useFormik } from "formik";
import PublicPageLayout from "src/components/Layout/PublicPageLayout";
import AuthPageLayout from "src/components/Layout/PublicPageLayout/AuthPageLayout";
import CarpInput from "src/components/CarpInput";
import { useState } from "react";
import AuthActionButton from "src/components/Buttons/AuthActionButton";
import CustomizedSnackbar, { SnackbarType } from "src/components/Snackbar";

const validationSchema = yup.object({
  firstName: yup.string().required("First name is required"),
  lastName: yup.string().required("Last name is required"),
  username: yup.string().optional(),
  password: yup
    .string()
    .min(8, "Password has to be at least 8 characters long")
    .required("Password is required"),
  'password-confirm': yup
    .string()
    .min(8, "Password has to be at least 8 characters long")
    .required("Password is required")
    .oneOf([yup.ref("password"), null], "Passwords must match"),
});

export default function Register(
  props: PageProps<Extract<KcContext, { pageId: "register.ftl" }>, I18n>
) {
  const formik = useFormik({
    initialValues: {
      firstName: "",
      lastName: "",
      email: "",
      username: "",
      password: "",
      'password-confirm': "",
    },
    validateOnChange: false,
    validationSchema,
    onSubmit: () => { },
  });
  const { kcContext } = props;

  const {
    url,
    realm,
    passwordRequired,
    message
  } = kcContext;

  const [snackbarState, setSnackbarState] = useState<SnackbarType>({
    snackbarOpen: false,
    snackbarType: 'error',
    snackbarMessage: 'Unexpected error',
  });

  useEffect(() => {
    if (message) {
      setSnackbarState({ snackbarType: message.type, snackbarOpen: true, snackbarMessage: message.summary })
    }
  }, [message])

  const [isRegisterButtonDisabled, setIsRegisterButtonDisabled] = useState(false);
  const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>(e => {
    e.preventDefault();

    setIsRegisterButtonDisabled(true);

    const formElement = e.target as HTMLFormElement;

    formElement.submit();
  });


  return (
    <PublicPageLayout loginUrl={url.loginUrl}>
      <AuthPageLayout title="Register account">
        <form
          id="kc-register-form"
          onSubmit={onSubmit}
          action={url.registrationAction}
          method="post"
        >
          <CarpInput
            name="firstName"
            label="First Name"
            type="text"
            formikConfig={formik}
            autoComplete="given-name"
            variant="outlined"
          />
          <CarpInput
            name="lastName"
            label="Last Name"
            type="text"
            formikConfig={formik}
            autoComplete="family-name"
            variant="outlined"
          />

          <CarpInput
            name="email"
            label="Email"
            type="text"
            formikConfig={formik}
            autoComplete="email"
            variant="outlined"
          />
          {!realm.registrationEmailAsUsername && (
            <CarpInput
              name="username"
              type="text"
              label="Username"
              formikConfig={formik}
              autoComplete="username"
              variant="outlined"
            />
          )}
          {passwordRequired && (
            <>
              <CarpInput
                name="password"
                label="Password"
                type="password"
                formikConfig={formik}
                autoComplete="new-password"
                variant="outlined"
              />
              <CarpInput
                name="password-confirm"
                label="Confirm Password"
                type="password"
                formikConfig={formik}
                autoComplete="new-password"
                variant="outlined"
              />
            </>
          )}
          <AuthActionButton text="Sign up" loading={isRegisterButtonDisabled} />
        </form>
        <CustomizedSnackbar {...snackbarState} setSnackbarState={setSnackbarState} />
      </AuthPageLayout>
    </PublicPageLayout>
  );
}
