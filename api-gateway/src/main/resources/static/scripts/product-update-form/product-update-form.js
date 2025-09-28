'use strict';

angular.module('productUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider

            .state('updateProductInventory', {
                parent: 'app',
                url: '/inventories/:inventoryId/products/:productId',
                template: '<product-update-form></product-update-form>'
            })

    }]);

angular.module('shopProductUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider

            .state('updateProduct', {
                parent: 'app',
                url: '/products/:productId',
                template: '<shop-product-update-form></shop-product-update-form>'
            })

    }]);
