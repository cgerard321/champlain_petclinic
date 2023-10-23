'use strict';

angular.module('vetDetails')
    .controller('VetDetailsController', ['$http', '$stateParams', '$scope', function ($http, $stateParams, $scope) {
        var self = this;
        //var vetId = $stateParams.vetId || 0;

        this.show = ($event, vetID) => {
            let child = document.getElementsByClassName("m" + vetID)[0];
            let left = $event.pageX;
            let top = $event.clientY;
            if (document.documentElement.clientWidth > 960) {
                child.style.left = (left + 221) + 'px';
            }
            if (document.documentElement.clientWidth < 420) {
                child.style.left = (170) + 'px';
            } else if (document.documentElement.clientWidth < 510) {
                child.style.left = (left + 334.5 / 2.5) + 'px';
            } else {
                child.style.left = (left + 200) + 'px';
            }
            child.style.top = (top) + 'px';
            child.classList.remove("modalOff");
            child.classList.add("modalOn");
        }
        this.hide = ($event, vetID) => {
            let child = document.getElementsByClassName("m" + vetID)[0];
            child.classList.remove("modalOn");
            child.classList.add("modalOff");
        }

        self.checkedCheckboxesUpdate = {};
        self.handleCheckboxClick = function(value, ratingId) {
            // Update the checked state for the specific ratingId.
            self.checkedCheckboxesUpdate[ratingId] = value;
            // Uncheck other checkboxes for the same ratingId.
            const checkboxes = document.querySelectorAll('input[type="checkbox"][name="predefinedDescriptionUpdate' + ratingId + '"]');
            checkboxes.forEach((checkbox) => {
                if (checkbox.value !== value) {
                    checkbox.checked = false;
                }
            });
        };

        const checkboxesForAddRating = document.querySelectorAll('.col-sm-12 input[type="checkbox"]');
        let lastCheckedCheckbox = null;
        checkboxesForAddRating.forEach(checkbox => {
            checkbox.addEventListener('click', function () {
                if (this !== lastCheckedCheckbox) {
                    if (lastCheckedCheckbox) {
                        lastCheckedCheckbox.checked = false;
                    }
                    lastCheckedCheckbox = this;
                }
                const value = lastCheckedCheckbox.checked ? lastCheckedCheckbox.value : null;
                console.log('Last checked value:', value);
            });
        });
        self.togglePredefinedDescription = function (rating, value) {
            console.log("Toggling predefinedDescription: ", value);
            if (rating['predefinedDescription' + value] === value) {
                rating['predefinedDescription' + value] = null;
            } else {
                rating['predefinedDescription' + value] = value;
            }
        };

        self.workdayToWorkHours=new Map()

        /* added /{{vet.vetID}} in the url */
        $http.get('api/gateway/vets/' + $stateParams.vetId).then(function (resp) {
            self.vet = resp.data;
            const workHoursObject = JSON.parse(self.vet.workHoursJson);

            // Convert the JSON object into a Map
            for (const key in workHoursObject) {
                if (workHoursObject.hasOwnProperty(key)) {
                    self.workdayToWorkHours.set(key, workHoursObject[key]);
                }
            }

            console.log(self.workdayToWorkHours);
        });

        // map of work hours
        self.selectWorkday = function (selectedWorkday) {
            self.workHours = [];

            // Check if the selected workday exists in the mapping
            if (self.workdayToWorkHours.has(selectedWorkday)) {
                self.workHours = self.workdayToWorkHours.get(selectedWorkday);
            } else {
                // Handle the case when the workday is not found in the mapping
                console.log('Workday not found in the mapping.');
            }

            // Get the table element by its ID
            var table = document.getElementById("workHoursTable");
            // Get all the <td> elements in the table
            var tdElements = table.getElementsByTagName("td");
            console.log(tdElements)

            if(table.style.display==="none"){
                table.style.display="block"
            }
            else{
                table.style.display="none"
            }

            // Loop through the <td> elements to check if their IDs correspond with the elements in workHours
            for (var i = 0; i < tdElements.length; i++) {
                var td = tdElements[i];
                var tdId = td.id;

                // Check if the tdId is in the self.workHours array
                if (self.workHours.includes(tdId)) {
                    // This <td> corresponds to a work hour for the selected day
                    // You can apply styling or any other actions you need here
                    td.style.border = "2px solid green"; // For example, add a green border
                } else {
                    // This <td> is not in the workHours array
                    // You can apply a different style for non-working hours
                    td.style.border = "2px solid black"; // For example, add a black border
                }
            }
        }

        $http.get("api/gateway/visits/vets/" + $stateParams.vetId).then(function (resp) {
            self.visitsList = resp.data;
        });

        $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
            console.log(resp.data)
            self.ratings = resp.data;
        });

        $http.get('api/gateway/vets/' + $stateParams.vetId + '/educations').then(function (resp) {
            console.log(resp.data)
            self.educations = resp.data;
        });

        $scope.deleteVetEducation = function (educationId) {
            let varIsConf = confirm('Are you sure you want to delete this educationId: ' + educationId + '?');
            if (varIsConf) {

                $http.delete('api/gateway/vets/' + $stateParams.vetId + '/educations/' + educationId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert(educationId + " Deleted Successfully!");
                    console.log(response, 'res');
                    //refresh list
                    $http.get('api/gateway/vets/' + $stateParams.vetId + '/educations').then(function (resp) {
                        self.educations = resp.data;
                        arr = resp.data;
                    });
                }

                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'cannot get data.');
                }
            }
        };

        //update education
        self.updateEducation = function (educationId) {
            const btn = document.getElementById("updateEducationBtn" + educationId);
            const updateContainer = document.getElementById("educationUpdate" + educationId);

            let updatedDegree = document.getElementById("updateDegree" + educationId).value;
            let updatedSchoolName = document.getElementById("updateSchoolName" + educationId).value;
            let updatedFieldOfStudy = document.getElementById("updateFieldOfStudy" + educationId).value;
            let updatedStartDate = document.getElementById("updateStartDate" + educationId).value;
            let updatedEndDate = document.getElementById("updateEndDate" + educationId).value;

            let updatedEducation = {
                educationId: educationId,
                degree: updatedDegree,
                schoolName: updatedSchoolName,
                vetId: $stateParams.vetId,
                fieldOfStudy: updatedFieldOfStudy,
                startDate: updatedStartDate,
                endDate: updatedEndDate,
            };

            if (updateContainer.style.display === "none") {
                // Show the update form
                updateContainer.style.display = "block";
                btn.textContent = "Save";
            } else if (btn.textContent === "Save") {
                if (
                    updatedDegree === "" ||
                    updatedSchoolName === "" ||
                    updatedFieldOfStudy === "" ||
                    updatedStartDate === "" ||
                    updatedEndDate === ""
                ) {
                    alert("Please fill in all education fields.");
                    return;
                }

                // Save the updated education
                $http.put("api/gateway/vets/" + $stateParams.vetId + "/educations/" + educationId, updatedEducation).then(function (resp) {
                    console.log(resp.data);
                    self.updatedEducation = resp.data;
                    alert('Your education was successfully updated!');

                    // Refresh list
                    $http.get('api/gateway/vets/' + $stateParams.vetId + '/educations').then(function (resp) {
                        console.log(resp.data);
                        self.educations = resp.data;
                    });
                });


                // Hide the update form
                updateContainer.style.display = "none";
                btn.textContent = "Update";

            }
        };


        $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings/percentages')
            .then(function (resp) {
                const ratingsData = resp.data;
                const ratingsContainer = document.getElementById('ratings-list');
                let html = '';
                for (const key in ratingsData) {
                    if (ratingsData.hasOwnProperty(key)) {
                        const percentage = ratingsData[key] * 100; // Convert fraction to percentage
                        html += key + ' stars - ' + percentage.toFixed(0) + '%';
                        html += '<br>';
                    }
                }
                ratingsContainer.innerHTML = html.slice(0, -2);
            });

        $scope.getRecentRatingBasedOnDate = function () {
            console.log("In function based on date")
            const wrongYearPattern = /^\d{4}$/;

            let yearQuery = document.getElementById("queryDate").value
            let year = new Date().getFullYear()

            if (!wrongYearPattern.test(yearQuery)) {
                // Throw an exception with a custom error message
               alert("Invalid year format. Please enter a valid year.");
               return;
            }
           else if (self.query === undefined || self.query === ''){
                let newYear = year - 2
                $http.get('api/gateway/vets/'+$stateParams.vetId +'/ratings/date?year='+newYear).then(function (resp) {
                    console.log(resp.data);
                    self.ratings = resp.data;
                    arr = resp.data;
                });
            }else{
                $http.get('api/gateway/vets/'+$stateParams.vetId +'/ratings/date?year='+yearQuery).then(function (resp) {
                    console.log(resp.data);
                    self.ratings = resp.data;
                    arr = resp.data;
                });
            }
        };

        $scope.deleteVetRating = function (ratingId) { //added $scope in this class
            let varIsConf = confirm('Are you sure you want to delete this ratingId: ' + ratingId + '?');
            if (varIsConf) {

                $http.delete('api/gateway/vets/' + $stateParams.vetId + '/ratings/' + ratingId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert(ratingId + " Deleted Successfully!");
                    console.log(response, 'res');
                    //refresh list
                    $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
                        self.ratings = resp.data;
                        arr = resp.data;
                    });
                    //refresh percentages
                    percentageOfRatings();

                    //refresh badge
                    $http.get('api/gateway/vets/'+$stateParams.vetId+'/badge').then(function(resp){
                        self.badge=resp.data;
                        console.log(resp.data)
                    })
                }

                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'cannot get data.');
                }
            }
        };

        self.updateRating = function (ratingId) {
            const btn = document.getElementById("updateRatingBtn" + ratingId);
            const updateContainer = document.getElementById("ratingUpdate" + ratingId);
            const selectedValue = parseInt(document.getElementById("ratingOptions" + ratingId).value);

            if (selectedValue < 1 || selectedValue > 5) {
                alert("rateScore should be between 1 and 5" + selectedValue);
                return;
            }

            let updatedDescription = document.getElementById("updateDescription" + ratingId).value;
            if (updatedDescription.trim() === "") {
                updatedDescription = null;
            }

            const predefinedDesc = document.querySelector('input[name="predefinedDescriptionUpdate' + ratingId + '"]:checked')
                ? document.querySelector('input[name="predefinedDescriptionUpdate' + ratingId + '"]:checked').value
                : null;

            //CHECK
            const ratingDate = document.querySelector('input[name="Year' + ratingId + '"]:checked')
                ? document.querySelector('input[name="Year' + ratingId + '"]:checked').value :null;

            let updatedRating = {
                ratingId: ratingId,
                rateScore: selectedValue,
                vetId: $stateParams.vetId,
                rateDescription: updatedDescription,
                rateDate: Date.now().toString(),
                predefinedDescription: predefinedDesc
            };

            if (updateContainer.style.display === "none") {
                // Show the update form
                updateContainer.style.display = "block";
                btn.textContent = "Save";
            } else if (btn.textContent === "Save") {
                // Save the updated rating
                $http.put("api/gateway/vets/" + $stateParams.vetId + "/ratings/" + ratingId, updatedRating).then(function (resp) {
                    console.log(resp.data);
                    self.updatedRating = resp.data;
                    alert('Your review was successfully updated!');

                    // Refresh list
                    $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
                        console.log(resp.data);
                        self.ratings = resp.data;
                    });

                    // Refresh percentages
                    percentageOfRatings();

                    //refresh badge
                    $http.get('api/gateway/vets/'+$stateParams.vetId+'/badge').then(function(resp){
                        self.badge=resp.data;
                        console.log(resp.data)
                    })
                });

                // Hide the update form
                updateContainer.style.display = "none";
                btn.textContent = "Update";

                // reset predefinedDescription radio buttons to unchecked
                document.querySelectorAll('input[name="predefinedDescriptionUpdate' + ratingId + '"]').forEach(function (radio) {
                    radio.checked = false;
                });

                //ADDTIION
                document.querySelectorAll('input[name="Year:' + ratingId + '"]').forEach(function (radio) {
                    radio.checked = true;
                });
            }
        };


        //badge
        $http.get('api/gateway/vets/'+$stateParams.vetId+'/badge').then(function(resp){
            self.badge=resp.data;
            console.log(resp.data)
        })
        self.init = function (){
            $http.get('api/gateway/vets/' + $stateParams.vetId + '/badge').then(function (resp) {
                self.badge= resp.data;
                console.log(resp.data)
            });
        }


        //default photo
       /* let defaultImageBase64 = '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';
        $http.get('api/gateway/vets/' + $stateParams.vetId+"/photo").then(function (resp) {
            console.log(self.vetPhoto.photo);
            if(self.vetPhoto.photo != null)
                self.vetPhoto = resp.data;
            else
                self.vetPhoto = defaultImageBase64;
        })
           .catch(function (error){
               self.vetPhoto = defaultImageBase64;

           });*/
        //photo
        $http.get('api/gateway/vets/' + $stateParams.vetId + '/default-photo').then(function (resp) {
            self.vetPhoto = resp.data;
            if(self.vetPhoto.filename == "vet_default.jpg")
                self.vetPhoto.photo = self.vetPhoto.resourceBase64;
            else
                throw new Error();
            console.log(resp.data);
        })
        .catch(function (error) {
            console.log(error);
            $http.get('api/gateway/vets/' + $stateParams.vetId + '/photo').then(function (resp) {
                self.vetPhoto = resp.data;
                console.log(self.vetPhoto.photo);
                console.log(resp.data);
            });
        });
       /* //photo
        $http.get('api/gateway/vets/' + $stateParams.vetId + '/photo').then(function (resp) {
            self.vetPhoto = resp.data;
        });
*/

        self.init = function (){
            $http.get('api/gateway/vets/' + $stateParams.vetId + '/photo').then(function (resp) {
                self.vetPhoto = resp.data;
            });
        }

        self.submitRatingForm = function () {
            var rating = {
                vetId: $stateParams.vetId,
                rateScore: parseFloat(document.getElementById("ratingScore").value),
                rateDate: new Date().toLocaleDateString(),
                rateDescription: document.getElementById("ratingDescription").value,
                predefinedDescription: document.querySelector('input[name="predefinedDescriptionPOOR"]:checked')
                    ? document.querySelector('input[name="predefinedDescriptionPOOR"]:checked').value
                    : document.querySelector('input[name="predefinedDescriptionGOOD"]:checked')
                        ? document.querySelector('input[name="predefinedDescriptionGOOD"]:checked').value
                        : document.querySelector('input[name="predefinedDescriptionEXCELLENT"]:checked')
                            ? document.querySelector('input[name="predefinedDescriptionEXCELLENT"]:checked').value
                            : null,
                //ADDITION
                ratingDate: document.querySelector('input[name="Year"]:checked')
                    ? document.querySelector('input[name="Year"]:checked').value: null,


            };
            if (!rating.rateScore) {
                alert("Please select a rating score");
                return;
            }
            else {
                // If ratingDescription is an empty string, set it to null
                if (rating.rateDescription.trim() === "") {
                    rating.rateDescription = null;
                }
                $http.post("api/gateway/vets/" + $stateParams.vetId + "/ratings", rating)
                    .then(function (resp) {
                        self.rating = resp.data;
                        alert('Your review was successfully added!');

                        // Refresh list
                        $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
                            console.log(resp.data);
                            self.ratings = resp.data;
                            arr = resp.data;
                        });

                        // Refresh percentages
                        percentageOfRatings();

                        document.getElementById("ratingScore").value = "";
                        document.getElementById("ratingDescription").value = "";

                        // Refresh list
                        $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
                            self.ratings = resp.data;
                            arr = resp.data;
                        });

                        //refresh badge
                        $http.get('api/gateway/vets/'+$stateParams.vetId+'/badge').then(function(resp){
                            self.badge=resp.data;
                            console.log(resp.data)
                        })
                    })
                    .catch(function (error) {
                        let errorMessage = "An error occurred while adding the rating. Please try again.";
                        if (error.data && error.data.errors) {
                            errorMessage = error.data.errors;
                        }
                        alert(errorMessage);
                        self.educations = resp.data;
                    });
                document.getElementById("ratingForm").reset();
            }
        }

        self.submitEducationForm = function (education) {
            education.vetId = $stateParams.vetId;
            education.degree = document.getElementById("degree").value;
            education.schoolName = document.getElementById("schoolName").value;
            education.fieldOfStudy = document.getElementById("fieldOfStudy").value;
            education.startDate = document.getElementById("startDate").value;
            education.endDate = document.getElementById("endDate").value;

            // Send a POST request to add the new education
            $http.post("api/gateway/vets/" + $stateParams.vetId + "/educations", education)
                .then(function (resp) {
                    console.log(resp.data);
                    self.education = resp.data;
                    self.addEducationFormVisible = false;
                    alert('Your education was successfully added!');

                    $http.get('api/gateway/vets/' + $stateParams.vetId + '/educations').then(function (resp) {
                        console.log(resp.data);
                        self.educations = resp.data;
                    });
                    // clear the form with .reset()

                    document.getElementById("educationForm").reset();
              
                    // Refresh the education data
                    $http.get('api/gateway/vets/' + $stateParams.vetId + '/educations').then(function (resp) {
                        console.log(resp.data);
                        self.educations = resp.data;
                    });
                }, function (error) {
                    alert('Error adding education: ' + error.data.message);
                });
        };

        function uuidv4() {
            return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
                (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
            );
        };

        function percentageOfRatings() {
            $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings/percentages')
                .then(function (resp) {
                    const ratingsData = resp.data;
                    const ratingsContainer = document.getElementById('ratings-list');
                    let html = '';
                    const ratingsArray = [];
                    for (const key in ratingsData) {
                        if (ratingsData.hasOwnProperty(key)) {
                            const percentage = ratingsData[key] * 100;
                            ratingsArray.push({ rating: parseFloat(key), percentage });
                        }
                    }
                    for (const ratingObj of ratingsArray) {
                        console.log("RATING" + ratingObj.percentage);
                        html += ratingObj.rating + ' stars - ' + ratingObj.percentage.toFixed(0) + '%';
                        html += '<br>';
                    }
                    ratingsContainer.innerHTML = html.slice(0, -2);
                });
        }

        var self = this;
        self.newEducation = {}; // Initialize newEducation

        self.addEducationFormVisible = false; // Hide the education form initially

        // Function to toggle the visibility of the education form
        self.addEducation = function () {
            self.addEducationFormVisible = true;
        };

        

        // Function to cancel adding education and hide the form
        self.cancelEducationForm = function () {
            self.newEducation = {}; // Clear form fields
            self.addEducationFormVisible = false; // Hide the education form
        };


    }]);

// function getOlderRatingBasedOnDate(vet){
//     let year = new Date().getFullYear()
//
//     let old = year - 4
//
//     $http.get('api/gateway/vets/{vetId}/ratings/date?year=' + old).then(function (resp) {
//         console.log(resp.data);
//         vet.showRating=true;
//         vet.ratingDate = parseFloat(resp.data.toFixed(1));
//
//     });
//
// }

    // $scope.refreshList = self.vetList;
    //
    // $scope.ReloadData = function () {
    //     let url = 'api/gateway/vets/' + $stateParams.vetId + '/ratings/date?year=' + year;
    //     let optionSelection = document.getElementById("filterOption").value;
    //     if (optionSelection === "Recent") {
    //         url+= '/date?year=2024';
    //     } else if (optionSelection === "Old") {
    //         url += '/date?year=2020';
    //     }
    //     self.selectedFilter=optionSelection;
    //
    //     $http.get(url).then(function (resp) {
    //         self.vetList = resp.data;
    //         arr = resp.data;
    //         angular.forEach(self.vetList, function(vet) {
    //             getRecentRatingBasedOnDate(vet)
    //             // getOlderRatingBasedOnDate(ratingsOld)
    //         });
    //
    //     });
    //
    // }



