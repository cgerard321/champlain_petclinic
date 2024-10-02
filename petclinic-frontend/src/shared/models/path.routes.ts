export enum AppRoutePaths {
  Default = '/',
  EditInventory = 'inventories/inventory/:inventoryId/edit',
  EditInventoryProducts = 'inventory/:inventoryId/products/:productId/edit',
  AddSupplyToInventory = 'inventory/:inventoryId/products/add',
  Review = '/reviews',
  UpdateReview = '/updateReview/:reviewId/edit',
  GetVisitByVistId = 'visits/:visitId',
  Form = '/forms',
  Inventories = '/inventories',
  Vet = '/vets',
  InventoryProducts = '/inventory/:inventoryId/products',
  InventorySupplies = '/inventories/:inventoryName/supplies',
  AdminBills = '/bills/admin',
  CustomerBills = '/bills/customer',
  PageNotFound = '/page-not-found',
  Unauthorized = '/unauthorized',
  RequestTimeout = '/request-timeout',
  InternalServerError = '/internal-server-error',
  ServiceUnavailable = '/service-unavailable',
  Login = '/users/login',
  CustomerProfileEdit = '/customer/profile/edit',
  AddingCustomer = '/customer/add',
  AllCustomers = '/customers',
  Home = '/home',
  Forbidden = '/forbidden',
  Products = '/products',
  Visits = '/visits',
  Carts = '/carts',
  UserCart = '/carts/:cartId',
  AddVisit = '/visits/add',
  CustomerProfile = '/customer/profile',
  Emailing = '/emailing',
  MockPage = '/mockpage',
  VetDetails = '/vets/{vetId}',
  EditVisit = '/visits/:visitId/edit',
  CustomerDetails = '/customers/:ownerId',
  UpdateCustomer = '/customers/:ownerId/edit',
  SignUp = '/users/signup',
  CustomerVisits = '/customer/visits',
}
