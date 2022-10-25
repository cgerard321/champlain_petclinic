'use strict';

angular.module('billsByOwnerId')
.controller('BillsByOwnerIdController', ['$http', '$stateParams',
    function ($http, $stateParams) {
    console.log("in billsByOwnerId, the customerId is: "+$stateParams.ownerId);
        var self = this;
        $http.get("api/gateway/bills/owner/" + ($stateParams.ownerId)).then(function (resp) {
            self.billsByOwnerId = resp.data;
        });

        $http.get("api/gateway/owners/" +($stateParams.ownerId)).then(function(resp){
            self.owner = resp.data;
        });

        $stateParams.deleteBillsByOwnerId = function (ownerId) {
            let varIsConf = confirm('You are about to delete all the bills for the owner with ownerId  ' + ownerId + '. Are you sure this is what you want to do?');
            if (varIsConf) {

                $http.delete('api/gateway/bills/owner/' + ownerId).then(successCallback, errorCallback)

                function successCallback(response) {
                    $stateParams.errors = [];
                    alert("The bills for owner with id " + ownerId + " were deleted successfully");
                    console.log(response, 'res');
                    // on success refresh bills to confirm they have all been deleted
                    $http.get("api/gateway/bills/owner/" + ($stateParams.ownerId)).then(function (resp) {
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