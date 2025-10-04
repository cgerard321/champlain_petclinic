'use strict';


angular.module('shopProductDetailsInfo', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('shopProductsDetails', {
                parent: 'app',
                url: '/products/:productId',
                template: '<shop-product-details-info></shop-product-details-info>'
            })
    }]);
