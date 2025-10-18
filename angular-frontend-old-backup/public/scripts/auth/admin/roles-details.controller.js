'use strict';

angular.module('rolesDetails').controller('RolesDetailsController', [
  '$http',
  '$scope',
  'authProvider',
  function ($http, $scope, authProvider) {
    var self = this;

    $http
      .get('api/gateway/admin/roles', {
        headers: { Authorization: 'Bearer ' + authProvider.getUser().token },
      })
      .then(function (resp) {
        self.roles = resp.data;
      });

    this.delete = function (roleId) {
      $http
        .delete('api/gateway/admin/roles/' + roleId, {
          headers: { Authorization: 'Bearer ' + authProvider.getUser().token },
        })
        .then(function () {
          $http
            .get('api/gateway/admin/roles', {
              headers: {
                Authorization: 'Bearer ' + authProvider.getUser().token,
              },
            })
            .then(function (resp) {
              self.roles = resp.data;
            });
        });
    };

    this.add = function () {
      var please = parseInt(this.parent);
      $http
        .post(
          'api/gateway/admin/roles/',
          {
            id: null,
            name: this.name,
            parent: please,
          },
          {
            headers: {
              Authorization: 'Bearer ' + authProvider.getUser().token,
            },
          }
        )
        .then(function () {
          $http
            .get('api/gateway/admin/roles', {
              headers: {
                Authorization: 'Bearer ' + authProvider.getUser().token,
              },
            })
            .then(function (resp) {
              self.roles = resp.data;
            });
        });
    };
  },
]);
