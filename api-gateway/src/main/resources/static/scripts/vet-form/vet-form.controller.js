'use strict';

angular.module('vetForm')
    .controller('VetFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var vetId = $stateParams.vetId || 0;
        if (!vetId || vetId === 0) {
            document.getElementById("title").innerHTML = "New Vet Sign Up";
            self.vet = {};

        } else {
            $http.get("api/gateway/vets/" + $stateParams.vetId).then(function (resp) {
                self.vet = resp.data;
                document.getElementById("title").innerHTML = "Edit Vet";
                document.getElementById("firstName").value = self.vet.firstName;
                document.getElementById("lastName").value = self.vet.lastName;
                document.getElementById("email").value = self.vet.email;
                document.getElementById("vetResume").value = self.vet.resume;
                document.getElementById("workDays").value = self.vet.workday;

                const specialties = self.vet.specialties;
                specialties.forEach(specs => {
                    document.getElementById(specs.name).checked = true;
                })

                let isAct = document.getElementsByClassName("isActiveRadio");
                if (self.vet.active) {
                    isAct[0].checked = true;
                } else {
                    isAct[1].checked = true;
                }
                let phoneNumber = self.vet.phoneNumber;
                let code = phoneNumber.substring(phoneNumber.length - 4);
                document.getElementById("phoneNumber").value = code;

            });
        }
        self.submitVetForm = function (vet) {
            let specialtyList = document.getElementsByClassName("specialty")
            let selectedSpecialtiesList = [];
            let specialtiesStr = "[";
            for (let i = 0; i < specialtyList.length; i++) {
                if (specialtyList[i].checked) {
                    selectedSpecialtiesList.push(specialtyList[i])
                }

            }
            for (let i = 0; i < selectedSpecialtiesList.length; i++) {
                if (selectedSpecialtiesList[i].checked) {
                    specialtiesStr += selectedSpecialtiesList[i].value;
                    if (i !== selectedSpecialtiesList.length - 1)
                        specialtiesStr += ", ";
                }
            }
            specialtiesStr += "]"
            var id = self.vet.vetId;
            const specialties = JSON.parse(specialtiesStr);
            vet.specialties = specialties;

            vet.firstName = document.getElementById("firstName").value;
            vet.lastName = document.getElementById("lastName").value;
            vet.email = document.getElementById("email").value;
            vet.resume = document.getElementById("vetResume").value;
            vet.workday = document.getElementById("workDays").value;

            let basePhoneNumber = "(514)-634-8276 #";
            vet.phoneNumber = basePhoneNumber + document.getElementById("phoneNumber").value;

            let isAct = document.getElementsByClassName("isActiveRadio");
            vet.active = isAct[0].checked;

            var req;
            if (id) {
                req = $http.put("api/gateway/vets/" + vetId, vet);
            } else {
                req = $http.post("api/gateway/vets", vet);
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
