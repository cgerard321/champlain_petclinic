'use strict';

angular.module('inventoryProductList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productList', {
                parent: 'app',
                url: '/inventory/:inventoryId/products-pagination?page&size',
                template: '<inventory-product-list></inventory-product-list>',
                controller: 'InventoryProductController',
                controllerAs: 'self'
            })

            .state('updateProduct', {
                    parent: 'app',
                    url: '/inventory/:inventoryId/products/:productId',
                    template: '<inventory-product-item></inventory-product-item>'
                }
            )
            .state('deleteProduct', {
                parent: 'app',
                url: '/inventory/:inventoryId/products/:productId',
                template: '<inventory-product-item></inventory-product-item>'
            })
            .state('deleteAllProducts', {
                parent: 'app',
                url: '/inventory/:inventoryId/products',
                template: '<inventory-product-item></inventory-product-item>'
            })
    }]);