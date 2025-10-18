'use strict';

angular.module('userModule').service('UserService', [
  function () {
    this.getAvailableRoles = function () {
      return ['ADMIN', 'VET', 'OWNER'];
    };
  },
]);
