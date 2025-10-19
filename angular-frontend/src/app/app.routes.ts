import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: '/welcome', pathMatch: 'full' },
  { path: 'welcome', component: HomeComponent, title: 'Welcome' },
  
  // Auth routes
  {
    path: 'login',
    loadComponent: () => import('./features/auth/pages/login/login.component').then(m => m.LoginComponent),
    title: 'Login'
  },
  {
    path: 'signup',
    loadComponent: () => import('./features/auth/pages/signup/signup.component').then(m => m.SignupComponent),
    title: 'Sign Up'
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/auth/pages/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent),
    title: 'Forgot Password'
  },
  {
    path: 'reset-password',
    loadComponent: () => import('./features/auth/pages/reset-password/reset-password.component').then(m => m.ResetPasswordComponent),
    title: 'Reset Password'
  },
  {
    path: 'verify-email',
    loadComponent: () => import('./features/auth/pages/verify-email/verify-email.component').then(m => m.VerifyEmailComponent),
    title: 'Verify Email'
  },
  {
    path: 'admin-panel',
    loadComponent: () => import('./features/auth/pages/admin-panel/admin-panel.component').then(m => m.AdminPanelComponent),
    canActivate: [authGuard],
    title: 'Admin Panel'
  },
  {
    path: 'user-details/:userId',
    loadComponent: () => import('./features/auth/pages/user-details/user-details.component').then(m => m.UserDetailsComponent),
    canActivate: [authGuard],
    title: 'User Details'
  },
  {
    path: 'role-update/:userId',
    loadComponent: () => import('./features/auth/pages/role-update/role-update.component').then(m => m.RoleUpdateComponent),
    canActivate: [authGuard],
    title: 'Role Update'
  },

  // Owner/Customer routes
  {
    path: 'owners',
    loadComponent: () => import('./features/customers/pages/owner-list/owner-list.component').then(m => m.OwnerListComponent),
    // canActivate: [authGuard], // Temporarily disabled for testing
    title: 'Owners'
  },
  {
    path: 'owners/:ownerId',
    loadComponent: () => import('./features/customers/pages/owner-details/owner-details.component').then(m => m.OwnerDetailsComponent),
    canActivate: [authGuard],
    title: 'Owner Details'
  },
  {
    path: 'owners/new',
    loadComponent: () => import('./features/customers/pages/owner-form/owner-form.component').then(m => m.OwnerFormComponent),
    canActivate: [authGuard],
    title: 'New Owner'
  },
  {
    path: 'owners/:ownerId/edit',
    loadComponent: () => import('./features/customers/pages/owner-form/owner-form.component').then(m => m.OwnerFormComponent),
    canActivate: [authGuard],
    title: 'Edit Owner'
  },
  {
    path: 'owner-register',
    loadComponent: () => import('./features/auth/pages/signup/signup.component').then(m => m.SignupComponent),
    title: 'Owner Registration'
  },
  {
    path: 'pet-types',
    loadComponent: () => import('./features/customers/pages/pet-type-list/pet-type-list.component').then(m => m.PetTypeListComponent),
    canActivate: [authGuard],
    title: 'Pet Types'
  },
  {
    path: 'owners/:ownerId/pets/new',
    loadComponent: () => import('./features/customers/pages/pet-form/pet-form.component').then(m => m.PetFormComponent),
    canActivate: [authGuard],
    title: 'New Pet'
  },
  {
    path: 'owners/:ownerId/pets/:petId/edit',
    loadComponent: () => import('./features/customers/pages/pet-form/pet-form.component').then(m => m.PetFormComponent),
    canActivate: [authGuard],
    title: 'Edit Pet'
  },
  {
    path: 'owners/:ownerId/pets/:petId',
    loadComponent: () => import('./features/customers/pages/pet-details/pet-details.component').then(m => m.PetDetailsComponent),
    canActivate: [authGuard],
    title: 'Pet Details'
  },
  {
    path: 'pet-owner-detail/:ownerId/:petId',
    loadComponent: () => import('./features/customers/pages/pet-owner-detail/pet-owner-detail.component').then(m => m.PetOwnerDetailComponent),
    canActivate: [authGuard],
    title: 'Pet Owner Detail'
  },

  // Vet routes
  {
    path: 'vets',
    loadComponent: () => import('./features/vets/pages/vet-list/vet-list.component').then(m => m.VetListComponent),
    canActivate: [authGuard],
    title: 'Veterinarians'
  },
  {
    path: 'vets/:vetId',
    loadComponent: () => import('./features/vets/pages/vet-details/vet-details.component').then(m => m.VetDetailsComponent),
    canActivate: [authGuard],
    title: 'Vet Details'
  },
  {
    path: 'vets/new',
    loadComponent: () => import('./features/vets/pages/vet-form/vet-form.component').then(m => m.VetFormComponent),
    canActivate: [authGuard],
    title: 'New Vet'
  },
  {
    path: 'vets/:vetId/edit',
    loadComponent: () => import('./features/vets/pages/vet-form/vet-form.component').then(m => m.VetFormComponent),
    canActivate: [authGuard],
    title: 'Edit Vet'
  },

  // Visit routes
  {
    path: 'visits',
    loadComponent: () => import('./features/visits/pages/visits/visits.component').then(m => m.VisitsComponent),
    canActivate: [authGuard],
    title: 'Visits'
  },
  {
    path: 'visit-list',
    loadComponent: () => import('./features/visits/pages/visit-list/visit-list.component').then(m => m.VisitListComponent),
    canActivate: [authGuard],
    title: 'Visit List'
  },
  {
    path: 'visits/:visitId',
    loadComponent: () => import('./features/visits/pages/visit-details-info/visit-details-info.component').then(m => m.VisitDetailsInfoComponent),
    canActivate: [authGuard],
    title: 'Visit Details'
  },

  // Billing routes
  {
    path: 'bills',
    redirectTo: 'bills/history',
    pathMatch: 'full'
  },
  {
    path: 'bills/history',
    loadComponent: () => import('./features/bills/pages/bill-history/bill-history.component').then(m => m.BillHistoryComponent),
    canActivate: [authGuard],
    title: 'Bill History'
  },
  {
    path: 'bills/new',
    loadComponent: () => import('./features/bills/pages/bill-form/bill-form.component').then(m => m.BillFormComponent),
    canActivate: [authGuard],
    title: 'New Bill'
  },
  {
    path: 'bills/owner/:ownerId',
    loadComponent: () => import('./features/bills/pages/bills-by-owner-id/bills-by-owner-id.component').then(m => m.BillsByOwnerIdComponent),
    canActivate: [authGuard],
    title: 'Bills by Owner'
  },
  {
    path: 'bills/vet/:vetId',
    loadComponent: () => import('./features/bills/pages/bills-by-vet-id/bills-by-vet-id.component').then(m => m.BillsByVetIdComponent),
    canActivate: [authGuard],
    title: 'Bills by Vet'
  },
  {
    path: 'bills/:billId/edit',
    loadComponent: () => import('./features/bills/pages/bill-form/bill-form.component').then(m => m.BillFormComponent),
    canActivate: [authGuard],
    title: 'Edit Bill'
  },
  {
    path: 'bills/:billId',
    loadComponent: () => import('./features/bills/pages/bill-details/bill-details.component').then(m => m.BillDetailsComponent),
    canActivate: [authGuard],
    title: 'Bill Details'
  },

  // Inventory routes
  {
    path: 'inventories',
    loadComponent: () => import('./features/inventory/pages/inventories-list/inventories-list.component').then(m => m.InventoriesListComponent),
    canActivate: [authGuard],
    title: 'Inventory List'
  },
  {
    path: 'inventories/new',
    loadComponent: () => import('./features/inventory/pages/inventories-form/inventories-form.component').then(m => m.InventoriesFormComponent),
    canActivate: [authGuard],
    title: 'Create Inventory'
  },
  {
    path: 'inventories/:id/edit',
    loadComponent: () => import('./features/inventory/pages/inventories-update-form/inventories-update-form.component').then(m => m.InventoriesUpdateFormComponent),
    canActivate: [authGuard],
    title: 'Update Inventory'
  },
  {
    path: 'inventories/:id/products',
    loadComponent: () => import('./features/inventory/pages/inventories-product-list/inventories-product-list.component').then(m => m.InventoriesProductListComponent),
    canActivate: [authGuard],
    title: 'Inventory Products'
  },
  {
    path: 'inventories/:id/products/new',
    loadComponent: () => import('./features/inventory/pages/inventories-product-form/inventories-product-form.component').then(m => m.InventoriesProductFormComponent),
    canActivate: [authGuard],
    title: 'Create Product'
  },
  {
    path: 'inventories/:id/products/:productId/edit',
    loadComponent: () => import('./features/inventory/pages/inventories-product-update-form/inventories-product-update-form.component').then(m => m.InventoriesProductUpdateFormComponent),
    canActivate: [authGuard],
    title: 'Update Product'
  },
  {
    path: 'inventories/:id/products/:productId',
    loadComponent: () => import('./features/inventory/pages/inventories-product-details-info/inventories-product-details-info.component').then(m => m.InventoriesProductDetailsInfoComponent),
    canActivate: [authGuard],
    title: 'Product Details'
  },

  // Products routes
  {
    path: 'products',
    loadComponent: () => import('./features/products/pages/product-list/product-list.component').then(m => m.ProductListComponent),
    canActivate: [authGuard],
    title: 'Products'
  },
  {
    path: 'products/new',
    loadComponent: () => import('./features/products/pages/product-form/product-form.component').then(m => m.ProductFormComponent),
    canActivate: [authGuard],
    title: 'New Product'
  },
  {
    path: 'products/:productId/edit',
    loadComponent: () => import('./features/products/pages/product-update-form/product-update-form.component').then(m => m.ProductUpdateFormComponent),
    canActivate: [authGuard],
    title: 'Edit Product'
  },
  {
    path: 'products/:productId',
    loadComponent: () => import('./features/products/pages/product-details-info/product-details-info.component').then(m => m.ProductDetailsInfoComponent),
    canActivate: [authGuard],
    title: 'Product Details'
  },

  { path: '**', redirectTo: '/welcome', pathMatch: 'full' }
];
