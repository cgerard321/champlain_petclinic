const date = new Date();

//specify which months goes with which numbers
//ex: 09 = September
const months = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
];

let selectedDate = date.getDate();
let selectedTime;

const renderCalendar = () => {
    date.setDate(1);

    const monthDays = document.querySelector('.days');

    const timeSlots = document.querySelector('.times');

    const lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
    const prevLastDay = new Date(date.getFullYear(), date.getMonth(), 0).getDate();

    const firstDayIndex = date.getDay();
    const lastDayIndex = new Date(date.getFullYear(), date.getMonth() + 1, 0).getDay();

    const nextDays = 7 - lastDayIndex - 1;

    let showSelectedDate = new Date("" + months[date.getMonth()] + " " + selectedDate + ", " + date.getFullYear());
    console.log("Selected Date: " + showSelectedDate);

    let formattedDate = showSelectedDate.toDateString().substring(0, 3) + ", " + showSelectedDate.toDateString().substring(3, 10).trim() + ", " + date.getFullYear();

    let isMonthUnavailable = false;
    if((new Date("" + months[date.getMonth()] + " " + (parseInt(selectedDate) + 1) + ", " + date.getFullYear()) < new Date())) {
        isMonthUnavailable = true;
        formattedDate = date.getFullYear();
    }

    //change currently selected month and date on the title UI elements
    document.querySelector(".date p").innerHTML = formattedDate;
    document.querySelector(".time-date h1").innerHTML = formattedDate;
    document.querySelector(".date h1").innerHTML = months[date.getMonth()];

    //create div elements to input into the html index page
    let days = "";

    //insert the previous days from the previous month
    for(let x = firstDayIndex; x > 0; x--) {
        let d = prevLastDay - x + 1;

        //if date is on the weekend, disable it
        if(CheckWeekend(d, -1)) {
            days += `<div data-target="previous-page-${d}" class="prev-date unavailable day">${d}</div>`;

        } else {
            days += `<div data-target="previous-page-${d}" class="prev-date day">${d}</div>`;
        }
    }

    //insert the actual days of the month
    for(let i = 1; i <= lastDay; i++) {

        //if the dates are on the weekend, disable them so that the user can't select them
        if (CheckWeekend(i, 0)) {
            days += `<div data-target="${i}" class="unavailable day">${i}</div>`;
        }

        //if days are before current date but within the same year, make dates unavailable
        else if((i < new Date().getDate() && date.getMonth() <= new Date().getMonth() && date.getFullYear() === new Date().getFullYear()) ||
            (date.getMonth() < new Date().getMonth() && date.getFullYear() === new Date().getFullYear())) {
            days += `<div data-target="${i}" class="unavailable day">${i}</div>`;
        }

        //if date is before current year, make dates unavailable
        else if(date.getFullYear() < new Date().getFullYear()) {
            days += `<div data-target="${i}" class="unavailable day">${i}</div>`;

        }
        //if a date is selected, show the selected date to the user on the calendar
        else if(selectedDate === i.toString()) {
            days += `<div data-target="${i}" class="date-selected day">${i}</div>`;
        }

        //specify the date of today
        else if (i === new Date().getDate() && date.getMonth() === new Date().getMonth() && new Date().getFullYear() === date.getFullYear()) {
            days += `<div data-target="${i}" class="today day">${i}</div>`;
        }

        //insert the days
        else {
            days += `<div data-target="${i}" class="day">${i}</div>`;
        }
    }

    //insert the days for the next month
    for(let j = 1; j <= nextDays; j++) {
        if(CheckWeekend(j, 1)) {
            days += `<div data-target="next-page-${j}" class="next-date unavailable day">${j}</div>`;

        } else {
            days += `<div data-target="next-page-${j}" class="next-date day">${j}</div>`;
        }
    }

    //upload UI tags for previous month's dates and current dates to the html document (index.hmtl)
    monthDays.innerHTML = days;

    let numberOfDays = document.getElementsByClassName("day").length;
    let numberOfNext = document.getElementsByClassName("next-date").length;

    console.log("number of days from next month: " + numberOfNext);

    //check if all the rows in the calendar are filled (42 fields)
    //if not, add 1 or two more rows to fill in the blank
    if(numberOfDays === 28) {
        for(let j = numberOfNext + 1; j <= 14 + numberOfNext; j++) {
            if(CheckWeekend(j, 1)) {
                days += `<div data-target="next-page-${j}" class="next-date weekend day">${j}</div>`;

            } else {
                days += `<div data-target="next-page-${j}" class="next-date day">${j}</div>`;
            }
        }
    } else if (numberOfDays === 35) {
        for (let j = numberOfNext + 1; j <= 7 + numberOfNext; j++) {
            if(CheckWeekend(j, 1)) {
                days += `<div data-target="next-page-${j}" class="next-date weekend day">${j}</div>`;

            } else {
                days += `<div data-target="next-page-${j}" class="next-date day">${j}</div>`;
            }
        }
    }
    //upload UI tags for next month's dates to the html document (index.html)
    monthDays.innerHTML = days;

    console.log("Number Of days in month " + (date.getMonth()+1) + " is equal to: " + numberOfDays);

    let slots = "";
    let timeIsSelected = false;

    //create time slots
    for(let i = 9; i < 17; i++) {
        let addition = 1;
        let val = i;
        let is1AM = true, is2AM = true;
        let firstTime, secondTime;

        //if time is 1PM, change 13 to 1 PM for english time format
        if(i > 12) {
            val = i - 12;
            is1AM = false;
            is2AM = false;

            //if time is 11AM, make second time period (12) a PM time
        } else if(i === 11) {
            is2AM = false;

            //if time is 12PM, make sure that PM times are posted and that second time is 1PM and not 13PM
        } else if(i === 12) {
            addition = -11;
            is2AM = false;
            is1AM = false;
        }

        //change from AM to PM for the first value
        if(is1AM) {
            firstTime = "AM";
        } else {
            firstTime = "PM";
        }

        //change from AM to PM for the second value
        if(is2AM) {
            secondTime = "AM";
        } else {
            secondTime = "PM";
        }

        //Make time unavailable
        if(isMonthUnavailable) {
            slots += `<div data-target="${i} " class="time-slots time-unavailable">${val} ${firstTime} - ${val + addition} ${secondTime}</div>`;
        }

        //if time slot is selected, show to user his selection via CSS.
        else if(i === parseInt(selectedTime)) {
            slots += `<div data-target="${i} " class="time-slots time-selected">${val} ${firstTime} - ${val + addition} ${secondTime}</div>`;
            timeIsSelected = true;
        }

        //Show user available time ranges
        else {
            slots += `<div data-target="${i} " class="time-slots">${val} ${firstTime} - ${val + addition} ${secondTime}</div>`;
        }
    }

    //make confirmation button appear if time was selected
    if(timeIsSelected) {
        slots += `<div class="confirmation-time">
                    <div class="submitBTN">Confirm</div>
                  </div>`;

        document.querySelector(".time-picker").classList.add("time-picker-selected");
    } else {
        document.querySelector(".time-picker").classList.remove("time-picker-selected");
    }

    //upload UI tags to the html document (index.hmtl)
    timeSlots.innerHTML = slots;
}

