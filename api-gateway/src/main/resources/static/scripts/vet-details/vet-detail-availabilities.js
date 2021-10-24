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
    let onejan = new Date(d.getFullYear(), 0, 1);
    let week = Math.ceil((((d.getTime() - onejan.getTime()) / 86400000) + onejan.getDay() + 1) / 7);

    return week;
}

function displayDays() {
    let visitsDatesNode = document.querySelectorAll(".visitsDates");
    let visitsDatesArray = [];
    visitsDatesArray = convetNodeListIdToArray(visitsDatesArray,visitsDatesNode);

    let date = new Date();
    let onejan = new Date(date.getFullYear(), 0, 1);
    let currentWeek = Math.ceil((((date.getTime() - onejan.getTime()) / 86400000) + onejan.getDay() + 1) / 7);

    let strDays = document.getElementById('workDays').textContent;

    let daysOfWeek = (strDays.replace(/\s+/g, '')).split(',');
    let currentYear = date.getFullYear();


    let visitDaysToRemove = [];
    let dayNb = [];
    for (let i = 0; i < visitsDatesArray.length; i++) {
        if(currentYear === new Date(visitsDatesArray[i]).getFullYear()){
            if(currentWeek === getWeekNumber(new Date(visitsDatesArray[i]))){
                dayNb.push((new Date(visitsDatesArray[i])).getDay() + 1);
            }
        }
    }
    dayNb.forEach(function(item) {
        visitDaysToRemove.push(dayOfWeekAsString(item));
    });
    daysOfWeek = daysOfWeek.filter(item => !visitDaysToRemove.includes(item));

    const targetDiv = document.getElementById("displayNone");
    const btn = document.getElementById("toggle");

    if (targetDiv.style.display === "none") {
        targetDiv.style.display = "block";
        btn.innerText = "Hide availabilities"
    } else {
        targetDiv.style.display = "none";
        btn.innerText = "Show availabilities"
    }

    let days = document.querySelectorAll(".square");

    let daysArray = [];
    daysArray = convetNodeListIdToArray(daysArray, days);
    for (let i = 0; i < daysArray.length; i++) {
        for(let x = 0; x < daysOfWeek.length; x++){
            if(daysArray[i] === daysOfWeek[x]){
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