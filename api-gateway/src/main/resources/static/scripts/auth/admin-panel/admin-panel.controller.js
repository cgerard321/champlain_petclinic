'use strict';
angular.module('adminPanel')
    .controller('AdminPanelController', ['$http', '$scope', 'authProvider', '$window', function ($http, $scope, authProvider, $window) {

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
        
        $scope.search = function () {
            if ($scope.query === '') {
                $http.get('api/gateway/users', {
                    headers: {'Authorization': "Bearer " + authProvider.getUser().token}
                })
                    .then(function (resp) {
                        self.users = resp.data;
                    });
            } else {
                $http.get('api/gateway/users', {
                    params: { username: $scope.query },
                    headers: {'Authorization': "Bearer " + authProvider.getUser().token}
                })
                    .then(function (resp) {
                        self.users = resp.data;
                    });
            }
        };

        self.showModal = false;
        self.userToDelete = null;
        

        self.showDeleteModal = function(userId) {
            self.userToDelete = userId;
            self.showModal = true;
        };
        

        self.confirmDelete = function() {
            if (!self.userToDelete) return;
            
            $http.delete('api/gateway/users/' + self.userToDelete, {
                headers: {'Authorization': 'Bearer ' + authProvider.getUser().token}
            })
            .then(function() {
                return $http.get('api/gateway/users', {
                    headers: {'Authorization': 'Bearer ' + authProvider.getUser().token}
                });
            })
            .then(function(resp) {
                self.users = resp.data;
                self.showModal = false;
                self.userToDelete = null;
                alert('User has been deleted successfully.');
            })
            .catch(function(error) {
                console.error('Error deleting user:', error);
                alert('Failed to delete user. Please try again.');
            });
        };
        

        self.cancelDelete = function() {
            self.showModal = false;
            self.userToDelete = null;
        };
        

        $scope.removeUser = function(userId) {
            self.showDeleteModal(userId);
        };
        

        $scope.$watch('$ctrl.showModal', function(newVal) {
            if (newVal) {
                document.body.style.overflow = 'hidden';
            } else {
                document.body.style.overflow = 'auto';
            }
        });


    }
    ]);
