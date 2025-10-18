'use strict';

angular.module('inventoriesProductForm', ['ui.router']).config([
  '$stateProvider',
  function ($stateProvider) {
    $stateProvider.state('inventoriesProductNew', {
      parent: 'app',
      url: '/inventories/:inventoryId/product/new',
      params: { inventoryId: null },
      // url: '/product/new',
      template: '<inventories-product-form></inventories-product-form>',
    });
  },
]);
