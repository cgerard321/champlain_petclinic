let strDays = "Wednesday, Monday, Friday";
let daysOfWeek = (strDays.replace(/\s+/g, '')).split(',');
let daysToRemove = 'Monday';

daysOfWeek.push(daysToRemove);

function removeItem(array, item) {
  var i = array.length;

  while (i--) {
      if (array[i] === item) {
          array.splice(array.indexOf(item), 1);
      }
  }
}


removeItem(daysOfWeek, daysToRemove);

console.log(daysOfWeek);

const sorter = {
    "sunday": 0,
    "monday": 1,
    "tuesday": 2,
    "wednesday": 3,
    "thursday": 4,
    "friday": 5,
    "saturday": 6
  }


  daysOfWeek.sort(function sortByDay(a, b) {
    return sorter[a] - sorter[b];
  });


function displayDays() {
    console.log(daysOfWeek);
    let days = document.querySelectorAll(".square");
    let j = 0;
    for (let i = 0; i < days.length; i++) {
        if(days[i].id == daysOfWeek[j]){
            days[i].style.backgroundColor = "#c7ffdc";
            console.log(typeof(daysOfWeek[i]))
            j++;
        }
    }
    const targetDiv = document.getElementById("displayNone");
    const btn = document.getElementById("toggle");


    if (targetDiv.style.display === "none") {
        targetDiv.style.display = "block";
    } else {
        targetDiv.style.display = "none";
    }

}


