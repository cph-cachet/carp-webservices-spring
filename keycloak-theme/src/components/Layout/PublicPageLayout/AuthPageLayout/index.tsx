import { ReactNode } from 'react';
import { AuthCenter, AuthContainer, AuthTitle } from './styles';

type Props = {
  children: ReactNode;
  title: string;
};

const AuthPageLayout = ({ children, title }: Props) => {
  return (
    <AuthCenter>
      <AuthContainer>
        <AuthTitle variant="h1_web">{title}</AuthTitle>
        {children}
      </AuthContainer>
    </AuthCenter>
  );
};

export default AuthPageLayout;
