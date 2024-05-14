import StyledLink from '../../../../components/StyledLink';
import { BannerActionText } from '../styles';

type Props = { loginUrl: string, msgStr: (key: string) => string};

const BannerLogin = ({ loginUrl, msgStr }: Props) => {
  return (
    <BannerActionText>
      {msgStr("loginBannerText")}
      <StyledLink to={loginUrl} isBold>
        {msgStr("doLogIn")}
      </StyledLink>
    </BannerActionText>
  );
};

export default BannerLogin;
