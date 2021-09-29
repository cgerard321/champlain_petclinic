'use strict';

// Configure a module for the bill history page
angular.module('billHistory')
    // Configure a controller while passing BillHistoryController as a provider and a HTTP GET protocol as a method
    .controller('BillHistoryController', ['$http', function ($http) {
        var self = this;

        // Outline the GET URL and then instantiate the bills for the controller to the repository data
        $http.get('api/gateway/bill/history').then(function (resp) {
            self.bills = resp.data;
        });

    }]);
