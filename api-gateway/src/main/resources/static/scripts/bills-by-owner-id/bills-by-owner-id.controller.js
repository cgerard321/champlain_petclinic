'use strict';

angular.module('billsByOwnerId')
.controller('BillsByOwnerIdController', ['$http', '$stateParams', '$scope',
    function ($http, $stateParams, $scope) {
        var self = this;
        $http.get("api/gateway/bills/customer/" + ($stateParams.customerId)).then(function (resp) {
            self.billsByOwnerId = resp.data;
        });

        $http.get("api/gateway/owners/" +($stateParams.customerId)).then(function(resp){
            self.owner = resp.data;
        });

        $scope.deleteAllBillsByOwnerId = function (customerId) {
            let varIsConf = confirm('You are about to delete all bills by owner id ' + customerId + '. Is it what you want to do ? ');
            if (varIsConf) {

                $http.delete('api/gateway/bills/customer/' + customerId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert("Bills by owner id" + customerId + " were deleted successfully");
                    console.log(response, 'res');
                    //refresh list
                    $http.get('api/gateway/bills/customer/' + customerId).then(function (resp) {
                        self.billsByOwnerId = resp.data;
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
