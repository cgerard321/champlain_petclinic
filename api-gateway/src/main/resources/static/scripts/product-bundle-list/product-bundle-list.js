'use strict';

angular.module('productBundleList', ['ui.router', 'ui.bootstrap'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productBundleList', {
                parent: 'app',
                url: '/product-bundles',
                template: '<product-bundle-list></product-bundle-list>',
                controller: 'ProductBundleListController',
                controllerAs: 'self'
            });
    }]);
