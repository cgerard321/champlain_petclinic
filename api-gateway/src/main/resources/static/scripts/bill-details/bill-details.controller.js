'use strict';

angular.module('billDetails')
    .controller('BillDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;
        var customerId = 0;

        $http.get('api/gateway/bills/' + $stateParams.billId).then(function (resp) {
            self.bills = resp.data;
        });
        $http.get('api/gateway/owners/' + $stateParams.ownerId).then(function (resp) {
            self.owner = resp.data;
        });

    }]);
