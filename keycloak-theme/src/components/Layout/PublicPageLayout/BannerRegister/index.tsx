import StyledLink from '../../../../components/StyledLink';
import { BannerActionText } from '../styles';

type Props = { registerUrl: string };

const BannerRegister = ({ registerUrl }: Props) => {
  return (
    <BannerActionText>
      New to CARP?
      <StyledLink to={registerUrl} isBold>
        Register
      </StyledLink>
    </BannerActionText>
  );
};

export default BannerRegister;
