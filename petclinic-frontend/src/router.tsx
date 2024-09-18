import { createBrowserRouter } from 'react-router-dom';
import { AppRoutePaths } from './shared/models/path.routes';
import Login from './pages/Login/Login';
import Inventories from '@/pages/Inventory/Inventories.tsx';
import Vet from '@/pages/Vet/Vet.tsx';
import { ProtectedRoute } from '@/shared/components/ProtectedRouteProps.tsx';
import Home from '@/pages/Home/Home.tsx';
import ProfileEdit from '@/pages/Customer/ProfileEdit.tsx';
import Products from '@/pages/Product/Products.tsx';
import AddingCustomer from '@/pages/Customer/AddingCustomer.tsx';
import CustomerBillingPage from '@/pages/Bills/CostumerBills.tsx';
import AllOwners from '@/pages/Customer/AllOwners.tsx';
import PageNotFound from '@/pages/Error/PageNotFound.tsx';
import Forbidden from '@/pages/Error/Forbidden.tsx';
import Unauthorized from '@/pages/Error/Unauthorized.tsx';
import InternalServerError from '@/pages/Error/InternalServerError.tsx';
import RequestTimeout from '@/pages/Error/RequestTimeout.tsx';
import ServiceUnavailable from '@/pages/Error/ServiceUnavailable.tsx';
import EditInventory from '@/features/inventories/EditInventory.tsx';

const router = createBrowserRouter([
  {
    path: AppRoutePaths.Default,
    children: [
      {
        path: AppRoutePaths.Home,
        element: (
          <ProtectedRoute>
            <Home />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Inventories,
        element: (
          <ProtectedRoute>
            <Inventories />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Vet,
        element: (
          <ProtectedRoute>
            <Vet />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.CustomerProfileEdit,
        element: (
          <ProtectedRoute>
            <ProfileEdit />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.AddingCustomer,
        element: (
          <ProtectedRoute>
            <AddingCustomer />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.CustomerBills,
        element: (
          <ProtectedRoute>
            <CustomerBillingPage />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.AllCustomers,
        element: (
          <ProtectedRoute>
            <AllOwners />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.PageNotFound,
        element: (
          <ProtectedRoute>
            <PageNotFound />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Forbidden,
        element: (
          <ProtectedRoute>
            <Forbidden />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Unauthorized,
        element: (
          <ProtectedRoute>
            <Unauthorized />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.InternalServerError,
        element: (
          <ProtectedRoute>
            <InternalServerError />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.RequestTimeout,
        element: (
          <ProtectedRoute>
            <RequestTimeout />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.ServiceUnavailable,
        element: (
          <ProtectedRoute>
            <ServiceUnavailable />
          </ProtectedRoute>
        ),
      },
      {
        path: '*',
        element: (
          <ProtectedRoute>
            <PageNotFound />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Products,
        element: (
          <ProtectedRoute>
            <Products />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.EditInventory,
        element: (
          <ProtectedRoute>
            <EditInventory />
          </ProtectedRoute>
        ),
      },
      //   {
      //       path: AppRoutePaths.PageNotFound,
      //       element: /* PageNotFoundComponent */
      //   },
      //   {
      //       path: AppRoutePaths.InternalServer,
      //       element: /* InternalServerErrorComponent */
      //   },
      //   {
      //       path: AppRoutePaths.ServiceTimeout,
      //       element: /* ServiceTimeoutComponent */
      //   },
      //   {
      //       path: AppRoutePaths.ServiceUnavailable,
      //       element: /* ServiceUnavailableComponent */
      //   },
      //   {
      //       path: AppRoutePaths.Unauthorized,
      //       element: /* UnauthorizedComponent */
      //   }
    ],
  },
  { path: AppRoutePaths.login, element: <Login /> },
  //   {path: '*', element: /* PageNotFoundComponent */},
]);

export default router;
