'use strict';

angular.module('billDetails')
    .controller('BillDetailsController', ['$http', '$stateParams', function ($http, $stateParams) {
        var self = this;

        $http.get('api/gateway/bills/' + $stateParams.billId).then(function (resp) {
            self.bills = resp.data;
        });
    }]);
