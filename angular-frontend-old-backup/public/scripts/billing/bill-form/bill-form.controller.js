'use strict';

angular.module('billForm').controller('BillFormController', [
  '$http',
  '$state',
  function ($http, $state) {
    var self = this;
    //for vets dropdown
    $http.get('api/gateway/vets').then(function (resp) {
      self.vetList = resp.data;
      arr = resp.data;
    });
    //for owner dropdown
    $http.get('api/gateway/owners').then(function (owners) {
      self.owners = owners.data;
      // console.log removed
    });

    self.submitBillForm = function () {
      var req;
      req = $http.post('api/gateway/bills', self.bill);

      req.then(
        function () {
          $state.go('bills');
        },
        function (response) {
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
        }
      );
    };
  },
]);
