'use strict';

angular.module('petTypeList')
    .controller('PetTypeListController',
        ['$http', '$stateParams', '$scope', '$state',
            function ($http, $stateParams, $scope, $state) {

                var self = this;

                // -------------------------------------------------------------------
                // Add Pet Type Form state
                // -------------------------------------------------------------------
                self.petTypes = [];
                self.showAddForm = false;
                self.newPetType = { name: '', petTypeDescription: '' };

                self.toggleAddForm = function () {
                    self.showAddForm = !self.showAddForm;
                    if (!self.showAddForm) {
                        self.newPetType = { name: '', petTypeDescription: '' };
                    }
                };

                self.addPetType = function () {
                    // Extra check: letters and spaces only
                    var lettersOnly = /^[a-zA-Z\s]+$/;

                    if (!lettersOnly.test(self.newPetType.name) ||
                        !lettersOnly.test(self.newPetType.petTypeDescription)) {
                        alert('Name and Description must contain letters and spaces only.');
                        return;
                    }

                    if (!self.newPetType.name || !self.newPetType.petTypeDescription) {
                        alert('Name and Description are required.');
                        return;
                    }

                    $http.post('api/gateway/owners/petTypes', self.newPetType)
                        .then(function () {
                            alert('Pet type added successfully!');
                            self.showAddForm = false;
                            self.newPetType = { name: '', petTypeDescription: '' };
                            loadDefaultData(); // refresh full list
                        }, function (error) {
                            alert('Failed to add pet type: ' +
                                (error.data.message || 'Unknown error'));
                        });
                };

                self.deletePetType = function (petTypeId) {
                    let isConfirmed = confirm('Are you sure you want to delete this pet type?');
                    if (isConfirmed) {
                        $http.delete('api/gateway/owners/petTypes/' + petTypeId)
                            .then(function () {
                                console.log('Pet type deleted successfully');
                                alert('Pet type deleted successfully!');
                                loadDefaultData();
                            }, function (error) {
                                console.error('Error deleting pet type:', error);
                                alert('Failed to delete pet type: ' +
                                    (error.data.message || 'Unknown error'));
                            });
                    }
                };

                // -------------------------------------------------------------------
                // Pagination + Filtering
                // -------------------------------------------------------------------
                self.currentPage = $stateParams.page || 0;
                self.pageSize = $stateParams.size || 5;
                self.currentPageOnSite = parseInt(self.currentPage) + 1;

                self.petTypeId = null;
                self.name = null;
                self.description = null;
                self.selectedSize = null;

                self.searchActive = false;
                self.allPetTypes = [];

                loadDefaultData();

                function loadDefaultData() {
                    $http.get('api/gateway/owners/petTypes').then(function (resp) {
                        self.allPetTypes = resp.data;
                        self.petTypes = resp.data;

                        self.totalItems = self.allPetTypes.length;
                        self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));

                        applyPagination();
                        updateCurrentPageOnSite();
                    });
                }

                function applyPagination() {
                    var startIndex = self.currentPage * parseInt(self.pageSize);
                    var endIndex = startIndex + parseInt(self.pageSize);
                    self.petTypes = self.allPetTypes.slice(startIndex, endIndex);
                }

                self.searchPetTypesByPaginationAndFilters =
                    function (currentPage = 0, prevOrNextPressed = false) {

                        self.selectedSize = document.getElementById("sizeInput").value;

                        if (!prevOrNextPressed) {
                            self.petTypeId = document.getElementById("petTypeIdInput").value;
                            self.name = document.getElementById("nameInput").value;
                            self.description = document.getElementById("descriptionInput").value;

                            if (checkIfAllEmpty(self.petTypeId, self.name,
                                self.description, self.selectedSize)) {
                                alert("Oops! It seems like you forgot to enter any filter criteria.");
                                return;
                            }
                        }

                        self.searchActive = true;

                        var filteredPetTypes = self.allPetTypes;

                        if (self.petTypeId) {
                            filteredPetTypes = filteredPetTypes.filter(function (petType) {
                                return petType.petTypeId &&
                                    petType.petTypeId.toString().includes(self.petTypeId);
                            });
                        }

                        if (self.name) {
                            filteredPetTypes = filteredPetTypes.filter(function (petType) {
                                return petType.name &&
                                    petType.name.toLowerCase().includes(self.name.toLowerCase());
                            });
                        }

                        if (self.description) {
                            filteredPetTypes = filteredPetTypes.filter(function (petType) {
                                return petType.petTypeDescription &&
                                    petType.petTypeDescription.toLowerCase()
                                        .includes(self.description.toLowerCase());
                            });
                        }

                        if (self.selectedSize) {
                            self.pageSize = self.selectedSize;
                        }

                        if (!prevOrNextPressed) {
                            self.currentPage = 0;
                        }

                        self.totalItems = filteredPetTypes.length;
                        self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));

                        var startIndex = self.currentPage * parseInt(self.pageSize);
                        var endIndex = startIndex + parseInt(self.pageSize);
                        self.petTypes = filteredPetTypes.slice(startIndex, endIndex);

                        updateCurrentPageOnSite();
                    };

                function checkIfAllEmpty(petTypeId, name, description, selectedSize) {
                    return (!petTypeId && !name && !description && !selectedSize);
                }

                self.clearInputAndResetDefaultData = function () {
                    document.getElementById("petTypeIdInput").value = "";
                    document.getElementById("nameInput").value = "";
                    document.getElementById("descriptionInput").value = "";
                    document.getElementById("sizeInput").selectedIndex = 0;

                    self.currentPage = 0;
                    self.pageSize = 5;
                    self.petTypeId = null;
                    self.name = null;
                    self.description = null;
                    self.selectedSize = null;
                    self.searchActive = false;

                    self.petTypes = self.allPetTypes;
                    self.totalItems = self.allPetTypes.length;
                    self.totalPages = Math.ceil(self.totalItems / parseInt(self.pageSize));
                    applyPagination();
                    updateCurrentPageOnSite();

                    alert("All filters have been cleared successfully.");
                };

                self.goNextPage = function () {
                    if (parseInt(self.currentPage) + 1 < self.totalPages) {
                        var currentPageInt = parseInt(self.currentPage) + 1;
                        self.currentPage = currentPageInt.toString();
                        updateCurrentPageOnSite();

                        if (self.searchActive) {
                            self.searchPetTypesByPaginationAndFilters(currentPageInt, true);
                        } else {
                            applyPagination();
                        }
                    }
                };

                self.goPreviousPage = function () {
                    if (self.currentPage - 1 >= 0) {
                        var currentPageInt = parseInt(self.currentPage) - 1;
                        self.currentPage = currentPageInt.toString();
                        updateCurrentPageOnSite();

                        if (self.searchActive) {
                            self.searchPetTypesByPaginationAndFilters(currentPageInt, true);
                        } else {
                            applyPagination();
                        }
                    }
                };

                function updateCurrentPageOnSite() {
                    self.currentPageOnSite = parseInt(self.currentPage) + 1;
                }

            }]);
