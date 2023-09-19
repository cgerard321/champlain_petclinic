'use strict';

angular.module('inventoryProductList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productList', {
                parent: 'app',
                //TODO change url to /inventory/:inventoryId/products when the get by inventoryId is implemented
                //url: '/inventory/:inventoryId/products',
                url: '/products',
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