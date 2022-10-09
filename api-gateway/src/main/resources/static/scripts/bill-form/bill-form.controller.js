'use strict';

angular.module('billForm')
    .controller('BillFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var billId = $stateParams.ownerId || 0;
        var method = $stateParams.method;

        if (!billId) {
            self.bill = {};
            self.checked = false
        } else {
            $http.get("api/gateway/bills/" + billId).then(function (resp) {
                self.bill = resp.data;
            });
            if(method == 'edit')
                self.checked = false
            else
                self.checked = true
        }

        self.submitBillForm = function () {
            var id = self.bill.id;
            console.log(self.bill);
            var req;
            if (id){
                if(method == 'edit')
                    req = $http.put("api/gateway/bills/" + id, self.bill);
                else
                    req = $http.delete("api/gateway/bills/" + id, self.bill)
            }
            else
                req = $http.post("api/gateway/bills", self.bill);

            req.then(function () {
                $state.go('bills');
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);
