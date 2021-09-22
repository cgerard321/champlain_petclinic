'use strict';

app = angular.module('vetList')
.controller('VetListController', ['$http', function ($http) {
        var self = this;
        this.show = ($event,vetID) => {
//               let body = document.getElementsByTagName("body")[0];
            console.log(vetID);
                let child = document.getElementsByClassName("m"+vetID)[0];
                let left = $event.pageX;
                  let top = $event.pageY;
                  child.style.left = (left + 390) + 'px';
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
