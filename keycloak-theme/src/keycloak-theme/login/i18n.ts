import { createUseI18n } from 'keycloakify/login';

export const { useI18n } = createUseI18n({
  en: {
    alphanumericalCharsOnly: 'Only alphanumerical characters',
    gender: 'Gender',
    // Here we overwrite the default english value for the message "doForgotPassword"
    // that is "Forgot Password?" see: https://github.com/InseeFrLab/keycloakify/blob/f0ae5ea908e0aa42391af323b6d5e2fd371af851/src/lib/i18n/generated_messages/18.0.1/login/en.ts#L17
    doForgotPassword: 'I forgot my password',
    invalidUserMessage: 'Invalid username or password.',
  },
});

export type I18n = NonNullable<ReturnType<typeof useI18n>>;
