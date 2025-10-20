'use strict';

angular.module('inventoriesList')
    .controller('InventoriesListController', ['$http', '$scope', '$stateParams', '$state', '$location', function ($http, $scope, $stateParams, $state, $location) {
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
                if (queryString !== '') {
                    queryString += "&";
                }
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
        }

        $scope.fetchInventoryList = function() {
            $http.get('api/gateway/inventories').then(function (resp) {
                self.inventoryList = resp.data;
                arr = resp.data;
            });
        };

        // ---- helpers (local-only; prevent logout on wrong creds) ----
        function getCurrentEmail() {
            try {
                // support both object-based and raw-string storage
                var stored = localStorage.getItem('auth.user');
                if (stored) {
                    var obj = JSON.parse(stored);
                    if (obj && obj.email) return obj.email;
                }
                var raw = localStorage.getItem('email');
                return raw || null;
            } catch (e) { return null; }
        }

        // Temporarily whitelist current route so 401 from login probe won't purge/redirect
        function withTempWhitelist(run) {
            var added = false;
            try {
                var key = $location.path().substring(1); // same logic as interceptor
                if (typeof whiteList !== 'undefined' && !whiteList.has(key)) {
                    whiteList.add(key);
                    added = true;
                }
            } catch (e) {}
            return Promise.resolve()
                .then(run)
                .finally(function () {
                    try {
                        if (added) {
                            var key = $location.path().substring(1);
                            whiteList.delete(key);
                        }
                    } catch (e) {}
                });
        }

        // prompt user and verify creds with up to N retries; never logs the user out
        function promptAndVerify(maxTries) {
            var tries = 0;
            return new Promise(function(resolve, reject) {
                (function ask() {
                    var email = getCurrentEmail() || prompt('Enter your account email to confirm:');
                    if (!email) return reject(new Error('cancelled'));
                    var password = prompt('Enter ADMIN or INVENTORY MANAGER password to confirm:');
                    if (!password) return reject(new Error('cancelled'));

                    withTempWhitelist(function () {
                        return $http.post('/api/gateway/users/login', { email: email, password: password });
                    })
                    .then(function(){ resolve({ email: email, password: password }); })
                    .catch(function(err){
                        tries++;
                        if (err && err.status === 401) {
                            alert('Wrong password. Please try again.');
                            if (tries < maxTries) return ask();
                            return reject(new Error('max-tries'));
                        }
                        alert('Authentication failed. Please try again.');
                        reject(err || new Error('auth-failed'));
                    });
                })();
            });
        }

        // ---- refined delete (confirm -> retrying verify -> delete) ----
        $scope.deleteInventory = function(inventory) {
            var ifConfirmed = confirm('Warning: Deleting this inventory cannot be undone. Continue?');
            if (!ifConfirmed) return;

            promptAndVerify(3)
                .then(function () {
                    return proceedToDelete(inventory);
                })
                .catch(function (e) {
                    if (e && e.message === 'cancelled') return;        // user cancelled
                    if (e && e.message === 'max-tries') { alert('Too many failed attempts.'); return; }
                    // other errors were already alerted
                });
        };

        // kept for compatibility with old UI (not used by refined flow)
        $scope.undoDelete = function(inventory) {
            inventory.isTemporarilyDeleted = false;
        };

        function proceedToDelete(inventory) {
            return $http.delete('api/gateway/inventories/' + inventory.inventoryId)
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

                setTimeout(() => {
                    showNotification(inventory.inventoryCode + " - " + inventory.inventoryName + " has been deleted successfully!");
                    setTimeout(() => {
                        location.reload();
                    }, 1000);
                }, 1000);
            }
            function errorCallback(error) {
                try {
                    alert((error.data && (error.data.errors || error.data.message)) || 'Delete failed');
                } catch (e) {
                    alert('Delete failed');
                }
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
