angular.module('verification')
    .controller('VerificationController', ['$http', '$scope', '$routeParams', "$location", function ($http, $scope, $routeParams, $location) {

        this.add = () => $http.get('/api/gateway/users/verification/' + $routeParams.token)
            .then(() => $location.path("/login"))
            .catch(n => {
                $scope.errorMessage = n.data.message;
                console.log(n);
            });

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);