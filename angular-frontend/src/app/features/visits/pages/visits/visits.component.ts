import { Component, OnInit, inject, ViewEncapsulation, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { VisitApiService } from '../../api/visit-api.service';
import { VisitRequest, Owner, Pet, Vet } from '../../models/visit.model';
import { BillApiService } from '../../../bills/api/bill-api.service';
import { BillRequest } from '../../../bills/models/bill.model';

@Component({
  selector: 'app-visits',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-calendar">
      <div class="calendar">
        <div class="month">
          <i class="fas fa-angle-left prev" (click)="previousMonth()"></i>
          <div class="date">
            <h1></h1>
            <p></p>
          </div>
          <i class="fas fa-angle-right next" (click)="nextMonth()"></i>
        </div>
        <div class="weekdays">
          <div>Sun</div>
          <div>Mon</div>
          <div>Tue</div>
          <div>Wed</div>
          <div>Thu</div>
          <div>Fri</div>
          <div>Sat</div>
        </div>
        <div class="days"></div>

        <div class="time-picker">
          <div class="time-date">
            <i class="fas fa-angle-left prev-day"></i>
            <div class="title-date">
              <h1></h1>
            </div>
            <i class="fas fa-angle-right next-day"></i>
          </div>
          <div class="times"></div>
        </div>
      </div>
    </div>

    <h2>Visits</h2>

    <form id="visitForm" (ngSubmit)="submit()" onsubmit="return false;">
      <!-- Sensitive Action Confirmation Modal -->
      <div
        class="visitModal fade"
        id="confirmationModal"
        tabindex="-1"
        role="dialog"
        aria-hidden="true"
      >
        <div class="modal-dialog modal-dialog-centered" role="document">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="confirmationModalTitle">PLACEHOLDER</h5>
              <button type="button" class="btn-close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
              </button>
            </div>
            <div class="modal-body">
              <p id="confirmationModalBody">PLACEHOLDER</p>
            </div>
            <div class="modal-footer">
              <button
                type="button"
                class="btn btn-secondary"
                id="confirmationModalDismissButton"
                data-dismiss="modal"
              >
                Cancel
              </button>
              <button
                type="button"
                class="btn btn-primary"
                id="confirmationModalConfirmButton"
                (click)="completeFormAction()"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="form-group">
        <label for="selectedVet">Practitioner</label>
        <select
          id="selectedVet"
          [(ngModel)]="practitionerId"
          (change)="loadVetInfo(); getVisitsForPractitionerIdAndMonth()"
          [ngModelOptions]="{ standalone: true }"
        >
          <option disabled selected value>Please Select A Veterinarian</option>
          <option
            *ngFor="let p of vets"
            [value]="p.vetId"
            [attr.data-target]="p.firstName + ' ' + p.lastName"
          >
            {{ p.firstName }} {{ p.lastName }}
          </option>
        </select>

        <br />

        <label for="vetPhoneNumber" style="margin-top: 10px;">Phone Number:</label>
        <input
          id="vetPhoneNumber"
          disabled="disabled"
          style="background-color: #dbd9ce;"
          [value]="selectedVet?.phoneNumber"
        />

        <label for="vetEmailAddress">Email Address:</label>
        <input
          id="vetEmailAddress"
          disabled="disabled"
          style="background-color: #dbd9ce;"
          [value]="selectedVet?.email"
        />

        <label for="vetSpecialties">Specialties:</label>
        <input
          id="vetSpecialties"
          disabled="disabled"
          style="background-color: #dbd9ce;"
          [value]="getVetSpecialties()"
        />

        <label for="vetWorkdays">Workdays:</label>
        <input
          id="vetWorkdays"
          disabled="disabled"
          style="background-color: #dbd9ce;"
          [value]="getVetWorkdays()"
          (change)="selectChanged()"
        />
      </div>

      <div class="form-group">
        <!--Owners Drop down-->
        <label for="selectedOwner">Owner</label>
        <select
          id="selectedOwner"
          [(ngModel)]="ownerId"
          (change)="loadOwnerInfo()"
          [ngModelOptions]="{ standalone: true }"
        >
          <option disabled selected value="">Please Select An Owner</option>
          <option *ngFor="let o of owners" [value]="o.ownerId">
            {{ o.firstName }} {{ o.lastName }}
          </option>
        </select>
        <!--Display pets drop down-->
        <label for="selectedPet">Pet</label>
        <select
          id="selectedPet"
          [(ngModel)]="petId"
          (change)="logPetId()"
          [ngModelOptions]="{ standalone: true }"
        >
          <option disabled selected value="">Please Select a Pet</option>
          <option *ngFor="let p of pets" [value]="p.petId">{{ p.name }}</option>
        </select>
      </div>

      <div class="form-group" style="margin-top: 12px">
        <label for="selectedVisitType">Visit Type:</label>
        <select
          id="selectedVisitType"
          name="selectedVisitType"
          [(ngModel)]="visitType"
          [ngModelOptions]="{ standalone: true }"
        >
          <option disabled selected value>Please Select A Visit Type</option>
          <option value="Examinations">Examinations</option>
          <option value="Injury">Injury</option>
          <option value="Medical">Medical</option>
          <option value="Chronic">Chronic</option>
          <option value="Consultations">Consultations</option>
          <option value="Operations">Operations</option>
        </select>
      </div>

      <div class="form-group" style="margin-top: 4px">
        <label for="date_input">Date</label>
        <input
          id="date_input"
          type="date"
          class="form-control"
          [(ngModel)]="selectedDate"
          disabled
          [ngModelOptions]="{ standalone: true }"
        />
      </div>

      <div class="form-group">
        <label for="description_textarea">Description</label>
        <textarea
          id="description_textarea"
          class="form-control"
          [(ngModel)]="desc"
          style="resize:vertical;"
          [ngModelOptions]="{ standalone: true }"
        ></textarea>
      </div>

      <div class="form-group" style="margin-top: 6px; margin-bottom: 6px">
        <button
          id="submit_button"
          (click)="showConfirmationModal($event)"
          class="btn btn-default"
          type="button"
          style="border: 1px solid gray; border-radius: 4px"
        >
          Add New Visit
        </button>
        <button
          id="cancel_button"
          (click)="resetForm()"
          class="btn btn-default"
          type="button"
          style="visibility: hidden; border: 1px solid gray; border-radius: 4px"
        >
          Cancel
        </button>
      </div>
    </form>

    <!-- Notifications Container-->
    <h3 style="margin-top: 12px">Notifications</h3>
    <div id="alertsContainer" class="form-group"></div>
    <button
      (click)="goBack()"
      class="btn btn-default"
      type="button"
      style="border: 1px solid gray; border-radius: 4px"
    >
      Back
    </button>
  `,
  styles: [
    `
      @import url('/css/petclinic.css');
      @import url('/css/visitsCalendarStyle.css');
      @import url('/css/addVisitFormStyle.css');
    `,
  ],
  encapsulation: ViewEncapsulation.None,
})
export class VisitsComponent implements OnInit, AfterViewInit {
  private visitApi = inject(VisitApiService);
  private billApi = inject(BillApiService);
  private router = inject(Router);

  // Form data
  practitionerId: string = '';
  ownerId: string = '';
  petId: string = '';
  visitType: string = '';
  selectedDate: string = '';
  desc: string = '';
  chosenDate: Date | null = null;
  chosenTime: string | null = null;

  // Data arrays
  vets: Vet[] = [];
  owners: Owner[] = [];
  pets: Pet[] = [];
  selectedVet: Vet | null = null;

  // Calendar variables
  private date = new Date();
  private selectedDateNum: number = this.date.getDate();
  private selectedTime: string = '';
  private availableDays: number[] = [];
  private vetAvailabilityStr: string = '';
  private nameOnCalenderStr: string = '';
  private availabilities: number[] = [];

  constructor() {}

  ngOnInit(): void {
    this.loadVets();
    this.loadOwners();
    this.initializeCalendar();
  }

  ngAfterViewInit(): void {
    // Ensure DOM is ready before initializing calendar
    setTimeout(() => {
      this.initializeCalendarScript();
      this.renderCalendar(); // Force initial render
      this.attachEventListeners(); // Attach event listeners after rendering
    }, 100);
  }

  private initializeCalendar(): void {
    this.selectedDateNum = this.date.getDate();
    this.selectedTime = '';
    this.availableDays = [];
    this.vetAvailabilityStr = '';
    this.nameOnCalenderStr = '';
    this.availabilities = [];
  }

  private initializeCalendarScript(): void {
    // Initialize calendar after DOM is ready
    setTimeout(() => {
      this.renderCalendar();
      this.attachEventListeners();
    }, 100);
  }

  private renderCalendar(): void {
    const months = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ];

    this.date.setDate(1);
    const monthDays = document.querySelector('.days');
    const timeSlots = document.querySelector('.times');

    if (!monthDays || !timeSlots) return;

    const lastDay = this.getDateTemplate(1).getDate();
    const prevLastDay = this.getDateTemplate(0).getDate();
    const firstDayIndex = this.date.getDay();
    const lastDayIndex = this.getDateTemplate(1).getDay();
    const nextDays = 7 - lastDayIndex - 1;

    localStorage.setItem('practitionerIdAndMonth', this.getFormatPractitionerIdAndMonth());

    const showSelectedDate = new Date(
      '' +
        months[this.date.getMonth()] +
        ' ' +
        this.selectedDateNum +
        ', ' +
        this.date.getFullYear()
    );
    let formattedDate =
      showSelectedDate.toDateString().substring(0, 3) +
      ', ' +
      showSelectedDate.toDateString().substring(3, 10).trim() +
      ', ' +
      this.date.getFullYear();

    let isMonthUnavailable = false;
    if (
      new Date(
        '' +
          months[this.date.getMonth()] +
          ' ' +
          (parseInt(this.selectedDateNum.toString()) + 1) +
          ', ' +
          this.date.getFullYear()
      ) < new Date()
    ) {
      isMonthUnavailable = true;
      formattedDate = this.date.getFullYear().toString();
    }

    this.availableDays = [];
    const vetWorkDays: number[] = [];
    let days = '';

    let vetsArray: string[] | null = null;
    if (this.vetAvailabilityStr !== undefined && this.vetAvailabilityStr !== null) {
      vetsArray = this.vetAvailabilityStr.split(',');
    }

    if (vetsArray !== null) {
      for (const d of vetsArray) {
        switch (d.toLowerCase().trim()) {
          case 'monday':
            vetWorkDays.push(1);
            break;
          case 'tuesday':
            vetWorkDays.push(2);
            break;
          case 'wednesday':
            vetWorkDays.push(3);
            break;
          case 'thursday':
            vetWorkDays.push(4);
            break;
          case 'friday':
            vetWorkDays.push(5);
            break;
          default:
            break;
        }
      }
    } else {
      formattedDate = this.date.getFullYear().toString();
      this.selectedDateNum = null;
    }

    if (this.nameOnCalenderStr === undefined) {
      this.nameOnCalenderStr = this.date.getFullYear().toString();
    }

    // Update UI elements
    const dateP = document.querySelector('.date p');
    const timeDateH1 = document.querySelector('.time-date h1');
    const dateH1 = document.querySelector('.date h1');

    if (dateP) dateP.innerHTML = formattedDate;
    if (timeDateH1) timeDateH1.innerHTML = this.nameOnCalenderStr;
    if (dateH1) dateH1.innerHTML = months[this.date.getMonth()];

    // Generate calendar days
    for (let x = firstDayIndex; x > 0; x--) {
      const d = prevLastDay - x + 1;
      if (this.CheckWeekend(d, -1)) {
        days += `<div id="day${d}" data-target="previous-page-${d}" class="prev-date unavailable day">${d}</div>`;
      } else {
        days += `<div id="day${d}" data-target="previous-page-${d}" class="prev-date day">${d}</div>`;
      }
    }

    // Generate current month days
    for (let i = 1; i <= lastDay; i++) {
      let workDay = false;
      for (const w of vetWorkDays) {
        if (this.getWeekDay(i, 0) === w) {
          workDay = true;
        }
      }

      let scheduledVisits = false;
      if (this.availabilities != null) {
        for (const a of this.availabilities) {
          if (a === i) {
            scheduledVisits = true;
          }
        }
      }

      if (
        this.CheckWeekend(i, 0) &&
        this.getDateFullFormatEqualsTo(i) &&
        this.vetAvailabilityStr !== null
      ) {
        days += `<div id="day${i}" data-target="${i}" class="today_weekend day">${i}</div>`;
      } else if (
        this.CheckWeekend(i, 0) ||
        this.vetAvailabilityStr === null ||
        (i < new Date().getDate() &&
          this.date.getMonth() <= new Date().getMonth() &&
          this.date.getFullYear() === new Date().getFullYear()) ||
        (this.date.getMonth() < new Date().getMonth() &&
          this.date.getFullYear() === new Date().getFullYear()) ||
        this.date.getFullYear() < new Date().getFullYear()
      ) {
        if (i.toString() === this.selectedDateNum.toString()) {
          days += `<div id="day${i}" data-target="${i}" class="editSelected unavailable day">${i}</div>`;
        } else {
          days += `<div id="day${i}" data-target="${i}" class="unavailable day">${i}</div>`;
        }
      } else if (workDay === false || scheduledVisits === true) {
        if (i.toString() === this.selectedDateNum.toString()) {
          days += `<div id="day${i}" data-target="${i}" class="editSelected holiday day">${i}</div>`;
        } else if (this.getDateFullFormatEqualsTo(i)) {
          days += `<div id="day${i}" data-target="${i}" class="today_holiday day">${i}</div>`;
        } else {
          days += `<div id="day${i}" data-target="${i}" class="holiday day">${i}</div>`;
        }
      } else if (this.selectedDateNum === i) {
        days += `<div id="day${i}" data-target="${i}" class="date-selected day">${i}</div>`;
        this.availableDays.push(i);
      } else if (this.getDateFullFormatEqualsTo(i)) {
        days += `<div id="day${i}" data-target="${i}" class="today day">${i}</div>`;
      } else {
        days += `<div id="day${i}" data-target="${i}" class="day">${i}</div>`;
        this.availableDays.push(i);
      }
    }

    // Generate next month days
    for (let j = 1; j <= nextDays; j++) {
      if (this.CheckWeekend(j, 1)) {
        days += `<div id="day${j}" data-target="next-page-${j}" class="next-date unavailable day">${j}</div>`;
      } else {
        days += `<div id="day${j}" data-target="next-page-${j}" class="next-date day">${j}</div>`;
      }
    }

    monthDays.innerHTML = days;

    // Generate time slots
    let slots = '';
    let timeIsSelected = false;

    for (let i = 9; i < 17; i++) {
      let addition = 1;
      let val = i;
      let is1AM = true,
        is2AM = true;

      if (i > 12) {
        val = i - 12;
        is1AM = false;
        is2AM = false;
      } else if (i === 11) {
        is2AM = false;
      } else if (i === 12) {
        addition = -11;
        is2AM = false;
        is1AM = false;
      }

      const firstTime = this.getAMorPM(is1AM);
      const secondTime = this.getAMorPM(is2AM);

      if (
        isMonthUnavailable ||
        formattedDate === this.date.getFullYear().toString() ||
        (new Date().getDate() === parseInt(this.selectedDateNum.toString()) &&
          new Date().getMonth() === this.date.getMonth() &&
          new Date().getFullYear() === this.date.getFullYear())
      ) {
        slots += `<div data-target="${i} " class="time-slots time-unavailable">${val} ${firstTime} - ${val + addition} ${secondTime}</div>`;
      } else if (i === parseInt(this.selectedTime)) {
        slots += `<div data-target="${i} " class="time-slots time-selected">${val} ${firstTime} - ${val + addition} ${secondTime}</div>`;
        timeIsSelected = true;
      } else {
        slots += `<div data-target="${i} " class="time-slots">${val} ${firstTime} - ${val + addition} ${secondTime}</div>`;
      }
    }

    if (timeIsSelected) {
      slots += `<div class="confirmation-time">
                <div class="submitBTN">Confirm</div>
              </div>`;
      document.querySelector('.time-picker')?.classList.add('time-picker-selected');
    } else {
      document.querySelector('.time-picker')?.classList.remove('time-picker-selected');
    }

    timeSlots.innerHTML = slots;

    if (timeIsSelected) {
      const submitBtn = document.querySelector('.submitBTN');
      if (submitBtn) {
        submitBtn.addEventListener('click', () => {
          let month = this.date.getMonth();
          let sDate = this.selectedDateNum;

          if (month < 9) {
            month = parseInt('0' + (month + 1));
          } else if (month !== 12) {
            month += 1;
          } else {
            month = 1;
          }

          if (sDate < 10) {
            sDate = parseInt('0' + sDate);
          }

          const dateInput = document.getElementById('date_input') as HTMLInputElement;
          if (dateInput) {
            dateInput.value = this.date.getFullYear() + '-' + month + '-' + sDate;
          }
        });
      }
    }
  }

  private attachEventListeners(): void {
    // Previous month
    const prevBtn = document.querySelector('.prev');
    if (prevBtn) {
      prevBtn.addEventListener('click', () => {
        this.date.setMonth(this.date.getMonth() - 1);
        this.renderCalendar();
        this.fixDays();
        this.selectedTime = '';
        this.renderCalendar();
      });
    }

    // Next month
    const nextBtn = document.querySelector('.next');
    if (nextBtn) {
      nextBtn.addEventListener('click', () => {
        this.date.setMonth(this.date.getMonth() + 1);
        this.renderCalendar();
        this.fixDays();
        this.selectedTime = '';
        this.renderCalendar();
      });
    }

    // Date selection
    const daysContainer = document.querySelector('.days');
    if (daysContainer) {
      daysContainer.addEventListener('click', (event: Event) => {
        const target = event.target.dataset.target;
        if (target !== undefined) {
          if (target.substring(0, 13) === 'previous-page') {
            this.selectedDateNum = parseInt(target.substring(14, 16).trim());
            this.date.setMonth(this.date.getMonth() - 1);
          } else if (target.substring(0, 9) === 'next-page') {
            this.selectedDateNum = parseInt(target.substring(10, 12).trim());
            this.date.setMonth(this.date.getMonth() + 1);
          } else {
            this.selectedDateNum = parseInt(target);
            this.date.setDate(parseInt(target));
          }
          this.selectedTime = '';
          this.chosenDate = new Date(this.date);
        }
        this.renderCalendar();
      });
    }

    // Time selection
    const timesContainer = document.querySelector('.times');
    if (timesContainer) {
      timesContainer.addEventListener('click', (event: Event) => {
        const selected = event.target.dataset.target;
        if (selected !== undefined) {
          this.selectedTime = selected;
          if (this.chosenDate) {
            this.chosenDate.setHours(parseInt(selected), 0, 0);
            // Update the date input field with the selected date and time
            this.updateDateInputField();
          }
        }
        this.renderCalendar();
      });
    }
  }

  private updateDateInputField(): void {
    if (this.chosenDate) {
      // Format the date and time for the input field
      const year = this.chosenDate.getFullYear();
      const month = String(this.chosenDate.getMonth() + 1).padStart(2, '0');
      const day = String(this.chosenDate.getDate()).padStart(2, '0');
      const hours = String(this.chosenDate.getHours()).padStart(2, '0');
      const minutes = String(this.chosenDate.getMinutes()).padStart(2, '0');

      const dateTimeString = `${year}-${month}-${day}T${hours}:${minutes}`;

      // Update the date input field
      const dateInput = document.getElementById('visitDate') as HTMLInputElement;
      if (dateInput) {
        dateInput.value = dateTimeString;
      }
    }
  }

  // Helper methods
  private CheckWeekend(tempDate: number, month: number): boolean {
    const months = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ];
    const weekend = new Date(
      '' + months[this.date.getMonth() + month] + ' ' + tempDate + ', ' + this.date.getFullYear()
    ).getDay();
    return weekend === 0 || weekend === 6;
  }

  private getWeekDay(day: number, month: number): number {
    const months = [
      'January',
      'February',
      'March',
      'April',
      'May',
      'June',
      'July',
      'August',
      'September',
      'October',
      'November',
      'December',
    ];
    return new Date(
      '' + months[this.date.getMonth() + month] + ' ' + day + ', ' + this.date.getFullYear()
    ).getDay();
  }

  private getAMorPM(isAM: boolean): string {
    return isAM ? 'AM' : 'PM';
  }

  private getDateTemplate(monthAddition: number): Date {
    return new Date(this.date.getFullYear(), this.date.getMonth() + monthAddition, 0);
  }

  private getDateFullFormatEqualsTo(chosenDate: number): boolean {
    return (
      chosenDate === new Date().getDate() &&
      this.date.getMonth() === new Date().getMonth() &&
      new Date().getFullYear() === this.date.getFullYear()
    );
  }

  private getFormatPractitionerIdAndMonth(): string {
    const lastDayOfMonth = new Date(this.date.getFullYear(), this.date.getMonth() + 1, 0).getDate();
    let month = this.date.getMonth() + 1;

    if (month < 10) {
      month = parseInt('0' + month);
    }

    const startDate = this.date.getFullYear() + '-' + month + '-01';
    const endDate = this.date.getFullYear() + '-' + month + '-' + lastDayOfMonth;

    const selectedVet = document.getElementById('selectedVet') as HTMLSelectElement;
    const selectedOption = selectedVet?.selectedOptions[0];
    return (selectedOption?.getAttribute('value') || '') + ',' + startDate + ',' + endDate;
  }

  private fixDays(): void {
    let valMin = 0;
    let valMax = 0;
    let wasMaxReached = false;

    for (const a of this.availableDays) {
      if (a > this.selectedDateNum && wasMaxReached === false) {
        valMax = a;
        wasMaxReached = true;
      }
      if (a < this.selectedDateNum) {
        valMin = a;
      }
    }

    if (valMin === 0) {
      this.selectedDateNum = valMax;
    } else if (valMax === 0) {
      this.selectedDateNum = valMin;
    } else {
      if (valMax - this.selectedDateNum <= this.selectedDateNum - valMin) {
        this.selectedDateNum = valMax;
      } else {
        this.selectedDateNum = valMin;
      }
    }
  }

  selectChanged(): void {
    this.vetAvailabilityStr = this.getVetWorkdays();
    this.nameOnCalenderStr = this.selectedVet
      ? `${this.selectedVet.firstName} ${this.selectedVet.lastName}`
      : '';
    this.renderCalendar();
  }

  // API methods
  loadVets(): void {
    this.visitApi.getVets().subscribe({
      next: vets => {
        this.vets = vets;
      },
      error: error => {
        console.error('Error loading vets:', error);
      },
    });
  }

  loadOwners(): void {
    this.visitApi.getOwners().subscribe({
      next: owners => {
        this.owners = owners;
      },
      error: error => {
        console.error('Error loading owners:', error);
      },
    });
  }

  loadVetInfo(): void {
    const selectedVetId = this.practitionerId;
    this.selectedVet = this.vets.find(vet => vet.vetId === selectedVetId) || null;

    if (this.selectedVet) {
      this.vetAvailabilityStr = this.selectedVet.workday.join(', '); // For calendar logic
      this.nameOnCalenderStr = `${this.selectedVet.firstName} ${this.selectedVet.lastName}`; // For calendar title
    } else {
      this.vetAvailabilityStr = '';
      this.nameOnCalenderStr = '';
    }
    this.renderCalendar(); // Re-render calendar based on new vet availability
  }

  loadOwnerInfo(): void {
    if (this.ownerId) {
      this.visitApi.getPetsByOwner(this.ownerId).subscribe({
        next: pets => {
          this.pets = pets;
        },
        error: error => {
          console.error('Error loading pets:', error);
        },
      });
    }
  }

  logPetId(): void {
    // Method for debugging pet selection
  }

  getVetSpecialties(): string {
    return this.selectedVet ? this.selectedVet.specialties.map(s => s.name).join(', ') : '';
  }

  getVetWorkdays(): string {
    return this.selectedVet ? this.selectedVet.workday.join(', ') : '';
  }

  getVisitsForPractitionerIdAndMonth(): void {
    // Method for loading visits for calendar display
  }

  showConfirmationModal(event: Event): void {
    const button = event.target as HTMLButtonElement;
    const buttonText = button.textContent || '';

    if (buttonText === 'Add New Visit' || buttonText === 'Update Visit') {
      const form = document.querySelector('form');
      if (!form || !(form as HTMLFormElement).reportValidity()) {
        return;
      }
    }

    const modalTitle = document.getElementById('confirmationModalTitle');
    const modalBody = document.getElementById('confirmationModalBody');

    if (modalTitle) modalTitle.textContent = buttonText;
    if (modalBody)
      modalBody.textContent = 'Are you sure you want to ' + buttonText.toLowerCase() + '?';

    this.showModal();
  }

  completeFormAction(): void {
    const modalTitle = document.getElementById('confirmationModalTitle')?.textContent;
    if (modalTitle === 'Add New Visit') {
      this.submit();
    }
    this.hideModal();
  }

  private showModal(): void {
    const modal = document.getElementById('confirmationModal');
    if (modal) {
      modal.style.display = 'block';
    }
  }

  private hideModal(): void {
    const modal = document.getElementById('confirmationModal');
    if (modal) {
      modal.style.display = 'none';
    }
  }

  submit(): void {
    // Validate all required fields
    if (!this.validateForm()) {
      return;
    }

    const visitData: VisitRequest = {
      visitDate: this.chosenDate!.toISOString(),
      description: this.desc,
      petId: this.petId,
      practitionerId: this.practitionerId,
    };

    this.visitApi.createVisit(visitData, this.ownerId, this.petId).subscribe({
      next: visit => {
        // Create corresponding bill after successful visit creation
        this.createBill(visit);
        this.createAlert('success', 'Successfully created visit!');
        this.router.navigate(['/visit-list']);
      },
      error: error => {
        console.error('Error creating visit:', error);
        this.createAlert(
          'danger',
          'Failed to create visit: ' + (error.error?.message || 'Unknown error')
        );
      },
    });
  }

  private validateForm(): boolean {
    const errors: string[] = [];

    // Validate practitioner selection
    if (!this.practitionerId || this.practitionerId === '') {
      errors.push('Please select a veterinarian');
    }

    // Validate owner selection
    if (!this.ownerId || this.ownerId === '') {
      errors.push('Please select an owner');
    }

    // Validate pet selection
    if (!this.petId || this.petId === '') {
      errors.push('Please select a pet');
    }

    // Validate visit type selection
    if (!this.visitType || this.visitType === '') {
      errors.push('Please select a visit type');
    }

    // Validate date selection
    if (!this.chosenDate) {
      errors.push('Please select a date from the calendar');
    }

    // Validate time selection
    if (!this.selectedTime || this.selectedTime === '') {
      errors.push('Please select a time slot');
    }

    // Validate description (optional but should have some content)
    if (!this.desc || this.desc.trim() === '') {
      errors.push('Please provide a description for the visit');
    }

    // Check if date is in the past
    if (this.chosenDate && this.chosenDate < new Date()) {
      errors.push('Cannot schedule visits in the past');
    }

    // Check if date is too far in the future (e.g., more than 1 year)
    const oneYearFromNow = new Date();
    oneYearFromNow.setFullYear(oneYearFromNow.getFullYear() + 1);
    if (this.chosenDate && this.chosenDate > oneYearFromNow) {
      errors.push('Cannot schedule visits more than 1 year in advance');
    }

    // Display errors if any
    if (errors.length > 0) {
      this.createAlert('danger', errors.join('<br>'));
      return false;
    }

    return true;
  }

  private createBill(): void {
    if (!this.ownerId || !this.practitionerId || !this.visitType) {
      return;
    }

    // Calculate due date (30 days from visit date)
    const dueDate = new Date(this.chosenDate!);
    dueDate.setDate(dueDate.getDate() + 30);

    const billData: BillRequest = {
      customerId: this.ownerId,
      visitType: this.visitType,
      vetId: this.practitionerId,
      date: this.chosenDate!.toISOString().split('T')[0],
      amount: this.calculateBillAmount(), // Calculate based on visit type
      billStatus: 'UNPAID',
      dueDate: dueDate.toISOString().split('T')[0], // Format as YYYY-MM-DD
    };

    this.billApi.createBill(billData).subscribe({
      next: () => {},
      error: error => {
        console.error('Failed to create corresponding bill:', error);
      },
    });
  }

  private calculateBillAmount(): number {
    // Calculate bill amount based on visit type
    const visitTypeAmounts: { [key: string]: number } = {
      Examinations: 100,
      Injury: 150,
      Medical: 200,
      Chronic: 250,
      Consultations: 75,
      Operations: 500,
    };

    return visitTypeAmounts[this.visitType] || 100; // Default to $100
  }

  resetForm(): void {
    this.practitionerId = '';
    this.ownerId = '';
    this.petId = '';
    this.visitType = '';
    this.selectedDate = '';
    this.desc = '';
    this.chosenDate = null;
    this.chosenTime = null;
    this.selectedVet = null;
    this.pets = [];

    const submitButton = document.getElementById('submit_button');
    const cancelButton = document.getElementById('cancel_button');
    if (submitButton) submitButton.textContent = 'Add New Visit';
    if (cancelButton) cancelButton.style.visibility = 'hidden';

    this.dateReset();
  }

  private dateReset(): void {
    this.date = new Date();
    this.selectedDateNum = this.date.getDate();
    this.renderCalendar();
  }

  goBack(): void {
    this.router.navigate(['/visit-list']);
  }

  private createAlert(alertType: string, alertMessage: string): void {
    const alertsContainer = document.getElementById('alertsContainer');
    if (!alertsContainer) return;

    const alertId = Date.now();
    const alertHtml = `
      <div id="alert-${alertId}" class="alert alert-${alertType}" role="alert">
        <p>${alertMessage}</p>
      </div>
    `;

    alertsContainer.innerHTML = alertHtml;

    setTimeout(() => {
      const alert = document.getElementById(`alert-${alertId}`);
      if (alert) {
        alert.remove();
      }
    }, 5000); // Increased timeout for validation errors
  }

  // Calendar navigation methods
  previousMonth(): void {
    this.date.setMonth(this.date.getMonth() - 1);
    this.renderCalendar();
    this.fixDays();
    this.selectedTime = '';
    this.renderCalendar();
  }

  nextMonth(): void {
    this.date.setMonth(this.date.getMonth() + 1);
    this.renderCalendar();
    this.fixDays();
    this.selectedTime = '';
    this.renderCalendar();
  }
}
