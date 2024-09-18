import { Link, useNavigate } from 'react-router-dom';
import { IsAdmin, useUser } from '@/context/UserContext';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import './AppNavBar.css';
import { useState } from 'react';

export function NavBar(): JSX.Element {
  const { user } = useUser();
  const navigate = useNavigate();
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const logoutUser = (): void => {
    axiosInstance.post(axiosInstance.defaults.baseURL + 'logout').then(() => {
      navigate(AppRoutePaths.login);
      localStorage.removeItem('username');
      localStorage.removeItem('email');
      localStorage.removeItem('UUID');
      localStorage.removeItem('roles');
    });
  };

  const toggleDropdown = (): void => {
    setDropdownOpen(prevDropdownOpen => !prevDropdownOpen);
  };

  return (
    <nav className="navbar">
      <a className="navbar-brand" href="#">
        PetClinic
      </a>

      <div className="navbar-collapse" id="navbarSupportedContent">
        {user ? (
          <>
            <ul className="navbar-nav">
              <li className="nav-item active">
                <Link className="nav-link" to={AppRoutePaths.Default}>
                  Home
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to={AppRoutePaths.Vet}>
                  Veterinarians
                </Link>
              </li>
              <li className="nav-item dropdown">
                <button
                  className="nav-link dropdown-toggle btn btn-link"
                  type="button"
                  onClick={toggleDropdown}
                >
                  Owners
                </button>
                <ul
                  className={`dropdown-menu ${dropdownOpen ? 'show' : ''}`}
                  aria-labelledby="navbarDropdown"
                >
                  <li>
                    <Link className="dropdown-item" to="/customers">
                      Owners
                    </Link>
                  </li>
                  <li>
                    <Link className="dropdown-item" to="/customer/add">
                      Edit Account
                    </Link>
                  </li>
                  <li>
                    <Link className="dropdown-item" to="">
                      Pet Types
                    </Link>
                  </li>
                </ul>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to={AppRoutePaths.CustomerBills}>
                  Bills
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to={AppRoutePaths.Visits}>
                  Visits
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to={AppRoutePaths.Inventories}>
                  Inventories
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to={AppRoutePaths.Products}>
                  Products
                </Link>
              </li>
            </ul>
            <ul className="navbar-nav justify-content-end">
              <li className="nav-item">
                <Link className="nav-link" to="">
                  Welcome back {user.username}!
                </Link>
              </li>
              {IsAdmin() && (
                <li className="nav-item">
                  <Link className="nav-link" to="">
                    Admin-Panel
                  </Link>
                </li>
              )}
              <li className="nav-item">
                <a
                  className="nav-link"
                  onClick={logoutUser}
                  style={{ cursor: 'pointer' }}
                >
                  Logout
                </a>
              </li>
            </ul>
          </>
        ) : (
          <ul>
            <li className="nav-item">
              <Link className="nav-link" to="">
                Signup
              </Link>
            </li>
            <li className="nav-item">
              <Link className="nav-link" to={AppRoutePaths.login}>
                Login
              </Link>
            </li>
          </ul>
        )}
      </div>
    </nav>
  );
}
