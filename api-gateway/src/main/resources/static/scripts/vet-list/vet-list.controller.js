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

        }
        this.hide = ($event, vetID) => {

            let child = document.getElementsByClassName("m" + vetID)[0];
            child.classList.remove("modalOn");
            child.classList.add("modalOff");
        }

        $scope.vetList = [];

        $http.get('api/gateway/vets').then(function (resp) {
            self.vetList = resp.data;
            arr = resp.data;

            $scope.vetList = resp.data;

            angular.forEach($scope.vetList, function(vet) {
                getCountOfRatings(vet)
            });
        });

        function getCountOfRatings(vet) {
            $http.get('api/gateway/vets/' + vet.vetId + "/ratings/count").then(function (resp) {
                console.log(resp.data)
                vet.count = resp.data;
            });
        }

        $scope.deleteVet = function (vetId) {
            let varIsConf = confirm('Want to delete vet with vetId:' + vetId + '. Are you sure?');
            if (varIsConf) {

                $http.delete('api/gateway/vets/' + vetId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert(vetId + " Deleted Successfully!");
                    console.log(response, 'res');
                    //refresh list
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
                url+= '/active';
            } else if (optionSelection === "Inactive") {
                url+= '/inactive';
            }
            $http.get(url).then(function (resp) {
                self.vetList = resp.data;
                arr = resp.data;
            });
        }
    }]);
