import StyledLink from "../../../StyledLink";
import { BannerActionText } from "../styles";

type Props = { registerUrl: string; msgStr: (str: string) => string };

const BannerRegister = ({ registerUrl, msgStr }: Props) => {
  return (
    <BannerActionText>
      {msgStr("newToCarp")}
      <StyledLink to={registerUrl} isBold>
        {msgStr("doRegister")}
      </StyledLink>
    </BannerActionText>
  );
};

export default BannerRegister;
