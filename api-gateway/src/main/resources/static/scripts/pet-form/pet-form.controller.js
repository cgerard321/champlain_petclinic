'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var petId = self.pet.id || 0;

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.types = resp.data;
        }).then(function () {
            if(petId)
                $http.get('api/gateway/owners/' + ownerId + "/pets/" + petId).then(function (resp) {
                    self.pet = {
                        owner: resp.data.firstName + " " + resp.data.lastName,
                        name: resp.data.pet.name,
                        birthDate: resp.data.pet.birthDate,
                    };
                    self.petTypeId = resp.data.petTypeId;
                    console.log(self.petTypeId);
                })
            else
                $http.get('api/gateway/owners/' + ownerId).then(function (resp){
                    self.pet = {
                        owner: resp.data.firstName + " " + resp.data.lastName
                    };
                    self.petTypeId = "1";
                    console.log(self.petTypeId);
                })
        });

        self.submit = function () {
            var id = self.pet.id || 0;

            var data = {
                id: id,
                name: self.pet.name,
                birthDate: self.pet.birthDate,
                typeId: self.petTypeId
            };

            console.log(data);

            var req;

            if(!id)
                req = $http.post("api/gateway/owners/" + ownerId + "/pets", data);
            else
                req = $http.delete("api/gateway/owners/" + ownerId + "/pets/" + id, data);

            req.then(function () {
                $state.go("owners/details", {ownerId: ownerId});
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
            });
        };
    }]);
