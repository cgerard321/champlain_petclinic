'use strict';

angular.module('inventoriesProductList', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider
      .state('inventoriesProduct', {
        parent: 'app',
        url: '/inventories/:inventoryId/products-pagination?page&size',
        template: '<inventories-product-list></inventories-product-list>',
      })

      .state('deleteInventoriesProduct', {
        parent: 'app',
        url: '/inventories/:inventoryId/products/:productId',
        template: '<inventory-product-item></inventory-product-item>',
      })
      .state('deleteAllInventoriesProducts', {
        parent: 'app',
        url: '/inventories/:inventoryId/products',
        template: '<inventory-product-item></inventory-product-item>',
      });
  },
]);
