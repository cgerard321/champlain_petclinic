'use strict';

angular.module('billsByOwnerId')
.controller('BillsByOwnerIdController', ['$http', '$stateParams',
    function ($http, $stateParams) {
        var self = this;
        $http.get("api/gateway/bills/customer/" + ($stateParams.customerId)).then(function (resp) {
            self.billsByOwnerId = resp.data;
        });

        $http.get("api/gateway/owners/" +($stateParams.customerId)).then(function(resp){
            self.owner = resp.data;
        });

        $stateParams.deleteBillsByOwnerId = function (customerId) {
            let varIsConf = confirm('You are about to delete all the bills for the owner with ownerId  ' + customerId + '. Are you sure this is what you want to do?');
            if (varIsConf) {

                $http.delete('api/gateway/bills/customer/' + customerId).then(successCallback, errorCallback)

                function successCallback(response) {
                    $stateParams.errors = [];
                    alert("The bills for owner with id " + customerId + " were deleted successfully");
                    console.log(response, 'res');
                    // on success refresh bills to confirm they have all been deleted
                    $http.get("api/gateway/bills/customer/" + ($stateParams.customerId)).then(function (resp) {
                        self.billsByOwnerId = resp.data;
                    });
                }

                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'Could not receive data');
                }
            }
        };

    }]);