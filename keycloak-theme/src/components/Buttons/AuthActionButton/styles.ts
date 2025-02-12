import { Button } from "@mui/material";
import { styled } from "../../../utils/theme";

const CarpButton = styled(Button)(({ theme }) => ({
  borderRadius: "24px",
  width: "100%",
  minHeight: 48,
  textTransform: "none",
  textAlign: "center",
  fontSize: theme.typography.h2_web.fontSize,
  lineHeight: theme.typography.h2_web.lineHeight,
  fontWeight: theme.typography.h2_web.fontWeight,
  marginBottom: 16,
}));

export default CarpButton;
