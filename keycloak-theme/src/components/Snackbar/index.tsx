import Alert, { AlertColor } from "@mui/material/Alert";
import { SyntheticEvent } from "react";
import StyledSnackbar from "./styles";

export interface SnackbarType {
  snackbarOpen: boolean;
  snackbarType: AlertColor;
  snackbarMessage: string;
}

export interface SnackbarProp {
  setSnackbarState: (state: SnackbarType) => void;
}

const CustomizedSnackbar = ({
  snackbarOpen,
  snackbarType,
  snackbarMessage,
  setSnackbarState,
}: SnackbarType & SnackbarProp) => {
  const handleClose = (_event?: SyntheticEvent | Event, reason?: string) => {
    if (reason === "clickaway") {
      return;
    }
    setSnackbarState({
      snackbarOpen: false,
      snackbarType,
      snackbarMessage,
    });
  };

  return (
    <StyledSnackbar
      open={snackbarOpen}
      onClose={handleClose}
      autoHideDuration={3000}
      anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
    >
      <Alert
        elevation={6}
        onClose={handleClose}
        variant="filled"
        severity={snackbarType}
      >
        {snackbarMessage}
      </Alert>
    </StyledSnackbar>
  );
};

export default CustomizedSnackbar;
