'use strict';

angular.module('vetList')
    .controller('VetListController', ['$http', function ($http) {
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
                let arr = Object.keys(self.vetList);
                console.log(typeof self.vetList)
                console.log(Array.isArray(arr))
                let optionSelection = document.getElementById("filterOption").value;
                if(optionSelection === "All"){
                    self.vetList = resp.data;
                }else if(optionSelection === "Available"){
                    self.vetList = self.vetList.filter(v => v.isActive === 1);
                }else if(optionSelection === "Unavailable"){
                    self.vetList = self.vetList.filter(v => v.isActive === 0);
                }
                console.log(self.vetList);
            });
    }]);


