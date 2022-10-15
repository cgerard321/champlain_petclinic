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

    }]);