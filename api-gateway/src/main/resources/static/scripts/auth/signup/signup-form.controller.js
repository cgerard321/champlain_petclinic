<!--        * Created by IntelliJ IDEA.-->
<!--        * User: @JordanAlbayrak-->
<!--        * Date: 22/09/21-->
<!--        * Ticket: feat(AUTH-CPC-102)_signup_user-->
'use strict';

angular.module('signupForm')
    .controller('SignupFormController', ['$http', '$scope', "$location", function ($http, $scope, $location) {

        this.add = () => $http.post('/api/gateway/users/', {
            username: $scope.signup.username,
            password: $scope.signup.password,
            email: $scope.signup.email,
        })
            .then(() => $location.path("/login"))
            .catch(n => {
                $scope.errorMessages = n.data.message.split`\n`;
                console.log(n);
            });

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);