//check if a date is on the weekend
const CheckWeekend = (tempDate, month) => {
    return (new Date("" + months[date.getMonth() + month] + " " + tempDate + ", " + date.getFullYear()).getDay() === 0 ||
        new Date("" + months[date.getMonth() + month] + " " + tempDate + ", " + date.getFullYear()).getDay() === 6);

}

//fix selected date if user changes month so that it can't select a weekend
const fixDays = () => {

    if(CheckWeekend(selectedDate, 0) && selectedDate >= "15") {
        selectedDate = (parseInt(selectedDate) - 1).toString();
        console.log("Selection - 1 :" + selectedDate)

        if(CheckWeekend(selectedDate, 0)) {
            selectedDate = (parseInt(selectedDate) - 1).toString();
            console.log("Selection - 1 :" + selectedDate)
        }

    } else if(CheckWeekend(selectedDate, 0) && selectedDate < "15") {
        selectedDate = (parseInt(selectedDate) + 1).toString();
        console.log("Selection + 1 :" + selectedDate)

        if(CheckWeekend(selectedDate, 0)) {
            selectedDate = (parseInt(selectedDate) + 1).toString();
            console.log("Selection + 1 :" + selectedDate)
        }
    }
}


renderCalendar();

//previous page selector
document.querySelector('.prev').addEventListener('click',() => {
    //go to previous month
    date.setMonth(date.getMonth() - 1);

    //fix selected date if user changes month so that it can't select a weekend
    fixDays();

    //reset selected time because new date was selected
    selectedTime = "";

    renderCalendar();
});

//next page selector
document.querySelector('.next').addEventListener('click',() => {
    //go to next month
    date.setMonth(date.getMonth() + 1);

    //fix selected date if user changes month so that it can't select a weekend

    fixDays();

    //reset selected time because new date was selected
    selectedTime = "";

    renderCalendar();
});

//select dates
document.querySelector('.days').addEventListener('click',(event) => {

    let target = event.target.dataset.target;

    //Recognize null clicks
    if(target === undefined){
        console.log("nothing was selected");

        //Previous page if user selects dates from last month
    } else if(target.substring(0,13) === "previous-page") {

        //get selected date for previous date
        selectedDate = target.substring(14, 16).trim();

        date.setMonth(date.getMonth() - 1);

        //reset selected time because new date was selected
        selectedTime = "";

        console.log("currently selected: ", target);

        //Next page if user selects dates from next month
    } else if(target.substring(0,9) === "next-page") {

        //get date for selection for next month
        selectedDate = target.substring(10, 12).trim();

        date.setMonth(date.getMonth() + 1);

        //reset selected time because new date was selected
        selectedTime = "";

        console.log("currently selected: ", target);

        //select the date that was selected by the user
    } else {
        selectedDate = target;
        date.setDate(parseInt(target));

        //reset selected time because new date was selected
        selectedTime = "";

        console.log("currently selected: ", target);
    }

    renderCalendar();
});

//select times for the different available dates
document.querySelector('.times').addEventListener('click',(event) => {

    let selected = event.target.dataset.target;

    // check to be sure that something is selected
    // if nothing is selected, select none or keep previous selection
    if(selected !== undefined) {
        selectedTime = selected;
    }

    console.log("Time selected: " + selectedTime);

    renderCalendar();
});