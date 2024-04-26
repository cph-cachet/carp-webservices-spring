import { useConstCallback } from "keycloakify/tools/useConstCallback";
import { useState, type FormEventHandler } from "react";
import { useGetClassName } from "keycloakify/login/lib/useGetClassName";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import { clsx } from "keycloakify/tools/clsx";
import type { I18n } from "../i18n";
import type { KcContext } from "../kcContext";
import * as yup from "yup";
import { useFormik } from "formik";
import CarpInput from "../../components/CarpInput";
import AuthActionButton from "../../components/Buttons/AuthActionButton";

const validationSchemaUsername = yup.object({
  username: yup.string().required("Username is required"),
  email: yup
    .string()
    .email("Enter a valid email")
    .required("Email is required"),
  firstName: yup.string().required("First name is required"),
  lastName: yup.string().required("Last name is required"),
});

const validationSchemaNoUsername = yup.object({
  email: yup
    .string()
    .email("Enter a valid email")
    .required("Email is required"),
  firstName: yup.string().required("First name is required"),
  lastName: yup.string().required("Last name is required"),
});

export default function LoginUpdateProfile(
  props: PageProps<
    Extract<KcContext, { pageId: "login-update-profile.ftl" }>,
    I18n
  >,
) {
  const [isLoading, setIsLoading] = useState(false);
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

  const { getClassName } = useGetClassName({
    doUseDefaultCss,
    classes,
  });

  const { msg, msgStr } = i18n;

  const { url, user, isAppInitiatedAction } = kcContext;

  const formik = useFormik({
    initialValues: {
      username: user.username ?? "",
      firstName: user.firstName ?? "",
      lastName: user.lastName ?? "",
      email: user.email ?? "",
    },
    validationSchema: user.editUsernameAllowed
      ? validationSchemaUsername
      : validationSchemaNoUsername,
    onSubmit: () => { },
  });

  const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>((e) => {
    e.preventDefault();
    setIsLoading(true);
    const formElement = e.target as HTMLFormElement;
    formElement.submit();
    setIsLoading(false);
  });

  return (
    <Template
      {...{ kcContext, i18n, doUseDefaultCss, classes }}
      headerNode={msg("loginProfileTitle")}
    >
      <form
        id="kc-update-profile-form"
        className={getClassName("kcFormClass")}
        onSubmit={onSubmit}
        action={url.loginAction}
        method="post"
      >
        {user.editUsernameAllowed && (
          <CarpInput
            name="username"
            label="Username"
            type="text"
            formikConfig={formik}
            autoComplete="username"
            variant="outlined"
          />
        )}

        <CarpInput
          name="email"
          label="Email"
          type="email"
          formikConfig={formik}
          autoComplete="email"
          variant="outlined"
        />

        <CarpInput
          name="firstName"
          label="First name"
          type="text"
          formikConfig={formik}
          autoComplete="given-name"
          variant="outlined"
        />

        <CarpInput
          name="lastName"
          label="Last name"
          type="text"
          formikConfig={formik}
          autoComplete="family-name"
          variant="outlined"
        />

        <div className={getClassName("kcFormGroupClass")}>
          <div
            id="kc-form-options"
            className={getClassName("kcFormOptionsClass")}
          >
            <div className={getClassName("kcFormOptionsWrapperClass")} />
          </div>

          <div
            id="kc-form-buttons"
            className={getClassName("kcFormButtonsClass")}
          >
            {isAppInitiatedAction ? (
              <>
                <input
                  className={clsx(
                    getClassName("kcButtonClass"),
                    getClassName("kcButtonPrimaryClass"),
                    getClassName("kcButtonLargeClass"),
                  )}
                  type="submit"
                  defaultValue={msgStr("doSubmit")}
                />
                <button
                  className={clsx(
                    getClassName("kcButtonClass"),
                    getClassName("kcButtonDefaultClass"),
                    getClassName("kcButtonLargeClass"),
                  )}
                  type="submit"
                  name="cancel-aia"
                  value="true"
                >
                  {msg("doCancel")}
                </button>
              </>
            ) : (
              <div
                id="kc-form-buttons"
                className={getClassName("kcFormButtonsClass")}
              >
                <AuthActionButton text="Submit" loading={isLoading} />
              </div>
            )}
          </div>
        </div>
      </form>
    </Template>
  );
}
