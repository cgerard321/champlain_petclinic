'use strict';

angular.module('productBundleUpdateForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productBundleUpdateForm', {
                parent: 'app',
                url: '/product-bundles/:bundleId/update',
                template: '<product-bundle-update-form></product-bundle-update-form>',
                controller: 'ProductBundleUpdateFormController',
                controllerAs: 'self'
            });
    }]);
