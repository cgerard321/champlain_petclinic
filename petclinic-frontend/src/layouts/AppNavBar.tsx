import { Link, useNavigate } from 'react-router-dom';
import { IsAdmin, useUser } from '@/context/UserContext';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import './AppNavBar.css';

export function NavBar(): JSX.Element {
  const { user } = useUser();
  const navigate = useNavigate();

  const logoutUser = (): void => {
    axiosInstance.post(axiosInstance.defaults.baseURL + 'logout').then(() => {
      navigate(AppRoutePaths.login);
      localStorage.removeItem('username');
      localStorage.removeItem('email');
      localStorage.removeItem('UUID');
      localStorage.removeItem('roles');
    });
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
                <a
                  className="nav-link dropdown-toggle"
                  href="#"
                  id="navbarDropdown"
                  role="button"
                  aria-haspopup="true"
                  aria-expanded="false"
                >
                  Owners
                </a>
                <div className="dropdown-menu" aria-labelledby="navbarDropdown">
                  <Link className="nav-link" to="">
                    Owners
                  </Link>
                  <Link className="nav-link" to="">
                    Edit Account
                  </Link>
                  <Link className="nav-link" to="">
                    Pet Types
                  </Link>
                </div>
              </li>
              {IsAdmin() && (
                <li className="nav-item">
                  <Link className="nav-link" to="">
                    Bills
                  </Link>
                </li>
              )}
              <li className="nav-item">
                <Link className="nav-link" to={AppRoutePaths.CustomerBills}>
                  Bills
                </Link>
              </li>
              <li className="nav-item">
                <Link className="nav-link" to="">
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
