import { Link, useNavigate } from 'react-router-dom';
import {
  IsAdmin,
  IsInventoryManager,
  IsOwner,
  IsReceptionist,
  IsVet,
  useUser,
} from '@/context/UserContext';
import { fetchCartIdByCustomerId } from '../features/carts/api/getCart';
import axiosInstance from '@/shared/api/axiosInstance';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useEffect, useState, useCallback } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Navbar, Nav, NavDropdown, Container } from 'react-bootstrap';
import { FaShoppingCart } from 'react-icons/fa'; // Importing the shopping cart icon
import './AppNavBar.css';

//  listen for cart changes broadcast by the app
import { CART_CHANGED } from '../features/carts/api/cartEvent';

interface ProductAPIResponse {
  productId: number;
  productName: string;
  productDescription: string;
  productSalePrice: number;
  averageRating: number;
  quantityInCart: number;
  productQuantity: number;
}

export function NavBar(): JSX.Element {
  const { user } = useUser();
  const navigate = useNavigate();
  const isInventoryManager = IsInventoryManager();
  const isVet = IsVet();
  const isReceptionist = IsReceptionist();
  const [navbarOpen, setNavbarOpen] = useState(false);
  const [cartId, setCartId] = useState<string | null>(null);
  const [cartItemCount, setCartItemCount] = useState<number>(0); // State for cart item count

  const logoutUser = (): void => {
    axiosInstance
      .post('/users/logout', {}, { useV2: false })
      .then(() => {
        navigate(AppRoutePaths.Login);
        localStorage.removeItem('user');
        // Reload the login page to remove all previous user data
        window.location.reload();
      })
      .catch(error => {
        console.error('Logout failed:', error);
      });
  };

  const toggleNavbar = (): void => {
    setNavbarOpen(prevNavbarOpen => !prevNavbarOpen);
  };

  /* 
    Note: Fetching the cart ID within the NavBar is not optimal and should be refactored 
    in future sprints for better performance and separation of concerns.
  */
  useEffect(() => {
    const fetchCartId = async (): Promise<void> => {
      if (user.userId && !isInventoryManager && !isVet && !isReceptionist) {
        try {
          const id = await fetchCartIdByCustomerId(user.userId);
          setCartId(id);
        } catch (error) {
          console.error('Error fetching cart ID:', error);
        }
      }
    };

    fetchCartId();
  }, [user.userId, isInventoryManager, isVet, isReceptionist]);

  // NEW: uses lightweight /count endpoint when available, falls back to full cart;
  // also listens for "cart:changed" (same tab) and "storage" (cross-tab) to refresh automatically.
  const fetchCartItemCount = useCallback(async (): Promise<void> => {
    if (!cartId) {
      setCartItemCount(0);
      return;
    }

    // Try the lightweight /count endpoint first
    try {
      const { data } = await axiosInstance.get<{ itemCount?: number }>(
        `/carts/${cartId}/count`,
        { useV2: true }
      );
      if (typeof data?.itemCount === 'number') {
        setCartItemCount(data.itemCount);
        return;
      }
    } catch {
      // fall through to the full cart request on error
    }

    // Fallback: fetch the entire cart and sum quantities
    try {
      const { data } = await axiosInstance.get<{
        products?: ProductAPIResponse[];
      }>(`/carts/${cartId}`, { useV2: false });

      if (Array.isArray(data.products)) {
        const totalCount = data.products.reduce(
          (acc: number, product: ProductAPIResponse) =>
            acc + (product.quantityInCart || 0),
          0
        );
        setCartItemCount(totalCount);
      } else {
        setCartItemCount(0);
      }
    } catch (error) {
      console.error('Error fetching cart item count:', error);
      setCartItemCount(0);
    }
  }, [cartId]);

  useEffect(() => {
    if (!cartId) {
      setCartItemCount(0);
      return;
    }

    // initial fetch
    void fetchCartItemCount();

    // refresh on same-tab cart changes
    const onCartChanged = (): void => {
      // fire-and-forget; ignore returned Promise
      void fetchCartItemCount();
    };

    // refresh on cross-tab cart changes
    const onStorage = (e: StorageEvent): void => {
      if (e.key === 'cart:changed') {
        void fetchCartItemCount();
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
  }, [cartId, fetchCartItemCount]);

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
                {(IsAdmin() || IsVet()) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Vet}>
                    Veterinarians
                  </Nav.Link>
                )}
                {(IsAdmin() || IsVet() || isReceptionist) && (
                  <NavDropdown title="Customers" id="owners-dropdown">
                    {(IsAdmin() || IsVet()) && (
                      <NavDropdown.Item
                        as={Link}
                        to={AppRoutePaths.AllCustomers}
                      >
                        Customers List
                      </NavDropdown.Item>
                    )}
                    {(IsAdmin() || isReceptionist) && (
                      <NavDropdown.Item
                        as={Link}
                        to={AppRoutePaths.AddingCustomer}
                      >
                        Add Customer
                      </NavDropdown.Item>
                    )}
                  </NavDropdown>
                )}
                {IsAdmin() && (
                  <NavDropdown title="Users" id="users-dropdown">
                    <NavDropdown.Item as={Link} to={AppRoutePaths.AllUsers}>
                      Users List
                    </NavDropdown.Item>
                    <NavDropdown.Item as={Link} to={AppRoutePaths.AllRoles}>
                      Roles List
                    </NavDropdown.Item>
                  </NavDropdown>
                )}
                {!IsAdmin() &&
                  !isInventoryManager &&
                  !IsVet() &&
                  !isReceptionist && (
                    <Nav.Link as={Link} to={AppRoutePaths.CustomerBills}>
                      Bills
                    </Nav.Link>
                  )}
                {!IsAdmin() && !IsVet() && !isInventoryManager && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerVisits}>
                    Visits
                  </Nav.Link>
                )}
                {!isInventoryManager && !isReceptionist && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerEmergency}>
                    Emergency
                  </Nav.Link>
                )}
                {IsAdmin() && (
                  <Nav.Link as={Link} to={AppRoutePaths.AdminBills}>
                    Bills
                  </Nav.Link>
                )}
                {(IsAdmin() || IsVet()) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Visits}>
                    Visits
                  </Nav.Link>
                )}
                {(isInventoryManager || IsAdmin()) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Inventories}>
                    Inventories
                  </Nav.Link>
                )}
                {IsAdmin() && (
                  <Nav.Link as={Link} to={AppRoutePaths.Promos}>
                    Promos
                  </Nav.Link>
                )}
                {!IsAdmin() && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerPromos}>
                    Promos
                  </Nav.Link>
                )}
                {
                  <Nav.Link as={Link} to={AppRoutePaths.Products}>
                    Shop
                  </Nav.Link>
                }

                {IsAdmin() && (
                  <Nav.Link as={Link} to={AppRoutePaths.Carts}>
                    Carts
                  </Nav.Link>
                )}

                {cartId && !isReceptionist && (
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
                {IsOwner() && (
                  <NavDropdown.Item
                    as={Link}
                    to={AppRoutePaths.CustomerProfile}
                  >
                    Profile
                  </NavDropdown.Item>
                )}
                {IsOwner() && (
                  <NavDropdown.Item
                    as={Link}
                    to={AppRoutePaths.CustomerProfileEdit}
                  >
                    Edit Profile
                  </NavDropdown.Item>
                )}
                {IsAdmin() && (
                  <NavDropdown.Item as={Link} to={AppRoutePaths.Home}>
                    Admin Panel
                  </NavDropdown.Item>
                )}
                {IsReceptionist() && (
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
