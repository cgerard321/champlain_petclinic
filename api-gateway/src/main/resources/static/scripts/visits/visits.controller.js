'use strict';

angular.module('visits')
    .controller('VisitsController', ['$http', '$state', '$stateParams', '$filter', function ($http, $state, $stateParams, $filter) {
        var self = this;
        var petId = $stateParams.petId || 0;
        var url = "api/gateway/visit/owners/" + ($stateParams.ownerId || 0) + "/pets/" + petId + "/visits";
        var vetsUrl = "api/gateway/vets";
        var billsUrl = "api/gateway/bill";
        var visitId = 0;
        self.practitionerId = 0;
        self.date = new Date();
        self.desc = "";

        $http.get("api/gateway/visits/"+petId).then(function (resp) {
            self.visits = resp.data;
            self.sortFetchedVisits();
        });
        

        // Function to... get the current date ;)
        function getCurrentDate() {
            let dateObj = new Date();
            var dd = String(dateObj.getDate()).padStart(2, '0');
            var mm = String(dateObj.getMonth() + 1).padStart(2, '0');
            var yyyy = dateObj.getFullYear();
            return Date.parse(yyyy + '-' + mm + '-' + dd);
        }

        // Lists holding visits for the table to display
        self.upcomingVisits = [];
        self.previousVisits = [];

        self.sortFetchedVisits = function() {
            let currentDate = getCurrentDate();


            $.each(self.visits, function(i, visit) {
                let selectedVisitDate = Date.parse(visit.date);

                if(selectedVisitDate >= currentDate) {
                    self.upcomingVisits.push(visit);
                } else {
                    self.previousVisits.push(visit);
                }
            });
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

        self.showConfirmationModal = function(e, visitId = 0) {
            // Get the name of button sender
            let buttonText = $(e.target).text();

            // Set modal's title and body to match the sender's request
            let confirmationModal = $('#confirmationModalTitle');
            confirmationModal.text(buttonText);
            $('#confirmationModalBody').text("Are you sure you want to " + buttonText.toLowerCase() + "?");

            // Set the targeted visit data attribute which is used if the button is for cancel or delete
            $('#confirmationModalConfirmButton').data("targetVisit", visitId);

            // Show the modal
            $('#confirmationModal').modal('show');
        }

        self.completeFormAction = function() {
            // Check which button modal was called by and perform appropriate action
            let modalTitle = $('#confirmationModalTitle').text();

            if(modalTitle === $('#submit_button').text()) {
                $('#visitForm').submit();
            }
            else if(modalTitle === "Delete visit") {
                self.deleteVisit($('#confirmationModalConfirmButton').data("targetVisit"));
            }
        }

        self.switchToUpdateForm = function (e, practitionerId, date, description, id, visitStatus){
            visitId = id;
            $("#selectedVet option[value='"+practitionerId+"']").prop("selected", true);
            $('#date_input').val(date);
            $('#description_textarea').val(description);
            $('#submit_button').text("Update Visit");
            $('#cancel_button').css("visibility", "visible");

            self.loadVetInfo();

            // Save the sender's index to data attribute on visitForm called data-update-index
            let form = $('#visitForm');
            form.data("update-table", $(e.target).closest('tr').data("table-name"));
            form.data("update-index", $(e.target).closest('tr').data("index"));

            self.submit = function () {
                var data = {
                    date: $('#date_input').val(),
                    description: $('#description_textarea').val(),
                    practitionerId: $("#selectedVet").val(),
                    status: visitStatus
                };

                url = "api/gateway/owners/*/pets/" + petId + "/visits/" + visitId;
                $http.put(url, data).then(function(response) {
                    let currentDate = getCurrentDate();
                    let form = $('#visitForm');

                    // Get the index of the visit to be updated
                    let index = parseInt(form.data("update-index"));

                    // See if the unedited visit was in upcoming
                    let outdatedVisitWasInUpcoming = form.data("update-table") === "upcomingVisits";

                    // See if the edited visit will be in upcoming or not
                    let updatedVisitWillBeInUpcoming = Date.parse(response.data.date) >= currentDate;

                    // Perform an action depending on where the visit was located and where it will be
                    if(outdatedVisitWasInUpcoming === true && updatedVisitWillBeInUpcoming === true) {
                        self.upcomingVisits[index] = response.data;
                    }
                    if(outdatedVisitWasInUpcoming === false && updatedVisitWillBeInUpcoming === false) {
                        self.previousVisits[index] = response.data;
                    }
                    if(outdatedVisitWasInUpcoming === true && updatedVisitWillBeInUpcoming === false) {
                        // Remove the old visit from upcoming
                        self.upcomingVisits.splice(index, 1);

                        // Add the edited visit to previous visits
                        self.previousVisits.push(response.data);
                    }
                    if(outdatedVisitWasInUpcoming === false && updatedVisitWillBeInUpcoming === true) {
                        // Remove the old visit from upcoming
                        self.previousVisits.splice(index, 1);

                        // Add the edited visit to previous visits
                        self.upcomingVisits.push(response.data);
                    }

                    // Call the last sort after adding if there is one
                    callLastSort(updatedVisitWillBeInUpcoming);
                }, function (response) {
                    var error = response.data;
                    alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
                });
            };
        };

        // This value will be set depending on what was last clicked
        let lastSort = "";
        let dateSortName = "Date";
        let descSortName = "Description";
        let vetSortName = "Veterinarian";
        let statusSortName = "Status";

        // This function will call the last sorted option without changing ascending or descending
        function callLastSort(isForUpcoming) {
            switch(lastSort) {
                case dateSortName:
                    self.SortTableByDate(isForUpcoming, false);
                    break;
                case descSortName:
                    self.SortTableByDesc(isForUpcoming, false);
                    break;
                case vetSortName:
                    self.SortTableByVet(isForUpcoming, false);
                    break;
                case statusSortName:
                    self.SortTableByStatus(isForUpcoming, false);
                    break;
            }
        }

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
        self.SortTableByDate = function(isForUpcoming, flipSortingBool = true) {
            lastSort = dateSortName;
            ResetSortButtonArrows(isForUpcoming);

            if(isForUpcoming) {
                if(flipSortingBool) {
                    sortTableDateAscendingUpcomingVisits = !sortTableDateAscendingUpcomingVisits;
                }

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
                if(flipSortingBool) {
                    sortTableDateAscendingPreviousVisits = !sortTableDateAscendingPreviousVisits;
                }

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
        self.SortTableByDesc = function(isForUpcoming, flipSortingBool = true) {
            lastSort = descSortName;
            ResetSortButtonArrows(isForUpcoming);

            if(isForUpcoming) {
                if(flipSortingBool) {
                    sortDescriptionAscendingUpcomingVisits = !sortDescriptionAscendingUpcomingVisits;
                }

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
                if(flipSortingBool) {
                    sortDescriptionAscendingPreviousVisits = !sortDescriptionAscendingPreviousVisits;
                }

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
        self.SortTableByVet = function(isForUpcoming, flipSortingBool = true) {
            lastSort = vetSortName;
            ResetSortButtonArrows(isForUpcoming);

            if(isForUpcoming) {
                if(flipSortingBool) {
                    sortVetAscendingUpcomingVisits = !sortVetAscendingUpcomingVisits;
                }

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
                if(flipSortingBool) {
                    sortVetAscendingPreviousVisits = !sortVetAscendingPreviousVisits;
                }

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
        self.SortTableByStatus = function(isForUpcoming, flipSortingBool = true) {
            lastSort = statusSortName;
            ResetSortButtonArrows(isForUpcoming);

            if(isForUpcoming) {
                if(flipSortingBool) {
                    sortStatusAscendingUpcomingVisits = !sortStatusAscendingUpcomingVisits;
                }

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
                if(flipSortingBool) {
                    sortStatusAscendingPreviousVisits = !sortStatusAscendingPreviousVisits;
                }

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
            
            var billData = {
                ownerId: $stateParams.ownerId,
                date: $filter('date')(self.date, "yyyy-MM-dd"),
                visitType : $("#selectedVisitType").val()
            }

            $http.post(url, data).then(function(response) {
                let currentDate = getCurrentDate();

                // Add the visit to one of the lists depending on its date
                let isForUpcomingVisitsTable = Date.parse(response.data.date) >= currentDate;
                if(isForUpcomingVisitsTable) {
                    self.upcomingVisits.push(response.data);
                } else {
                    self.previousVisits.push(response.data);
                }

                // Call the last sort after adding if there is one
                callLastSort(isForUpcomingVisitsTable);
            },function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });

            $http.post(billsUrl, billData).then(function () {

            }, function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"));
            });
        }

        self.deleteVisit = function (e, visitId){
            $http.delete("api/gateway/visits/" + visitId).then(function () {
                // Get the parent row of the sender
                let parentRow = $(e.target).closest('tr');

                // Get the index of the sender from the parent table row data attribute
                let index = parseInt(parentRow.data("index"));

                // See if the sender is in upcoming or previous visits
                let deleteFromUpcomingVisits = parentRow.data("table-name") === "upcomingVisits";

                // Remove the visit from the list of either upcoming or previous visits
                if(deleteFromUpcomingVisits) {
                    self.upcomingVisits.splice(index, 1);
                } else {
                    self.previousVisits.splice(index, 1);
                }
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

        self.cancelVisit = function (e, id, visitStatus, visitPractitionerId, visitDate, visitDescription){
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

            $http.put(url, data).then(function(response) {
                // Get the index of the sender from the parent table row data attribute
                let index = parseInt($(e.target).closest('tr').data("index"));

                // Delete that visit (must delete in order to update index when sorted)
                self.upcomingVisits[index] = response.data;

                // Call the last sort if there was one
                callLastSort(true);
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
