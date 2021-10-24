'use strict';
let arr;
angular.module('vetList')
    .controller('VetListController', ['$http','$scope', function ( $http, $scope) {
        var self = this;

        this.show = ($event,vetID) => {
//               let body = document.getElementsByTagName("body")[0];
                let child = document.getElementsByClassName("m"+vetID)[0];
                let left = $event.pageX;
                  let top = $event.pageY;
                  if(document.documentElement.clientWidth > 960){
                    child.style.left = (left + 221) + 'px';
                  }
                  else{
                    child.style.left = (left + 200) + 'px';
                  }
                  child.style.top = (top+80) + 'px';
               child.classList.remove("modalOff");
               child.classList.add("modalOn");

        }
        this.hide = ($event,vetID) => {

            let child = document.getElementsByClassName("m"+vetID)[0];
            child.classList.remove("modalOn");
            child.classList.add("modalOff");
        }

        $http.get('api/gateway/vets').then(function (resp) {
            self.vetList = resp.data;
            arr = resp.data;
        });
        $scope.deleteVet = function (vetId) {
            let varIsConf = confirm('Want to delete vet with vetId:' + vetId + '. Are you sure?');
            if (varIsConf) {

                $http.delete('api/gateway/vets/' + vetId)
                    .then(successCallback, errorCallback)

                function successCallback(response) {
                    $scope.errors = [];
                    alert(vetId + " Deleted Successfully!");
                    console.log(response, 'res');
                    //refresh list
                    $http.get('api/gateway/vets').then(function (resp) {
                        self.vetList = resp.data;
                        arr = resp.data;
                    });
                }

                function errorCallback(error) {
                    alert(data.errors);
                    console.log(error, 'can not get data.');
                }
            }
        };

        $scope.refreshList = self.vetList;

        $scope.ReloadData = function () {
            self.vetList = FilterList();
            $http.get('api/gateway/vets').then(function (resp) {
                arr = resp.data;
            });
        }
    }]);

function FilterList(){
    let optionSelection = document.getElementById("filterOption").value;
    if(optionSelection === "Available"){
        arr = arr.filter(v => v.isActive === 1);
        return arr;
    }else if(optionSelection === "Unavailable"){
        arr = arr.filter(v => v.isActive === 0);
        return arr;
    }else{
        return arr;
    }
}
