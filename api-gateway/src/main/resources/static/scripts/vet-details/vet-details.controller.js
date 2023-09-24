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
                    console.log(error, 'can not get data.');
                }
            }
        };

        self.updateRating = function (ratingId, rating) {
            const btn = document.getElementById("updateRatingBtn");

            const updateContainer=document.getElementById("ratingUpdate")
            const selectedValue=parseInt(document.getElementById("ratingOptions").value)
            const updatedDescription= document.getElementById("updateDescription").value

            if(updateContainer.style.display=="none"){
                updateContainer.style.display="block"
                btn.textContent="Save"
            }
            else if(btn.textContent=="Save"){
                rating.rateScore=selectedValue
                rating.vetId=$stateParams.vetId
                rating.rateDescription=updatedDescription
                rating.rateDate = Date.now().toString()
                console.log(rating.rateScore);
                updateContainer.style.display="none"
                btn.textContent="Update"

                $http.put("api/gateway/vets/" + $stateParams.vetId + "/ratings/"+ratingId, rating).then(function (resp){
                    console.log(resp.data)
                    self.rating = resp.data;
                    alert('Your review was successfully updated!');
                    //refresh list
                    $http.get('api/gateway/vets/' + $stateParams.vetId + '/ratings').then(function (resp) {
                        console.log(resp.data)
                        self.ratings = resp.data;
                        arr = resp.data;
                    });
                    //refresh percentages
                    percentageOfRatings();
                });
            }
        }

        //photo
        $http.get('api/gateway/vets/photo/' + $stateParams.vetId).then(function (resp) {
            self.vetPhoto = resp.data;
        });
        const fileInput = document.querySelector('input[id="photoVet"]');
        let vetPhoto = "";
        fileInput.addEventListener('change', (e) => {
            const file = e.target.files[0];
            const reader = new FileReader();
            reader.onloadend = () => {
                vetPhoto = reader.result
                    .replace('data:', '')
                    .replace(/^.+,/, '');
                self.PreviewImage = vetPhoto;
                var image = {
                    name: uuidv4(),
                    type: "jpeg",
                    photo: vetPhoto
                };
                var test = $http.post('api/gateway/vets/photo/' + $stateParams.vetId, image);
                console.log(test);

            };
            reader.readAsDataURL(file);
        });

        self.init = function (id){
            $http.get('api/gateway/vets/' + $stateParams.vetId + '/vets/photo/' + id).then(function (resp) {
                self.vetPhoto = resp.data;
            });
        }
        self.submitRatingForm = function (rating) {
            rating.vetId = $stateParams.vetId;
            rating.rateScore = document.getElementById("ratingScore").value;
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