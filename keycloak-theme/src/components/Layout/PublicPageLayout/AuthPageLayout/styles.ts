import { Typography } from "@mui/material";
import { styled } from "../../../../utils/theme";

export const AuthCenter = styled("div")({
  display: "flex",
  justifyContent: "center",
  width: "100%",
});

export const AuthContainer = styled("div")(({ theme }) => ({
  width: 550,
  marginTop: 82,
  [theme.breakpoints.down("sm")]: {
    width: "100%",
    padding: "0 8px",
    marginTop: 150,
  },
  [theme.breakpoints.down("xs")]: {
    marginTop: 24,
  },
}));

export const AuthTitle = styled(Typography)(({ theme }) => ({
  color: theme.palette.primary.main,
  textAlign: "center",
  marginBottom: 36,
  [theme.breakpoints.down("sm")]: {
    fontSize: theme.typography.h2.fontSize,
    marginBottom: 12,
  },
}));

export const AuthInfoText = styled(Typography, {
  shouldForwardProp: (prop) => prop !== "hideOnMobile",
})<{ hideOnMobile?: boolean }>(({ hideOnMobile, theme }) => ({
  margin: "8px 0 24px 0",
  [theme.breakpoints.down("sm")]: {
    display: hideOnMobile ? "none" : "block",
  },
}));
