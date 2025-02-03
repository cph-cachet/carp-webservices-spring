// ejected using 'npx eject-keycloak-page'
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import { useState, type FormEventHandler } from "react";
import { useGetClassName } from "keycloakify/login/lib/useGetClassName";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { I18n } from "../i18n";
import type { KcContext } from "../kcContext";
import { UserProfileFormFields } from "./shared/UserProfileFormFields";
import AuthActionButton from "../../components/Buttons/AuthActionButton";

const RegisterUserProfile = (
  props: PageProps<
    Extract<KcContext, { pageId: "register-user-profile.ftl" }>,
    I18n
  >,
) => {
  const [isLoading, setIsLoading] = useState(false);
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

  const { getClassName } = useGetClassName({
    doUseDefaultCss,
    classes,
  });

  const { url, messagesPerField, recaptchaRequired, recaptchaSiteKey } =
    kcContext;

  const { msg, msgStr } = i18n;

  const [, setIsFormSubmittable] = useState(false);

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
      displayMessage={messagesPerField.exists("global")}
      displayRequiredFields
      headerNode={msg("registerTitle")}
    >
      <form
        id="kc-register-form"
        className={getClassName("kcFormClass")}
        action={url.registrationAction}
        onSubmit={onSubmit}
        method="post"
      >
        <UserProfileFormFields
          kcContext={kcContext}
          onIsFormSubmittableValueChange={setIsFormSubmittable}
          i18n={i18n}
          getClassName={getClassName}
        />
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
        <div
          className={getClassName("kcFormGroupClass")}
          style={{ marginBottom: 30 }}
        >
          <AuthActionButton text={msgStr("doSubmit")} loading={isLoading} />
        </div>
      </form>
    </Template>
  );
};

export default RegisterUserProfile;
