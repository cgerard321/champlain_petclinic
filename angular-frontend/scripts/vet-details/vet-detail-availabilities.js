//takes a day position to convert it into a day of the week
function dayOfWeekAsString(dayIndex) {
    return ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"][dayIndex] || '';
}

//takes a date to figure out the week number so that we can compare it later to the current week
function getWeekNumber(d) {
    let onejan = new Date(d.getFullYear(), 0, 1);
    return Math.ceil((((d.getTime() - onejan.getTime()) / 86400000) + onejan.getDay() + 1) / 7);
}

//function is called by button click on vet detail page
function displayDays() {
    let visitsDatesNode = document.querySelectorAll(".visitsDates");
    let visitsDatesArray = [];
    //converts a node list of id's into a usable array
    visitsDatesArray = convertNodeListIdToArray(visitsDatesArray, visitsDatesNode);

    let date = new Date();
    let onejan = new Date(date.getFullYear(), 0, 1);
    let currentWeek = Math.ceil((((date.getTime() - onejan.getTime()) / 86400000) + onejan.getDay() + 1) / 7);

    //work days of a specific vet as a string
    let strDays = document.getElementById('workDays').textContent;

    //work days of a specific vet as an array
    let daysOfWeek = (strDays.replace(/\s+/g, '')).split(',');

    //used to check if a visit date is on the current year
    let currentYear = date.getFullYear();

    let visitDaysToRemove = [];
    let dayNb = [];
    //this section creates an array of visits positions (0,1,..) that are in the current year and week
    for (let i = 0; i < visitsDatesArray.length; i++) {
        if (currentYear === new Date(visitsDatesArray[i]).getFullYear()) {
            if (currentWeek === getWeekNumber(new Date(visitsDatesArray[i]))) {
                dayNb.push((new Date(visitsDatesArray[i])).getDay() + 1);
            }
        }
    }

    //this creates a new array of actual visits days (Monday, Tuesday, etc) based on the array of visits positions
    dayNb.forEach(function (item) {
        visitDaysToRemove.push(dayOfWeekAsString(item));
    });

    //this removes visit(s) days from the vet availability
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
    daysArray = convertNodeListIdToArray(daysArray, days);
    //if a vet is available, the day will be green
    for (let i = 0; i < daysArray.length; i++) {
        for (let x = 0; x < daysOfWeek.length; x++) {
            if (daysArray[i] === daysOfWeek[x]) {
                days[i].style.backgroundColor = "#c7ffdc";
            }
        }
    }
}

function convertNodeListIdToArray(array, nodeList) {
    for (let i = 0; i < nodeList.length; i++) {
        array.push(nodeList[i].id);
    }
    return array;
}