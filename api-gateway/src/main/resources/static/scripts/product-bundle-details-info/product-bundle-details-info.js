'use strict';

angular.module('productBundleDetailsInfo', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productBundleDetailsInfo', {
                parent: 'app',
                url: '/product-bundles/:bundleId',
                template: '<product-bundle-details-info></product-bundle-details-info>',
                controller: 'ProductBundleDetailsInfoController',
                controllerAs: 'self'
            });
    }]);
