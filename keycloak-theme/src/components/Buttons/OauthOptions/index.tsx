import {
  LoginOauthOptionButton,
  LoginOauthOptionText,
  StyledOption,
} from "./style";

type Props = {
  logoSrc: string;
  name: string;
};

const LoginOauthOption = ({ logoSrc, name }: Props) => {
  return (
    <StyledOption>
      <LoginOauthOptionButton>
        <img src={logoSrc} alt="login oauth option" />
      </LoginOauthOptionButton>
      <LoginOauthOptionText>{name}</LoginOauthOptionText>
    </StyledOption>
  );
};

export default LoginOauthOption;
