import StyledLink from '../../../../components/StyledLink';
import { BannerActionText } from '../styles';

type Props = { loginUrl: string };

const BannerLogin = ({ loginUrl }: Props) => {
  return (
    <BannerActionText>
      Already have an account?
      <StyledLink to={loginUrl} isBold>
        Login
      </StyledLink>
    </BannerActionText>
  );
};

export default BannerLogin;
