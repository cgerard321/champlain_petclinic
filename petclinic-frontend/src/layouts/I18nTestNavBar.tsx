import { Link, useNavigate } from 'react-router-dom';
import {
  IsAdmin,
  IsInventoryManager,
  IsCustomer,
  IsReceptionist,
  IsVet,
  useUser,
} from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useCallback, useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';
import { FaShoppingCart } from 'react-icons/fa';
import './AppNavBar.css';

import { useCart } from '@/context/CartContext';
import { clinic } from '@/shared/content';
import { useTranslation } from 'react-i18next';
import { LanguageSwitcher } from '@/shared/components/language/LanguageSwitcher';

export function I18nTestNavBar(): JSX.Element {
  const { t } = useTranslation('home');
  const { user, logout } = useUser();
  const { cartCount, refreshFromAPI } = useCart();
  const navigate = useNavigate();
  const isAdmin = IsAdmin();
  const isInventoryManager = IsInventoryManager();
  const isReceptionist = IsReceptionist();
  const isVet = IsVet();
  const isCustomer = IsCustomer();
  const [navbarOpen, setNavbarOpen] = useState(false);
  const [cartLoading, setCartLoading] = useState(false);

  const hasStaffVisits = isAdmin || isVet || isReceptionist;

  const logoutUser = async (): Promise<void> => {
    await logout();
    navigate(AppRoutePaths.Login);
  };

  const toggleNavbar = (): void => {
    setNavbarOpen(prevNavbarOpen => !prevNavbarOpen);
  };

  const goToCart = useCallback(async () => {
    if (!user?.userId) {
      navigate(AppRoutePaths.Login);
      return;
    }
    if (cartLoading) return;
    setCartLoading(true);
    try {
      const { cartId: resolvedId } = await refreshFromAPI();
      if (resolvedId) {
        navigate(AppRoutePaths.UserCart.replace(':cartId', resolvedId));
      } else {
        navigate(AppRoutePaths.Products);
      }
    } catch (e) {
      console.error('Could not go to cart: ' + e);
      navigate(AppRoutePaths.Products);
    } finally {
      setCartLoading(false);
    }
  }, [user?.userId, navigate, cartLoading, refreshFromAPI]);

  return (
    <Navbar bg="light" expand="lg" className="navbar">
      <Container>
        <Navbar.Brand as={Link} to={AppRoutePaths.Home}>
          {clinic.name}
        </Navbar.Brand>
        <Navbar.Toggle
          aria-controls="basic-navbar-nav"
          onClick={toggleNavbar}
        />

        <Navbar.Collapse
          id="basic-navbar-nav"
          className={navbarOpen ? 'show' : ''}
        >
          <Nav className="me-auto">
            <Nav.Link as={Link} to={AppRoutePaths.Home}>
              {t('nav.home')}
            </Nav.Link>

            {user?.userId && (
              <>
                {(isAdmin || isVet) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Vet}>
                    {t('nav.vets')}
                  </Nav.Link>
                )}
                {(isAdmin || isVet || isReceptionist) && (
                  <NavDropdown title={t('nav.customers')} id="owners-dropdown">
                    {(isAdmin || isVet) && (
                      <NavDropdown.Item
                        as={Link}
                        to={AppRoutePaths.AllCustomers}
                      >
                        {t('nav.customersList')}
                      </NavDropdown.Item>
                    )}
                    {(isAdmin || isReceptionist) && (
                      <NavDropdown.Item
                        as={Link}
                        to={AppRoutePaths.AddingCustomer}
                      >
                        {t('nav.addCustomer')}
                      </NavDropdown.Item>
                    )}
                  </NavDropdown>
                )}
                {isAdmin && (
                  <NavDropdown title={t('nav.users')} id="users-dropdown">
                    <NavDropdown.Item as={Link} to={AppRoutePaths.AllUsers}>
                      {t('nav.usersList')}
                    </NavDropdown.Item>
                    <NavDropdown.Item as={Link} to={AppRoutePaths.AllRoles}>
                      {t('nav.rolesList')}
                    </NavDropdown.Item>
                  </NavDropdown>
                )}
                {!isAdmin &&
                  !isInventoryManager &&
                  !isVet &&
                  !isReceptionist && (
                    <Nav.Link as={Link} to={AppRoutePaths.CustomerBills}>
                      {t('nav.bills')}
                    </Nav.Link>
                  )}
                {isCustomer && !hasStaffVisits && (
                  <NavDropdown
                    title={t('nav.visits')}
                    id="owner-visits-dropdown"
                  >
                    <NavDropdown.Item
                      as={Link}
                      to={AppRoutePaths.CustomerVisits}
                    >
                      {t('nav.listView')}
                    </NavDropdown.Item>
                    <NavDropdown.Item
                      as={Link}
                      to={AppRoutePaths.CustomerVisitsCalendar}
                    >
                      {t('nav.calendarView')}
                    </NavDropdown.Item>
                  </NavDropdown>
                )}
                {isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.AdminBills}>
                    {t('nav.bills')}
                  </Nav.Link>
                )}
                {(isAdmin || isReceptionist) && (
                  <NavDropdown
                    title={t('nav.visits')}
                    id="staff-visits-dropdown"
                  >
                    <NavDropdown.Item as={Link} to={AppRoutePaths.Visits}>
                      {t('nav.listView')}
                    </NavDropdown.Item>
                    <NavDropdown.Item
                      as={Link}
                      to={AppRoutePaths.VisitsCalendar}
                    >
                      {t('nav.calendarView')}
                    </NavDropdown.Item>
                  </NavDropdown>
                )}
                {isVet && (
                  <NavDropdown title={t('nav.visits')} id="vet-visits-dropdown">
                    <NavDropdown.Item as={Link} to={AppRoutePaths.Visits}>
                      {t('nav.listView')}
                    </NavDropdown.Item>
                    <NavDropdown.Item
                      as={Link}
                      to={AppRoutePaths.VisitsCalendar}
                    >
                      {t('nav.calendarView')}
                    </NavDropdown.Item>
                    <NavDropdown.Divider />
                    <NavDropdown.Item
                      as={Link}
                      to={AppRoutePaths.CustomerVisits}
                    >
                      {t('nav.mySchedule')}
                    </NavDropdown.Item>
                  </NavDropdown>
                )}
                {(isInventoryManager || isAdmin) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Inventories}>
                    {t('nav.inventories')}
                  </Nav.Link>
                )}
                {isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.Promos}>
                    {t('nav.promos')}
                  </Nav.Link>
                )}
                {!isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerPromos}>
                    {t('nav.promos')}
                  </Nav.Link>
                )}
                <Nav.Link as={Link} to={AppRoutePaths.Products}>
                  {t('nav.shop')}
                </Nav.Link>
                {isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.Carts}>
                    {t('nav.carts')}
                  </Nav.Link>
                )}
                {isCustomer && (
                  <Nav.Link
                    href="#"
                    onClick={e => {
                      e.preventDefault();
                      void goToCart();
                    }}
                    aria-busy={cartLoading}
                    className={`cart-link${cartCount === 0 ? ' cart-empty' : ''}`}
                    title={cartLoading ? 'Loading cart...' : 'View Cart'}
                  >
                    <FaShoppingCart aria-label="Shopping Cart" />
                    {cartCount > 0 && (
                      <span
                        className="cart-badge"
                        aria-label={`Cart has ${cartCount} items`}
                      >
                        {cartCount}
                      </span>
                    )}
                  </Nav.Link>
                )}
              </>
            )}
          </Nav>

          <Nav className="ms-auto align-items-center gap-2">
            <div className="me-2">
              <LanguageSwitcher />
            </div>

            {user?.userId ? (
              <NavDropdown title={user.username} id="user-dropdown">
                {isCustomer && (
                  <NavDropdown.Item
                    as={Link}
                    to={AppRoutePaths.CustomerProfile}
                  >
                    {t('nav.profile')}
                  </NavDropdown.Item>
                )}
                {isCustomer && (
                  <NavDropdown.Item
                    as={Link}
                    to={AppRoutePaths.CustomerProfileEdit}
                  >
                    {t('nav.editProfile')}
                  </NavDropdown.Item>
                )}
                <NavDropdown.Item
                  onClick={() => void logoutUser()}
                  style={{ cursor: 'pointer' }}
                >
                  {t('nav.logout')}
                </NavDropdown.Item>
              </NavDropdown>
            ) : (
              <>
                <Nav.Link as={Link} to={AppRoutePaths.SignUp}>
                  {t('nav.signup')}
                </Nav.Link>
                <Nav.Link as={Link} to={AppRoutePaths.Login}>
                  {t('nav.login')}
                </Nav.Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}
