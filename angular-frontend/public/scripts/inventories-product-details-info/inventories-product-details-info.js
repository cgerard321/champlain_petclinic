'use strict';

angular.module('inventoriesProductDetailsInfo', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('inventoriesProductDetails', {
                parent: 'app',
                url: '/inventories/:inventoryId/products/:productId/details',
                template: '<inventories-product-details-info></inventories-product-details-info>'
            })
    }]);