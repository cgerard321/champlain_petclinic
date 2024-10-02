import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppRoutePaths } from './shared/models/path.routes';
import Login from '@/pages/User/Login';
import Inventories from '@/pages/Inventory/Inventories.tsx';
import InventoryProducts from '@/features/inventories/InventoryProducts.tsx';
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
import Visits from './pages/Visit/Visit';
import AddReviewForm from './features/visits/Review/AddReviewForm';
import EditReviewForm from './features/visits/Review/EditReviewForm';
import Review from './pages/Review/Review';
import EditInventory from '@/features/inventories/EditInventory.tsx';
import CartPage from '@/pages/Carts/Cart.tsx';
import VisitByVisitId from './features/visits/visits/VisitByVisitId';
import AddingVisit from './features/visits/models/AddingVisit';
import ProfilePage from '@/pages/Customer/ProfilePage.tsx';
import AdminBillingPage from '@/pages/Bills/AdminBill.tsx';
import UserCart from '@/features/carts/components/UserCart.tsx';
import VetDetails from '@/pages/Vet/VetDetails.tsx';
import EmailingPage from '@/pages/Emailing/EmailingPage.tsx';
import MockPage from '@/pages/Inventory/MockPage.tsx';
// import InventorySupplies from '@/features/inventories/InventorySupplies.tsx';
import EditingVisit from './features/visits/models/EditingVisit';
import UpdateCustomerPage from '@/pages/Customer/UpdateCustomerPage.tsx';
import CustomerDetailsPage from '@/pages/Customer/CustomerDetailsPage.tsx';

const router = createBrowserRouter([
  {
    children: [
      {
        path: AppRoutePaths.EditInventory,
        element: (
          <ProtectedRoute>
            <EditInventory />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.GetVisitByVistId,
        element: (
          <ProtectedRoute>
            <VisitByVisitId />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Form,
        element: (
          <ProtectedRoute>
            <AddReviewForm />
          </ProtectedRoute>
        ),
      },

      {
        path: AppRoutePaths.Review,
        element: (
          <ProtectedRoute>
            <Review />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.UpdateReview,
        element: (
          <ProtectedRoute>
            <EditReviewForm />
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
        path: AppRoutePaths.InventoryProducts,
        element: (
          <ProtectedRoute>
            <InventoryProducts />
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
        path: `${AppRoutePaths.Vet}/:vetId`,
        element: (
          <ProtectedRoute>
            <VetDetails />
          </ProtectedRoute>
        ),
      },

      {
        path: AppRoutePaths.CustomerProfileEdit,
        element: (
          <ProtectedRoute roles={['OWNER']}>
            <ProfileEdit />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.AddingCustomer,
        element: (
          <ProtectedRoute roles={['ADMIN']}>
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
        path: AppRoutePaths.AdminBills,
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <AdminBillingPage />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.AllCustomers,
        element: (
          <ProtectedRoute roles={['ADMIN', 'VET']}>
            <AllOwners />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.UpdateCustomer,
        element: (
          <ProtectedRoute roles={['ADMIN', 'VET']}>
            <UpdateCustomerPage />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.CustomerDetails,
        element: (
          <ProtectedRoute roles={['ADMIN', 'VET']}>
            <CustomerDetailsPage />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Forbidden,
        element: <Forbidden />,
      },
      {
        path: AppRoutePaths.Unauthorized,
        element: <Unauthorized />,
      },
      {
        path: AppRoutePaths.PageNotFound,
        element: <PageNotFound />,
      },
      {
        path: AppRoutePaths.InternalServerError,
        element: <InternalServerError />,
      },
      {
        path: AppRoutePaths.RequestTimeout,
        element: <RequestTimeout />,
      },
      {
        path: AppRoutePaths.ServiceUnavailable,
        element: <ServiceUnavailable />,
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
        path: AppRoutePaths.Visits,
        element: (
          <ProtectedRoute>
            <Visits />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.AddVisit,
        element: (
          <ProtectedRoute>
            <AddingVisit />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Carts,
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <CartPage />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Emailing,
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <EmailingPage />
          </ProtectedRoute>
        ),
      },
      {
        path: `${AppRoutePaths.Carts}/:cartId`, // Route for viewing a specific cart
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <UserCart />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.CustomerProfile,
        element: (
          <ProtectedRoute roles={['OWNER']}>
            <ProfilePage />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.EditVisit,
        element: (
          <ProtectedRoute>
            <EditingVisit />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.MockPage,
        element: (
          <ProtectedRoute>
            <MockPage />
          </ProtectedRoute>
        ),
      },
      // {
      //   path: AppRoutePaths.InventorySupplies,
      //   element: (
      //     <ProtectedRoute>
      //       <InventorySupplies />
      //     </ProtectedRoute>
      //   ),
      // },
    ],
  },
  {
    path: AppRoutePaths.Default,
    element: <Navigate to={AppRoutePaths.Home} replace />,
  },
  {
    path: AppRoutePaths.Home,
    element: <Home />,
  },
  { path: AppRoutePaths.Login, element: <Login /> },
  {
    path: '*',
    element: <PageNotFound />,
  },
]);

export default router;
