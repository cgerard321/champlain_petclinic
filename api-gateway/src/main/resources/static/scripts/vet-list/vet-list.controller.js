'use strict';
let arr;
angular.module('vetList')
    .controller('VetListController', ['$http', '$scope', function ($http, $scope) {
        var self = this;

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
        };

        this.hide = ($event, vetID) => {
            let child = document.getElementsByClassName("m" + vetID)[0];
            child.classList.remove("modalOn");
            child.classList.add("modalOff");
        };

        this.selectedFilter = 'Top Vets';

        $scope.vetList = [];

        const eventSourceHandler = (url, callback) => {
            const eventSource = new EventSource(url);
            eventSource.onmessage = (event) => {
                const data = JSON.parse(event.data);
                callback(data);
            };
            eventSource.onerror = () => {
                eventSource.close();
            };
        };

        const eventSource = new EventSource('api/gateway/vets');
        self.vetList = [];
        arr = [];

        eventSource.onmessage = function (event) {
            const vet = JSON.parse(event.data);
            self.vetList.push(vet);
            arr.push(vet);

            $scope.$apply(function () {
                $scope.vetList = self.vetList;

                getCountOfRatings(vet);
                getAverageRating(vet);
                getTopThreeVetsWithHighestRating(vet);
            });
        };

        eventSource.onerror = function () {
            console.error("Error fetching vet list");
            eventSource.close();
        };

        function getAverageRating(vet) {
            if (vet.rating !== undefined) {
                return;
            }
            console.log("Hello " + vet.vetId);
            $http.get('api/gateway/vets/' + vet.vetId + "/ratings/average").then(function (resp) {
                console.log(resp.data);
                vet.showRating = true;
                vet.rating = parseFloat(resp.data.toFixed(1));
            });
        }
        function getTopThreeVetsWithHighestRating(vet) {
            eventSourceHandler('api/gateway/vets/topVets', (data) => {
                console.log(data);
                if (data && typeof data.averageRating === 'number') {
                    vet.showRating = true;
                    vet.rating = parseFloat(data.averageRating.toFixed(1));
                } else {
                    console.error("Invalid data format for rating:", data);
                }
            });
        }

        function getCountOfRatings(vet) {
            $http.get('api/gateway/vets/' + vet.vetId + "/ratings/count").then(function (resp) {
                console.log(resp.data);
                vet.count = resp.data;
            });
        }

        $scope.deleteVet = function (vetId) {
            let varIsConf = confirm('Want to delete vet with vetId:' + vetId + '. Are you sure?');
            if (varIsConf) {
                $http.delete('api/gateway/vets/' + vetId)
                    .then(successCallback, errorCallback);

                function successCallback(response) {
                    $scope.errors = [];
                    alert(vetId + " Deleted Successfully!");
                    console.log(response, 'res');
                    // Refresh list
                    $http.get('api/gateway/vets').then(function (resp) {
                        self.vetList = resp.data;
                        arr = resp.data;
                    });
                }

                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'can not get data.');
                }
            }
        };

        $scope.refreshList = self.vetList;

        $scope.ReloadData = function () {
            let url = 'api/gateway/vets';
            let optionSelection = document.getElementById("filterOption").value;
            if (optionSelection === "Active") {
                url += '/active';
            } else if (optionSelection === "Inactive") {
                url += '/inactive';
            } else if (optionSelection === "Top Vets") {
                url += '/topVets';
            }
            self.selectedFilter = optionSelection;

            if (optionSelection === "Top Vets" || optionSelection === "Active" || optionSelection === "Inactive") {
                eventSourceHandler(url, (data) => {
                    self.vetList = data;
                    arr = data;
                    angular.forEach(self.vetList, function (vet) {
                        getAverageRating(vet);
                        getCountOfRatings(vet);
                        getTopThreeVetsWithHighestRating(vet);
                    });
                });
            } else {
                $http.get(url).then(function (resp) {
                    self.vetList = resp.data;
                    arr = resp.data;
                    angular.forEach(self.vetList, function (vet) {
                        getAverageRating(vet);
                        getCountOfRatings(vet);
                        getTopThreeVetsWithHighestRating(vet);
                    });
                });
            }
        };
    }]);