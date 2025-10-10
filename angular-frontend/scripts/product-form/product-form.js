'use strict';

angular.module('shopProductForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('shopProductNew', {
                parent: 'app',
                url: '/product/new',
                template: '<shop-product-form></shop-product-form>'
            })

    }]);
