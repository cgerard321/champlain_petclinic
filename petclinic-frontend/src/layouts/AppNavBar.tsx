import { Link, useNavigate } from 'react-router-dom';
import {
  IsAdmin,
  IsInventoryManager,
  IsOwner,
  IsReceptionist,
  IsVet,
  useUser,
} from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useState, useCallback } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Navbar, Nav, NavDropdown, Container } from 'react-bootstrap';
import { FaShoppingCart } from 'react-icons/fa'; // Importing the shopping cart icon
import './AppNavBar.css';

// Uses centralized cart context
import { useCart } from '@/context/CartContext';
import { clinic } from '@/shared/content';

export function NavBar(): JSX.Element {
  const { user } = useUser();
  const { cartCount, refreshFromAPI } = useCart();
  const navigate = useNavigate();
  const isAdmin = IsAdmin();
  const isInventoryManager = IsInventoryManager();
  const isReceptionist = IsReceptionist();
  const isVet = IsVet();
  const isOwner = IsOwner();
  const [navbarOpen, setNavbarOpen] = useState(false);
  const [cartLoading, setCartLoading] = useState(false);

  const hasStaffVisits = isAdmin || isVet || isReceptionist;
  const showVetVisitsDropdown = isVet;

  const logoutUser = (): void => {
    // Client-side logout only. Keep API calls out of navbar
    try {
      localStorage.removeItem('user');
      localStorage.removeItem('cart:id');
      localStorage.removeItem('cart:count');
    } catch {
      // ignore
    }
    navigate(AppRoutePaths.Login);
    window.location.reload();
  };

  const toggleNavbar = (): void => {
    setNavbarOpen(prevNavbarOpen => !prevNavbarOpen);
  };

  const goToCart = useCallback(async () => {
    if (!user?.userId) {
      // not logged in? Then send to login.
      navigate(AppRoutePaths.Login);
      return;
    }
    if (cartLoading) return; // prevent multiple clicks
    setCartLoading(true);
    try {
      // Fetch the latest cart ID and count before redirecting
      const { cartId: resolvedId } = await refreshFromAPI();
      if (resolvedId) {
        navigate(AppRoutePaths.UserCart.replace(':cartId', resolvedId));
      } else {
        // if no active cart, redirect to shop
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
              Home
            </Nav.Link>
            {
              // check if user is logged in
            }
            {user.userId && (
              <>
                {(isAdmin || isVet) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Vet}>
                    Veterinarians
                  </Nav.Link>
                )}
                {(isAdmin || isVet || isReceptionist) && (
                  <NavDropdown title="Customers" id="owners-dropdown">
                    {(isAdmin || isVet) && (
                      <NavDropdown.Item
                        as={Link}
                        to={AppRoutePaths.AllCustomers}
                      >
                        Customers List
                      </NavDropdown.Item>
                    )}
                    {(isAdmin || isReceptionist) && (
                      <NavDropdown.Item
                        as={Link}
                        to={AppRoutePaths.AddingCustomer}
                      >
                        Add Customer
                      </NavDropdown.Item>
                    )}
                  </NavDropdown>
                )}
                {isAdmin && (
                  <NavDropdown title="Users" id="users-dropdown">
                    <NavDropdown.Item as={Link} to={AppRoutePaths.AllUsers}>
                      Users List
                    </NavDropdown.Item>
                    <NavDropdown.Item as={Link} to={AppRoutePaths.AllRoles}>
                      Roles List
                    </NavDropdown.Item>
                  </NavDropdown>
                )}
                {!isAdmin &&
                  !isInventoryManager &&
                  !isVet &&
                  !isReceptionist && (
                    <Nav.Link as={Link} to={AppRoutePaths.CustomerBills}>
                      Bills
                    </Nav.Link>
                  )}
                {!hasStaffVisits && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerVisits}>
                    Visits
                  </Nav.Link>
                )}
                {isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.AdminBills}>
                    Bills
                  </Nav.Link>
                )}
                {hasStaffVisits && !IsAdmin() && !IsVet() && (
                  <Nav.Link as={Link} to={AppRoutePaths.Visits}>
                    Visits
                  </Nav.Link>
                )}
                {(IsAdmin() || IsVet() || IsOwner()) && (
                  <NavDropdown title="Visits" id="visits-dropdown">
                    <NavDropdown.Item as={Link} to={AppRoutePaths.Visits}>
                      List View
                    </NavDropdown.Item>

                    <NavDropdown.Item
                      as={Link}
                      to={AppRoutePaths.VisitsCalendar}
                    >
                      Calendar View
                    </NavDropdown.Item>

                    {showVetVisitsDropdown && IsVet() && (
                      <>
                        <NavDropdown.Divider />
                        <NavDropdown.Item
                          as={Link}
                          to={AppRoutePaths.CustomerVisits}
                        >
                          My Schedule
                        </NavDropdown.Item>
                      </>
                    )}
                  </NavDropdown>
                )}
                {(isInventoryManager || isAdmin) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Inventories}>
                    Inventories
                  </Nav.Link>
                )}
                {isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.Promos}>
                    Promos
                  </Nav.Link>
                )}
                {!isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerPromos}>
                    Promos
                  </Nav.Link>
                )}
                <Nav.Link as={Link} to={AppRoutePaths.Products}>
                  Shop
                </Nav.Link>
                {isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.Carts}>
                    Carts
                  </Nav.Link>
                )}
                {isOwner && (
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
          <Nav className="ms-auto">
            {user.userId ? (
              <NavDropdown title={user.username} id="user-dropdown">
                {isOwner && (
                  <NavDropdown.Item
                    as={Link}
                    to={AppRoutePaths.CustomerProfile}
                  >
                    Profile
                  </NavDropdown.Item>
                )}
                {isOwner && (
                  <NavDropdown.Item
                    as={Link}
                    to={AppRoutePaths.CustomerProfileEdit}
                  >
                    Edit Profile
                  </NavDropdown.Item>
                )}
                {isAdmin && (
                  <NavDropdown.Item as={Link} to={AppRoutePaths.Home}>
                    Admin Panel
                  </NavDropdown.Item>
                )}
                {isReceptionist && (
                  <NavDropdown.Item as={Link} to={AppRoutePaths.AddingCustomer}>
                    Receptionist Panel
                  </NavDropdown.Item>
                )}
                <NavDropdown.Item
                  onClick={logoutUser}
                  style={{ cursor: 'pointer' }}
                >
                  Logout
                </NavDropdown.Item>
              </NavDropdown>
            ) : (
              <>
                <Nav.Link as={Link} to={AppRoutePaths.SignUp}>
                  Signup
                </Nav.Link>
                <Nav.Link as={Link} to={AppRoutePaths.Login}>
                  Login
                </Nav.Link>
              </>
            )}
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}
