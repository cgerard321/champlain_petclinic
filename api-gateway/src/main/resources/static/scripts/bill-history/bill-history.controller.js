'use strict';

angular.module('billHistory')
    .controller('BillHistoryController', ['$http', function ($http) {
        var self = this;

        $http.get('api/gateway/bill/history').then(function (resp) {
            self.bills = resp.data;
        });

    }]);
