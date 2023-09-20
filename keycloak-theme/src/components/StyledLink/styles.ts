import { styled } from "../../utils/theme";

const StyledStyledLink = styled("a", {
  shouldForwardProp: (prop) => prop !== "isBold",
})<{ isBold?: boolean }>(({ isBold, theme }) => ({
  textDecoration: "none",
  fontSize: "inherit",
  color: theme.palette.primary.main,
  fontWeight: isBold ? 700 : "inherit",
}));

export default StyledStyledLink;
