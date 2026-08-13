'use strict';

angular.module('productBundleForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('productBundleForm', {
                parent: 'app',
                url: '/product-bundles/add',
                template: '<product-bundle-form></product-bundle-form>',
                controller: 'ProductBundleFormController',
                controllerAs: '$ctrl'
            });
    }]);
