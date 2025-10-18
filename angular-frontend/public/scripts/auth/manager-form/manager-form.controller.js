'use strict';

angular.module('managerForm').controller('managerFormController', [
  '$http',
  '$scope',
  '$location',
  function ($http, $scope, $location) {
    let loaderDiv = document.getElementById('loaderDiv');
    loaderDiv.style.display = 'none';

    this.add = () => {
      loaderDiv.style.display = 'block';
      $http
        .post('/api/gateway/users/inventoryManager', {
          username: $scope.signup.username,
          password: $scope.signup.password,
          email: $scope.signup.email,
        })
        .then(() => {
          loaderDiv.style.display = 'none';
          alert('Email was sent !');
          $location.path('/adminPanel');
        })
        .catch(n => {
          loaderDiv.style.display = 'none';
          console.warn(n);
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
