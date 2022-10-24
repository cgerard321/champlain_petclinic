'use strict';

angular.module('bundleForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('bundleNew', {
                parent: 'app',
                url: '/bundle/new',
                template: '<bundle-form></bundle-form>'
            })

    }]);