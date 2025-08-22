import { Dialog, DialogTitle, Modal } from "@mui/material";
import { Scanner } from "@yudiel/react-qr-scanner";

const QRScannerModal = ({ open }: { open: boolean }) => {
  return (
    <Dialog open={open}>
      <DialogTitle>QR Code Scanner</DialogTitle>
      <Scanner onScan={(data) => window.location.replace(data[0].rawValue)}></Scanner>
    </Dialog>
  );
};

export default QRScannerModal;
