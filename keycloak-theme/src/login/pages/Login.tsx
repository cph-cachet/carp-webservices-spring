import { useState, type FormEventHandler } from "react";
import { clsx } from "keycloakify/tools/clsx";
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import { useGetClassName } from "keycloakify/login/lib/useGetClassName";
import {
  Checkbox,
  CssBaseline,
  FormControlLabel,
  FormGroup,
  StyledEngineProvider,
  ThemeProvider,
} from '@mui/material';
import * as yup from 'yup';
import AppleLogo from '../../assets/images/logo-apple.png';
import GoogleLogo from '../../assets/images/logo-google.png';
import PasskeyLogo from '../../assets/images/logo-passkey.png';
import type { KcContext } from "../kcContext";
import type { I18n } from "../i18n";
import { themeInstance } from '../../utils/theme';
import BannerRegister from '../../components/Layout/PublicPageLayout/BannerRegister'
import { useFormik } from "formik";
import CarpInput from "src/components/CarpInput";
import { LoginAdditionalActions, LoginOauthOptions, LoginSeparator, LoginSeparatorText } from "./styles";
import { AuthInfoText } from "src/components/Layout/PublicPageLayout/AuthPageLayout/styles";
import StyledLink from "src/components/StyledLink";
import LoginOauthOption from "src/components/Buttons/OauthOptions";
import AuthActionButton from "src/components/Buttons/AuthActionButton";

const validationSchema = yup.object({
  username: yup
    .string()
    .email('Enter a valid email')
    .required('Email is required'),
  password: yup.string().required('Password is required'),
});

export default function Login(props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) {
  const { kcContext, i18n, doUseDefaultCss, Template, classes } = props;

  const { getClassName } = useGetClassName({
    doUseDefaultCss,
    classes
  });

  const { social, realm, url, usernameHidden, login, auth, registrationDisabled } = kcContext;

  const { msg, msgStr } = i18n;

  const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);

  const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>(e => {
    e.preventDefault();

    setIsLoginButtonDisabled(true);

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
      <div id="kc-form" className={clsx(realm.password && social.providers !== undefined && getClassName("kcContentWrapperClass"))}>
        <div
          id="kc-form-wrapper"
          className={clsx(
            realm.password &&
            social.providers && [getClassName("kcFormSocialAccountContentClass"), getClassName("kcFormSocialAccountClass")]
          )}
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
              <AuthActionButton loading={isLoginButtonDisabled} text="Log in" />
              <AuthInfoText variant="h4_web" hideOnMobile>
                By logging in, you agree to the{' '}
                <StyledLink to="https://carp.cachet.dk/privacy-policy-service/">
                  Cachet Privacy Statement
                </StyledLink>{' '}
                and <StyledLink to="https://carp.cachet.dk/privacy-policy-service/">Terms of Service</StyledLink>.
              </AuthInfoText>
            </form>
          )}
        </div>
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
      </div>
    </Template >
  );
}
