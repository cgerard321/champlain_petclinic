angular.module('billUpdateForm').controller('BillUpdateFormController', [
  '$http',
  '$state',
  '$stateParams',
  function ($http, $state, $stateParams) {
    var self = this;
    var billId = $stateParams.billId || '';
    var method = $stateParams.method;

    if (!billId) {
      self.bill = {}; // Initialize an empty bill object for creating a new bill
    } else {
      // Fetch the bill details using $http (similar to owner details)
      // Populate self.bill with the fetched data
      $http.get('api/gateway/bills/' + billId).then(function (resp) {
        self.bill = resp.data;
      });
    }

    self.submitBillForm = function () {
      var id = self.bill.billId;
      var req;

      if (id) {
        if (method == 'edit') {
          // Handle updating an existing bill
          req = $http.put('api/gateway/bills/' + id, self.bill);
        } else {
          // Handle other methods (e.g., delete)
          req = $http.delete('api/gateway/bills/' + id);
        }
      } else {
        // Handle creating a new bill
        req = $http.post('api/gateway/bills', self.bill);
      }

      req.then(
        function () {
          // Redirect to the appropriate page after successful submission
          $state.go('bills');
        },
        function (response) {
          // Handle error response if needed
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
