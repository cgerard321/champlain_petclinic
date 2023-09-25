'use strict';

angular.module('inventoryProductList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productList', {
                parent: 'app',
                url: '/inventory/:inventoryId/products',
                template: '<inventory-product-list></inventory-product-list>'
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
    }]);