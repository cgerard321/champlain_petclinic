'use strict';

angular.module('vetList')
    .controller('VetListController', ['$http', function ($http) {
        var self = this;
        this.buttonChangeState = (v) =>
        {
            if(v)
            {
                $http.get('api/gateway/vets').then(function (resp) {
                    self.vetList = resp.data;
                });
            }
            else
            {
                $http.get('api/gateway/vets/disabled').then(function (resp) {
                    self.vetList = resp.data;
                });
            }
        }
        this.show = ($event,vetID) => {
//               let body = document.getElementsByTagName("body")[0];
            console.log(vetID);
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
        this.test = console.log;

        $http.get('api/gateway/vets').then(function (resp) {
            self.vetList = resp.data;
        });

    }]);
