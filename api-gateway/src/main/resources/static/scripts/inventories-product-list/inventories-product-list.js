'use strict';

angular.module('inventoriesProductList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventoriesProduct', {
                parent: 'app',
                url: '/inventories/:inventoryId/products-pagination?page&size',
                template: '<inventories-product-list></inventories-product-list>',
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