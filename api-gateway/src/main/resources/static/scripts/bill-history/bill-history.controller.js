'use strict';

angular.module('billHistory')
    .controller('BillHistoryController', ['$http', function ($http) {
        var self = this;

        $http.get('api/gateway/bills').then(function (resp) {
            self.billHistory = resp.data;
        });

    }]);
