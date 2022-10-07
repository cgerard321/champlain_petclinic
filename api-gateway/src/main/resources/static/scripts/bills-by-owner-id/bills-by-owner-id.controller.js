'use strict';

angular.module('billsByOwnerId')
    .controller('BillsByOwnerIdController',
        ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;

        $http.get('api/gateway/bills/customer/' + $stateParams.customerId).then(function (resp) {
            self.bills = resp.data;
        });
    }]);
