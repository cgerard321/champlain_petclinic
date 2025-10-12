'use strict';

angular.module('inventoriesList')
    .controller('InventoriesListController', ['$http', '$scope', '$stateParams', '$state', function ($http, $scope, $stateParams, $state) {
        var self = this;
        self.currentPage = $stateParams.page = 0
        self.listSize = $stateParams.size = 10
        self.realPage = parseInt(self.currentPage) + 1
        var numberOfPage
        var name
        var code
        var type
        var desc

        getInventoryList()
        $http.get('api/gateway/inventories').then(function (resp) {
            self.inventoryList = resp.data;
            console.log("Resp data: " + resp.data)
            console.log("inventory list: " + self.inventoryList)
        });
        $scope.inventoryTypeOptions = []
        //custom types handler
        $http.get("api/gateway/inventories/types").then(function (resp) {
            //Includes all types inside the array
            resp.data.forEach(function (type) {
                $scope.inventoryTypeOptions.push(type.type);
            })});

        //clear inventory queries
        $scope.clearQueries = function (){
            // Clear the input fields
            $scope.inventoryName = '';
            $scope.inventoryCode = '';
            $scope.inventoryType = '';
            $scope.inventoryDescription = '';
            // Reset the list by searching all inventories again
            $scope.searchInventory('', '', '');
        }
//search by inventory field
        $scope.searchInventory = function (inventoryCode, inventoryName, inventoryType, inventoryDescription){
            getInventoryList(inventoryCode, inventoryName, inventoryType, inventoryDescription)
        }
//search by inventory field
        function getInventoryList(inventoryCode, inventoryName, inventoryType, inventoryDescription){

            $state.transitionTo('inventories', {page: self.currentPage, size: self.listSize}, {notify: false});
            var queryString = '';
            name = ""
            code = ""
            type = ""
            desc = ""

            if (inventoryCode != null && inventoryCode !== '') {
                code = inventoryCode.toUpperCase();
                queryString += "inventoryCode=" + code;
            }

            if (inventoryName != null && inventoryName !== '') {
                name = inventoryName
                queryString += "inventoryName=" + inventoryName;
            }

            if (inventoryType) {
                if (queryString !== '') {
                    queryString += "&";
                }
                type = inventoryType
                queryString += "inventoryType=" + inventoryType;
            }

            if (inventoryDescription) {
                if (queryString !== '') {
                    queryString += "&";
                }
                desc = inventoryDescription
                queryString += "inventoryDescription=" + inventoryDescription;
            }

            if (queryString !== '') {
                self.currentPage = 0
                self.realPage = parseInt(self.currentPage) + 1

                $http.get("api/gateway/inventories?page=" + self.currentPage + "&size=" + self.listSize + "&" + queryString)
                    .then(function(resp) {
                        numberOfPage = Math.ceil(resp.data.length / 10)
                        self.inventoryList = resp.data;
                        arr = resp.data; // Ensure 'arr' is declared or handled accordingly
                    })
                    .catch(function(error) {
                        if (error.status === 404) {
                            alert('inventory not found.');
                        } else {
                            alert('An error occurred: ' + error.statusText);
                        }
                    });
            } else {
                $http.get("api/gateway/inventories?page=" + self.currentPage + "&size=" + self.listSize)
                    .then(function(resp) {
                        numberOfPage = Math.ceil(resp.data.length / 10)
                        self.inventoryList = resp.data;
                        arr = resp.data; // Ensure 'arr' is declared or handled accordingly
                    })
                    .catch(function(error) {
                        if (error.status === 404) {
                            alert('inventory not found.');
                        } else {
                            alert('An error occurred: ' + error.statusText);
                        }
                    });
            }
        };


        $scope.deleteAllInventories = function () {
            let varIsConf = confirm('Are you sure you want to clear all entries from the inventory?');
            if (varIsConf) {
                $http.delete('api/gateway/inventories')
                    .then(function(response) {
                        alert("All inventory entries have been cleared!");

                        $http.get('api/gateway/inventories').then(function (resp) {
                            self.inventoryList = [];
                        });

                    }, function(error) {
                        alert(error.data.errors);
                        console.log(error, 'Failed to clear inventory entries.');
                    });
            }
        };

        $scope.fetchInventoryList = function() {
            $http.get('api/gateway/inventories').then(function (resp) {
                self.inventoryList = resp.data;
                arr = resp.data;
            });
        };

        $scope.deleteInventory = function(inventory) {
            let ifConfirmed = confirm('Are you sure you want to remove this inventory?');
            if (ifConfirmed) {
                // Step 1: Mark as temporarily deleted on frontend.
                inventory.isTemporarilyDeleted = true;

                // Display an Undo button for say, 5 seconds.
                setTimeout(function() {
                    if (inventory.isTemporarilyDeleted) {
                        // If it's still marked as deleted after 5 seconds, proceed with actual deletion.
                        proceedToDelete(inventory);
                    }
                }, 5000);  // 5 seconds = 5000ms.
            }
        };

        $scope.undoDelete = function(inventory) {
            inventory.isTemporarilyDeleted = false;
            // Hide the undo button.
        };

        function proceedToDelete(inventory) {
            if (!inventory.isTemporarilyDeleted) return;  // In case the user clicked undo just before the timeout.

            $http.delete('api/gateway/inventories/' + inventory.inventoryId)
                .then(successCallback, errorCallback)

            function showNotification(message) {
                const notificationElement = document.getElementById('notification');
                notificationElement.innerHTML = message;
                notificationElement.style.display = 'block';

                setTimeout(() => {
                    notificationElement.style.display = 'none';
                }, 5000);  // Hide after 5 seconds
            }

            function successCallback(response) {
                $scope.errors = [];
                console.log(response, 'res');

                // After deletion, wait for a short moment (e.g., 1 second) before showing the notification
                setTimeout(() => {
                    showNotification(inventory.inventoryCode + " - " + inventory.inventoryName + " has been deleted successfully!");
                    // Then, after displaying the notification for 5 seconds, reload the page
                    setTimeout(() => {
                        location.reload();
                    }, 1000);
                }, 1000);  // Wait for 1 second before showing notification
            }
            function errorCallback(error) {
                // If the error message is nested under 'data.errors' in your API response:
                alert(error.data.errors);
                console.log(error, 'Data is inaccessible.');
            }
        }


        $scope.pageBefore = function () {
            if (self.currentPage - 1 >= 0){
                self.currentPage = (parseInt(self.currentPage) - 1).toString();
                self.realPage = parseInt(self.currentPage) + 1

                getInventoryList(name, type, desc)
            }
        }

        $scope.pageAfter = function () {
            if (self.currentPage + 1 <= numberOfPage) {
                self.currentPage = (parseInt(self.currentPage) + 1).toString();
                self.realPage = parseInt(self.currentPage) + 1

                getInventoryList(name, type, desc)
            }
        }

        $scope.inventoryType = '';


        $scope.$watch('inventoryType', function(newType, oldType) {
            if (newType !== oldType) {
                $scope.searchInventory($scope.inventoryCode || '', $scope.inventoryName || '', newType, $scope.inventoryDescription || '');
            }
        });
    }]);
