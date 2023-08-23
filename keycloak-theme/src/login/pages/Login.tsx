
import { type FormEventHandler } from "react";
import { useConstCallback } from "keycloakify/tools/useConstCallback";
import type { PageProps } from "keycloakify/login/pages/PageProps";
import { useGetClassName } from "keycloakify/login/lib/useGetClassName";
import type { KcContext } from "../kcContext";
import type { I18n } from "../i18n";
import { Typography } from "@mui/material";

import { SessionLoginParams } from '@cph-cachet/carp.httpclient-ts';
import { Checkbox, FormControlLabel, FormGroup } from '@mui/material';

import CarpInput from '../../components/CarpInput';
import PublicPageLayout from '../../components/Layout/PublicPageLayout';
import { useFormik } from 'formik';
import { useEffect, useState } from 'react';
import * as yup from 'yup';

import AppleLogo from '../../assets/images/logo-apple.png';
import GoogleLogo from '../../assets/images/logo-google.png';
import PasskeyLogo from '../../assets/images/logo-passkey.png';
import AuthActionButton from '../../components/Buttons/AuthActionButton';
import LoginOauthOption from '../../components/Buttons/OauthOptions';
import AuthPageLayout from '../../components/Layout/PublicPageLayout/AuthPageLayout';
import { AuthInfoText } from '../../components/Layout/PublicPageLayout/AuthPageLayout/styles';
import StyledLink from '../../components/StyledLink';

import {
    LoginAdditionalActions,
    LoginOauthOptions,
    LoginSeparator,
    LoginSeparatorText,
} from './styles';

const validationSchema = yup.object({
    email: yup
        .string()
        .email('Enter a valid email')
        .required('Email is required'),
    password: yup.string().required('Password is required'),
});
export default function Login(props: PageProps<Extract<KcContext, { pageId: "login.ftl" }>, I18n>) {
    const { kcContext, Template } = props;
    const { social, realm, url, login, auth } = kcContext;

    const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);

    const formik = useFormik({
        initialValues: {
            email: '',
            password: '',
        },
        validationSchema,
        onSubmit: () => { }
    });

    const [staySignedIn, setStaySignedIn] = useState(false);
    const toggleSignedIn = () => setStaySignedIn(!staySignedIn);

    const onSubmit = useConstCallback<FormEventHandler<HTMLFormElement>>(e => {
        e.preventDefault();

        setIsLoginButtonDisabled(true);

        const formElement = e.target as HTMLFormElement;

        //NOTE: Even if we login with email Keycloak expect username and password in
        //the POST request.
        formElement.querySelector("input[name='email']")?.setAttribute("name", "username");

        formElement.submit();
    });

    return (
        <PublicPageLayout>
            <AuthPageLayout title="Log in">
                <form onSubmit={onSubmit}>
                    <CarpInput
                        name="email"
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
                    <AuthActionButton loading={false} text="Log in" />
                    {/*
                    // <AuthActionButton loading={loginMutation.isLoading} text="Log in" />
                    */}
                    <AuthInfoText variant="h4_web" hideOnMobile>
                        By logging in, you agree to the{' '}
                        <StyledLink to="/forgot-password">
                            Cachet Privacy Statement
                        </StyledLink>{' '}
                        and <StyledLink to="/forgot-password">Terms of Service</StyledLink>.
                    </AuthInfoText>
                </form>
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
            </AuthPageLayout>
        </PublicPageLayout>
    )
}
