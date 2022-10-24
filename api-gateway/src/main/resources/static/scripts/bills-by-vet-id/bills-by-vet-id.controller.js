'use strict';

angular.module('billsByVetId')
.controller('BillsByVetIdController', ['$http', '$stateParams', '$scope',
    function ($http, $stateParams, $scope) {
        var self = this;
        $http.get("api/gateway/bills/vet/" + ($stateParams.vetId)).then(function (resp) {
            self.billsByVetId = resp.data;
        });

        $http.get("api/gateway/vets/" +($stateParams.vetId)).then(function(resp){
            self.vet = resp.data;
        });
        $scope.deleteBillsByVetId = function (vetId) {
            let varIsConf = confirm('You are about to all bills by vet ' + vetId + '. Is it what you want to do ? ');
            if (varIsConf) {

                $http.delete('api/gateway/bills/vet/' + vetId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert(billId + " bills were deleted successfully");
                    console.log(response, 'res');
                    //refresh list
                    $http.get('api/gateway/bills/vet/' + vetId).then(function (resp) {
                        self.billsByVetId = resp.data;
                        arr = resp.data;
                    });
                }

                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'Could not receive data');
                }
            }
        };
    }]);