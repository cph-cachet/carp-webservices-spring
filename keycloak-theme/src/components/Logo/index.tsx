import logoFlatColored from '@Assets/images/logo-carp-flat-colored.png';
import logoFlatWhite from '@Assets/images/logo-carp-flat-white.png';
import logoFlat from '@Assets/images/logo-carp-flat.png';
import logoWhite from '@Assets/images/logo-carp-white.png';
import logoDefault from '@Assets/images/logo-carp.png';
import {
  DefaultLogo,
  FlatColoredLogo,
  FlatLogo,
  FlatWhiteLogo,
} from './styles';

interface Props {
  type?: 'white' | 'flat' | 'flat-colored' | 'flat-white';
}

const Logo = ({ type }: Props) => {
  switch (type) {
    case 'white':
      return (
        <DefaultLogo to="/" data-testid="link-white-logo">
          <img src={logoWhite} alt="White carp logo" />
        </DefaultLogo>
      );
    case 'flat':
      return (
        <FlatLogo to="/" data-testid="link-flat-logo">
          <img src={logoFlat} alt="carp logo" />
        </FlatLogo>
      );
    case 'flat-colored':
      return (
        <FlatColoredLogo to="/" data-testid="link-flat-colored-logo">
          <img src={logoFlatColored} alt="carp logo" />
        </FlatColoredLogo>
      );
    case 'flat-white':
      return (
        <FlatWhiteLogo to="/" data-testid="link-flat-colored-logo">
          <img src={logoFlatWhite} alt="carp logo" />
        </FlatWhiteLogo>
      );
    default:
      return (
        <DefaultLogo to="/" data-testid="link-default-logo">
          <img src={logoDefault} alt="carp logo" />
        </DefaultLogo>
      );
  }
};

export default Logo;
