'use strict';

angular.module('petOwnerDetails').controller('PetOwnerDetailsController', [
  '$http',
  '$state',
  '$stateParams',
  function ($http, $state, $stateParams) {
    var self = this;

    $http
      .get('api/gateway/owners/' + $stateParams.ownerId + '/pets/' + 5)
      .then(function (resp) {
        // console.log removed('Owner id is ' + $stateParams.ownerId);
        // console.log removed(resp.data);
        self.owner = resp.data;
      });
  },
]);
