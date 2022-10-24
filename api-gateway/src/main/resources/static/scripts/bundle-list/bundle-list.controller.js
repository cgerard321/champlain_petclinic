'use strict';
let arr;
angular.module('bundleList')
    .controller('BundleListController', ['$http', '$scope', function ($http, $scope) {
        var self = this;

                $http.get('api/gateway/bundles').then(function (resp) {
                    self.bundleList = resp.data;
                    arr = resp.data;
                });
                $scope.deleteBundle = function (bundleUUID) {
                    let varIsConf = confirm('Want to delete Bundle with Bundle Id:' + bundleUUID + '. Are you sure?');
                    if (varIsConf) {

                        $http.delete('api/gateway/bundles/' + bundleUUID)
                            .then(successCallback, errorCallback)

                        function successCallback(response) {
                            $scope.errors = [];
                            alert(bundleUUID + " Deleted Successfully!");
                            console.log(response, 'res');
                            //refresh list
                            $http.get('api/gateway/bundles').then(function (resp) {
                                self.bundleList = resp.data;
                                arr = resp.data;
                            });
                        }

                        function errorCallback(error) {
                            alert(data.errors);
                            console.log(error, 'can not get data.');
                        }
                    }
                };

                $scope.refreshList = self.bundleList;

                $scope.ReloadData = function () {
                    let url = 'api/gateway/bundles';
                    let optionSelection = document.getElementById("filterOption").value;
                    if (optionSelection === "Active") {
                        console.log("Get active");
                        url+= '/active';
                    } else if (optionSelection === "Inactive") {
                        console.log("Get inactive");
                        url+= '/inactive';
                    }
                    $http.get(url).then(function (resp) {
                        self.bundleList = resp.data;
                        arr = resp.data;
                    });
                }
            }]);
