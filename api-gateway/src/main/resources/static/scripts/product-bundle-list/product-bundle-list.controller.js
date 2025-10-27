'use strict';

angular.module('productBundleList')
    .controller('ProductBundleListController', [
        '$http', '$scope', '$stateParams', '$window', '$uibModal', '$interval',
        function ($http, $scope, $stateParams, $window, $uibModal, $interval) {

            var self = this;
            var pageSize = 15;
            self.currentPage = $stateParams.page || 0;
            self.pageSize = $stateParams.size || pageSize;
            self.actualCurrentPageShown = parseInt(self.currentPage) + 1;
            self.totalPages = 9999;

            self.baseUrl = "api/gateway/products/bundles";
            self.lastParams = {
                bundleName: '',
                bundleId: '',
                maxOriginalTotalPrice: '',
                maxBundlePrice: ''
            };
            self.bundleList = [];

            // --- toast helpers ---
            $scope.toast = { visible: false, title: '', lines: [], actions: [] };
            var toastTimer = null;

            function toastShow({ title = '', lines = [], actions = [], autoHideMs = 0 }) {
                if (toastTimer) { clearTimeout(toastTimer); toastTimer = null; }
                $scope.toast.title = title;
                $scope.toast.lines = lines;
                $scope.toast.actions = actions;
                $scope.toast.visible = true;
                if (!actions.length && autoHideMs > 0) toastTimer = setTimeout(toastHide, autoHideMs);
            }

            function toastHide() {
                $scope.toast.visible = false;
                $scope.toast.title = '';
                $scope.toast.lines = [];
                $scope.toast.actions = [];
                if (toastTimer) { clearTimeout(toastTimer); toastTimer = null; }
            }

            $scope.onToastAction = function (index) {
                var action = $scope.toast.actions[index];
                toastHide();
                if (action && typeof action.onClick === 'function') action.onClick();
            };

            // --- delete ---
            function performDelete(bundleId) {
                var url = self.baseUrl + '/' + encodeURIComponent(bundleId);
                return $http.delete(url);
            }

            function scheduleSoftDelete(bundle) {
                bundle.isTemporarilyDeleted = true;
                toastShow({
                    title: 'Deleting in 5 seconds…',
                    lines: [bundle.bundleName],
                    actions: [{
                        label: 'Undo',
                        kind: 'secondary',
                        onClick: function () { bundle.isTemporarilyDeleted = false; }
                    }]
                });

                setTimeout(function () {
                    if (!bundle.isTemporarilyDeleted) return;
                    performDelete(bundle.bundleId)
                        .then(function () {
                            toastShow({ title: 'Deleted', lines: ['Bundle removed.'], autoHideMs: 2000 });
                            self.bundleList = self.bundleList.filter(function(b) { return b.bundleId !== b.bundleId; });
                        })
                        .catch(function (err) {
                            bundle.isTemporarilyDeleted = false;
                            var msg = (err.data && err.data.message) || err.statusText || 'Delete failed';
                            toastShow({ title: 'Error', lines: [msg], autoHideMs: 3000 });
                        });
                }, 5000);
            }

            $scope.deleteBundle = function (bundle) {
                toastShow({
                    title: 'Delete bundle?',
                    lines: ['Are you sure you want to delete "' + bundle.bundleName + '"?'],
                    actions: [
                        { label: 'Delete', kind: 'danger', onClick: function () { scheduleSoftDelete(bundle); } },
                        { label: 'Cancel', kind: 'secondary', onClick: function () {} }
                    ]
                });
            };

            $scope.undoDeleteBundle = function (bundle) {
                bundle.isTemporarilyDeleted = false;
                toastShow({ title: 'Restored', lines: [bundle.bundleName + ' restored (UI only)'], autoHideMs: 2000 });
            };

            // --- filters ---
            $scope.clearBundleQueries = function () {
                self.lastParams = { bundleName: '', bundleId: '', maxOriginalTotalPrice: '', maxBundlePrice: '' };
                fetchBundleList();
            };

            $scope.searchBundle = function (filters) {
                self.lastParams = filters;
                applyClientSideFilter();
            };

            function applyClientSideFilter() {
                var nameFilter = (self.lastParams.bundleName || '').toLowerCase();
                var idFilter = (self.lastParams.bundleId || '').toLowerCase();
                var maxOrig = parseFloat(self.lastParams.maxOriginalTotalPrice || '');
                var maxBundle = parseFloat(self.lastParams.maxBundlePrice || '');

                self.bundleList.forEach(function (b) {
                    b.__hiddenByFilter = false;
                    if (nameFilter && !(b.bundleName && b.bundleName.toLowerCase().includes(nameFilter))) b.__hiddenByFilter = true;
                    if (!b.__hiddenByFilter && idFilter && !(b.bundleId && b.bundleId.toLowerCase().includes(idFilter))) b.__hiddenByFilter = true;
                    if (!b.__hiddenByFilter && !isNaN(maxOrig) && b.originalTotalPrice > maxOrig) b.__hiddenByFilter = true;
                    if (!b.__hiddenByFilter && !isNaN(maxBundle) && b.bundlePrice > maxBundle) b.__hiddenByFilter = true;
                });
            }

            function fetchBundleList() {
                $http.get(self.baseUrl)
                    .then(function (resp) {
                        self.bundleList = (resp.data || []).map(function (bundle) {
                            bundle.originalTotalPrice = parseFloat(bundle.originalTotalPrice || 0).toFixed(2);
                            bundle.bundlePrice = parseFloat(bundle.bundlePrice || 0).toFixed(2);
                            bundle.isTemporarilyDeleted = false;
                            return bundle;
                        });
                        applyClientSideFilter();
                    })
                    .catch(function (error) {
                        console.error('Error loading bundles:', error);
                        toastShow({ title: 'Error', lines: ['Could not load bundles.'], autoHideMs: 3000 });
                    });
            }

            self.nextPage = function () { console.log('Next page'); };
            self.previousPage = function () { console.log('Previous page'); };

            (function init() { fetchBundleList(); })();

        }]);
