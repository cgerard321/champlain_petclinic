/**
 * Created by IntelliJ IDEA.
 * User: @JordanAlbayrak
 * Date: 22/09/21
 * Ticket: feat(AUTH-CPC-102)_signup_user
 */
'use strict';

angular.module('signupForm').controller('SignupFormController', [
  '$http',
  '$scope',
  '$location',
  function ($http, $scope, $location) {
    let loaderDiv = document.getElementById('loaderDiv');
    loaderDiv.style.display = 'none';

    this.add = () => {
      loaderDiv.style.display = 'block';
      $http
        .post('/api/gateway/users', {
          username: $scope.signup.username,
          password: $scope.signup.password,
          email: $scope.signup.email,
          owner: {
            firstName: $scope.signup.firstName,
            lastName: $scope.signup.lastName,
            address: $scope.signup.address,
            city: $scope.signup.city,
            province: $scope.signup.province,
            telephone: $scope.signup.telephone,
          },
        })
        .then(() => {
          loaderDiv.style.display = 'none';
          alert('Email was sent !');
          $location.path('/login');
        })
        .catch(n => {
          loaderDiv.style.display = 'none';
          // console.log removed
          try {
            $scope.errorMessages = n.data.password.split`\n`;
          } catch (e) {
            $scope.errorMessages = n.data.message.split`\n`;
          }
        });
    };

    this.keypress = ({ originalEvent: { key } }) =>
      key === 'Enter' && this.add();
  },
]);
