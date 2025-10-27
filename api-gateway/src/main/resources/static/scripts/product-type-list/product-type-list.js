'use strict';

angular.module('productTypeList', ['ui.router', 'ui.bootstrap'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productTypeList', {
                parent: 'app',
                url: '/products/types',
                template: '<product-type-list></product-type-list>',
                controller: 'ProductTypeController',
                controllerAs: 'self'
            })
    }]);