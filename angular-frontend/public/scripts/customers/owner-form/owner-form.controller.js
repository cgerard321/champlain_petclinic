'use strict';

angular.module('ownerForm').controller('OwnerFormController', [
  '$http',
  '$state',
  '$stateParams',
  function ($http, $state, $stateParams) {
    var self = this;
    var ownerId = $stateParams.ownerId || '';
    var method = $stateParams.method;

    // Initialize
    self.owner = {};
    self.checked = false;
    self.showModal = false;

    if (ownerId) {
      $http.get('api/gateway/owners/' + ownerId).then(function (resp) {
        self.owner = resp.data;
      });
      if (method !== 'edit') {
        self.checked = true;
      }
    }

    // Open confirmation modal
    self.submitOwnerForm = function () {
      self.showModal = true;
    };

    // Cancel modal
    self.cancelModal = function () {
      self.showModal = false;
    };

    // Confirm modal: submit form
    self.confirmModal = function () {
      self.showModal = false;

      var req;
      if (self.owner.ownerId) {
        if (method === 'edit') {
          req = $http.put(
            'api/gateway/owners/' + self.owner.ownerId,
            self.owner
          );
        } else {
          req = $http.delete('api/gateway/owners/' + self.owner.ownerId);
        }
      } else {
        req = $http.post('api/gateway/owners', self.owner);
      }

      req
        .then(function () {
          $state.go('owners');
        })
        .catch(function (response) {
          var error = response.data;
          error.errors = error.errors || [];
          alert(
            error.error +
              '\r\n' +
              error.errors
                .map(function (e) {
                  return e.field + ': ' + e.defaultMessage;
                })
                .join('\r\n')
          );
        });
    };
  },
]);
