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
        self.submitVetForm = function (vet)
        {
            let specialtyList = document.getElementsByClassName("specialty")
            let selectedSpecialtiesList = [];
            let specialtiesStr = "[";
            for(let i = 0; i<specialtyList.length; i++)
            {
                if(specialtyList[i].checked)
                {
                    selectedSpecialtiesList.push(specialtyList[i])
                }

            }
            for(let i = 0; i<selectedSpecialtiesList.length; i++)
            {
                if(selectedSpecialtiesList[i].checked)
                {
                    specialtiesStr += selectedSpecialtiesList[i].value;
                        if(i !== selectedSpecialtiesList.length - 1)
                            specialtiesStr += ", ";
                }

            }
            specialtiesStr += "]"
            // console.log(specialties);
            const specialties = JSON.parse(specialtiesStr);
            vet.specialties = specialties;
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
