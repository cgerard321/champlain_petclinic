'use strict';

angular.module('userModule').controller('UpdateUserRoleController', [
  '$scope',
  '$http',
  'UserService',
  '$state',
  function ($scope, $http, UserService, $state) {
    var ctrl = this; // Capture the controller instance

    $scope.roles = UserService.getAvailableRoles();
    $scope.selectedRole = {};

    // Use $onInit lifecycle hook to set $scope.userId
    ctrl.$onInit = function () {
      $scope.userId = ctrl.userId;
    };

    $scope.selectedRoles = {};

    $scope.updateRole = function () {
      var rolesList = [];
      for (var role in $scope.selectedRoles) {
        if ($scope.selectedRoles[role]) {
          rolesList.push(role);
        }
      }

      if (rolesList.length === 0) {
        alert('Please select at least one role.');
        return;
      }

      if (rolesList.length > 1) {
        alert('Please select only one role.');
        return;
      }

      var rolesChangeRequest = {
        roles: rolesList,
      };

      // Send the PATCH request
      $http({
        method: 'PATCH',
        url: 'api/gateway/users/' + $scope.userId,
        data: rolesChangeRequest,
        headers: {
          'Content-Type': 'application/json',
          // Add token headers if needed
        },
      }).then(
        function successCallback() {
          alert('Roles updated successfully!');
          $state.go('AdminPanel');
        },
        function errorCallback(response) {
          alert('Failed to update roles. ' + response.data.message);
        }
      );
    };
  },
]);
