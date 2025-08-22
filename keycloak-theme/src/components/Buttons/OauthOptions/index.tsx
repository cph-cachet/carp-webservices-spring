import React from "react";
import {
  LoginOauthOptionButton,
  LoginOauthOptionText,
  StyledOption,
} from "./style";

type Props = {
  logoSrc?: string | null;
  logoComponent?: React.ComponentType;
  name: string;
  onClick?: () => void;
};

const LoginOauthOption = ({ logoSrc, logoComponent, name, onClick }: Props) => {
  return (
    <StyledOption onClick={onClick}>
      <LoginOauthOptionButton>
        {logoComponent
          ? React.createElement(logoComponent)
          : logoSrc && <img src={logoSrc} alt="login oauth option" />}
      </LoginOauthOptionButton>
      <LoginOauthOptionText>{name}</LoginOauthOptionText>
    </StyledOption>
  );
};

export default LoginOauthOption;
