import { useState, type FormEventHandler } from "react";
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import {
  Checkbox,
  FormControlLabel,
  FormGroup,
} from '@mui/material';
import * as yup from 'yup';
import { useFormik } from "formik";
import CarpInput from "src/components/CarpInput";
import { AuthInfoText } from "src/components/Layout/PublicPageLayout/AuthPageLayout/styles";
import StyledLink from "src/components/StyledLink";
import LoginOauthOption from "src/components/Buttons/OauthOptions";
import AuthActionButton from "src/components/Buttons/AuthActionButton";
import AppleLogo from '../../assets/images/logo-apple.png';
import GoogleLogo from '../../assets/images/logo-google.png';
import PasskeyLogo from '../../assets/images/logo-passkey.png';
import type { KcContext } from "../kcContext";
import type { I18n } from "../i18n";
import BannerRegister from '../../components/Layout/PublicPageLayout/BannerRegister'
import { LoginAdditionalActions, LoginOauthOptions, LoginSeparator, LoginSeparatorText } from "./styles";

const validationSchema = yup.object({
  username: yup
    .string()
    .email('Enter a valid email')
    .required('Email is required'),
  password: yup.string().required('Password is required'),
});

const Login = (props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) => {
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

  const { social, realm, url, usernameHidden, login, registrationDisabled } = kcContext;

  const { msg } = i18n;

  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>(e => {
    e.preventDefault();
    setIsLoading(true);
    const formElement = e.target as HTMLFormElement;

    // NOTE: Even if we login with email Keycloak expect username and password in
    // the POST request.
    formElement.querySelector("input[name='email']")?.setAttribute("name", "username");
    formElement.submit();
  });

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
    <Template
      {...{ kcContext, i18n, doUseDefaultCss, classes }}
      displayInfo={social.displayInfo}
      displayWide={realm.password && social.providers !== undefined}
      headerNode={msg("doLogIn")}
      infoNode={
        realm.registrationAllowed && !registrationDisabled && <BannerRegister registerUrl={url.registrationUrl} />
      }
    >
      {realm.password && (
        <form id="kc-form-login" onSubmit={onSubmit} action={url.loginAction} method="post">
          {!usernameHidden &&
            <CarpInput
              name="username"
              label="Email Address"
              type="email"
              formikConfig={formik}
              autoComplete="email section-blue"
              variant="outlined"
            />
          }

          <CarpInput
            name="password"
            label="Password"
            type="password"
            formikConfig={formik}
            autoComplete="current-password section-blue"
            variant="outlined"
          />
          <LoginAdditionalActions>
            {realm.rememberMe && !usernameHidden && (
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
            )}
            {realm.resetPasswordAllowed && (
              <AuthInfoText variant="h4_web">
                <StyledLink to={url.loginResetCredentialsUrl}>Forgot your password?</StyledLink>
              </AuthInfoText>
            )}
          </LoginAdditionalActions>
          <AuthActionButton loading={isLoading} text="Log in" />
          <AuthInfoText variant="h4_web" hideOnMobile>
            By logging in, you agree to the{' '}
            <StyledLink to="https://carp.cachet.dk/privacy-policy-service/">
              Cachet Privacy Statement
            </StyledLink>{' '}
            and <StyledLink to="https://carp.cachet.dk/privacy-policy-service/">Terms of Service</StyledLink>.
          </AuthInfoText>
        </form>
      )}

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

    </Template >
  );
}

export default Login;
