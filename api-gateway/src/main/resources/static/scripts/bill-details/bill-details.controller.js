'use strict';

angular.module('billDetails')
    .controller('BillDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;

        $http.get('api/gateway/bill/' + $stateParams.billId).then(function (resp) {
            self.bills = resp.data;
        });

        $http.delete('api/gateway/bill/' + $stateParams.billId).then(function (resp) {
            self.bills = resp.data;
        });
    }]);
