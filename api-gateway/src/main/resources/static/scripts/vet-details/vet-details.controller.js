'use strict';

angular.module('vetDetails')
    .controller('VetDetailsController', ['$http','$stateParams', function ($http, $stateParams) {
        var self = this;
        let visitsURL = "api/gateway/visits/vets/";
        /* added /{{vet.vetID}} in the url */
        $http.get('api/gateway/vets/'+$stateParams.vetId).then(function (resp) {
            self.vet = resp.data;
        });

        $http.get(visitsURL + "/" + +$stateParams.vetId).then(function (resp) {
            self.visits = resp.data;
        });
    }]);
