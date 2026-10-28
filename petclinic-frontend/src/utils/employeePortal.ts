const rawEmployeePortalUrl: string =
  import.meta.env.VITE_EMPLOYEE_PORTAL_URL ?? '';

const EMPLOYEE_PORTAL_URL: string = rawEmployeePortalUrl.replace(/\/+$/, '');

export function redirectToEmployeePortal(path: '/home' | '/login'): void {
  if (!EMPLOYEE_PORTAL_URL) {
    // A relative redirect would cause a loop.
    console.error(
      '[employeePortal] VITE_EMPLOYEE_PORTAL_URL is not set; cannot redirect.'
    );
    return;
  }

  window.location.assign(`${EMPLOYEE_PORTAL_URL}${path}`);
}
