'use strict';

angular.module('vetForm')
    .controller('VetFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        var vetId = $stateParams.vetId || 0;

        if (!vetId) {
            self.vet = {};
        } else {
            $http.get("api/gateway/vets/" + vetId).then(function (resp) {
                self.vet = resp.data;
            });
        }

        self.submitVetForm = function () {
            var id = self.vet.id;
            var req;
            if (id) {
                req = $http.put("api/gateway/vets" + id, self.vet);
            } else {
                req = $http.post("api/gateway/vets", self.vet);
            }

            req.then(function () {
                $state.go('vets');
            }, function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
            });
        };
    }]);
