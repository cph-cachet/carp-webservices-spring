import StyledLink from "../../StyledLink";
import { ReactNode } from "react";
import { BannerActionText, PublicPageBanner, StyledLogo } from "./styles";

type Props = {
  loginUrl: string;
  registrationUrl: string;
  children: ReactNode;
};

const PublicPageLayout = ({ children, registrationUrl, loginUrl }: Props) => {
  return (
    <>
      <PublicPageBanner>
        <StyledLogo type="flat-colored" />
          {
            // eslint-disable-next-line
            location.pathname.includes('registration') ? (
              <BannerActionText>
                Already have an account?{' '}
                <StyledLink to="/" isBold>
                  Login
                </StyledLink>
              </BannerActionText>
            ) : (
              <BannerActionText>
                New to CARP?{" "}
                <StyledLink to={registrationUrl} isBold>
                  Sign up
                </StyledLink>
              </BannerActionText>
            )
          }
      </PublicPageBanner>
      {children}
    </>
  );
};

export default PublicPageLayout;
