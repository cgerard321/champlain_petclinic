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
import VetDetails from "@/pages/Vet/VetDetails.tsx";
import Review from './pages/Review/Review';
import EditReviewForm from './features/visits/Review/EditReviewForm';
import AllOwners from '@/pages/Customer/AllOwners.tsx';
import PageNotFound from '@/pages/Error/PageNotFound.tsx';
import Forbidden from '@/pages/Error/Forbidden.tsx';
import Unauthorized from '@/pages/Error/Unauthorized.tsx';
import InternalServerError from '@/pages/Error/InternalServerError.tsx';
import RequestTimeout from '@/pages/Error/RequestTimeout.tsx';
import ServiceUnavailable from '@/pages/Error/ServiceUnavailable.tsx';
import Visits from './pages/Visit/Visit';
import AddReviewForm from './features/visits/Review/AddReviewForm';
import EditInventory from '@/features/inventories/EditInventory.tsx';

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
        path: AppRoutePaths.Home,
        element: (
            <ProtectedRoute>
              <Home />
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
        path: AppRoutePaths.AllCustomers,
        element: (
            <ProtectedRoute roles={['ADMIN', 'VET']}>
              <AllOwners />
            </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.PageNotFound,
        element: <PageNotFound />,
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
