import { Link, useNavigate } from 'react-router-dom';
import {
  IsAdmin,
  IsInventoryManager,
  IsOwner,
  IsVet,
  useUser,
} from '@/context/UserContext';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { useState } from 'react';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Navbar, Nav, NavDropdown, Container } from 'react-bootstrap';
import './AppNavBar.css';

export function NavBar(): JSX.Element {
  const { user } = useUser();
  const navigate = useNavigate();
  const [navbarOpen, setNavbarOpen] = useState(false);

  const logoutUser = (): void => {
    axiosInstance
      .post('http://localhost:8080/api/gateway/users/logout')
      .then(() => {
        navigate(AppRoutePaths.Login);
        localStorage.removeItem('user');
      });
  };

  const toggleNavbar = (): void => {
    setNavbarOpen(prevNavbarOpen => !prevNavbarOpen);
  };

  return (
    <Navbar bg="light" expand="lg" className="navbar">
      <Container>
        <Navbar.Brand href={AppRoutePaths.Home}>PetClinic</Navbar.Brand>
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
            {user.userId !== '' && (
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
                {!IsInventoryManager() && (
                  <Nav.Link as={Link} to={AppRoutePaths.CustomerBills}>
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
                {
                  <Nav.Link as={Link} to={AppRoutePaths.Products}>
                    Products
                  </Nav.Link>

                }
                {IsAdmin() &&
                    <Nav.Link as={Link} to={AppRoutePaths.Carts}>
                      Carts
                    </Nav.Link>


                }
              </>
            )}
          </Nav>
          <Nav className="ms-auto">
            {user.userId !== '' ? (
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
                    Admin-Panel
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
                <Nav.Link as={Link} to={AppRoutePaths.Home}>
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
