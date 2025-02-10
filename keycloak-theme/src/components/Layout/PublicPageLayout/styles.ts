import { Typography } from "@mui/material";
import Logo from "../../Logo";
import { styled } from "../../../utils/theme";

export const PublicPageBanner = styled("div")(({ theme }) => ({
  display: "flex",
  justifyContent: "space-between",
  padding: "2rem 4rem 1em",
  alignItems: "center",
  [theme.breakpoints.down("sm")]: {
    padding: "0 1.5rem 0",
    "& *> img": {
      height: 18,
    },
  },
  borderBottom: `1px solid ${theme.palette.grey[200]}`,
}));

export const StyledLogo = styled(Logo)({
  height: 28,
});

export const BannerActionText = styled(Typography)(({ theme }) => ({
  display: "flex",
  alignItems: "center",
  textAlign: "right",
  gap: 8,
  "& > a": {
    visibility: "visible",
    fontSize: theme.typography.h4.fontSize,
  },
  [theme.breakpoints.down("sm")]: {
    fontSize: 0,
  },
}));
