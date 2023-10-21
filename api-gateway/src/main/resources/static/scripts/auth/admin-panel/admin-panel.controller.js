'use strict';
angular.module('adminPanel')
    .controller('AdminPanelController', ['$http', '$scope', "authProvider", function ($http, $scope, authProvider) {

        var self = this;
        self.users = []

        let eventSource = new EventSource("api/gateway/users")
        eventSource.addEventListener('message', function (event){
            $scope.$apply(function(){
                console.log(event.data)
                self.users.push(JSON.parse(event.data))
            })
        })
        eventSource.onerror = (error) =>{
            if(eventSource.readyState === 0){
                eventSource.close()
                console.log("EventSource was closed by server successfully."+error)
            }else{
                console.log("EventSource error: "+error)
            }
        }

        $scope.startsWith = function (actual, expected) {
            let  lowerStr = (actual + "").toLowerCase();
            let lowerExpected = (expected + "").toLowerCase();
            return lowerStr.indexOf(lowerExpected) === 0;
        };


        $scope.removeUser = function (userid) {
            $http.delete('api/gateway/users/' + userid, {
                headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
                .then(function () {
                $http.get('api/gateway/users', {
                    headers: {'Authorization': "Bearer " + authProvider.getUser().token}})
                    .then(function (resp) {
                    self.users = resp.data;
                });
            });
        };
    }
    ]);
