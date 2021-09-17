'use strict';

angular.module('signupForm')
    .controller('SignupFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;

        var userId = $stateParams.userId || 0;

        if (!userId) {
            self.user = {};
        }
        //  else {
        //     $http.get("api/gateway/customer/owners/" + ownerId).then(function (resp) {
        //         self.owner = resp.data;
        //     });
        //}

        // self.submitOwnerForm = function () {
        //     var id = self.owner.id;
        //     var req;
        //     if (id) {
        //         req = $http.put("api/gateway/customer/owners/" + id, self.owner);
        //     } else {
        //         req = $http.post("api/gateway/customer/owners", self.owner);
        //     }
        //
        //     req.then(function () {
        //         $state.go('owners');
        //     }, function (response) {
        //         var error = response.data;
        //         alert(error.error + "\r\n" + error.errors.map(function (e) {
        //             return e.field + ": " + e.defaultMessage;
        //         }).join("\r\n"));
        //     });
        // };
    }]);
