'use strict';

angular.module('rolesDetails', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('rolesDetails', {
                parent: 'app',
                url: '/roles',
                template: '<roles-details></roles-details>'
            })
    }]);