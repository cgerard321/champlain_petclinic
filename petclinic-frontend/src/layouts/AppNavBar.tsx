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
import { useEffect, useState, useCallback } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Navbar, Nav, NavDropdown, Container } from 'react-bootstrap';
import { FaShoppingCart } from 'react-icons/fa'; // Importing the shopping cart icon
import './AppNavBar.css';
import { isAxiosError } from 'axios';

// localStorage-driven cart badge (no API calls in navbar)
import {
  CART_CHANGED,
  getCartIdFromLS,
  getCartCountFromLS,
} from '@/features/carts/api/cartEvent';

export function NavBar(): JSX.Element {
  const { user } = useUser();
  const navigate = useNavigate();
  const isAdmin = IsAdmin();
  const isInventoryManager = IsInventoryManager();
  const isReceptionist = IsReceptionist();
  const isVet = IsVet();
  const isOwner = IsOwner();
  const [navbarOpen, setNavbarOpen] = useState(false);
  const [cartId, setCartId] = useState<string | null>(null);
  const [cartItemCount, setCartItemCount] = useState<number>(0); // State for cart item count

  const logoutUser = (): void => {
    // Client-side logout only; keep API calls out of navbar
    try {
      localStorage.removeItem('user');
      localStorage.removeItem('cart:id');
      localStorage.removeItem('cart:count');
    } catch {
      /* ignore */
    }
    navigate(AppRoutePaths.Login);
    window.location.reload();
  };

  const toggleNavbar = (): void => {
    setNavbarOpen(prevNavbarOpen => !prevNavbarOpen);
  };

  // LocalStorage-driven sync (no network here)
  const refreshFromLocalStorage = useCallback(() => {
    setCartId(getCartIdFromLS());
    setCartItemCount(getCartCountFromLS());
  }, []);

  useEffect(() => {
    // initialize from LS
    refreshFromLocalStorage();
    // same-tab updates
    const onCartChanged = (): void => refreshFromLocalStorage();
    // cross-tab updates
    const onStorage = (e: StorageEvent): void => {
      if (
        e.key === 'cart:changed' ||
        e.key === 'cart:count' ||
        e.key === 'cart:id'
      ) {
        refreshFromLocalStorage();
      }
    };

    window.addEventListener(
      CART_CHANGED as unknown as string,
      onCartChanged as EventListener
    );
    window.addEventListener('storage', onStorage);
    return () => {
      window.removeEventListener(
        CART_CHANGED as unknown as string,
        onCartChanged as EventListener
      );
      window.removeEventListener('storage', onStorage);
    };
  }, [
    refreshFromLocalStorage,
    user.userId,
    isInventoryManager,
    isReceptionist,
    isVet,
  ]);

  return (
    <Navbar bg="light" expand="lg" className="navbar">
      <Container>
        <Navbar.Brand as={Link} to={AppRoutePaths.Home}>
          PetClinic
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" onClick={toggleNavbar}>
          <span className="navbar-toggler-icon"></span>
        </Navbar.Toggle>
        <Navbar.Collapse
          id="basic-navbar-nav"
          className={navbarOpen ? 'show' : ''}
        >
          <Nav className="me-auto">
            <Nav.Link as={Link} to={AppRoutePaths.Home}>
              Home
            </Nav.Link>
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
                {!isAdmin && !isInventoryManager && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerVisits}>
                    Visits
                  </Nav.Link>
                )}
                {!isInventoryManager && !isReceptionist && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerEmergency}>
                    Emergency
                  </Nav.Link>
                )}
                {isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.AdminBills}>
                    Bills
                  </Nav.Link>
                )}
                {(isAdmin || isVet) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Visits}>
                    Visits
                  </Nav.Link>
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
                {
                  <Nav.Link as={Link} to={AppRoutePaths.Products}>
                    Shop
                  </Nav.Link>
                }

                {isAdmin && (
                  <Nav.Link as={Link} to={AppRoutePaths.Carts}>
                    Carts
                  </Nav.Link>
                )}

                {cartId && isOwner && (
                  <Nav.Link
                    as={Link}
                    to={AppRoutePaths.UserCart.replace(':cartId', cartId)}
                    className="cart-link"
                  >
                    <FaShoppingCart aria-label="Shopping Cart" />
                    {cartItemCount > 0 && (
                      <span
                        className="cart-badge"
                        aria-label={`Cart has ${cartItemCount} items`}
                      >
                        {cartItemCount}
                      </span>
                    )}
                  </Nav.Link>
                )}
              </>
            )}
          </Nav>
          <Nav className="ms-auto">
            {user.userId ? (
              <NavDropdown title={`${user.username}`} id="user-dropdown">
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
