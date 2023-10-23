angular.module('visit')
    .controller('VisitController',  ['$http', '$stateParams', function ($http, $stateParams){
        var self = this;

        $http.get('api/gateway/visits/' + $stateParams.visitId).then(function (resp) {
            self.visit = resp.data;
        });


    }])