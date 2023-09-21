import { ReactNode } from 'react';
import { PublicPageBanner, StyledLogo } from './styles';

type Props = {
  infoNode: ReactNode;
  children: ReactNode;
};

const PublicPageLayout = ({ children, infoNode }: Props) => {
  return (
    <>
      <PublicPageBanner>
        <StyledLogo type="flat-colored" />
        {infoNode}
      </PublicPageBanner>
      {children}
    </>
  );
};

export default PublicPageLayout;
