'use strict';

angular.module('vetForm')
    .controller('VetFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var vetId = $stateParams.vetId || 0;
        if (!vetId || vetId === 0) {
            self.vet = {};
        } else {
            $http.get("api/gateway/vets/" + $stateParams.vetId).then(function (resp) {
                self.vet = resp.data;

                document.getElementById("firstName").value = self.vet.firstName;
                document.getElementById("lastName").value = self.vet.lastName;

                let isAct = document.getElementsByClassName("isActiveRadio");
                if(self.vet.isActive === 1)
                {
                    isAct[0].checked = true;
                }
                else if(self.vet.isActive === 0)
                {
                    isAct[1].checked = true;
                }

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
            var id = self.vet.vetId;
            const specialties = JSON.parse(specialtiesStr);
            vet.specialties = specialties;
            let vetC = vet;
            var req;
            if (id) {
                req = $http.put("api/gateway/vets/" + vetId, vetC);
            } else {
                req = $http.post("api/gateway/vets", vetC);
                console.log(self.vet)
            }

            req.then(function () {
                $state.go('vets');
            }, function (response) {
                let error = "Missing fields, please fill out the form";
                alert(error);
            });
        };
    }]);
