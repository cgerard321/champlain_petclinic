<!--        * Created by IntelliJ IDEA.-->
<!--        * User: @JordanAlbayrak-->
<!--        * Date: 22/09/21-->
<!--        * Ticket: feat(AUTH-CPC-102)_signup_user-->
'use strict';

angular.module('signupForm')
    .controller('SignupFormController', ['$http', '$scope', function ($http, $scope) {

        this.add = () => $http.post('/api/gateway/users/', {
            username: $scope.signup.username,
            password: $scope.signup.password,
            email: $scope.signup.email,
        })
            .then(() => $location.path("/login"))
            .catch(console.log);

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);