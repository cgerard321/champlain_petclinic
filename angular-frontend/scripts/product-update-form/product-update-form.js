'use strict';



angular.module('shopProductUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider

            .state('updateProduct', {
                parent: 'app',
                url: '/products/:productId',
                template: '<shop-product-update-form></shop-product-update-form>'
            })

    }]);
