'use strict';

angular.module('customerRegister')
    .controller('CustomerRegisterController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var customerId = $stateParams.customerId || "";
        if (!customerId) {
            self.customer = {};
            self.checked = false
        } else {
            $http.get("api/gateway/customers/" + customerId).then(function (resp) {
                self.customer = resp.data;
            });

        }


    }]);

