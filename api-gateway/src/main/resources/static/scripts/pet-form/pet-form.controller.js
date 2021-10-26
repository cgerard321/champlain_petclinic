'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var method = $stateParams.method;
        var petId = $stateParams.petId || 0;
        var petType;

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.types = resp.data;
        }).then(function () {
            if(method == 'delete')
                $http.get('api/gateway/owners/' + ownerId + "/pets/" + petId).then(function (resp) {
                    var ownerName = self.owner = {
                        name: resp.data.firstName + " " + resp.data.lastName
                    }
                    self.pet = {
                        owner: ownerName,
                        name: resp.data.name,
                        birthDate: resp.data.birthDate,
                    };
                    self.petTypeId = resp.data.petTypeId.typeId;
                    console.log(self.pet);
                    console.log(self.petTypeId);
                })
            else
                $http.get('api/gateway/owners/' + ownerId).then(function (resp) {
                    self.pet = {
                        owner: resp.data.firstName + " " + resp.data.lastName,
                    };
                })
        });

        self.submit = function () {
            var id = self.pet.id || 0;

            var data = {
                id: id,
                name: self.pet.name,
                birthDate: self.pet.birthDate,
                owner: ownerId,
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

        self.selectType = function (id) {
            petType = id;
            console.log(petType);
        };
    }]);
