'use strict';

angular.module('vetForm')
    .controller('VetFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        var vetId = $stateParams.vetId || 0;

        if (!vetId) {
            self.vet = {};
        } else {
            $http.get("api/gateway/vets/" + vetId).then(function (resp) {
                self.vet = resp.data;
            });
        }

        self.submitVetForm = function () {
            var id = self.vet.vetId;
            var req;
            if (id) {
                req = $http.put("api/gateway/vets" + id, self.vet);
            } else {
                req = $http.post("api/gateway/vets", self.vet);
            }

            req.then(function () {
                $state.go('vets');
            }, function (response) {
                var error = response.data;
                alert(error.error + "\r\n" + error.errors.map(function (e) {
                        return e.field + ": " + e.defaultMessage;
                    }).join("\r\n"));
            });
        };
    }]);


angular.module('uploadModule', [])
    .controller('uploadCtrl', [
        '$scope',
        '$upload',
        function ($scope, $upload) {
            $scope.model = {};
            $scope.selectedFile = [];
            $scope.uploadProgress = 0;

            $scope.uploadFile = function () {
                var file = $scope.selectedFile[0];
                $scope.upload = $upload.upload({
                    url: 'api/gateway/vets/',
                    method: 'POST',
                    data: angular.toJson($scope.model),
                    file: file
                }).progress(function (evt) {
                    $scope.uploadProgress = parseInt(100.0 * evt.loaded / evt.total, 10);
                }).success(function (data) {
                    //do something
                });
            };

            $scope.onFileSelect = function ($files) {
                $scope.uploadProgress = 0;
                $scope.selectedFile = $files;
            };
        }
    ])
    .directive('progressBar', [
        function () {
            return {
                link: function ($scope, el, attrs) {
                    $scope.$watch(attrs.progressBar, function (newValue) {
                        el.css('width', newValue.toString() + '%');
                    });
                }
            };
        }
    ]);
