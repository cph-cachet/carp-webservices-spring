import {
  CssBaseline,
  StyledEngineProvider,
  ThemeProvider,
} from "@mui/material";
import { lazy, Suspense } from "react";
import { createRoot } from "react-dom/client";
import { kcContext as kcAccountThemeContext } from "./account/kcContext";
import { kcContext as kcLoginThemeContext } from "./login/kcContext";
import { themeInstance } from "./utils/theme";

const KcLoginThemeApp = lazy(() => import("./login/KcApp"));
const KcAccountThemeApp = lazy(() => import("./account/KcApp"));
const App = lazy(() => import("./App"));

const container = document.getElementById("root");
const root = createRoot(container);

root.render(
  <Suspense>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={themeInstance}>
        <CssBaseline />
        {(() => {
          if (kcLoginThemeContext !== undefined) {
            return <KcLoginThemeApp kcContext={kcLoginThemeContext} />;
          }

          if (kcAccountThemeContext !== undefined) {
            return <KcAccountThemeApp kcContext={kcAccountThemeContext} />;
          }

          return <App />;
        })()}
      </ThemeProvider>
    </StyledEngineProvider>
  </Suspense>,
);
