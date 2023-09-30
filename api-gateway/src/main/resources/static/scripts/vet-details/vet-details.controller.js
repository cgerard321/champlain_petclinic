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

        self.updateRating = function (ratingId, rating) {
            const btn = document.getElementById("updateRatingBtn"+ratingId);

            const updateContainer=document.getElementById("ratingUpdate"+ratingId)
            const selectedValue=parseInt(document.getElementById("ratingOptions"+ratingId).value)
            if(selectedValue<1||selectedValue>5){
                alert("rateScore should be between 1 and 5" + selectedValue)
                return
            }

            const updatedDescription= document.getElementById("updateDescription"+ratingId).value
            if(updateContainer.style.display=="none"){
                updateContainer.style.display="block"
                btn.textContent="Save"
            }
            else if(btn.textContent=="Save"){
                const updatedRating = {
                    rateScore: selectedValue,
                    vetId: $stateParams.vetId,
                    rateDescription: updatedDescription,
                    rateDate: Date.now().toString()
                };
                console.log(updatedRating.rateScore)

                $http.put("api/gateway/vets/" + $stateParams.vetId + "/ratings/" + ratingId, updatedRating).then(function (resp) {
                    console.log(resp.data)
                    rating = resp.data;
                    alert('Your review was successfully updated!');
                    //refresh list
                    $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
                        console.log(resp.data)
                        self.ratings = resp.data;
                    });
                    //refresh percentages
                    percentageOfRatings();
                });

                updateContainer.style.display="none"
                btn.textContent="Update"
            }
        }

        //photo
        $http.get('api/gateway/vets/' + $stateParams.vetId + '/photo').then(function (resp) {
            self.vetPhoto = resp.data;
        });

        self.init = function (){
            $http.get('api/gateway/vets/' + $stateParams.vetId + '/photo').then(function (resp) {
                self.vetPhoto = resp.data;
            });
        }
        self.submitRatingForm = function (rating) {
            rating.vetId = $stateParams.vetId;
            rating.rateScore = document.getElementById("ratingScore").value;
            if(rating.rateScore<1||rating.rateScore>5){
                alert("rateScore should be between 1 and 5" + selectedValue)
                return
            }
            rating.rateDescription = document.getElementById("ratingDescription").value;
            var currentDate = new Date();
            var readableDate = currentDate.toLocaleDateString();
            rating.rateDate = readableDate;

            if(!rating.rateScore){
                alert("Please select a rating score")
                return;
            }
            else {

            $http.post("api/gateway/vets/" + $stateParams.vetId + "/ratings", rating).then(function (resp){
                console.log(resp.data)
                self.rating = resp.data;
                alert('Your review was successfully added!');


                //refresh list
                $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
                    console.log(resp.data)
                    self.ratings = resp.data;
                    arr = resp.data;
                });
                //refresh percentages
                percentageOfRatings();


                document.getElementById("ratingScore").value = ""
                document.getElementById("ratingDescription").value = ""
                //refresh list
                $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
                    self.ratings = resp.data;
                    arr = resp.data;
                });
            }, function (response) {
                let error = "Missing rating, please input a rating.";
                alert(error);
            })
        }};
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
    }]);