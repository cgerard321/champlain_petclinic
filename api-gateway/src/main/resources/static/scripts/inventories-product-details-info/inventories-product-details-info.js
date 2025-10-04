'use strict';

angular.module('inventoriesProductDetailsInfo', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productDetails', {
                parent: 'app',
                url: '/inventories/:inventoryId/products/:productId/details',
                template: '<inventories-product-details-info></inventories-product-details-info>'
            })

            .state('products', {
                parent: 'app',
                url: '/inventories/:inventoryId/products',
                template: '<inventories-product-list></inventories-product-list>'
            })
    }]);