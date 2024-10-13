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
import UpdateBillPage from '@/pages/Bills/UpdateBill.tsx';
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
import EditInventory from '@/features/inventories/EditInventory.tsx';
import { ProtectedRoute } from './shared/components/ProtectedRouteProps';
import CustomerDetailsPage from '@/pages/Customer/CustomerDetailsPage.tsx';
import UpdateCustomerPage from '@/pages/Customer/UpdateCustomerPage.tsx';
import VisitDetails from './features/visits/visits/VisitByVisitId';
import CustomerVisits from '@/pages/Visit/CustomerVisits.tsx';
import UpdateOwnerPetPage from '@/pages/Customer/UpdateOwnerPetPage.tsx';
import EditInventoryProducts from './features/inventories/EditInventoryProducts';
import AddSupplyToInventory from './features/inventories/AddSupplyToInventory';
//import AddEmergencyForm from './features/visits/Emergency/AddEmergencyForm';
//import EditEmergency from './features/visits/Emergency/EditEmergency';
import EmergencyList from './features/visits/Emergency/EmergencyList';
import ProductDetails from '@/features/products/api/ProductDetails.tsx';
import AddPetPage from '@/pages/Customer/AddPetPage.tsx';
import EditProduct from './features/products/components/EditProduct';
import ForgotPassword from '@/pages/User/ForgotPassword.tsx';
import ResetPassword from '@/pages/User/ResetPassword.tsx';
import PromoPage from '@/pages/Promos/PromoListPage.tsx';
import AddPromoPage from '@/pages/Promos/AddPromoPage.tsx';
import UpdatePromoPage from '@/pages/Promos/UpdatePromoPage.tsx';
import CustomerEmergency from './pages/Visit/CustomerEmergency';
import AddEmergencyForm from './features/visits/Emergency/AddEmergencyForm';
import LowStockProducts from '@/features/inventories/LowStockProducts.tsx';
import EmergencyDetails from './features/visits/EmergencyByEmergencyId';
import CustomerPromoPage from "@/pages/Promos/CustomerPromoPage.tsx";

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
        path: AppRoutePaths.LowStockProducts,
        element: (
          <ProtectedRoute>
            <LowStockProducts />
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
        path: AppRoutePaths.CustomerEmergency,
        element: (
          <ProtectedRoute>
            <CustomerEmergency />
          </ProtectedRoute>
        ),
      },

      {
        path: AppRoutePaths.Emergency,
        element: (
          <ProtectedRoute>
            <AddEmergencyForm />
          </ProtectedRoute>
        ),
      },

      {
        path: AppRoutePaths.EmergencyById,
        element: (
          <ProtectedRoute>
            <EmergencyDetails />
          </ProtectedRoute>
        ),
      },
      /*
      {
        path: AppRoutePaths.EditEmergency,
        element: (
          <ProtectedRoute>
            <EditEmergency />
          </ProtectedRoute>
        ),
      },*/

      {
        path: AppRoutePaths.EmergencyList,
        element: (
          <ProtectedRoute>
            <EmergencyList />
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
        path: AppRoutePaths.UpdateBill,
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <UpdateBillPage />
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
        path: AppRoutePaths.EditProduct,
        element: (
          <ProtectedRoute roles={['ADMIN', 'INVENTORY_MANAGER']}>
            <EditProduct />
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
          <ProtectedRoute>
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
        path: AppRoutePaths.Promos,
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <PromoPage />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.CustomerPromos,
        element: (
            <ProtectedRoute>
              <CustomerPromoPage />
            </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.AddPromo,
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <AddPromoPage />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.UpdatePromo,
        element: (
          <ProtectedRoute roles={['ADMIN']}>
            <UpdatePromoPage />
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
        path: AppRoutePaths.AddPet,
        element: (
          <ProtectedRoute roles={['ADMIN', 'VET']}>
            <AddPetPage />
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
  { path: AppRoutePaths.ForgotPassword, element: <ForgotPassword /> },
  { path: AppRoutePaths.ResetPassword, element: <ResetPassword /> },
  {
    path: '*',
    element: <PageNotFound />,
  },
]);

export default router;
