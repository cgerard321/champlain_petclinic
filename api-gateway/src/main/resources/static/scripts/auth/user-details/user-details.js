'use strict';

angular.module('userDetails', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('userDetails', {
                parent: 'app',
                url: '/users/details/:userId',
                params: {userId: null},
                template: '<user-details></user-details>'
            })
    }]);