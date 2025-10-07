'use strict';

angular.module('inventoriesList')
    .controller('InventoriesListController', ['$http', '$scope', '$stateParams', '$state', function ($http, $scope, $stateParams, $state) {
        var self = this;
        self.currentPage = $stateParams.page = 0
        self.listSize = $stateParams.size = 10
        self.realPage = parseInt(self.currentPage) + 1
        var numberOfPage
        var code
        var name
        var type
        var desc

        getInventoryList()


        $http.get('api/gateway/inventories').then(function (resp) {
            self.inventoryList = resp.data;
            console.log("Resp data: " + resp.data)
            console.log("inventory list: " + self.inventoryList)
        });

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
            $scope.inventoryCode = '';
            $scope.inventoryName = '';
            $scope.inventoryType = '';
            $scope.inventoryDescription = '';
            // Reset the list by searching all inventories again
            $scope.searchInventory('', '', '', '');
        }
//search by inventory field
        $scope.searchInventory = function (inventoryCode, inventoryName, inventoryType, inventoryDescription){
            getInventoryList(inventoryCode, inventoryName, inventoryType, inventoryDescription)
        }
//search by inventory field
        function getInventoryList(inventoryCode, inventoryName, inventoryType, inventoryDescription){
            $state.transitionTo('inventories', {page: self.currentPage, size: self.listSize}, {notify: false});

            code = inventoryCode || "";
            name = inventoryName || "";
            type = inventoryType || "";
            desc = inventoryDescription || "";

            $http.get("api/gateway/inventories/all")
                .then(function(resp) {
                    var filtered = resp.data.filter(function(item) {
                        var codeMatch = !code || !code.trim() ||
                            (item.inventoryCode && item.inventoryCode.toUpperCase().indexOf(code.toUpperCase()) !== -1);
                        var nameMatch = !name || !name.trim() ||
                            (item.inventoryName && item.inventoryName.toLowerCase().indexOf(name.toLowerCase()) !== -1);
                        var typeMatch = !type || !type.trim() ||
                            (item.inventoryType && item.inventoryType === type);
                        var descMatch = !desc || !desc.trim() ||
                            (item.inventoryDescription && item.inventoryDescription.toLowerCase().indexOf(desc.toLowerCase()) !== -1);

                        return codeMatch && nameMatch && typeMatch && descMatch;
                    });

                    numberOfPage = Math.ceil(filtered.length / self.listSize);

                    var startIndex = self.currentPage * self.listSize;
                    var endIndex = startIndex + self.listSize;
                    self.inventoryList = filtered.slice(startIndex, endIndex);
                    arr = filtered;
                })
                .catch(function(error) {
                    alert('An error occurred: ' + error.statusText);
                    self.inventoryList = [];
                });
        }


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

                getInventoryList(code,name, type, desc)
            }
        }

        $scope.pageAfter = function () {
            if (self.currentPage + 1 <= numberOfPage) {
                self.currentPage = (parseInt(self.currentPage) + 1).toString();
                self.realPage = parseInt(self.currentPage) + 1

                getInventoryList(code, name, type, desc)
            }
        }
    }]);
