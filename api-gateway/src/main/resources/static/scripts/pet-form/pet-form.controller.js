'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var method = $stateParams.method;
        var petId = $stateParams.petId || 0;
        var owner = "";
        var myDate = new Date();
      
        $http.get('api/gateway/owners/' + ownerId).then(function (resp){
            owner = resp.data.firstName + " " + resp.data.lastName;
        })

        $http.get('api/gateway/owners/petTypes').then(function (resp) {
            self.types = resp.data;
            console.log(self.types);
        }).then(function () {
            if(method == 'delete')
                $http.get('api/gateway/owners/' + ownerId + "/pets/" + petId).then(function (resp) {
                    myDate = new Date(Date.parse(resp.data.birthDate))
                    self.petType = {
                        id: resp.data.type.id,
                        name: resp.data.type.name
                    }

                    self.pet = {
                        owner: owner,
                        name: resp.data.name,
                        birthDate: myDate,
                        type: self.petType
                    }

                    self.checked = true
                    /*console.log(self.pet);
                    console.log(self.pet.type);*/
                })
            else
                $http.get('api/gateway/owners/' + ownerId).then(function (resp) {
                    self.pet = {
                        owner: owner
                    }

                    self.checked = false
                })
        });

        self.submit = function () {
            var petType = {
                id: self.pet.type   ,
                name: self.pet.type.name
            }

            /*console.log(temp)*/
            var data = {
                id: petId,
                name: self.pet.name,
                birthDate: self.pet.birthDate,
                owner: ownerId,
                type: petType
            }
            /*console.log(data);*/
            var req;

            if(method != 'delete')
                req = $http.post("api/gateway/owners/" + ownerId + "/pets", data);
            else
                req = $http.delete("api/gateway/owners/" + ownerId + "/pets/" + petId, data);

            req.then(function () {
                $state.go('ownerDetails', {ownerId: ownerId})
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
            });
        };
    }]);
