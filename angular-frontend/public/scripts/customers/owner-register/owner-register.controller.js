'use strict';

angular.module('ownerRegister').controller('OwnerRegisterController', [
  '$http',
  '$state',
  '$stateParams',
  function ($http, $state, $stateParams) {
    var self = this;
    var ownerId = $stateParams.ownerId || '';
    if (!ownerId) {
      self.owner = {};
      self.checked = false;
    } else {
      $http.get('api/gateway/owners/' + ownerId).then(function (resp) {
        self.owner = resp.data;
      });
    }
  },
]);
