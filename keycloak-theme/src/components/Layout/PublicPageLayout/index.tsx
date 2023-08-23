import StyledLink from "../../StyledLink";
import { ReactNode } from "react";
import { BannerActionText, PublicPageBanner, StyledLogo } from "./styles";

type Props = {
  children: ReactNode;
};

const PublicPageLayout = ({ children }: Props) => {
  return (
    <>
      <PublicPageBanner>
        <StyledLogo type="flat-colored" />
        {/* 
          TODO: fix

        {location.pathname.includes('/register') ? (
          <BannerActionText>
            Already have an account?{' '}
            <StyledLink to="/" isBold>
              Login
            </StyledLink>
          </BannerActionText>
        ) : (
        */}
        <BannerActionText>
          New to CARP?{" "}
          <StyledLink to="/register" isBold>
            Sign up
          </StyledLink>
        </BannerActionText>
        {/*         )} */}
      </PublicPageBanner>
      {children}
    </>
  );
};

export default PublicPageLayout;
