'use strict';

angular.module('bundleList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('bundles', {
                parent: 'app',
                url: '/bundles',
                template: '<bundle-list></bundle-list>'
            })
            .state('deleteBundle', {
                parent: 'app',
                url: '/bundles/:bundleUUID/deleteBundle',
                template: '<bundle-list></bundle-list>'
            })
    }]);