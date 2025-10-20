import { Component, OnInit, OnDestroy, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { VisitApiService } from '../../api/visit-api.service';
import { Visit, VisitStatus } from '../../models/visit.model';
import { OrderByPipe } from '../../pipes/order-by.pipe';
import { FilterPipe } from '../../pipes/filter.pipe';

@Component({
  selector: 'app-visit-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, OrderByPipe, FilterPipe],
  template: `
    <style>
      .sortorder:after {
        content: '▲'; /* BLACK UP-POINTING TRIANGLE */
      }
      .sortorder.reverse:after {
        content: '▼'; /* BLACK DOWN-POINTING TRIANGLE */
      }

      .button-wrapper {
        display: flex;
        justify-content: flex-end; /* Align to the right */
        align-items: center;
      }

      .delete-all-bttn {
        color: #fff;
        background-color: red;
        border-color: red;
        font-size: 15px;
        border-radius: 5px;
        width: 200px;
        height: 40px;
        font-weight: bold;
        margin-left: 10px; /* Add some space between the buttons if needed */
      }

      .original-bttn {
        color: #fff;
        background-color: #19d256;
        border-color: #19d256;
        font-size: 18px;
        border-radius: 5px;
        width: 200px;
        height: 40px;
        font-weight: bold;
      }

      .original-bttn:hover {
        box-shadow: 0 0 rgba(0, 0, 0, 0.4);
        border-bottom-width: 2px;
        transform: translateY(2px);
      }

      .delete-all-bttn:hover {
        box-shadow: 0 0 rgba(0, 0, 0, 0.4);
        border-bottom-width: 2px;
        transform: translateY(2px);
      }

      .arrow {
        font-size: 24px; /* Adjust the size as needed */
        color: black; /* Adjust the color as needed */
      }

      /* Action button classes to match Angular JS reference */
      .btn-confirm-upcoming {
        background-color: #5cb85c !important;
        border-color: #4cae4c !important;
        color: white !important;
        border-radius: 4px !important;
        padding: 6px 12px !important;
        font-weight: bold !important;
        text-decoration: none !important;
        display: inline-block !important;
        cursor: pointer !important;
      }

      .btn-confirm-confirmed {
        background-color: #5bc0de !important;
        border-color: #46b8da !important;
        color: white !important;
        border-radius: 4px !important;
        padding: 6px 12px !important;
        font-weight: bold !important;
        text-decoration: none !important;
        display: inline-block !important;
        cursor: pointer !important;
      }

      .btn-cancel-upcoming {
        background-color: #d9534f !important;
        border-color: #d43f3a !important;
        color: white !important;
        border-radius: 4px !important;
        padding: 6px 12px !important;
        font-weight: bold !important;
        text-decoration: none !important;
        display: inline-block !important;
        cursor: pointer !important;
      }

      .btn-cancel-disabled {
        background-color: #fff !important;
        border-color: #ccc !important;
        color: #333 !important;
        border-radius: 4px !important;
        padding: 6px 12px !important;
        font-weight: bold !important;
        text-decoration: none !important;
        display: inline-block !important;
        cursor: not-allowed !important;
        opacity: 0.65 !important;
      }

      .btn-edit-royal {
        background-color: royalblue !important;
        color: white !important;
        border: 1px solid royalblue !important;
      }

      .btn-delete-red {
        background-color: #d9534f !important;
        border-color: #d43f3a !important;
        color: white !important;
      }
    </style>

    <div id="loadingIndicator" style="display: none;">Loading...</div>
    <!--Page loading message-->

    <div class="button-wrapper">
      <a routerLink="/visits"> <button class="original-bttn" id="addBtn">Create Visit</button> </a>
      <button class="delete-all-bttn" (click)="deleteAllCancelledVisits()">
        Delete All Cancelled Visits
      </button>
    </div>

    <div *ngIf="upcomingVisits.length > 0">
      <h3 style="margin: 0; display: inline-block;">Upcoming Visits</h3>
      <button
        class="btn btn-link"
        (click)="showUpcomingVisits = !showUpcomingVisits"
        style="vertical-align: middle;"
      >
        <span *ngIf="showUpcomingVisits">&#9660;</span>
        <span *ngIf="!showUpcomingVisits">&#9650;</span>
      </button>
    </div>
    <table class="table table-striped" *ngIf="showUpcomingVisits && upcomingVisits.length > 0">
      <thead>
        <tr>
          <th>
            <button class="btn btn-default" (click)="sortBy('visitId')" style="color: white">
              VisitId<span
                class="sortorder"
                *ngIf="propertyName === 'visitId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('visitDate')" style="color: white">
              Sort by date<span
                class="sortorder"
                *ngIf="propertyName === 'visitDate'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('description')" style="color: white">
              Sort by description<span
                class="sortorder"
                *ngIf="propertyName === 'description'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('practitionerId')" style="color: white">
              Sort by veterinarian<span
                class="sortorder"
                *ngIf="propertyName === 'practitionerId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('petId')" style="color: white">
              Sort by pet<span
                class="sortorder"
                *ngIf="propertyName === 'petId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" style="color: white">Sort by Bill</button>
          </th>
          <th style="text-align:center; vertical-align:middle"><label>Status</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Confirm</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Cancel</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Edit</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Delete</label></th>
        </tr>
      </thead>
      <tbody id="upcomingTable">
        <tr class="bg-white">
          <td class="border">
            <input
              style="width:100%"
              [(ngModel)]="search.visitId"
              placeholder="Filter by VisitID"
            />
          </td>
          <td class="border">
            <input style="width:100%" [(ngModel)]="search.date" placeholder="Filter by Date" />
          </td>
          <td class="border">
            <input
              style="width:100%"
              [(ngModel)]="search.description"
              placeholder="Filter by Description"
            />
          </td>
          <td class="border">
            <input
              style="width:100%"
              [(ngModel)]="search.vetId"
              placeholder="Filter by Veterinarian"
            />
          </td>
          <td class="border">
            <input style="width:100%" [(ngModel)]="search.petId" placeholder="Filter by PetId" />
          </td>
          <td class="border"><input [(ngModel)]="search.billId" placeholder="Filter by Bill" /></td>
          <td class="border"></td>
          <td class="border"></td>
          <td class="border"></td>
          <td class="border"></td>
          <td class="border"></td>
        </tr>

        <tr
          id="upcomingVisitId"
          *ngFor="
            let v of upcomingVisits | orderBy: propertyName : reverse | filter: search : query;
            trackBy: trackByVisitId
          "
          data-table-name="upcomingVisits"
        >
          <td class="border">
            <a [routerLink]="['/visits', v.visitId]"> {{ v.visitId }}</a>
          </td>
          <td class="border">{{ v.visitDate | date: 'yyyy-MM-ddTHH:mm:ss' }}</td>
          <td class="border" style="white-space: pre-line">{{ v.description }}</td>
          <td class="border" style="white-space: pre-line">
            {{ v.vetFirstName }} {{ v.vetLastName }}
          </td>
          <td class="border" style="white-space: pre-line">{{ v.petName }}</td>
          <td class="status-text" style="white-space: pre-line"></td>
          <td class="status-text border" style="white-space: pre-line">{{ v.status }}</td>
          <td class="border">
            <a
              [ngClass]="{
                'btn btn-confirm-upcoming': v.status === 'UPCOMING',
                'btn btn-confirm-confirmed': v.status === 'CONFIRMED',
                'btn btn-cancel-disabled': v.status === 'COMPLETED' || v.status === 'CANCELLED',
              }"
              href="javascript:void(0)"
              (click)="confirmVisit(v.visitId, v.status)"
            >
              {{
                v.status === 'UPCOMING'
                  ? 'Confirm Visit'
                  : v.status === 'CONFIRMED'
                    ? 'Complete Visit'
                    : v.status === 'CANCELLED'
                      ? 'Cannot Confirm Cancelled Visit'
                      : 'Visit Completed!'
              }}
            </a>
          </td>

          <td class="border">
            <a
              [ngClass]="{
                'btn btn-cancel-upcoming': v.status === 'UPCOMING',
                'btn btn-cancel-disabled':
                  v.status === 'CANCELLED' || v.status === 'CONFIRMED' || v.status === 'COMPLETED',
              }"
              href="javascript:void(0)"
              (click)="cancelVisit(v.visitId, v.status)"
            >
              {{
                v.status === 'UPCOMING'
                  ? 'Cancel Visit'
                  : v.status === 'CONFIRMED'
                    ? 'Cannot Cancel Confirmed Visit'
                    : v.status === 'COMPLETED'
                      ? 'Cannot Cancel Completed Visit'
                      : 'Visit Cancelled!'
              }}
            </a>
          </td>
          <td class="border" style="text-align:center; vertical-align:middle">
            <button
              class="btn btn-edit-royal"
              type="button"
              (click)="
                switchToUpdateForm($event, v.practitionerId, v.description, v.visitId, v.status)
              "
            >
              Edit Visit
            </button>
          </td>
          <td class="border">
            <a class="btn btn-delete-red" href="javascript:void(0)" (click)="deleteVisit(v.visitId)"
              >Delete Visit</a
            >
          </td>
        </tr>
      </tbody>
    </table>

    <br />
    <div *ngIf="confirmedVisits.length > 0">
      <h3 style="margin: 0; display: inline-block;">Confirmed Visits</h3>
      <button
        class="btn btn-link"
        (click)="showConfirmedVisits = !showConfirmedVisits"
        style="vertical-align: middle;"
      >
        <span *ngIf="showConfirmedVisits">&#9660;</span>
        <span *ngIf="!showConfirmedVisits">&#9650;</span>
      </button>
    </div>
    <table class="table table-striped" *ngIf="showConfirmedVisits && confirmedVisits.length > 0">
      <thead>
        <tr>
          <th>
            <button class="btn btn-default" (click)="sortBy('visitId')" style="color: white">
              VisitId<span
                class="sortorder"
                *ngIf="propertyName === 'visitId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('visitDate')" style="color: white">
              Sort by date<span
                class="sortorder"
                *ngIf="propertyName === 'visitDate'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('description')" style="color: white">
              Sort by description<span
                class="sortorder"
                *ngIf="propertyName === 'description'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('practitionerId')" style="color: white">
              Sort by veterinarian<span
                class="sortorder"
                *ngIf="propertyName === 'practitionerId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('petId')" style="color: white">
              Sort by pet<span
                class="sortorder"
                *ngIf="propertyName === 'petId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" style="color: white">Sort by Bill</button>
          </th>
          <th style="text-align:center; vertical-align:middle"><label>Status</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Confirm</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Cancel</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Edit</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Delete</label></th>
        </tr>
      </thead>
      <tbody id="confirmedTable">
        <tr
          id="confirmedVisitId"
          *ngFor="
            let v of confirmedVisits | orderBy: propertyName : reverse | filter: search : query;
            trackBy: trackByVisitId
          "
          data-table-name="confirmedVisits"
        >
          <td class="border">
            <a [routerLink]="['/visits', v.visitId]"> {{ v.visitId }}</a>
          </td>
          <td class="border">{{ v.visitDate | date: 'yyyy-MM-ddTHH:mm:ss' }}</td>
          <td class="border" style="white-space: pre-line">{{ v.description }}</td>
          <td class="border" style="white-space: pre-line">
            {{ v.vetFirstName }} {{ v.vetLastName }}
          </td>
          <td class="border" style="white-space: pre-line">{{ v.petName }}</td>
          <td class="status-text" style="white-space: pre-line"></td>
          <td class="status-text border" style="white-space: pre-line">{{ v.status }}</td>
          <td class="border">
            <a
              [ngClass]="{
                'btn btn-confirm-upcoming': v.status === 'UPCOMING',
                'btn btn-confirm-confirmed': v.status === 'CONFIRMED',
                'btn btn-cancel-disabled': v.status === 'COMPLETED' || v.status === 'CANCELLED',
              }"
              href="javascript:void(0)"
              (click)="confirmVisit(v.visitId, v.status)"
            >
              {{
                v.status === 'UPCOMING'
                  ? 'Confirm Visit'
                  : v.status === 'CONFIRMED'
                    ? 'Complete Visit'
                    : v.status === 'CANCELLED'
                      ? 'Cannot Confirm Cancelled Visit'
                      : 'Visit Completed!'
              }}
            </a>
          </td>

          <td class="border">
            <a
              [ngClass]="{
                'btn btn-cancel-upcoming': v.status === 'UPCOMING',
                'btn btn-cancel-disabled':
                  v.status === 'CANCELLED' || v.status === 'CONFIRMED' || v.status === 'COMPLETED',
              }"
              href="javascript:void(0)"
              (click)="cancelVisit(v.visitId, v.status)"
            >
              {{
                v.status === 'UPCOMING'
                  ? 'Cancel Visit'
                  : v.status === 'CONFIRMED'
                    ? 'Cannot Cancel Confirmed Visit'
                    : v.status === 'COMPLETED'
                      ? 'Cannot Cancel Completed Visit'
                      : 'Visit Cancelled!'
              }}
            </a>
          </td>
          <td class="border" style="text-align:center; vertical-align:middle">
            <button
              class="btn btn-edit-royal"
              type="button"
              (click)="
                switchToUpdateForm($event, v.practitionerId, v.description, v.visitId, v.status)
              "
            >
              Edit Visit
            </button>
          </td>
          <td class="border">
            <a class="btn btn-delete-red" href="javascript:void(0)" (click)="deleteVisit(v.visitId)"
              >Delete Visit</a
            >
          </td>
        </tr>
      </tbody>
    </table>

    <br />
    <div *ngIf="cancelledVisits.length > 0">
      <h3 style="margin: 0; display: inline-block;">Cancelled Visits</h3>
      <button
        class="btn btn-link"
        (click)="showCancelledVisits = !showCancelledVisits"
        style="vertical-align: middle;"
      >
        <span *ngIf="showCancelledVisits">&#9660;</span>
        <span *ngIf="!showCancelledVisits">&#9650;</span>
      </button>
    </div>
    <table class="table table-striped" *ngIf="showCancelledVisits && cancelledVisits.length > 0">
      <thead>
        <tr>
          <th>
            <button class="btn btn-default" (click)="sortBy('visitId')" style="color: white">
              VisitId<span
                class="sortorder"
                *ngIf="propertyName === 'visitId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('visitDate')" style="color: white">
              Sort by date<span
                class="sortorder"
                *ngIf="propertyName === 'visitDate'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('description')" style="color: white">
              Sort by description<span
                class="sortorder"
                *ngIf="propertyName === 'description'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('practitionerId')" style="color: white">
              Sort by veterinarian<span
                class="sortorder"
                *ngIf="propertyName === 'practitionerId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('petId')" style="color: white">
              Sort by pet<span
                class="sortorder"
                *ngIf="propertyName === 'petId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" style="color: white">Sort by Bill</button>
          </th>
          <th style="text-align:center; vertical-align:middle"><label>Status</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Confirm</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Cancel</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Edit</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Delete</label></th>
        </tr>
      </thead>
      <tbody id="cancelledTable">
        <tr
          id="cancelledVisitId"
          *ngFor="
            let v of cancelledVisits | orderBy: propertyName : reverse | filter: search : query;
            trackBy: trackByVisitId
          "
          data-table-name="cancelledVisits"
        >
          <td class="border">
            <a [routerLink]="['/visits', v.visitId]"> {{ v.visitId }}</a>
          </td>
          <td class="border">{{ v.visitDate | date: 'yyyy-MM-ddTHH:mm:ss' }}</td>
          <td class="border" style="white-space: pre-line">{{ v.description }}</td>
          <td class="border" style="white-space: pre-line">
            {{ v.vetFirstName }} {{ v.vetLastName }}
          </td>
          <td class="border" style="white-space: pre-line">{{ v.petName }}</td>
          <td class="status-text" style="white-space: pre-line"></td>
          <td class="status-text border" style="white-space: pre-line">{{ v.status }}</td>
          <td class="border">
            <a
              [ngClass]="{
                'btn btn-confirm-upcoming': v.status === 'UPCOMING',
                'btn btn-confirm-confirmed': v.status === 'CONFIRMED',
                'btn btn-cancel-disabled': v.status === 'COMPLETED' || v.status === 'CANCELLED',
              }"
              href="javascript:void(0)"
              (click)="confirmVisit(v.visitId, v.status)"
            >
              {{
                v.status === 'UPCOMING'
                  ? 'Confirm Visit'
                  : v.status === 'CONFIRMED'
                    ? 'Complete Visit'
                    : v.status === 'CANCELLED'
                      ? 'Cannot Confirm Cancelled Visit'
                      : 'Visit Completed!'
              }}
            </a>
          </td>

          <td class="border">
            <a
              [ngClass]="{
                'btn btn-cancel-upcoming': v.status === 'UPCOMING',
                'btn btn-cancel-disabled':
                  v.status === 'CANCELLED' || v.status === 'CONFIRMED' || v.status === 'COMPLETED',
              }"
              href="javascript:void(0)"
              (click)="cancelVisit(v.visitId, v.status)"
            >
              {{
                v.status === 'UPCOMING'
                  ? 'Cancel Visit'
                  : v.status === 'CONFIRMED'
                    ? 'Cannot Cancel Confirmed Visit'
                    : v.status === 'COMPLETED'
                      ? 'Cannot Cancel Completed Visit'
                      : 'Visit Cancelled!'
              }}
            </a>
          </td>
          <td class="border" style="text-align:center; vertical-align:middle">
            <button
              class="btn btn-edit-royal"
              type="button"
              (click)="
                switchToUpdateForm($event, v.practitionerId, v.description, v.visitId, v.status)
              "
            >
              Edit Visit
            </button>
          </td>
          <td class="border">
            <a class="btn btn-delete-red" href="javascript:void(0)" (click)="deleteVisit(v.visitId)"
              >Delete Visit</a
            >
          </td>
        </tr>
      </tbody>
    </table>

    <br />
    <div *ngIf="completedVisits.length > 0">
      <h3 style="margin: 0; display: inline-block;">Completed Visits</h3>
      <button
        class="btn btn-link"
        (click)="showCompletedVisits = !showCompletedVisits"
        style="vertical-align: middle;"
      >
        <span *ngIf="showCompletedVisits">&#9660;</span>
        <span *ngIf="!showCompletedVisits">&#9650;</span>
      </button>
    </div>
    <table class="table table-striped" *ngIf="showCompletedVisits && completedVisits.length > 0">
      <thead>
        <tr>
          <th>
            <button class="btn btn-default" (click)="sortBy('visitId')" style="color: white">
              VisitId<span
                class="sortorder"
                *ngIf="propertyName === 'visitId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('visitDate')" style="color: white">
              Sort by date<span
                class="sortorder"
                *ngIf="propertyName === 'visitDate'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('description')" style="color: white">
              Sort by description<span
                class="sortorder"
                *ngIf="propertyName === 'description'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('practitionerId')" style="color: white">
              Sort by veterinarian<span
                class="sortorder"
                *ngIf="propertyName === 'practitionerId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" (click)="sortBy('petId')" style="color: white">
              Sort by pet<span
                class="sortorder"
                *ngIf="propertyName === 'petId'"
                [ngClass]="{ reverse: reverse }"
              ></span>
            </button>
          </th>
          <th>
            <button class="btn btn-default" style="color: white">Sort by Bill</button>
          </th>
          <th style="text-align:center; vertical-align:middle"><label>Status</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Confirm</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Cancel</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Edit</label></th>
          <th style="text-align:center; vertical-align:middle"><label>Delete</label></th>
        </tr>
      </thead>
      <tbody id="completedTable">
        <tr
          id="completedVisitId"
          *ngFor="
            let v of completedVisits | orderBy: propertyName : reverse | filter: search : query;
            trackBy: trackByVisitId
          "
          data-table-name="completedVisits"
        >
          <td class="border">
            <a [routerLink]="['/visits', v.visitId]"> {{ v.visitId }}</a>
          </td>
          <td class="border">{{ v.visitDate | date: 'yyyy-MM-ddTHH:mm:ss' }}</td>
          <td class="border" style="white-space: pre-line">{{ v.description }}</td>
          <td class="border" style="white-space: pre-line">
            {{ v.vetFirstName }} {{ v.vetLastName }}
          </td>
          <td class="border" style="white-space: pre-line">{{ v.petName }}</td>
          <td class="status-text" style="white-space: pre-line"></td>
          <td class="status-text border" style="white-space: pre-line">{{ v.status }}</td>
          <td class="border">
            <a
              [ngClass]="{
                'btn btn-confirm-upcoming': v.status === 'UPCOMING',
                'btn btn-confirm-confirmed': v.status === 'CONFIRMED',
                'btn btn-cancel-disabled': v.status === 'COMPLETED' || v.status === 'CANCELLED',
              }"
              href="javascript:void(0)"
              (click)="confirmVisit(v.visitId, v.status)"
            >
              {{
                v.status === 'UPCOMING'
                  ? 'Confirm Visit'
                  : v.status === 'CONFIRMED'
                    ? 'Complete Visit'
                    : v.status === 'CANCELLED'
                      ? 'Cannot Confirm Cancelled Visit'
                      : 'Visit Completed!'
              }}
            </a>
          </td>

          <td class="border">
            <a
              [ngClass]="{
                'btn btn-cancel-upcoming': v.status === 'UPCOMING',
                'btn btn-cancel-disabled':
                  v.status === 'CANCELLED' || v.status === 'CONFIRMED' || v.status === 'COMPLETED',
              }"
              href="javascript:void(0)"
              (click)="cancelVisit(v.visitId, v.status)"
            >
              {{
                v.status === 'UPCOMING'
                  ? 'Cancel Visit'
                  : v.status === 'CONFIRMED'
                    ? 'Cannot Cancel Confirmed Visit'
                    : v.status === 'COMPLETED'
                      ? 'Cannot Cancel Completed Visit'
                      : 'Visit Cancelled!'
              }}
            </a>
          </td>
          <td class="border" style="text-align:center; vertical-align:middle">
            <button
              class="btn btn-edit-royal"
              type="button"
              (click)="
                switchToUpdateForm($event, v.practitionerId, v.description, v.visitId, v.status)
              "
            >
              Edit Visit
            </button>
          </td>
          <td class="border">
            <a class="btn btn-delete-red" href="javascript:void(0)" (click)="deleteVisit(v.visitId)"
              >Delete Visit</a
            >
          </td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [
    `
      @import url('/css/petclinic.css');
    `,
  ],
  encapsulation: ViewEncapsulation.None,
})
export class VisitListComponent implements OnInit, OnDestroy {
  private visitApi = inject(VisitApiService);

  // Lists holding visits for the tables to display
  upcomingVisits: Visit[] = [];
  confirmedVisits: Visit[] = [];
  cancelledVisits: Visit[] = [];
  completedVisits: Visit[] = [];

  // Show/hide sections
  showUpcomingVisits = true;
  showConfirmedVisits = true;
  showCancelledVisits = true;
  showCompletedVisits = true;

  // Sorting
  propertyName = 'visitId';
  reverse = false;

  // Search filters
  search = {
    visitId: '',
    date: '',
    description: '',
    vetId: '',
    petId: '',
    billId: '',
  };
  query = '';

  private eventSource: EventSource | null = null;

  constructor() {}

  ngOnInit(): void {
    this.loadVisits();
    this.initializeEventSource();
  }

  ngOnDestroy(): void {
    if (this.eventSource) {
      this.eventSource.close();
    }
  }

  trackByVisitId(_index: number, visit: Visit): string {
    return visit.visitId;
  }

  loadVisits(): void {
    // Get user role and load visits accordingly
    const roles = localStorage.getItem('roles') || '';
    const practitionerId = localStorage.getItem('practitionerIdAndMonth')?.split(',')[0] || '';
    const ownerId = localStorage.getItem('UUID') || '';

    let visitObservable;
    if (roles.includes('ADMIN')) {
      visitObservable = this.visitApi.getAllVisits();
    } else if (roles.includes('VET')) {
      visitObservable = this.visitApi.getVisitsByVet(practitionerId);
    } else if (roles.includes('OWNER')) {
      visitObservable = this.visitApi.getVisitsByOwner(ownerId);
    } else {
      visitObservable = this.visitApi.getAllVisits(); // Default fallback
    }

    visitObservable.subscribe({
      next: visits => {
        this.categorizeVisits(visits);
      },
      error: error => {
        console.error('Error loading visits:', error);
      },
    });
  }

  categorizeVisits(visits: Visit[]): void {
    this.upcomingVisits = visits.filter(v => v.status === VisitStatus.UPCOMING);
    this.confirmedVisits = visits.filter(v => v.status === VisitStatus.CONFIRMED);
    this.cancelledVisits = visits.filter(v => v.status === VisitStatus.CANCELLED);
    this.completedVisits = visits.filter(v => v.status === VisitStatus.COMPLETED);
  }

  initializeEventSource(): void {
    // Get user role and construct appropriate URL
    const roles = localStorage.getItem('roles') || '';
    const practitionerId = localStorage.getItem('practitionerIdAndMonth')?.split(',')[0] || '';
    const ownerId = localStorage.getItem('UUID') || '';

    let url = '';
    if (roles.includes('ADMIN')) {
      url = 'api/gateway/visits';
    } else if (roles.includes('VET')) {
      url = `api/gateway/visits/vets/${practitionerId}`;
    } else if (roles.includes('OWNER')) {
      url = `api/gateway/visits/owners/${ownerId}`;
    }

    if (url) {
      this.eventSource = new EventSource(url);

      this.eventSource.addEventListener('message', event => {
        const visitData = JSON.parse(event.data);

        // Add visit to appropriate list based on status
        switch (visitData.status) {
          case 'UPCOMING':
            this.upcomingVisits.push(visitData);
            break;
          case 'CONFIRMED':
            this.confirmedVisits.push(visitData);
            break;
          case 'CANCELLED':
            this.cancelledVisits.push(visitData);
            break;
          case 'COMPLETED':
            this.completedVisits.push(visitData);
            break;
          default:
            break;
        }
      });

      this.eventSource.onerror = () => {
        if (this.eventSource?.readyState === 0) {
          this.eventSource.close();
        } else {
        }
      };
    }
  }

  sortBy(propertyName: string): void {
    this.reverse = this.propertyName === propertyName ? !this.reverse : false;
    this.propertyName = propertyName;
  }

  confirmVisit(visitId: string, status: string): void {
    let newStatus: string;
    switch (status) {
      case 'UPCOMING':
        newStatus = 'CONFIRMED';
        break;
      case 'CONFIRMED':
        newStatus = 'COMPLETED';
        break;
      default:
        return;
    }

    this.visitApi.updateVisitStatus(visitId, newStatus).subscribe({
      next: () => {
        alert(visitId + ' visit was confirmed successfully');
        this.delayedReload();
      },
      error: error => {
        console.error('Error confirming visit:', error);
        alert('Error confirming visit');
      },
    });
  }

  cancelVisit(visitId: string, status: string): void {
    if (status !== 'UPCOMING') {
      return; // Can only cancel upcoming visits
    }

    this.visitApi.updateVisitStatus(visitId, 'CANCELLED').subscribe({
      next: () => {
        alert(visitId + ' visit was cancelled successfully');
        this.delayedReload();
      },
      error: error => {
        console.error('Error cancelling visit:', error);
        alert('Error cancelling visit');
      },
    });
  }

  deleteVisit(visitId: string): void {
    const confirmed = confirm(
      'You are about to delete visit ' + visitId + '. Is this what you want to do?'
    );
    if (confirmed) {
      this.visitApi.deleteVisit(visitId).subscribe({
        next: () => {
          alert(visitId + ' visit was deleted successfully');
          this.delayedReload();
        },
        error: error => {
          console.error('Error deleting visit:', error);
          alert('Error deleting visit');
        },
      });
    }
  }

  deleteAllCancelledVisits(): void {
    const confirmed = confirm(
      'You are about to delete all canceled visits. Is this what you want to do?'
    );
    if (confirmed) {
      this.visitApi.deleteAllCancelledVisits().subscribe({
        next: () => {
          alert('All canceled visits were deleted successfully');
          this.delayedReload();
        },
        error: error => {
          console.error('Error deleting cancelled visits:', error);
          alert('Error deleting cancelled visits');
        },
      });
    }
  }

  switchToUpdateForm(): void {
    // Method for navigating to visit update form
  }

  delayedReload(): void {
    const loadingIndicator = document.getElementById('loadingIndicator');
    if (loadingIndicator) {
      loadingIndicator.style.display = 'block';
    }
    setTimeout(() => {
      this.loadVisits();
      if (loadingIndicator) {
        loadingIndicator.style.display = 'none';
      }
    }, 150);
  }
}
