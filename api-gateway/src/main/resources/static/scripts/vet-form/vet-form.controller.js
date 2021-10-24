'use strict';

angular.module('vetForm')
    .controller('VetFormController', ["$http", '$state', '$scope', function ($http, $state, $scope) {
        var self = this;
        var vetId = $scope.vetId || 0;
        if (!vetId || vetId === 0) {
            self.vet = {};
        } else {
            $http.get("api/gateway/vets/" + vetId).then(function (resp) {
                self.vet = resp.data;
            });
        }
        self.submitVetForm = function (vet) {
            self.vet = vet;
            var id = self.vet.vetId;
            var req;
            if (id) {
                req = $http.put("api/gateway/vets" + id, self.vet);
            } else {
                req = $http.post("api/gateway/vets", self.vet);
                console.log(self.vet)
            }

            req.then(function () {
                $state.go('vets');
            }, function (response) {
                var error = response.data;
                alert(error);
            });
        };
    }]);
