'use strict';

angular.module('petForm')
    .controller('PetFormController', ['$http', '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || 0;
        var method = $stateParams.method;
        var petId = $stateParams.petId || 0;
        var owner = "";
        var myDate = new Date();

        $http.get('api/gateway/petTypes').then(function (resp) {
            self.types = resp.data;
        });
        // Fetch owner data and initialize the form
        $http.get('api/gateway/owners/' + ownerId).then(function (resp) {
            owner = resp.data.firstName + " " + resp.data.lastName;
        }).then(function () {
            if (method == 'delete') {
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

                    self.checked = true;
                });
            } else {
                $http.get('api/gateway/owners/' + ownerId).then(function (resp) {
                    self.pet = {
                        owner: owner
                    }

                    self.checked = false;
                });
            }
        });

        // Function to submit the form
        self.submit = function () {
            var petType = {
                id: self.pet.type.id,
                name: self.pet.type.name
            }

            var data = {
                id: petId,
                name: self.pet.name,
                birthDate: self.pet.birthDate,
                owner: ownerId,
                type: petType
            }

            var req;

            if (method == 'edit') {
                req = $http.put("api/gateway/owners/" + ownerId + "/pets/" + petId, data);
            } else if (method == 'delete') {
                req = $http.delete("api/gateway/owners/" + ownerId + "/pets/" + petId);
            } else {
                req = $http.post("api/gateway/owners/" + ownerId + "/pets", data);
            }

            req.then(function () {
                $state.go('ownerDetails', { ownerId: ownerId });
            }).catch(function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };
    }]);
