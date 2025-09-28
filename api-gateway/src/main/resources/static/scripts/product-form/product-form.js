'use strict';

angular.module('productForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productNew', {
                parent: 'app',
                url: '/inventory/:inventoryId/product/new',
                params : {inventoryId: null},
               // url: '/product/new',
                template: '<product-form></product-form>'
            })

    }]);

angular.module('shopProductForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('shopProductNew', {
                parent: 'app',
                url: '/product/new',
                template: '<shop-product-form></shop-product-form>'
            })

    }]);
