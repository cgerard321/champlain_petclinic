'use strict';

angular.module('userNew')
    .controller('AdminUserAddController', ['$http', '$scope', "authProvider", "$window", function ($http, $scope, authProvider, $window) {

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
    }]);
