'use strict';

angular.module('billsByOwnerId').controller('BillsByOwnerIdController', [
  '$http',
  '$stateParams',
  '$scope',
  function ($http, $stateParams, $scope) {
    let self = this;
    self.billsByOwnerId = [];

    let eventSource = new EventSource(
      'api/gateway/bills/customer/' + $stateParams.customerId
    );
    eventSource.addEventListener('message', function (event) {
      $scope.$apply(function () {
        self.billsByOwnerId.push(JSON.parse(event.data));
      });
    });
    eventSource.onerror = () => {
      if (eventSource.readyState === 0) {
        eventSource.close();
        // console.log removed('Event source was closed by server succesfully. ' + error);
      } else {
        // console.log removed('EventSource error: ' + error);
      }
    };

    // $http.get("api/gateway/bills/customer/" + ($stateParams.customerId)).then(function (resp) {
    //     self.billsByOwnerId = resp.data;
    // });
    //
    // $http.get("api/gateway/owners/" +($stateParams.customerId)).then(function(resp){
    //     self.owner = resp.data;
    // });

    $stateParams.deleteBillsByOwnerId = function (customerId) {
      let varIsConf = confirm(
        'You are about to delete all the bills for the owner with ownerId  ' +
          customerId +
          '. Are you sure this is what you want to do?'
      );
      if (varIsConf) {
        $http.delete('api/gateway/bills/customer/' + customerId).then(
          function successCallback() {
            $stateParams.errors = [];
            alert(
              'The bills for owner with id ' +
                customerId +
                ' were deleted successfully'
            );
            // console.log removed(response, 'res');
            // on success refresh bills to confirm they have all been deleted
            $http
              .get('api/gateway/bills/customer/' + $stateParams.customerId)
              .then(function (resp) {
                self.billsByOwnerId = resp.data;
              });
          },
          function errorCallback() {
            alert(data.errors);
            // console.log removed(error, 'Could not receive data');
          }
        );
      }
    };
  },
]);
