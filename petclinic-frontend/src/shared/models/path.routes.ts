export enum AppRoutePaths {
  Default = '/',
  EditInventory = 'inventories/inventory/:inventoryId/edit',
  Review = '/reviews',
  UpdateReview = '/updateReview/:reviewId/edit',
  GetVisitByVistId = 'visits/:visitId',
  Form = '/forms',
  Inventories = '/inventories',
  Vet = '/vets',
  InventoryProducts = '/inventory/:inventoryId/products',
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
  AddVisit = '/visits/add'

  AddVisit = '/visits/add',
  CustomerProfile = '/customer/profile',

}
