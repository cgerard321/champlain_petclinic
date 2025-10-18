'use strict';

angular.module('billsByVetId').controller('BillsByVetIdController', [
  '$http',
  '$stateParams',
  '$scope',
  function ($http, $stateParams, $scope) {
    let self = this;
    self.billsByVetId = [];

    let eventSource = new EventSource(
      'api/gateway/bills/vet/' + $stateParams.vetId
    );
    eventSource.addEventListener('message', function (event) {
      $scope.$apply(function () {
        self.billsByVetId.push(JSON.parse(event.data));
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

    // $http.get("api/gateway/bills/vet/" + ($stateParams.vetId)).then(function (resp) {
    //     self.billsByVetId = resp.data;
    // });
    //
    // $http.get("api/gateway/vets/" +($stateParams.vetId)).then(function(resp){
    //     self.vet = resp.data;
    // });
    $scope.deleteBillsByVetId = function (vetId) {
      let varIsConf = confirm(
        'You are about to all bills by vet ' +
          vetId +
          '. Is it what you want to do ? '
      );
      if (varIsConf) {
        $http.delete('api/gateway/bills/vet/' + vetId).then(
          function successCallback() {
            $scope.errors = [];
            alert(vetId + ' bills were deleted successfully');
            // console.log removed(response, 'res');
            //refresh list
            $http.get('api/gateway/bills/vet/' + vetId).then(function (resp) {
              self.billsByVetId = resp.data;
              arr = resp.data;
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
