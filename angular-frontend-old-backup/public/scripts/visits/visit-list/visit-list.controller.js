'use strict';
angular.module('visitList').controller('VisitListController', [
  '$http',
  '$scope',
  '$window',
  function ($http, $scope, $window) {
    let self = this;
    // Lists holding visits for the tables to display
    self.upcomingVisits = [];
    self.confirmedVisits = [];
    self.cancelledVisits = [];
    self.completedVisits = [];

    $scope.showUpcomingVisits = true; //initialize to true to show automatically
    $scope.showConfirmedVisits = true;
    $scope.showCancelledVisits = true;
    $scope.showCompletedVisits = true;

    let url;
    //sorted by order or most to least permissions
    if ($window.localStorage.getItem('roles').includes('ADMIN')) {
      url = 'api/gateway/visits';
    } else if ($window.localStorage.getItem('roles').includes('VET')) {
      url =
        'api/gateway/visits/vets/' +
        $window.localStorage.getItem('practitionerIdAndMonth').split(',')[0];
    } else if ($window.localStorage.getItem('roles').includes('OWNER')) {
      url = 'api/gateway/visits/owners/' + $window.localStorage.getItem('UUID');
    } else {
      url = '';
    }

    let eventSource = new EventSource(url);
    eventSource.addEventListener('message', function (event) {
      $scope.$apply(function () {
        // console.log removed(event.data);
        const visitData = JSON.parse(event.data);
        switch (visitData.status) {
          case 'UPCOMING':
            self.upcomingVisits.push(visitData);
            break;
          case 'CONFIRMED':
            self.confirmedVisits.push(visitData);
            break;
          case 'CANCELLED':
            self.cancelledVisits.push(visitData);
            break;
          case 'COMPLETED':
            self.completedVisits.push(visitData);
            break;
          default:
            break;
        }
      });
    });
    eventSource.onerror = () => {
      if (eventSource.readyState === 0) {
        eventSource.close();
        // console.log removed('EventSource was closed by server successfully.' + error);
      } else {
        // console.log removed('EventSource error: ' + error);
      }
    };

    $scope.confirmVisit = function (visitId, status) {
      // console.log removed(status);
      switch (status) {
        case 'UPCOMING':
          status = 'CONFIRMED';
          break;
        case 'CONFIRMED':
          status = 'COMPLETED';
          break;
        default:
          break;
      }

      // console.log removed(status);
      let putURL = 'api/gateway/visits/' + visitId + '/status/' + status;

      $http.put(putURL, status).then(successCallback, errorCallback);

      function successCallback() {
        $scope.errors = [];
        alert(visitId + ' visit was confirmed successfully');
        // console.log removed(response, 'res');
        delayedReload();
      }
    };

    $scope.cancelVisit = function (visitId, status) {
      let putURL = 'api/gateway/visits/' + visitId + '/status/' + status;
      // console.log removed(putURL);
      $http.put(putURL, status).then(successCallback, errorCallback);

      function successCallback() {
        $scope.errors = [];
        alert(visitId + ' visit was cancelled successfully');
        // console.log removed(response, 'res');
        delayedReload();
      }
    };

    $scope.deleteVisit = function (visitId) {
      let varIsConf = confirm(
        'You are about to delete visit ' +
          visitId +
          '. Is this what you want to do ? '
      );
      if (varIsConf) {
        $http.delete('api/gateway/visits/' + visitId).then(
          function successCallback() {
            $scope.errors = [];
            alert(visitId + ' visit was deleted successfully');
            // console.log removed(response, 'res');
            delayedReload();
          },
          function errorCallback() {
            alert(data.errors);
            // console.log removed(error, 'Could not receive data');
          }
        );
      }
    };

    $scope.deleteAllCancelledVisits = function () {
      let varIsConf = confirm(
        'You are about to delete all canceled visits. Is this what you want to do ? '
      );
      if (varIsConf) {
        $http.delete('api/gateway/visits/cancelled').then(
          function successCallback() {
            $scope.errors = [];
            alert('All canceled visits were deleted successfully');
            // console.log removed(response, 'res');
            delayedReload();
          },
          function errorCallback() {
            alert(data.errors);
            // console.log removed(error, 'Could not receive data');
          }
        );
      }
    };

    $scope.propertyName = 'visitId';
    $scope.reverse = false;
    $scope.upcomingVisits = self.upcomingVisits;
    $scope.sortBy = function (propertyName) {
      $scope.reverse =
        $scope.propertyName === propertyName ? !$scope.reverse : false;
      $scope.propertyName = propertyName;
    };

    function delayedReload() {
      let loadingIndicator = document.getElementById('loadingIndicator');
      loadingIndicator.style.display = 'block';
      setTimeout(function () {
        location.reload();
      }, 150); //delay reload to be more graceful
    }
    function errorCallback(error) {
      alert(error.errors);
      // console.log removed(error, 'Could not receive data');
    }
  },
]);

//     // self.sortFetchedVisits = function() {
//     //     let currentDate = getCurrentDate()
//     //     $.each(self.visits, function(i, visit) {
//     //         let selectedVisitDate = Date.parse(visit.date);
//     //         if(selectedVisitDate >= currentDate) {
//     //             self.upcomingVisits.push(visit)
//     //         } else {
//     //             self.previousVisits.push(visit)
//     //         }
//     //     })
//     // }
//     // function getCurrentDate() {
//     //     let dateObj = new Date()
//     //     var dd = String(dateObj.getDate()).padStart(2, '0')
//     //     var mm = String(dateObj.getMonth() + 1).padStart(2, '0')
//     //     var yyyy = dateObj.getFullYear()
//     //     return Date.parse(yyyy + '-' + mm + '-' + dd)
//     // }
// }])
