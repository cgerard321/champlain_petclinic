import { Link, useNavigate } from 'react-router-dom';
import {
  IsAdmin,
  IsInventoryManager,
  IsOwner,
  IsVet,
  useUser,
} from '@/context/UserContext';
import { fetchCartIdByCustomerId } from '../features/carts/api/getCart';
import axiosInstance from '@/shared/api/axiosInstance';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useEffect, useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Navbar, Nav, NavDropdown, Container } from 'react-bootstrap';
import { FaShoppingCart } from 'react-icons/fa'; // Importing the shopping cart icon
import './AppNavBar.css';

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
  const [navbarOpen, setNavbarOpen] = useState(false);
  const [cartId, setCartId] = useState<string | null>(null);
  const [cartItemCount, setCartItemCount] = useState<number>(0); // State for cart item count

  const logoutUser = (): void => {
    axiosInstance
      .post('http://localhost:8080/api/gateway/users/logout')
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
      if (user.userId) {
        try {
          const id = await fetchCartIdByCustomerId(user.userId);
          setCartId(id);
        } catch (error) {
          console.error('Error fetching cart ID:', error);
        }
      }
    };

    fetchCartId();
  }, [user.userId]);

  // Fetch cart item count
  useEffect(() => {
    const fetchCartItemCount = async (): Promise<void> => {
      if (cartId) {
        try {
          const response = await fetch(
            `http://localhost:8080/api/v2/gateway/carts/${cartId}`,
            {
              headers: { Accept: 'application/json' },
              credentials: 'include',
            }
          );

          if (response.ok) {
            const data = await response.json();
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
          } else {
            setCartItemCount(0);
          }
        } catch (error) {
          console.error('Error fetching cart item count:', error);
          setCartItemCount(0);
        }
      } else {
        setCartItemCount(0);
      }
    };

    fetchCartItemCount();
  }, [cartId]);

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
                {(IsAdmin() || IsVet()) && (
                  <NavDropdown title="Customers" id="owners-dropdown">
                    <NavDropdown.Item as={Link} to={AppRoutePaths.AllCustomers}>
                      Customers List
                    </NavDropdown.Item>
                    {IsAdmin() && (
                      <NavDropdown.Item
                        as={Link}
                        to={AppRoutePaths.AddingCustomer}
                      >
                        Add Customer
                      </NavDropdown.Item>
                    )}
                  </NavDropdown>
                )}
                {!IsAdmin() && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerBills}>
                    Bills
                  </Nav.Link>
                )}
                {!IsAdmin() && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerVisits}>
                    Visits
                  </Nav.Link>
                )}
                {!IsAdmin() && (
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
                {(IsInventoryManager() || IsAdmin()) && (
                  <Nav.Link as={Link} to={AppRoutePaths.Inventories}>
                    Inventories
                  </Nav.Link>
                )}
                {IsAdmin() && (
                  <Nav.Link as={Link} to={AppRoutePaths.Emailing}>
                    Emails
                  </Nav.Link>
                )}
                <Nav.Link as={Link} to={AppRoutePaths.Products}>
                  Shop
                </Nav.Link>
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

                {cartId && (
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
