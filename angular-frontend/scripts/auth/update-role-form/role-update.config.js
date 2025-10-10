'use strict';

angular.module('userModule', ['ui.router'])
    .config(['$stateProvider', function($stateProvider) {
        $stateProvider
            .state('updateUserRole', {
                parent: 'app',
                url: '/users/:userId/updateRole',
                component: 'updateUserRoleComponent',
                resolve: {
                    userId: ['$stateParams', function($stateParams) {
                        return $stateParams.userId;
                    }]
                }
            });
    }]);