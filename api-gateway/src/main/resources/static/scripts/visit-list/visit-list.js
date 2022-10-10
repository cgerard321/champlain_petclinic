'use strict';

angular.module('visitList', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('visitList', {
                parent: 'app',
                url: '/visitList',
                template: '<visit-list></visit-list>'
            })
    }]);
