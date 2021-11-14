'use strict';

angular.module('ownerForm')
    .controller('OwnerFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var method = $stateParams.method;

        if (!ownerId) {
            self.owner = {};
            self.checked = false
        } else {
            $http.get("api/gateway/owners/" + ownerId).then(function (resp) {
                self.owner = resp.data;
            });
            if(method == 'edit')
                self.checked = false
            else
                self.checked = true
        }

        self.submitOwnerForm = function () {
            var id = self.owner.id;
            console.log(self.owner);
            var req;
            if (id){
                if(method == 'edit')
                    req = $http.put("api/gateway/owners/" + id, self.owner);
                else
                    req = $http.delete("api/gateway/owners/" + id, self.owner)
            }
            else
                req = $http.post("api/gateway/owners", self.owner);

            req.then(function () {
                $state.go('owners');
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);
