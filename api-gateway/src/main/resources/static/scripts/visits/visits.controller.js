'use strict';

angular.module('visits')
    .controller('VisitsController', ['$http', '$state', '$stateParams', '$filter', function ($http, $state, $stateParams, $filter) {
        var self = this;
        var petId = $stateParams.petId || 0;
        var url = "api/gateway/visit/owners/" + ($stateParams.ownerId || 0) + "/pets/" + petId + "/visits";
        var vetsUrl = "api/gateway/vets";
        var visitId = 0;
        self.practitionerId = 0;
        self.date = new Date();
        self.desc = "";

        $http.get(url).then(function (resp) {
            self.visits = resp.data;
        });

        $http.get("api/gateway/visits/"+petId).then(function (resp) {
            self.visits = resp.data;
        });

        $http.get(vetsUrl).then(function (resp) {
            self.vets = resp.data;
        });

        self.loadVetInfo = function() {
            let selectedVetsId = $("#selectedVet").val();

            let foundVet = false;
            let vetPhoneNumber = "";
            let vetEmailAddress = "";
            let vetWorkdays = "";
            let vetSpecialtiesObject = null;

            $.each(self.vets, function(i, vet) {
                if(selectedVetsId == vet.vetId) {
                    foundVet = true;
                    vetPhoneNumber = vet.phoneNumber;
                    vetEmailAddress = vet.email;
                    vetSpecialtiesObject = vet.specialties;
                    vetWorkdays = vet.workday;

                    return false;
                }
            });

            let vetSpecialties = "";
            $.each(vetSpecialtiesObject, function(i, specialty) {
                if(i < vetSpecialtiesObject.length - 1) {
                    vetSpecialties += specialty.name + ", ";
                } else {
                    vetSpecialties += specialty.name;
                }
            });

            if(foundVet) {
                $("#vetPhoneNumber").val(vetPhoneNumber);
                $("#vetEmailAddress").val(vetEmailAddress);
                $("#vetSpecialties").val(vetSpecialties);
                $("#vetWorkdays").val(vetWorkdays).trigger("change");
            } else {
                $("#vetPhoneNumber").val("");
                $("#vetEmailAddress").val("");
                $("#vetSpecialties").val("");
                $("#vetWorkdays").val("");
            }
        }

        self.getPractitionerName = function (id){
            var practitionerName = "";
            $.each(self.vets, function (i, vet){
               if (vet.vetId == id){
                   practitionerName = vet.firstName + " " + vet.lastName;
                   return false;
               }
            });
            return practitionerName;
        };

        self.switchToUpdateForm = function (practitionerId, date, description, id){
            visitId = id;
            $("#selectedVet option[value='"+practitionerId+"']").prop("selected", true);
            $('#date_input').val(date);
            $('#description_textarea').val(description);
            $('#submit_button').text("Update Visit");
            $('#cancel_button').css("visibility", "visible");
            self.loadVetInfo();
            self.submit = function () {
                var data = {
                    date: $filter('date')(self.date, "yyyy-MM-dd"),
                    description: $('#description_textarea').val(),
                    practitionerId: $("#selectedVet").val()
                };

                url = "api/gateway/owners/*/pets/" + petId + "/visits/" + visitId;
                $http.put(url, data).then(function () {
                    window.location.reload();
                }, function (response) {
                    var error = response.data;
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });
            };
        };

        self.submit = function () {
            var data = {
                date: $filter('date')(self.date, "yyyy-MM-dd"),
                description: self.desc,
                practitionerId: self.practitionerId
            };

            $http.post(url, data).then(function () {
                $state.go("owners", {ownerId: $stateParams.ownerId});
            }, function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        }
    }]);
