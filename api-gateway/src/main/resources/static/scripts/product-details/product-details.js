'use strict';

angular.module('productDetails', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productDetails', {
                parent: 'app',
                url: '/inventory/:inventoryId/products/details/:productId',
                template: '<product-details></product-details>'
            })
    }]);