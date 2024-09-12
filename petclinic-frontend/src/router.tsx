import {createBrowserRouter} from 'react-router-dom';
import {AppRoutePaths} from './shared/models/path.routes';
import Login from './pages/Login/Login';
import Inventories from '@/pages/Inventory/Inventories.tsx';
import {ProtectedRoute} from '@/shared/components/ProtectedRouteProps.tsx';
import Products from "@/pages/Product/Products.tsx";

const router = createBrowserRouter([
  {
    path: AppRoutePaths.Default,
    children: [
      {
        path: AppRoutePaths.Inventories,
        element: (
          <ProtectedRoute>
            <Inventories />
          </ProtectedRoute>
        ),
      },
      {
        path: AppRoutePaths.Products,
        element: (
            <ProtectedRoute>
              <Products/>
            </ProtectedRoute>
        )
      }
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
      //       path: AppRoutePaths.Unauthorized
      //       element: /* UnauthorizedComponent */
      //   }
    ],
  },
  { path: AppRoutePaths.login, element: <Login /> },
  //   {path: '*', element: /* PageNotFoundComponent */},
]);
export default router;
