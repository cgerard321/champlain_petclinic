'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var method = $stateParams.method;
        var petId = $stateParams.petId || 0;
        var owner = "";
        var myDate = new Date();


        $http.get('api/gateway/owners' + ownerId).then(function (resp){
            owner = resp.data.firstName + " " + resp.data.lastName;
        })

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.types = resp.data;
        }).then(function () {
            if(method == 'delete')
                $http.get('api/gateway/owners/' + ownerId + "/pets/" + petId).then(function (resp) {
                    self.pet = {
                        owner: owner,
                        name: resp.data.name,
                        birthDate: myDate = $filter(resp.data.birthDate)(myDate, 'YYYY-MM-DD'),
                    };
                    self.petTypeId = resp.data.petTypeId;
                    console.log(self.pet);
                    console.log(self.petTypeId);
                })
            else
                $http.get('api/gateway/owners/' + ownerId).then(function (resp) {
                    self.pet = {
                        owner: owner
                    };
                    self.petTypeId = "0";
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
    }]);
