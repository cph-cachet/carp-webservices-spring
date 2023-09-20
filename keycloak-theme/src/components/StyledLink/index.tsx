import { ReactNode } from 'react';
import StyledStyledLink from './styles';

type Props = {
  children: ReactNode;
  to: string;
  isBold?: boolean;
};

const StyledLink = ({ children, to, isBold }: Props) => {
  return (
    <StyledStyledLink href={to} isBold={isBold}>
      {children}
    </StyledStyledLink>
  );
};

export default StyledLink;
