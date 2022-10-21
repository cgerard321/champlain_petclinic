'use strict';

angular.module('billsByVetId')
    .controller('BillsByVetIdController', ['$http', '$stateParams',
        function ($http, $stateParams) {
            var self = this;
            $http.get("api/gateway/bills/vetId/" + ($stateParams.vetId)).then(function (resp) {
                self.billsByVetId = resp.data;
            });

            $http.get("api/gateway/vets/details/" +($stateParams.vetId)).then(function(resp){
                self.vet = resp.data;
            });

        }]);