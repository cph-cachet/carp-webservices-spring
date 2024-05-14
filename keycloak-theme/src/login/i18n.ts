import { createUseI18n } from "keycloakify/login";

export const { useI18n } = createUseI18n({
  en: {
    alphanumericalCharsOnly: "Only alphanumerical characters",
    gender: "Gender",
    // Here we overwrite the default english value for the message "doForgotPassword"
    // that is "Forgot Password?" see: https://github.com/InseeFrLab/keycloakify/blob/f0ae5ea908e0aa42391af323b6d5e2fd371af851/src/lib/i18n/generated_messages/18.0.1/login/en.ts#L17
    doForgotPassword: "Forgot your password?",
    invalidUserMessage: "Invalid username or password.",
    backToApplication: "Back to application",
    proceedWithAction: "Click here to proceed",
    doLogIn: "Log in",
    agreeToPrivacyPolicy: "By logging in, you agree to the",
    carpPrivacyPolicy: "CARP Privacy Policy",
    newToCarp: "New to CARP?",
  },
  da: {
    agreeToPrivacyPolicy: "Når du logger ind, accepterer du",
    carpPrivacyPolicy: "CARP's privatlivspolitik",
    newToCarp: "Ny på CARP?",
  },
});

export type I18n = NonNullable<ReturnType<typeof useI18n>>;
