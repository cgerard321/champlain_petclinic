'use strict';
angular.module('vetList').controller('VetListController', [
  '$http',
  '$scope',
  function ($http, $scope) {
    var self = this;

    this.show = ($event, vetID) => {
      let child = document.getElementsByClassName('m' + vetID)[0];
      let left = $event.pageX;
      let top = $event.clientY;
      if (document.documentElement.clientWidth > 960) {
        child.style.left = left + 221 + 'px';
      }
      if (document.documentElement.clientWidth < 420) {
        child.style.left = 170 + 'px';
      } else if (document.documentElement.clientWidth < 510) {
        child.style.left = left + 334.5 / 2.5 + 'px';
      } else {
        child.style.left = left + 200 + 'px';
      }
      child.style.top = top + 'px';
      child.classList.remove('modalOff');
      child.classList.add('modalOn');
    };
    this.hide = ($event, vetID) => {
      let child = document.getElementsByClassName('m' + vetID)[0];
      child.classList.remove('modalOn');
      child.classList.add('modalOff');
    };

    this.selectedFilter = 'Top Vets';

    $scope.vetList = [];

    $http.get('api/gateway/vets').then(function (resp) {
      self.vetList = resp.data;
      arr = resp.data;

      $scope.vetList = resp.data;

      angular.forEach($scope.vetList, function (vet) {
        getCountOfRatings(vet);
        getAverageRating(vet);
        getTopThreeVetsWithHighestRating(vet);
      });
    });

    function getAverageRating(vet) {
      // console.log removed('Hello ' + vet.vetId);
      $http
        .get('api/gateway/vets/' + vet.vetId + '/ratings/average')
        .then(function (resp) {
          // console.log removed(resp.data);
          vet.showRating = true;
          vet.rating = parseFloat(resp.data.toFixed(1));
        });
    }

    function getTopThreeVetsWithHighestRating(vet) {
      $http.get('api/gateway/vets/topVets').then(function (resp) {
        // console.log removed(resp.data);
        vet.showRating = true;
        vet.rating = parseFloat(resp.data.toFixed(1));
      });
    }

    function getCountOfRatings(vet) {
      $http
        .get('api/gateway/vets/' + vet.vetId + '/ratings/count')
        .then(function (resp) {
          // console.log removed(resp.data);
          vet.count = resp.data;
        });
    }

    $scope.deleteVet = function (vetId) {
      let varIsConf = confirm(
        'Want to delete vet with vetId:' + vetId + '. Are you sure?'
      );
      if (varIsConf) {
        $http.delete('api/gateway/vets/' + vetId).then(
          function successCallback() {
            $scope.errors = [];
            alert(vetId + ' Deleted Successfully!');
            // console.log removed(response, 'res');
            //refresh list
            $http.get('api/gateway/vets').then(function (resp) {
              self.vetList = resp.data;
              arr = resp.data;
            });
          },
          function errorCallback() {
            alert(data.errors);
            // console.log removed(error, 'can not get data.');
          }
        );
      }
    };
    $scope.refreshList = self.vetList;

    $scope.ReloadData = function () {
      let url = 'api/gateway/vets';
      let optionSelection = document.getElementById('filterOption').value;
      if (optionSelection === 'Active') {
        url += '/active';
      } else if (optionSelection === 'Inactive') {
        url += '/inactive';
      } else if (optionSelection === 'Top Vets') {
        url += '/topVets';
      }
      self.selectedFilter = optionSelection;

      $http.get(url).then(function (resp) {
        self.vetList = resp.data;
        arr = resp.data;
        angular.forEach(self.vetList, function (vet) {
          getAverageRating(vet);
          getCountOfRatings(vet);
          getTopThreeVetsWithHighestRating(vet);
        });
      });
    };
  },
]);
