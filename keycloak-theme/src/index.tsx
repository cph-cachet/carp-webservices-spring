import { lazy, StrictMode, Suspense } from 'react';
import { createRoot } from 'react-dom/client';
import { kcContext as kcAccountThemeContext } from './keycloak-theme/account/kcContext';
import { kcContext as kcLoginThemeContext } from './keycloak-theme/login/kcContext';

const KcLoginThemeApp = lazy(() => import('./keycloak-theme/login/KcApp'));
const KcAccountThemeApp = lazy(() => import('./keycloak-theme/account/KcApp'));
const App = lazy(() => import('./App'));

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <Suspense>
      {(() => {
        if (kcLoginThemeContext !== undefined) {
          return <KcLoginThemeApp kcContext={kcLoginThemeContext} />;
        }

        if (kcAccountThemeContext !== undefined) {
          return <KcAccountThemeApp kcContext={kcAccountThemeContext} />;
        }

        return <App />;
      })()}
    </Suspense>
  </StrictMode>
);
