angular.module('productTypeList')
    .controller('ProductTypeController', ['$http', '$scope', '$stateParams','$window', '$uibModal', '$interval',
        function ($http, $scope, $stateParams, $window, $uibModal, $interval) {
        var self = this;
        const pageSize = 15;
        self.currentPage = $stateParams.page || 0;
        self.pageSize = $stateParams.size || pageSize;
        self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        self.baseUrl = "api/gateway/products/types";

        fetchProductTypesList();

        $scope.toast = { visible: false, title: '', lines: [], actions: [] };
        $scope.toastHide = toastHide;
        let toastTimer = null;

        function toastHide() {
            if (toastTimer) {
                clearTimeout(toastTimer);
                toastTimer = null;
            }
            $scope.toast.visible = false;
            $scope.toast.title = '';
            $scope.toast.lines = [];
            $scope.toast.actions = [];
            $scope.toast.productType = null;
            $scope.toast.isEdit = false;
            $scope.$applyAsync();
        }

        function toastShow({ title = '', lines = [], actions = [], autoHideMs = 0 }) {
            toastHide();

            // Assign new toast content
            $scope.toast.title = title;
            $scope.toast.lines = lines;
            $scope.toast.actions = actions;
            $scope.toast.visible = true;

            // Schedule auto-hide if applicable
            if (autoHideMs > 0) {
                toastTimer = setTimeout(() => {
                    toastHide();
                }, autoHideMs);
            }

            $scope.$applyAsync();
        }


        self.openFormToast = function(productType = null) {
            // If productType is provided → Edit, else → Add
            $scope.toast.productType = productType ? angular.copy(productType) : { typeName: '' };
            $scope.toast.isEdit = !!productType;
            $scope.toast.title = productType ? 'Edit Product Type' : 'Add Product Type';
            $scope.toast.lines = [];
            $scope.toast.actions = [
                {
                    label: 'Cancel',
                    kind: 'secondary',
                    callback: () => toastHide()
                },
                {
                    label: productType ? 'Save Changes' : 'Add Product Type',
                    kind: 'primary',
                    callback: () => {
                        const pt = $scope.toast.productType;
                         if (!pt) return;
                        const payload = { typeName: pt.typeName };

                        if (!$scope.toast.isEdit) {
                            // POST
                            $http.post(self.baseUrl, payload).then(resp => {
                                self.productTypeList.push(resp.data);
                                toastHide();
                            });
                        } else {
                            // PUT
                            $http.put(`${self.baseUrl}/${pt.productTypeId}`, payload).then(resp => {
                                const idx = self.productTypeList.findIndex(p => p.productTypeId === pt.productTypeId);
                                if (idx !== -1) self.productTypeList[idx] = resp.data;
                                toastHide();
                            });
                        }
                    }
                }
            ];
            $scope.toast.visible = true;
        };

        $scope.onToastAction = function(index) {
            const action = $scope.toast.actions[index];
            if (!action) return;
            if (typeof action.onClick === 'function') action.onClick();
            else if (typeof action.callback === 'function') action.callback();
            toastHide();

        };

        function performDelete(productTypeId) {
            const url = self.baseUrl + '/' + encodeURIComponent(productTypeId);
            const cfg = undefined;
            return $http.delete(url, cfg);
        }

        // Schedule soft delete with undo
        function scheduleSoftDelete(productType) {
            productType.isTemporarilyDeleted = true;
            let deleteTimeout;

            const undoAction = {
                label: 'Undo',
                kind: 'secondary',
                onClick: function () {
                    productType.isTemporarilyDeleted = false;
                    clearTimeout(deleteTimeout); // cancel delete
                    toastShow({
                        title: 'Cancelled',
                        lines: ['Deletion undone.'],
                        autoHideMs: 1500
                    });
                }
            };

            toastShow({
                title: 'Deleting in 5 seconds…',
                lines: [productType.typeName],
                actions: [undoAction]
            });

            deleteTimeout = setTimeout(function () {
                if (!productType.isTemporarilyDeleted) return; // user undid it

                performDelete(productType.productTypeId)
                    .then(function () {
                        toastShow({
                            title: 'Success',
                            lines: ['Product Type deleted.'],
                            autoHideMs: 2000
                        });

                        const idx = self.productTypeList.findIndex(
                            p => p.productTypeId === productType.productTypeId
                        );
                        if (idx !== -1) self.productTypeList.splice(idx, 1);
                    })
                    .catch(function (err) {
                        productType.isTemporarilyDeleted = false;
                        const msg = (err.data && err.data.message) || err.statusText || 'Delete failed';
                        toastShow({ title: 'Error', lines: [msg], autoHideMs: 3000 });
                    });
            }, 5000);
        }

        // Delete button click
        self.deleteProductType = function (productType) {
            toastShow({
                title: 'Delete product type?',
                lines: ['Are you sure you want to delete "' + productType.typeName + '"?'],
                actions: [
                    { label: 'Delete', kind: 'danger', onClick: () => scheduleSoftDelete(productType) },
                    { label: 'Cancel', kind: 'secondary', onClick: () => {} }
                ]
            });
        };

        self.undoDelete = function (productType) {
            productType.isTemporarilyDeleted = false;
            toastShow({
                title: 'Restored',
                lines: [productType.typeName + ' was restored.'],
                autoHideMs: 1500
            });
        };

        function fetchProductTypesList() {
                $http.get('api/gateway/products/types').then(function (resp) {
                    self.productTypeList = parseProductsFromResponse(resp.data);
                    if (!self.productTypeList || self.productTypeList.length === 0) {

                        console.log("The productTypes list is empty!");
                    }
                }).catch(function (error) {
                    console.error('An error occurred:', error);
                });

        }
        self.nextPage = function () {
            if (parseInt(self.currentPage) + 1 < self.totalPages) {
                var currentPageInt = parseInt(self.currentPage) + 1
                self.currentPage = currentPageInt.toString();
                updateActualCurrentPageShown();
                //refresh product list
                fetchProductTypesList();
            }
        }

        self.previousPage = function () {
            if (self.currentPage - 1 >= 0) {
                var currentPageInt = parseInt(self.currentPage) - 1
                self.currentPage = currentPageInt.toString();
                updateActualCurrentPageShown();
                // Refresh the owner's list with the new page size
                fetchProductTypesList();
            }
        }

        function updateActualCurrentPageShown() {
            self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
        }

        function parseProductsFromResponse(rawData) {
            let productTypes = [];

            // Split by "\n\n" to get each data line
            let lines = rawData.split('\n\n');

            lines.forEach(line => {
                line = line.trim();
                if (line.startsWith('data:')) {
                    let jsonString = line.substring(5); // remove 'data:'
                    try {
                        let productType = JSON.parse(jsonString);
                        productTypes.push(productType);
                    } catch (e) {
                        console.error('Failed to parse productType:', e, jsonString);
                    }
                }
            });
            return productTypes;
        }
    }]);