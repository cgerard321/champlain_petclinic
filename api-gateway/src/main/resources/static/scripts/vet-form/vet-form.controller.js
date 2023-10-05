'use strict';

angular.module('vetForm')
    .controller('VetFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        document.getElementById("loaderDiv").style.display = "none";

        var vetId = $stateParams.vetId || 0;
        if (!vetId || vetId === 0) {
            document.getElementById("title").innerHTML = "New Vet Sign Up";
            self.vet = {};


        } else {
            $http.get("api/gateway/vets/" + $stateParams.vetId).then(function (resp) {
                self.vet = resp.data;
                console.log(self.vet);
                document.getElementById("title").innerHTML = "Edit Vet";
                document.getElementById("firstName").value = self.vet.firstName;
                document.getElementById("lastName").value = self.vet.lastName;
                document.getElementById("email-info").style.display = "none";
                document.getElementById("vetResume").value = self.vet.resume;
                document.getElementById("user-info").style.display = "none";

                const specialties = self.vet.specialties;
                specialties.forEach(specs => {
                    document.getElementById(specs.name).checked = true;
                })

                const workdays = self.vet.workday;
                workdays.forEach(work => {
                    console.log(work);
                    document.getElementById(work).checked = true;
                });


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
        
        self.submitVetForm = function (vet = self.vet) {
            console.log("vet please: " + vet);
            document.getElementById("loaderDiv").style.display = "block";

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
            if(specialtiesStr=="[]"){
                alert("vet should have at least one specialty: "+specialtiesStr)
                return
            }

            var id = self.vet.vetId;
            const specialties = JSON.parse(specialtiesStr);
            vet.specialties = specialties;

            let workdayList = document.getElementsByClassName("workday");
            let selectedWorkdayList = [];
            for (let i = 0; i < workdayList.length; i++) {
                if (workdayList[i].checked) {
                    selectedWorkdayList.push(workdayList[i].value);
                }
            }
            let workdaysArray = selectedWorkdayList; // Convert to an array

            if (workdaysArray.length === 0) {
                alert("vet should have at least one workday: " + workdaysArray);
                return;
            }
            vet.workday = workdaysArray; // Assign the array to vet.workday

            vet.firstName = document.getElementById("firstName").value;
            var namePattern = /^[a-zA-Z -]+/;
            if (!namePattern.test(vet.firstName)) {
                alert("first name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: "+vet.firstName)
                return
            }
            if(vet.firstName.length>30||vet.firstName.length<2){
                alert("first name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: "+vet.firstName)
                return
            }

            vet.lastName = document.getElementById("lastName").value;
            if (!namePattern.test(vet.lastName)) {
                alert("last name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: "+vet.lastName)
                return
            }
            if(vet.lastName.length>30||vet.lastName.length<2){
                alert("last name should be minimum 2 characters and maximum of 30 characters, only letters, spaces, and hyphens: "+vet.lastName)
                return
            }
/*
            var emailPattern = /\b[\w.%-]+@[-.\w]+\.[A-Za-z]{2,3}\b/;
            if (!emailPattern.test(document.getElementById("email").value)) {
                alert("email should be minimum 6 characters and maximum 320 characters. Top level domain should have 2 to 3 letters: "+vet.email)
                return
            }

 */

            let basePhoneNumber = "(514)-634-8276 #";
            let inputPhoneNumber=document.getElementById("phoneNumber").value;
            if(inputPhoneNumber.length!==4){
                alert("phoneNumber length not equal to 4: "+vet.phoneNumber)
                return
            }
            vet.phoneNumber = basePhoneNumber + inputPhoneNumber;

            vet.resume = document.getElementById("vetResume").value;
            if(vet.resume.length<10){
                alert("resume should be minimum 10 characters: "+vet.resume)
                return
            }

            let isAct = document.getElementsByClassName("isActiveRadio");
            vet.active = isAct[0].checked;

            var req;
            if (id) {
                req = $http.put("api/gateway/vets/" + vetId, vet);
            } else {
                req = $http.post("api/gateway/users/vets", {
                    username: vet.username,
                    password: vet.password,
                    email: vet.email,
                    vet:vet
                });
                console.log(self.vet)
            }

            req.then(function () {
                $state.go('vets');
            }, function (response) {
                let error = "Missing fields, please fill out the form";
                $state.go('vets');
                alert(error);
            });
        };
    }]);
