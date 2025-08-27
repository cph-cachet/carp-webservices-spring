import { Dialog, DialogActions } from "@mui/material";
import { Scanner } from "@yudiel/react-qr-scanner";
import CarpButton from "../Buttons/AuthActionButton/styles";
import { Title } from "./styles";

const checkURL = (url: string) => {
  try {
    const parsedURL = new URL(url);
    if (parsedURL.protocol !== "https:") return false;
    return true;
  } catch {
    return false;
  }
};

const QRScannerModal = ({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) => {
  return (
    <Dialog open={open} fullWidth>
      <Title variant="h2">QR Code Scanner</Title>
      <Scanner
        sound={false}
        onScan={(data) => {
          const url = data[0]?.rawValue;
          if (url && checkURL(url)) {
            window.location.replace(url);
          } else {
            alert("Invalid URL scanned");
          }
        }}
        styles={{
          container: {
            width: "90%",
            height: "100%",
            margin: 8,
            borderRadius: 16,
            border: "1px solid #ccc",
            alignSelf: "center",
          },
        }}
      ></Scanner>
      <DialogActions sx={{ justifyContent: "center", paddingTop: 2 }}>
        <CarpButton
          variant="contained"
          sx={{ width: "50%", height: "40px" }}
          onClick={onClose}
        >
          Close
        </CarpButton>
      </DialogActions>
    </Dialog>
  );
};

export default QRScannerModal;
