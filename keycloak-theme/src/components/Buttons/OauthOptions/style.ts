import { ButtonBase, Typography } from "@mui/material";
import { styled } from "../../../utils/theme";

export const StyledOption = styled("div")({
  display: "flex",
  alignItems: "center",
  flexDirection: "column",
});

export const LoginOauthOptionButton = styled(ButtonBase)(({ theme }) => ({
  border: `1px solid ${theme.palette.grey[500]}`,
  borderRadius: 16,
  padding: 18,
  "& > img": {
    height: 30,
  },
  [theme.breakpoints.down("sm")]: {
    padding: 12,
    borderRadius: 16,
    "& > img": {
      height: 20,
    },
  },
  [theme.breakpoints.down("xs")]: {
    padding: 12,
    borderRadius: 12,
    "& > img": {
      height: 16,
    },
  },
}));

export const LoginOauthOptionText = styled(Typography)(({ theme }) => ({
  color: theme.palette.grey[500],
  marginTop: 12,
  [theme.breakpoints.down("sm")]: {
    display: "none",
  },
}));
