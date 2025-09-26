'use strict';

angular.module('productDetailsInfo', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productDetails', {
                parent: 'app',
                url: '/inventories/:inventoryId/products/:productId',
                template: '<product-details-info></product-details-info>'
            })

            .state('products', {
                parent: 'app',
                url: '/inventories/:inventoryId/products',
                template: '<inventory-product-list></inventory-product-list>'
            })
    }]);