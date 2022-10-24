'use strict';

angular.module('billsByVetId')
.controller('BillsByVetIdController', ['$http', '$stateParams',
    function ($http, $stateParams) {
        var self = this;
        $http.get("api/gateway/bills/vet/" + ($stateParams.vetId)).then(function (resp) {
            self.billsByVetId = resp.data;
        });

        $http.get("api/gateway/vets/" +($stateParams.vetBillId)).then(function(resp){
            self.vet = resp.data;
        });

    }]);