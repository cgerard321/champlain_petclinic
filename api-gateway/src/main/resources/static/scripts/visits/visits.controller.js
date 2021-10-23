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

        $http.get("api/gateway/visits/"+petId).then(function (resp) {
            self.visits = resp.data;
            self.sortFetchedVisits();
        });

        self.sortFetchedVisits = function() {
            let dateObj = new Date();
            var dd = String(dateObj.getDate()).padStart(2, '0');
            var mm = String(dateObj.getMonth() + 1).padStart(2, '0');
            var yyyy = dateObj.getFullYear();
            let currentDate = Date.parse(yyyy + '-' + mm + '-' + dd);

            self.upcomingVisits = [];
            self.previousVisits = [];

            $.each(self.visits, function(i, visit) {
                let selectedVisitDate = Date.parse(visit.date);

                if(selectedVisitDate >= currentDate) {
                    self.upcomingVisits.push(visit);
                } else {
                    self.previousVisits.push(visit);
                }
            });
        }

        self.getVisitsForPractitionerIdAndMonth = function() {
            let pIdAndMonth = localStorage.getItem("practitionerIdAndMonth");

            if(pIdAndMonth != null && pIdAndMonth !== "") {
                let info = pIdAndMonth.split(",");

                if(info[0] !== undefined){
                    let practitionerId = parseInt(info[0]);
                    let startDate = info[1];
                    let endDate = info[2];

                    $http.get("api/gateway/visits/calendar/" + practitionerId + "?dates=" + startDate + "," + endDate).then(function (resp) {
                        self.availableVisits = resp.data;
                        availabilities = [];

                        $.each(self.availableVisits, function(i, visit) {
                            let date = visit.date.toString().split("-");

                            availabilities.push(parseInt(date[2]));
                        });

                        renderCalendar();
                    });
                }
            }
        }

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

        let ResetSortButtonArrows = function(isForUpcoming) {
            if(isForUpcoming) {
                $('#sortByDateButtonUpcomingVisits').text("Sort by date ⇅");
                $('#sortByDescButtonUpcomingVisits').text("Sort by description ⇅");
                $('#sortByVetButtonUpcomingVisits').text("Sort by veterinarian ⇅");
                $('#sortByStatusButtonUpcomingVisits').text("Sort by status ⇅");
            } else {
                $('#sortByDateButtonPreviousVisits').text("Sort by date ⇅");
                $('#sortByDescButtonPreviousVisits').text("Sort by description ⇅");
                $('#sortByVetButtonPreviousVisits').text("Sort by veterinarian ⇅");
                $('#sortByStatusButtonPreviousVisits').text("Sort by status ⇅");
            }
        }

        let sortTableDateAscendingUpcomingVisits = false;
        let sortTableDateAscendingPreviousVisits = false;
        self.SortTableByDate = function(isForUpcoming) {
            ResetSortButtonArrows(isForUpcoming);

            if(isForUpcoming) {
                sortTableDateAscendingUpcomingVisits = !sortTableDateAscendingUpcomingVisits;

                if(sortTableDateAscendingUpcomingVisits) {
                    self.upcomingVisits.sort(function (a, b) {
                        return Date.parse(b.date) - Date.parse(a.date);
                    });
                    $('#sortByDateButtonUpcomingVisits').text("Sort by date ↓")
                } else {
                    self.upcomingVisits.sort(function (a, b) {
                        return Date.parse(b.date) + Date.parse(a.date);
                    });
                    $('#sortByDateButtonUpcomingVisits').text("Sort by date ↑")
                }
            } else {
                sortTableDateAscendingPreviousVisits = !sortTableDateAscendingPreviousVisits;

                if(sortTableDateAscendingPreviousVisits) {
                    self.previousVisits.sort(function (a, b) {
                        return Date.parse(b.date) - Date.parse(a.date);
                    });
                    $('#sortByDateButtonPreviousVisits').text("Sort by date ↓")
                } else {
                    self.previousVisits.sort(function (a, b) {
                        return Date.parse(b.date) + Date.parse(a.date);
                    });
                    $('#sortByDateButtonPreviousVisits').text("Sort by date ↑")
                }
            }
        }

        let sortDescriptionAscendingUpcomingVisits = false;
        let sortDescriptionAscendingPreviousVisits = false;
        self.SortTableByDesc = function(isForUpcoming) {
            ResetSortButtonArrows(isForUpcoming);

            if(isForUpcoming) {
                sortDescriptionAscendingUpcomingVisits = !sortDescriptionAscendingUpcomingVisits;

                if(sortDescriptionAscendingUpcomingVisits) {
                    self.upcomingVisits.sort(function (a, b) {
                        a = a.description.toLowerCase();
                        b = b.description.toLowerCase();

                        return a < b ? -1 : a > b ? 1 : 0;
                    });
                    $('#sortByDescButtonUpcomingVisits').text("Sort by description ↓")
                } else {
                    self.upcomingVisits.sort(function (a, b) {
                        a = a.description.toLowerCase();
                        b = b.description.toLowerCase();

                        return a > b ? -1 : a < b ? 1 : 0;
                    });
                    $('#sortByDescButtonUpcomingVisits').text("Sort by description ↑")
                }
            } else {
                sortDescriptionAscendingPreviousVisits = !sortDescriptionAscendingPreviousVisits;

                if(sortDescriptionAscendingPreviousVisits) {
                    self.previousVisits.sort(function (a, b) {
                        a = a.description.toLowerCase();
                        b = b.description.toLowerCase();

                        return a < b ? -1 : a > b ? 1 : 0;
                    });
                    $('#sortByDescButtonPreviousVisits').text("Sort by description ↓")
                } else {
                    self.previousVisits.sort(function (a, b) {
                        a = a.description.toLowerCase();
                        b = b.description.toLowerCase();

                        return a > b ? -1 : a < b ? 1 : 0;
                    });
                    $('#sortByDescButtonPreviousVisits').text("Sort by description ↑")
                }
            }
        }


        let sortVetAscendingUpcomingVisits = false;
        let sortVetAscendingPreviousVisits = false;
        self.SortTableByVet = function(isForUpcoming) {
            ResetSortButtonArrows(isForUpcoming);

            if(isForUpcoming) {
                sortVetAscendingUpcomingVisits = !sortVetAscendingUpcomingVisits;

                if(sortVetAscendingUpcomingVisits) {
                    self.upcomingVisits.sort(function (a, b) {
                        a = self.getPractitionerName(a.practitionerId).toLowerCase();
                        b = self.getPractitionerName(b.practitionerId).toLowerCase();

                        return a < b ? -1 : a > b ? 1 : 0;
                    });
                    $('#sortByVetButtonUpcomingVisits').text("Sort by veterinarian ↓")
                } else {
                    self.upcomingVisits.sort(function (a, b) {
                        a = self.getPractitionerName(a.practitionerId).toLowerCase();
                        b = self.getPractitionerName(b.practitionerId).toLowerCase();

                        return a > b ? -1 : a < b ? 1 : 0;
                    });
                    $('#sortByVetButtonUpcomingVisits').text("Sort by veterinarian ↑")
                }
            } else {
                sortVetAscendingPreviousVisits = !sortVetAscendingPreviousVisits;

                if(sortVetAscendingPreviousVisits) {
                    self.previousVisits.sort(function (a, b) {
                        a = self.getPractitionerName(a.practitionerId).toLowerCase();
                        b = self.getPractitionerName(b.practitionerId).toLowerCase();

                        return a < b ? -1 : a > b ? 1 : 0;
                    });
                    $('#sortByVetButtonPreviousVisits').text("Sort by veterinarian ↓")
                } else {
                    self.previousVisits.sort(function (a, b) {
                        a = self.getPractitionerName(a.practitionerId).toLowerCase();
                        b = self.getPractitionerName(b.practitionerId).toLowerCase();

                        return a > b ? -1 : a < b ? 1 : 0;
                    });
                    $('#sortByVetButtonPreviousVisits').text("Sort by veterinarian ↑")
                }
            }
        }

        let sortStatusAscendingUpcomingVisits = false;
        let sortStatusAscendingPreviousVisits = false;
        self.SortTableByStatus = function(isForUpcoming) {
            ResetSortButtonArrows(isForUpcoming);

            if(isForUpcoming) {
                sortStatusAscendingUpcomingVisits = !sortStatusAscendingUpcomingVisits;

                if(sortStatusAscendingUpcomingVisits) {
                    self.upcomingVisits.sort(function (a, b) {
                        a = self.getStatus(a.status).toLowerCase();
                        b = self.getStatus(b.status).toLowerCase();

                        return a < b ? -1 : a > b ? 1 : 0;
                    });
                    $('#sortByStatusButtonUpcomingVisits').text("Sort by status ↓")
                } else {
                    self.upcomingVisits.sort(function (a, b) {
                        a = self.getStatus(a.status).toLowerCase();
                        b = self.getStatus(b.status).toLowerCase();

                        return a > b ? -1 : a < b ? 1 : 0;
                    });
                    $('#sortByStatusButtonUpcomingVisits').text("Sort by status ↑")
                }
            } else {
                sortStatusAscendingPreviousVisits = !sortStatusAscendingPreviousVisits;

                if(sortStatusAscendingPreviousVisits) {
                    self.previousVisits.sort(function (a, b) {
                        a = self.getStatus(a.status).toLowerCase();
                        b = self.getStatus(b.status).toLowerCase();

                        return a < b ? -1 : a > b ? 1 : 0;
                    });
                    $('#sortByStatusButtonPreviousVisits').text("Sort by status ↓")
                } else {
                    self.previousVisits.sort(function (a, b) {
                        a = self.getStatus(a.status).toLowerCase();
                        b = self.getStatus(b.status).toLowerCase();

                        return a > b ? -1 : a < b ? 1 : 0;
                    });
                    $('#sortByStatusButtonPreviousVisits').text("Sort by status ↑")
                }
            }
        }

        self.submit = function () {
            var data = {
                date: $filter('date')(self.date, "yyyy-MM-dd"),
                description: self.desc,
                practitionerId: self.practitionerId,
                status: true
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

        self.deleteVisit = function (visitId){
            $http.delete("api/gateway/visits/" + visitId).then(function () {
                window.location.reload();
            }, function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };

        self.getStatus = function (status) {
            var statusText = "";

            if(status === false){
                statusText = "Canceled";
            }
            else{
                statusText = "Not Canceled";
            }

            return statusText;
        };

        self.cancelVisit = function (id, visitStatus, visitPractitionerId, visitDate, visitDescription){
            visitId = id;
            var data = {};

            if (visitStatus) {
                data = {
                    date: visitDate,
                    description: visitDescription,
                    practitionerId: visitPractitionerId,
                    status: false
                };
            }else {
                data = {
                    date: visitDate,
                    description: visitDescription,
                    practitionerId: visitPractitionerId,
                    status: true
                };
            }


            url = "api/gateway/owners/*/pets/" + petId + "/visits/" + visitId;

            $http.put(url, data).then(function () {
                $http.get("api/gateway/visits/"+petId).then(function (resp) {
                    self.visits = resp.data;
                    self.sortFetchedVisits();
                });
            },function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        };

        self.setCancelButtonText = function (visitStatus){
            var cancelText = "";

            if (visitStatus){
                cancelText = "Cancel";
            }
            else {
                cancelText = "Revert Cancel";
            }

            return cancelText;
        };
    }]);