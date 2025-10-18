'use strict';

angular.module('forgotPwdForm').controller('forgotPwdFormController', [
  '$http',
  '$location',
  '$scope',
  function ($http, $location, $scope) {
    $scope.isLoading = false;
    let loaderDiv = document.getElementById('loaderDiv');
    loaderDiv.style.display = 'none';

    this.forgotPwdPost = () => {
      // console.log removed
      loaderDiv.style.display = 'block';
      // console.log removed
      $http
        .post('/api/gateway/users/forgot_password', {
          email: $scope.forgotPwdPost.email,
          url: window.location.origin + '/#!/reset_password/',
        })

        .then(() => {
          alert('Email was sent !');
          $location.path('/welcome');
        })
        .catch(error => {
          // console.log removed
          alert(
            'Email was not sent !, please try again!\n' + error.data.message
          );
        })
        .finally(() => {
          loaderDiv.style.display = 'none';
        });
    };

    this.keypress = ({ originalEvent: { key } }) =>
      key === 'Enter' && this.add();
  },
]);
