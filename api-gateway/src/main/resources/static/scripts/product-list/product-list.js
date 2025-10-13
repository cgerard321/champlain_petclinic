'use strict';

angular.module('productList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productList', {
                parent: 'app',
                url: '/products',
                template: '<product-list></product-list>',
                controller: 'ProductController',
                controllerAs: 'self'
            })
    }]);