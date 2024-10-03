import { createBrowserRouter, Navigate } from 'react-router-dom';
import { AppRoutePaths } from './shared/models/path.routes';
import Login from '@/pages/User/Login';
import SignUp from '@/pages/User/SignUp';
import Home from '@/pages/Home/Home.tsx';
import ProfilePage from '@/pages/Customer/ProfilePage.tsx';
import ProfileEdit from '@/pages/Customer/ProfileEdit.tsx';
import Products from '@/pages/Product/Products.tsx';
import Inventories from '@/pages/Inventory/Inventories.tsx';
import InventoryProducts from '@/features/inventories/InventoryProducts.tsx';
import Vet from '@/pages/Vet/Vet.tsx';
import VetDetails from '@/pages/Vet/VetDetails.tsx';
import Visits from './pages/Visit/Visit';
import AddReviewForm from './features/visits/Review/AddReviewForm';
import EditReviewForm from './features/visits/Review/EditReviewForm';
import Review from './pages/Review/Review';
import CartPage from '@/pages/Carts/Cart.tsx';
import UserCart from '@/features/carts/components/UserCart.tsx';
import AddingCustomer from '@/pages/Customer/AddingCustomer.tsx';
import AllOwners from '@/pages/Customer/AllOwners.tsx';
import CustomerBillingPage from '@/pages/Bills/CostumerBills.tsx';
import AdminBillingPage from '@/pages/Bills/AdminBill.tsx';
import EditingVisit from './features/visits/models/EditingVisit';
import AddingVisit from './features/visits/models/AddingVisit';
import InternalServerError from '@/pages/Error/InternalServerError.tsx';
import RequestTimeout from '@/pages/Error/RequestTimeout.tsx';
import ServiceUnavailable from '@/pages/Error/ServiceUnavailable.tsx';
import Forbidden from '@/pages/Error/Forbidden.tsx';
import Unauthorized from '@/pages/Error/Unauthorized.tsx';
import PageNotFound from '@/pages/Error/PageNotFound.tsx';
import EmailingPage from '@/pages/Emailing/EmailingPage.tsx';
import MockPage from '@/pages/Inventory/MockPage.tsx';
import InventorySupplies from '@/features/inventories/InventorySupplies.tsx';
import EditInventory from '@/features/inventories/EditInventory.tsx';
import { ProtectedRoute } from './shared/components/ProtectedRouteProps';
import CustomerDetailsPage from '@/pages/Customer/CustomerDetailsPage.tsx';
import UpdateCustomerPage from '@/pages/Customer/UpdateCustomerPage.tsx';
import VisitDetails from './features/visits/visits/VisitByVisitId';
import CustomerVisits from '@/pages/Visit/CustomerVisits.tsx';
import EditInventoryProducts from '@/features/inventories/EditInventoryProducts.tsx';
import AddSupplyToInventory from './features/inventories/AddSupplyToInventory';
import UpdateOwnerPetPage from '@/pages/Customer/UpdateOwnerPetPage.tsx';
import ProductDetails from "@/features/products/api/ProductDetails.tsx";

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
        path: AppRoutePaths.EditInventoryProducts,
        element: (
          <ProtectedRoute>
            <EditInventoryProducts />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.AddSupplyToInventory,
        element: (
          <ProtectedRoute>
            <AddSupplyToInventory />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.GetVisitByVistId,
        element: (
          <ProtectedRoute>
            <VisitDetails />
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
        path: `${AppRoutePaths.Carts}/:cartId`,
        element: (
          <ProtectedRoute roles={['ADMIN', 'OWNER']}>
            <UserCart />
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
        path: AppRoutePaths.UpdatePet,
        element: (
          <ProtectedRoute roles={['ADMIN', 'VET']}>
            <UpdateOwnerPetPage />
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
      {
        path: AppRoutePaths.InventorySupplies,
        element: (
          <ProtectedRoute>
            <InventorySupplies />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.CustomerVisits,
        element: (
          <ProtectedRoute>
            <CustomerVisits />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.ProductDetails,
        element: (
            <ProtectedRoute>
              <ProductDetails />
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
  { path: AppRoutePaths.SignUp, element: <SignUp /> },
  {
    path: '*',
    element: <PageNotFound />,
  },
]);

export default router;
