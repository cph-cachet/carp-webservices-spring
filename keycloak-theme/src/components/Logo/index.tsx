import logoFlatColored from '../../assets/images/logo-carp-flat-colored.png';
import logoFlatWhite from '../../assets/images/logo-carp-flat-white.png';
import logoFlat from '../../assets/images/logo-carp-flat.png';
import logoWhite from '../../assets/images/logo-carp-white.png';
import logoDefault from '../../assets/images/logo-carp.png';
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
        <DefaultLogo href="/" data-testid="link-white-logo">
          <img src={logoWhite} alt="White carp logo" />
        </DefaultLogo>
      );
    case 'flat':
      return (
        <FlatLogo href="/" data-testid="link-flat-logo">
          <img src={logoFlat} alt="carp logo" />
        </FlatLogo>
      );
    case 'flat-colored':
      return (
        <FlatColoredLogo href="/" data-testid="link-flat-colored-logo">
          <img src={logoFlatColored} alt="carp logo" />
        </FlatColoredLogo>
      );
    case 'flat-white':
      return (
        <FlatWhiteLogo href="/" data-testid="link-flat-colored-logo">
          <img src={logoFlatWhite} alt="carp logo" />
        </FlatWhiteLogo>
      );
    default:
      return (
        <DefaultLogo href="/" data-testid="link-default-logo">
          <img src={logoDefault} alt="carp logo" />
        </DefaultLogo>
      );
  }
};

export default Logo;
