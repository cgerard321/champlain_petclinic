'use strict';
angular.module('adminPanel').controller('AdminPanelController', [
  '$http',
  '$scope',
  'authProvider',
  '$window',
  function ($http, $scope, authProvider, $window) {
    var self = this;
    self.users = [];

    let eventSource = new EventSource('api/gateway/users');
    eventSource.addEventListener('message', function (event) {
      $scope.$apply(function () {
        // console.log removed
        self.users.push(JSON.parse(event.data));
      });
    });
    eventSource.onerror = () => {
      if (eventSource.readyState === 0) {
        eventSource.close();
        // console.log removed
      } else {
        // console.log removed
      }
    };

    $scope.search = function () {
      if ($scope.query === '') {
        $http
          .get('api/gateway/users', {
            headers: {
              Authorization: 'Bearer ' + authProvider.getUser().token,
            },
          })
          .then(function (resp) {
            self.users = resp.data;
          });
      } else {
        $http
          .get('api/gateway/users', {
            params: { username: $scope.query },
            headers: {
              Authorization: 'Bearer ' + authProvider.getUser().token,
            },
          })
          .then(function (resp) {
            self.users = resp.data;
          });
      }
    };

    $scope.removeUser = function (userid) {
      $http
        .delete('api/gateway/users/' + userid, {
          headers: { Authorization: 'Bearer ' + authProvider.getUser().token },
        })
        .then(function () {
          $http
            .get('api/gateway/users', {
              headers: {
                Authorization: 'Bearer ' + authProvider.getUser().token,
              },
            })
            .then(function (resp) {
              self.users = resp.data;
              alert('User has been deleted successfully.');
              $window.location.reload();
            });
        });
    };
  },
]);
