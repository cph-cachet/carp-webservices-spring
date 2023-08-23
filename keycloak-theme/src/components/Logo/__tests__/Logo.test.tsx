import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import Logo from '@Components/Logo';

describe('Unit: <Logo>', () => {
  test(' renders white version', () => {
    const { getByTestId } = render(<Logo type="white" />, {
      wrapper: MemoryRouter,
    });

    const navLink = getByTestId('link-white-logo');
    expect(navLink).toBeTruthy();
    expect(navLink.getAttribute('href')).toBe('/');
  });

  test(' renders flat version', () => {
    const { getByTestId } = render(<Logo type="flat" />, {
      wrapper: MemoryRouter,
    });

    const navLink = getByTestId('link-flat-logo');
    expect(navLink).toBeTruthy();
    expect(navLink.getAttribute('href')).toBe('/');
  });

  test(' renders default version', () => {
    const { getByTestId } = render(<Logo />, { wrapper: MemoryRouter });

    const navLink = getByTestId('link-default-logo');
    expect(navLink).toBeTruthy();
    expect(navLink.getAttribute('href')).toBe('/');
  });
});
