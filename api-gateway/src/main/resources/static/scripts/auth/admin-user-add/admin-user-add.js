'use strict';

angular.module('userNew', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('userNew', {
                parent: 'app',
                url: '/users/new',
                template: '<admin-user-add></admin-user-add>'
            })
    }]);
