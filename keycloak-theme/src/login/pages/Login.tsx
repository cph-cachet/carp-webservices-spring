
import { useEffect, type FormEventHandler } from "react";
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import type { KcContext } from "../kcContext";
import type { I18n } from "../i18n";

import { Checkbox, FormControlLabel, FormGroup } from '@mui/material';

import CarpInput from '../../components/CarpInput';
import PublicPageLayout from '../../components/Layout/PublicPageLayout';
import { useFormik } from 'formik';
import { useState } from 'react';
import * as yup from 'yup';

import AppleLogo from '../../assets/images/logo-apple.png';
import GoogleLogo from '../../assets/images/logo-google.png';
import PasskeyLogo from '../../assets/images/logo-passkey.png';
import AuthActionButton from '../../components/Buttons/AuthActionButton';
import LoginOauthOption from '../../components/Buttons/OauthOptions';
import AuthPageLayout from '../../components/Layout/PublicPageLayout/AuthPageLayout';
import { AuthInfoText } from '../../components/Layout/PublicPageLayout/AuthPageLayout/styles';
import StyledLink from '../../components/StyledLink';
import tos_url from '../assets/tos.md'
import privacy_policy_url from '../assets/privacy_policy.md'

import {
  LoginAdditionalActions,
  LoginOauthOptions,
  LoginSeparator,
  LoginSeparatorText,
} from './styles';
import CustomizedSnackbar, { SnackbarType } from "src/components/Snackbar";

const validationSchema = yup.object({
  username: yup
    .string()
    .email('Enter a valid email')
    .required('Email is required'),
  password: yup.string().required('Password is required'),
});
export default function Login(props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) {
  const { kcContext } = props;
  const { social, url, login, message } = kcContext;
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

  const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>(e => {
    e.preventDefault();

    setIsLoginButtonDisabled(true);

    const formElement = e.target as HTMLFormElement;

    formElement.submit();
  });

  const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);
  const formik = useFormik({
    initialValues: {
      username: '',
      password: '',
    },
    validationSchema,
    onSubmit: () => { }
  });

  const [staySignedIn, setStaySignedIn] = useState(false);
  const toggleSignedIn = () => setStaySignedIn(!staySignedIn);

  return (
    <PublicPageLayout registrationUrl={url.registrationUrl} loginUrl={url.loginUrl}>
      <AuthPageLayout title="Log in">
        <form id="kc-form-login" onSubmit={onSubmit} action={url.loginAction} method="post">
          <CarpInput
            name="username"
            label="Email Address"
            type="email"
            formikConfig={formik}
            autoComplete="email section-blue"
            variant="outlined"
          />
          <CarpInput
            name="password"
            label="Password"
            type="password"
            formikConfig={formik}
            autoComplete="current-password section-blue"
            variant="outlined"
          />
          <LoginAdditionalActions>
            <FormGroup>
              <FormControlLabel
                control={
                  <Checkbox
                    {...(login.rememberMe
                      ? {
                        "checked": true
                      }
                      : {})}
                    onChange={toggleSignedIn}
                    name="rememberMe"
                    inputProps={{ 'aria-label': 'controlled' }}
                  />
                }
                label="Stay signed in"
              />
            </FormGroup>
            <AuthInfoText variant="h4_web">
              <StyledLink to={url.loginResetCredentialsUrl}>Forgot your password?</StyledLink>
            </AuthInfoText>
          </LoginAdditionalActions>
          <AuthActionButton loading={isLoginButtonDisabled} text="Log in" />
          <AuthInfoText variant="h4_web" hideOnMobile>
            By logging in, you agree to the{' '}
            <StyledLink to={privacy_policy_url}>
              Cachet Privacy Statement
            </StyledLink>{' '}
            and <StyledLink to={tos_url}>Terms of Service</StyledLink>.
          </AuthInfoText>
        </form>
        {
          social.providers !== undefined && (
            <>
              <LoginSeparator>
                <LoginSeparatorText variant="h4_web" component="span">
                  Or log in with
                </LoginSeparatorText>
              </LoginSeparator>
              <LoginOauthOptions>
                <LoginOauthOption logoSrc={AppleLogo} name="Apple" />
                <LoginOauthOption logoSrc={PasskeyLogo} name="Passkey" />
                <LoginOauthOption logoSrc={GoogleLogo} name="Google" />
              </LoginOauthOptions>
            </>
          )
        }
        <CustomizedSnackbar {...snackbarState} setSnackbarState={setSnackbarState}/>
      </AuthPageLayout>
    </PublicPageLayout>
  )
}
