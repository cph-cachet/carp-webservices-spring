import { DialogTitle, styled } from "@mui/material";

export const Title = styled(DialogTitle)(({ theme }) => ({
  color: theme.palette.primary.main,
  [theme.breakpoints.down("sm")]: {
    fontSize: theme.typography.h2.fontSize,
  },
}));
