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
        <div style={{display: 'flex', gap: 8 }}>
        {infoNode}
        </div>
      </PublicPageBanner>
      {children}
    </>
  );
};

export default PublicPageLayout;
