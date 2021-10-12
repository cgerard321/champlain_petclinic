'use strict';

angular.module('vetDetails')
    .controller('VetDetailsController', ['$http','$stateParams', function ($http, $stateParams) {
        var self = this;
        let visits;

        /* added /{{vet.vetID}} in the url */
        $http.get('api/gateway/vets/'+$stateParams.vetId).then(function (resp) {
            console.log($stateParams.vetId)
            self.vet = resp.data;
        });

        $http.get("api/gateway/visits/vets/" +$stateParams.vetId).then(function (resp) {
            self.visitsList = resp.data;
            console.log(self.visitsList)
        });

    }]);
