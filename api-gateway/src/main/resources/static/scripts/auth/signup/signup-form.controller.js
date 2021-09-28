<!--        * Created by IntelliJ IDEA.-->
<!--        * User: @JordanAlbayrak-->
<!--        * Date: 22/09/21-->
<!--        * Ticket: feat(AUTH-CPC-102)_signup_user-->
'use strict';

angular.module('signupForm')
    .controller('SignupFormController', ['$http', '$scope', function ($http, $scope) {
        $http.get('api/gateway/signup/')
            .then(res => (this.signup = console.log(res) || res.data.content))
            .catch(console.log);

        this.add = () => $http.post('api/gateway/signup/', {
            username: $scope.newUser.username,
            password: $scope.newUser.password,
            email: $scope.newUser.email,
        })
            .then(res => this.singup.push(res.data) && ($scope.newUser.username = ''))
            .catch(console.log);

        this.keypress = ({ originalEvent: { key } }) => key === 'Enter' && this.add()
    }]);