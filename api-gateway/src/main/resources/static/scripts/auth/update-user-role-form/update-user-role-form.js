'use strict';

angular.module('updateUserRoleForm', ['ui.router'])
    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
            .state('updateRole', {
                parent: 'app',
                url: '/users/:userId/:method',
                template: '<update-user-role-form></update-user-role-form>'
            })
    }]);