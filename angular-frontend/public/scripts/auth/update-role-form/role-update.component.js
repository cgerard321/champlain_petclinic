'use strict';

angular.module('userModule')
    .component('updateUserRoleComponent', {
        templateUrl: 'scripts/auth/update-role-form/role-update.template.html',
        controller: 'UpdateUserRoleController',
        bindings: {
            userId: '<'
        }
    });