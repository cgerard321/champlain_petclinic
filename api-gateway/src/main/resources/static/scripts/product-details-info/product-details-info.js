'use strict';

angular.module('productDetailsInfo', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productDetails', {
                parent: 'app',
                url: '/inventory/:inventoryId/products/:productId',
                template: '<product-details-info></product-details-info>'
            })

            .state('products', {
                parent: 'app',
                url: '/inventory/:inventoryId/products',
                template: '<inventory-product-list></inventory-product-list>'
            })
    }]);
angular.module('shopProductDetailsInfo', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('shopProductsDetails', {
                parent: 'app',
                url: '/products/:productId',
                template: '<shop-product-details-info></shop-product-details-info>'
            })
    }]);
