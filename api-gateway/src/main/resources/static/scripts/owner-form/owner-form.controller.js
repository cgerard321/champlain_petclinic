'use strict';

angular.module('ownerForm')
    .controller('OwnerFormController', ["$http", '$state', '$stateParams', function ($http, $state, $stateParams) {
        var self = this;
        var ownerId = $stateParams.ownerId || "";
        var method = $stateParams.method;

        self.notification = {
            show: false,
            message: '',
            type: 'info'
        };

        self.showNotification = function(message, type) {
            self.notification.show = true;
            self.notification.message = message;
            self.notification.type = type;
            
            setTimeout(function() {
                self.hideNotification();
            }, 5000);
        };

        self.hideNotification = function() {
            self.notification.show = false;
            self.notification.message = '';
        };

        if (!ownerId) {
            self.owner = {};
            self.checked = false
        } else {
            $http.get("api/gateway/owners/" + ownerId).then(function (resp) {
                self.owner = resp.data;
            });
            if(method == 'edit')
                self.checked = false
            else
                self.checked = true
        }

        self.submitOwnerForm = function () {
            var id = self.owner.ownerId;
            console.log(self.owner);
            var req;
            if (id){
                if(method == 'edit')
                    req = $http.put("api/gateway/owners/" + id, self.owner);
                else
                    req = $http.delete("api/gateway/owners/" + id, self.owner)
            }
            else
                req = $http.post("api/gateway/owners", self.owner);

            req.then(function () {
                $state.go('owners');
            }, function (response) {
                var error = response.data;
                error.errors = error.errors || [];
                self.showNotification(error.error + "\r\n" + error.errors.map(function (e) {
                    return e.field + ": " + e.defaultMessage;
                }).join("\r\n"), 'error');
            });
        };
    }]);