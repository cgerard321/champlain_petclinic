'use strict';

angular.module('inventoriesProductUpdateForm', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('updateProductInventory', {
      parent: 'app',
      url: '/inventories/:inventoryId/products/:productId',
      template:
        '<inventories-product-update-form></inventories-product-update-form>',
    });
  },
]);
