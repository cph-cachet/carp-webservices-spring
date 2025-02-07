import type { ClassKey } from "keycloakify/login/TemplateProps";
import type { Attribute } from "keycloakify/login/kcContext/KcContext";
import { useFormValidation } from "keycloakify/login/lib/useFormValidation";
import { Fragment, JSX, useEffect } from "react";
import CarpInputNoFormik from "../../../components/CarpInputNoFormik";
import type { I18n } from "../../i18n";

export type UserProfileFormFieldsProps = {
  kcContext: Parameters<typeof useFormValidation>[0]["kcContext"];
  i18n: I18n;
  getClassName: (classKey: ClassKey) => string;
  onIsFormSubmittableValueChange: (isFormSubmittable: boolean) => void;
  BeforeField?: (props: { attribute: Attribute }) => JSX.Element | null;
  AfterField?: (props: { attribute: Attribute }) => JSX.Element | null;
};

export const UserProfileFormFields = (props: UserProfileFormFieldsProps) => {
  const {
    kcContext,
    onIsFormSubmittableValueChange,
    i18n,
    getClassName,
    BeforeField,
    AfterField,
  } = props;

  const { advancedMsgStr, msg } = i18n;

  const {
    formValidationState: { fieldStateByAttributeName, isFormSubmittable },
    formValidationDispatch,
    attributesWithPassword,
  } = useFormValidation({
    kcContext,
    i18n,
  });

  useEffect(() => {
    onIsFormSubmittableValueChange(isFormSubmittable);
  }, [isFormSubmittable]);

  return (
    <>
      {attributesWithPassword.map((attribute, i) => {
        const { value, displayableErrors } =
          fieldStateByAttributeName[attribute.name];

        return (
          // eslint-disable-next-line react/no-array-index-key
          <Fragment key={i}>
            {BeforeField && <BeforeField attribute={attribute} />}
            <div className={getClassName("kcInputWrapperClass")}>
              {(() => {
                const { options } = attribute.validators;

                if (options !== undefined) {
                  return (
                    <select
                      id={attribute.name}
                      name={attribute.name}
                      onChange={(event) =>
                        formValidationDispatch({
                          action: "update value",
                          name: attribute.name,
                          newValue: event.target.value,
                        })
                      }
                      onBlur={() =>
                        formValidationDispatch({
                          action: "focus lost",
                          name: attribute.name,
                        })
                      }
                      value={value}
                    >
                      <option value="" selected disabled hidden>
                        {msg("selectAnOption")}
                      </option>
                      {options.options.map((option) => (
                        <option key={option} value={option}>
                          {option}
                        </option>
                      ))}
                    </select>
                  );
                }

                return (
                  <CarpInputNoFormik
                    type={(() => {
                      switch (attribute.name) {
                        case "password-confirm":
                        case "password":
                          return "password";
                        default:
                          return "text";
                      }
                    })()}
                    label={
                      (advancedMsgStr(attribute.displayName) as string) ?? ""
                    }
                    id={attribute.name}
                    variant="outlined"
                    name={attribute.name}
                    value={value}
                    required={attribute.required}
                    onChange={(event) =>
                      formValidationDispatch({
                        action: "update value",
                        name: attribute.name,
                        newValue: event.target.value,
                      })
                    }
                    onBlur={() =>
                      formValidationDispatch({
                        action: "focus lost",
                        name: attribute.name,
                      })
                    }
                    aria-invalid={displayableErrors.length !== 0}
                    disabled={attribute.readOnly}
                    autoComplete={attribute.autocomplete}
                  />
                );
              })()}
              {displayableErrors.length !== 0 &&
                (() => {
                  const divId = `input-error-${attribute.name}`;

                  return (
                    <>
                      <style>{`#${divId} > span: { display: block; }`}</style>
                      <span
                        id={divId}
                        className={getClassName("kcInputErrorMessageClass")}
                        style={{
                          position:
                            displayableErrors.length === 1
                              ? "absolute"
                              : undefined,
                        }}
                        aria-live="polite"
                      >
                        {displayableErrors.map(
                          ({ errorMessage }) => errorMessage,
                        )}
                      </span>
                    </>
                  );
                })()}
            </div>

            {AfterField && <AfterField attribute={attribute} />}
          </Fragment>
        );
      })}
    </>
  );
};
