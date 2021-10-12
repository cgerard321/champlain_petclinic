function removeItem(array, item) {
    var i = array.length;

    while (i--) {
        if (array[i] === item) {
            array.splice(array.indexOf(item), 1);
        }
    }
}


function dayOfWeekAsString(dayIndex) {
    return ["Sunday", "Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"][dayIndex] || '';
}

function getWeekNumber(d){
    // d = new Date();
    let onejan = new Date(d.getFullYear(), 0, 1);
    let week = Math.ceil((((d.getTime() - onejan.getTime()) / 86400000) + onejan.getDay() + 1) / 7);

    console.log(week);
    return week;
}

function displayDays() {
    let date = new Date();
    let onejan = new Date(date.getFullYear(), 0, 1);
    let currentWeek = Math.ceil((((date.getTime() - onejan.getTime()) / 86400000) + onejan.getDay() + 1) / 7);

    let strDays = "Wednesday, Monday, Friday";
    let daysOfWeek = (strDays.replace(/\s+/g, '')).split(',');
    let daysToRemove = new Date('2021-10-11 00:00:00.0');


    let daysToRemoveYear= daysToRemove.getFullYear();
    let currentYear = date.getFullYear();
    console.log("year: " + daysToRemoveYear);

    let nbDayWeekToRemove = daysToRemove.getDay();

    console.log(nbDayWeekToRemove);


    let DayWeekToRemove = dayOfWeekAsString(nbDayWeekToRemove);

    getWeekNumber(daysToRemove);
    console.log("current week: " + currentWeek)
    if(currentYear == daysToRemoveYear){
        if(currentWeek == getWeekNumber(daysToRemove)){
            console.log("Works")
            removeItem(daysOfWeek, DayWeekToRemove);
        }
    }


    const targetDiv = document.getElementById("displayNone");
    const btn = document.getElementById("toggle");

    if (targetDiv.style.display === "none") {
        targetDiv.style.display = "block";
        btn.innerText = "Hide availabilities"
    } else {
        targetDiv.style.display = "none";
        btn.innerText = "Show availabilities"
    }

    console.log(daysOfWeek);
    let days = document.querySelectorAll(".square");
    let daysArray = [];
    daysArray = convetNodeListIdToArray(daysArray, days);
    console.log(daysArray);
    for (let i = 0; i < daysArray.length; i++) {
        for(let x = 0; x < daysOfWeek.length; x++){
            if(daysArray[i] == daysOfWeek[x]){
                days[i].style.backgroundColor = "#c7ffdc";
            }
        }
    }
}

function convetNodeListIdToArray(array, nodeList){
    for(let i = 0; i < nodeList.length; i++){
        array.push(nodeList[i].id);
    }
    return array;
}