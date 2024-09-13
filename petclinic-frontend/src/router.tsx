import { createBrowserRouter } from 'react-router-dom';
import { AppRoutePaths } from './shared/models/path.routes';
import Login from './pages/Login/Login';
import Inventories from '@/pages/Inventory/Inventories.tsx';
import Vet from '@/pages/Vet/Vet.tsx';
import { ProtectedRoute } from '@/shared/components/ProtectedRouteProps.tsx';
import Home from '@/pages/Home/Home.tsx';

const router = createBrowserRouter([
  {
    path: AppRoutePaths.Default,
    children: [
      {
        path: AppRoutePaths.Default,
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
      }, //   {
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
