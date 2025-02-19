import { createContext, ReactNode, useContext, useState } from "react";
import { SnackbarType } from "../components/Snackbar";

type ProviderProps = {
  children: ReactNode;
};

type SnackbarContextType = {
  snackbarState: SnackbarType;
  setSnackbarState: (type: SnackbarType) => void;
  setSnackbarSuccess: (message: string) => void;
  setSnackbarError: (message: string) => void;
};

export const SnackbarContext = createContext({} as SnackbarContextType);

export const useSnackbar = (): SnackbarContextType => {
  return useContext(SnackbarContext);
};

export const SnackbarProvider = ({ children }: ProviderProps) => {
  const [snackbarState, setSnackbarState] = useState<SnackbarType>({
    snackbarOpen: false,
    snackbarType: "error",
    snackbarMessage: "Unexpected error",
  });
  const setSnackbarSuccess = (message: string) => {
    setSnackbarState({
      snackbarOpen: true,
      snackbarType: "success",
      snackbarMessage: message,
    });
  };

  const setSnackbarError = (message: string) => {
    setSnackbarState({
      snackbarOpen: true,
      snackbarType: "error",
      snackbarMessage: message,
    });
  };

  return (
    <SnackbarContext.Provider
      value={{
        snackbarState,
        setSnackbarState,
        setSnackbarSuccess,
        setSnackbarError,
      }}
    >
      {children}
    </SnackbarContext.Provider>
  );
};
