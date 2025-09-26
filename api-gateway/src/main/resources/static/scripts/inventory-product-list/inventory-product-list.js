'use strict';

angular.module('inventoryProductList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productList', {
                parent: 'app',
                url: '/inventories/:inventoryId/products-pagination?page&size',
                template: '<inventory-product-list></inventory-product-list>',
                controller: 'InventoryProductController',
                controllerAs: 'self'
            })

            .state('deleteProduct', {
                parent: 'app',
                url: '/inventories/:inventoryId/products/:productId',
                template: '<inventory-product-item></inventory-product-item>'
            })
            .state('deleteAllProducts', {
                parent: 'app',
                url: '/inventories/:inventoryId/products',
                template: '<inventory-product-item></inventory-product-item>'
            })
    }]);