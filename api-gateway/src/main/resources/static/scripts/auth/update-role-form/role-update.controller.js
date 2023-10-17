'use strict';

angular.module('userModule')
    .controller('UpdateUserRoleController', ['$scope', 'UserService', function($scope, UserService) {
        $scope.roles = UserService.getAvailableRoles();
        $scope.selectedRole = {};

        $scope.updateRole = function() {
            // Here, you'll need to handle the role update logic as you see fit
            $scope.onUpdate();
        };
    }]);