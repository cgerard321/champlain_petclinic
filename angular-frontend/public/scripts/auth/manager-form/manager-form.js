'use strict';

angular.module('managerForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('managerForm', {
                parent: 'app',
                url: '/manager',
                template: '<manager-form></manager-form>'
            })
    }]);