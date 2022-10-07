'use strict';

angular.module('billsByOwnerId')
    .controller('BillHistoryController', ['$http','$scope', function ($http,$scope) {
        var self = this;

        $http.get('api/gateway/bills/customer/' + $scope.customerId).then(function (resp) {
            self.bills = resp.data;
        });
    }]);
