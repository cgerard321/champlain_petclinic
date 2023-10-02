'use strict';

angular.module('vetDetails')
    .controller('VetDetailsController', ['$http', '$stateParams', '$scope', function ($http, $stateParams, $scope) {
        var self = this;
        //var vetId = $stateParams.vetId || 0;

        /* added /{{vet.vetID}} in the url */
        $http.get('api/gateway/vets/' + $stateParams.vetId).then(function (resp) {
            self.vet = resp.data;
        });

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
                });

                // Hide the update form
                updateContainer.style.display = "none";
                btn.textContent = "Update";

                // reset predefinedDescription radio buttons to unchecked
                document.querySelectorAll('input[name="predefinedDescriptionUpdate' + ratingId + '"]').forEach(function (radio) {
                    radio.checked = false;
                });
            }
        };

        //photo
        $http.get('api/gateway/vets/photo/' + $stateParams.vetId).then(function (resp) {
            self.vetPhoto = resp.data;
        });
//photo
        $http.get('api/gateway/vets/' + $stateParams.vetId + '/photo').then(function (resp) {
            self.vetPhoto = resp.data;
        });

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
                predefinedDescription: document.querySelector('input[name="predefinedDescription"]:checked')
                    ? document.querySelector('input[name="predefinedDescription"]:checked').value
                    : null,
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

        // Function to handle the form submission for adding education

        // Function to cancel adding education and hide the form
        self.cancelEducationForm = function () {
            self.newEducation = {}; // Clear form fields
            self.addEducationFormVisible = false; // Hide the education form
        };

    }]);