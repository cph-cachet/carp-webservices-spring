import StyledLink from '@Components/StyledLink';
import { ReactNode } from 'react';
import { useLocation } from 'react-router-dom';
import { BannerActionText, PublicPageBanner, StyledLogo } from './styles';

type Props = {
  children: ReactNode;
};

const PublicPageLayout = ({ children }: Props) => {
  const location = useLocation();

  return (
    <>
      <PublicPageBanner>
        <StyledLogo type="flat-colored" />
        {location.pathname.includes('/register') ? (
          <BannerActionText>
            Already have an account?{' '}
            <StyledLink to="/" isBold>
              Login
            </StyledLink>
          </BannerActionText>
        ) : (
          <BannerActionText>
            New to CARP?{' '}
            <StyledLink to="/register" isBold>
              Sign up
            </StyledLink>
          </BannerActionText>
        )}
      </PublicPageBanner>
      {children}
    </>
  );
};

export default PublicPageLayout;
