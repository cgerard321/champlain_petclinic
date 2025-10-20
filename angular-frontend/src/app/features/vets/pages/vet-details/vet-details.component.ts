import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VetApiService } from '../../api/vet-api.service';
import { Vet } from '../../models/vet.model';
import { Education, EducationRequest } from '../../models/education.model';
import { Rating, RatingRequest, PredefinedDescription } from '../../models/rating.model';
import { Badge } from '../../models/badge.model';
import { Photo } from '../../models/photo.model';

@Component({
  selector: 'app-vet-details',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  encapsulation: ViewEncapsulation.None,
  template: `
    <div class="bg-light">
      <div class="container-fluid">
        <!-- Vet Information Header -->
        <div class="py-3 text-center">
          <h2 class="titleVetForm">Vet Information</h2>
        </div>

        <div class="row g-5" *ngIf="vet">
          <div class="col-md-4">
            <!-- Vet Profile Photo -->
            <div class="text-center mb-4">
              <h5>Vet Photo</h5>
              <img
                [src]="
                  vetPhoto?.photo
                    ? 'data:image/*;base64,' + vetPhoto.photo
                    : '/images/vet_default.jpg'
                "
                alt="Profile picture preview"
                width="200"
                height="200"
                style="border-radius: 8px; object-fit: cover; border: 2px solid #ddd;"
              />
            </div>

            <!-- Vet Badge -->
            <div class="text-center">
              <h5>Badge</h5>
              <div class="parent">
                <a style="text-decoration: none;">
                  <span
                    class="info v{{ badge?.vetId }}"
                    (mouseenter)="show($event, badge?.vetId)"
                    (mouseleave)="hide($event, badge?.vetId)"
                  >
                    <img
                      [src]="
                        badge?.resourceBase64
                          ? 'data:image/*;base64,' + badge.resourceBase64
                          : '/images/vet_default.jpg'
                      "
                      alt="Badge preview"
                      width="150"
                      height="150"
                      style="border-radius: 8px; object-fit: cover; border: 2px solid #ddd;"
                    />
                  </span>
                </a>
              </div>

              <!-- Badge Modal -->
              <div class="modal m{{ badge?.vetId }} modalOff" *ngIf="badge">
                <span class="modal_title"><b>Title:</b> {{ badge.badgeTitle }}</span>
                <span class="modal_date"><b>Date:</b> {{ badge.badgeDate }}</span>
              </div>
            </div>
          </div>

          <div class="col-md-8">
            <!-- Veterinarian Name -->
            <div class="mb-3">
              <h4 style="margin-bottom: 0.5rem;">Veterinarian</h4>
              <p style="font-size: 1.1rem; margin: 0;">
                <strong>Name:</strong> {{ vet.firstName }} {{ vet.lastName }}
              </p>
            </div>

            <hr />

            <!-- Contact Information -->
            <div class="mb-3">
              <p style="margin: 0.5rem 0;"><strong>Email:</strong> {{ vet.email }}</p>
              <p style="margin: 0.5rem 0;"><strong>Phone:</strong> {{ vet.phoneNumber }}</p>
            </div>

            <hr />

            <!-- Work Information Section -->
            <div class="mb-3">
              <h4 style="margin-bottom: 1rem;">Work Information:</h4>

              <!-- Resume -->
              <div class="mb-3">
                <h5 style="margin-bottom: 0.5rem;">Resume:</h5>
                <p
                  style="padding: 0.75rem; background-color: #f8f9fa; border-radius: 4px; border-left: 3px solid #007bff;"
                >
                  {{ vet.resume || 'No resume provided' }}
                </p>
              </div>

              <!-- Work Days -->
              <div class="mb-3">
                <h5 style="margin-bottom: 0.5rem;">Work Days:</h5>
                <div style="padding: 0.75rem; background-color: #f8f9fa; border-radius: 4px;">
                  <ul style="margin: 0; padding-left: 1.5rem; list-style: none;">
                    <li
                      *ngFor="let workday of vet.workday"
                      (click)="selectWorkday(workday)"
                      style="cursor: pointer; padding: 0.25rem 0; color: #007bff;"
                    >
                      {{ workday }}
                    </li>
                  </ul>

                  <table
                    id="workHoursTable"
                    style="display: none; width: 100%; margin-top: 1rem; border-collapse: separate; border-spacing: 0;"
                  >
                    <thead>
                      <tr>
                        <th
                          style="background-color: #e9ecef; padding: 0.5rem; border: 1px solid #dee2e6;"
                        >
                          Hours
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr>
                        <td id="Hour_8_9" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          8:00-9:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_9_10" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          9:00-10:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_10_11" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          10:00-11:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_11_12" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          11:00-12:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_12_13" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          12:00-13:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_13_14" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          13:00-14:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_14_15" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          14:00-15:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_15_16" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          15:00-16:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_16_17" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          16:00-17:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_17_18" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          17:00-18:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_18_19" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          18:00-19:00
                        </td>
                      </tr>
                      <tr>
                        <td id="Hour_19_20" style="padding: 0.5rem; border: 1px solid #dee2e6;">
                          19:00-20:00
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- Specialties with Delete Buttons -->
              <div class="mb-3">
                <h5 style="margin-bottom: 0.5rem;">Specialties:</h5>
                <div style="padding: 0.75rem; background-color: #f8f9fa; border-radius: 4px;">
                  <div
                    *ngFor="let specialty of vet.specialties"
                    style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem; margin-bottom: 0.5rem; background-color: white; border-radius: 4px; border: 1px solid #dee2e6;"
                  >
                    <span style="font-weight: 500;">{{ specialty.name }}</span>
                    <button
                      class="btn btn-sm btn-danger"
                      style="padding: 0.25rem 0.75rem; font-size: 0.875rem;"
                      (click)="deleteSpecialty(specialty)"
                    >
                      Delete
                    </button>
                  </div>
                  <p
                    *ngIf="vet.specialties.length === 0"
                    style="margin: 0; color: #6c757d; font-style: italic;"
                  >
                    No specialties listed
                  </p>
                </div>
              </div>

              <!-- Status -->
              <div class="mb-3">
                <h5 style="margin-bottom: 0.5rem;">Status:</h5>
                <div style="padding: 0.75rem; background-color: #f8f9fa; border-radius: 4px;">
                  <span [class]="vet.active ? 'badge bg-success' : 'badge bg-danger'">
                    {{ vet.active ? 'Active' : 'Inactive' }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="text-center mb-5" *ngIf="vet">
        <a [routerLink]="['/vets', vet.vetId, 'edit']">
          <button class="btn btn-success btn-lg" style="min-width: 200px;">Edit Vet</button>
        </a>
      </div>

      <hr style="margin: 3rem 0;" />

      <div>
        <h3 class="form-label-rating text-center" style="margin-top: 5%">Educations</h3>
        <div class="col-md-12" *ngIf="educations && educations.length > 0">
          <div class="col-md-12">
            <div *ngIf="educations.length === 0" style="text-align: center">
              This vet has no educations listed.
            </div>
            <!-- Education Table -->
            <table class="table">
              <thead>
                <tr>
                  <th>Degree</th>
                  <th>Institution</th>
                  <th>Field Of Study</th>
                  <th>Start Year</th>
                  <th>End Year</th>
                  <th>Update</th>
                  <th>Delete</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let education of educations; trackBy: trackByEducationId">
                  <td>{{ education.degree }}</td>
                  <td>{{ education.schoolName }}</td>
                  <td>{{ education.fieldOfStudy }}</td>
                  <td>{{ education.startDate }}</td>
                  <td>{{ education.endDate }}</td>
                  <td>
                    <form
                      enctype="multipart/form-data"
                      id="updateEducationForm{{ education.educationId }}"
                      name="updateEducationForm"
                    >
                      <div
                        class="col-sm-12"
                        name="educationUpdate"
                        id="educationUpdate{{ education.educationId }}"
                        style="display: none"
                      >
                        <div class="form-group">
                          <label>Degree</label>
                          <input
                            type="text"
                            class="form-control"
                            id="updateDegree{{ education.educationId }}"
                            [(ngModel)]="education.degree"
                            name="updateDegree"
                            required
                          />
                        </div>
                        <div class="form-group">
                          <label>School Name</label>
                          <input
                            type="text"
                            class="form-control"
                            id="updateSchoolName{{ education.educationId }}"
                            [(ngModel)]="education.schoolName"
                            name="updateSchoolName"
                            required
                          />
                        </div>
                        <div class="form-group">
                          <label>Field of Study</label>
                          <input
                            type="text"
                            class="form-control"
                            id="updateFieldOfStudy{{ education.educationId }}"
                            [(ngModel)]="education.fieldOfStudy"
                            name="updateFieldOfStudy"
                            required
                          />
                        </div>
                        <div class="form-group">
                          <label>Start Date</label>
                          <input
                            type="text"
                            class="form-control"
                            id="updateStartDate{{ education.educationId }}"
                            [(ngModel)]="education.startDate"
                            name="updateStartDate"
                            required
                          />
                        </div>
                        <div class="form-group">
                          <label>End Date</label>
                          <input
                            type="text"
                            class="form-control"
                            id="updateEndDate{{ education.educationId }}"
                            [(ngModel)]="education.endDate"
                            name="updateEndDate"
                            required
                          />
                        </div>
                      </div>
                    </form>
                    <button
                      class="update-vet-button btn btn-success"
                      id="updateEducationBtn{{ education.educationId }}"
                      (click)="updateEducation(education.educationId)"
                    >
                      Update
                    </button>
                  </td>
                  <td>
                    <a
                      class="btn btn-danger educationButton"
                      href="javascript:void(0)"
                      (click)="deleteVetEducation(education.educationId)"
                      >Delete Education</a
                    >
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- Vet Education add button -->
      <div
        class="container-fluid"
        style="margin-top: 0.5rem; margin-bottom: 2rem; text-align: left"
      >
        <button
          class="add-vet-button btn btn-primary"
          style="width: 140px"
          (click)="addEducation()"
        >
          Add Education
        </button>
      </div>

      <!-- Education Form (conditionally shown) -->
      <div
        class="container-fluid"
        *ngIf="addEducationFormVisible"
        style="margin-top: 1rem; margin-bottom: 2rem;"
      >
        <form (ngSubmit)="saveEducation()" #addEducationForm="ngForm">
          <div class="row mb-3">
            <div class="col-md-3">
              <input
                type="text"
                class="form-control"
                placeholder="School Name"
                [(ngModel)]="newEducation.schoolName"
                name="schoolName"
                required
              />
            </div>
            <div class="col-md-3">
              <input
                type="text"
                class="form-control"
                placeholder="Degree"
                [(ngModel)]="newEducation.degree"
                name="degree"
                required
              />
            </div>
            <div class="col-md-3">
              <input
                type="text"
                class="form-control"
                placeholder="Field of Study"
                [(ngModel)]="newEducation.fieldOfStudy"
                name="fieldOfStudy"
                required
              />
            </div>
            <div class="col-md-3">
              <input
                type="text"
                class="form-control"
                placeholder="Start Date"
                [(ngModel)]="newEducation.startDate"
                name="startDate"
                required
              />
            </div>
          </div>
          <div class="row mb-3">
            <div class="col-md-3">
              <input
                type="text"
                class="form-control"
                placeholder="End Date"
                [(ngModel)]="newEducation.endDate"
                name="endDate"
                required
              />
            </div>
          </div>
          <div class="row">
            <div class="col-md-12 text-end">
              <button type="submit" class="btn btn-success" [disabled]="addEducationForm.invalid">
                Save
              </button>
              <button type="button" class="btn btn-secondary" (click)="cancelEducationForm()">
                Cancel
              </button>
            </div>
          </div>
        </form>
      </div>

      <div *ngIf="!vet" class="text-center">
        <p>Loading vet information...</p>
      </div>

      <!-- Ratings and Add Rating Section -->
      <div class="row g-5">
        <!-- Percentage of ratings -->
        <div class="col-md-6" style="margin-top: 9%" *ngIf="false">
          <h3 class="form-label-rating">Percentages of Ratings:</h3>
          <div class="container">
            <div id="ratings-list" style="text-align: left; margin-left: 40%"></div>
          </div>
        </div>
        <!-- Add Rating -->
        <div class="col-md-6" *ngIf="false">
          <div class="add-rating-container">
            <!-- Add Rating -->
            <div class="col-md-12">
              <div class="add-rating-container">
                <h3 class="form-label-rating">Add A Rating</h3>
                <!-- Add Rating Form -->
                <form enctype="multipart/form-data" id="ratingForm" name="ratingForm">
                  <div class="col-sm-12 form-label-rating">
                    <select name="rateScore" id="ratingScore" [(ngModel)]="rating.rateScore">
                      <option value="1">1/5</option>
                      <option value="2">2/5</option>
                      <option value="3">3/5</option>
                      <option value="4">4/5</option>
                      <option value="5">5/5</option>
                    </select>
                  </div>
                  <div class="col-sm-12">
                    <h5 class="form-label">Review Description</h5>
                    <textarea
                      class="form-control"
                      id="ratingDescription"
                      maxlength="350"
                      name="description"
                      [(ngModel)]="rating.rateDescription"
                      pattern="^(.*)"
                      placeholder="Description"
                      title="Maximum of 350 characters."
                    ></textarea>
                  </div>
                  <div class="col-sm-12 form-label-rating">
                    <label>Predefined Description:</label>
                    <input
                      type="radio"
                      name="predefinedDescription"
                      value="POOR"
                      [(ngModel)]="rating.predefinedDescription"
                      (change)="onPredefinedDescriptionChange('POOR')"
                    />
                    Poor
                    <input
                      type="radio"
                      name="predefinedDescription"
                      value="AVERAGE"
                      [(ngModel)]="rating.predefinedDescription"
                      (change)="onPredefinedDescriptionChange('AVERAGE')"
                    />
                    Average
                    <input
                      type="radio"
                      name="predefinedDescription"
                      value="GOOD"
                      [(ngModel)]="rating.predefinedDescription"
                      (change)="onPredefinedDescriptionChange('GOOD')"
                    />
                    Good
                    <input
                      type="radio"
                      name="predefinedDescription"
                      value="EXCELLENT"
                      [(ngModel)]="rating.predefinedDescription"
                      (change)="onPredefinedDescriptionChange('EXCELLENT')"
                    />
                    Excellent
                  </div>

                  <br />
                  <button
                    class="btn btn-primary ratingButton"
                    (click)="submitRatingForm()"
                    type="submit"
                  >
                    Submit
                  </button>
                  <i class="fa-solid fa-face-smile-beam"></i>
                </form>
              </div>
            </div>
          </div>
        </div>
        <div class="container-fluid py-4">
          <!-- RECENT & OLDER RATINGS -->
          <form onsubmit="void(0)" style="max-width: 20em; margin-top: 2em;">
            <div class="form-group">
              <label for="queryDate"><strong>Filter Ratings By Year</strong></label>
              <input
                id="queryDate"
                class="form-control"
                [(ngModel)]="yearQuery"
                name="yearQuery"
                placeholder="Enter year (e.g. 2024)"
                type="text"
              />
            </div>
            <button
              class="btn btn-primary ratingButton"
              (click)="getRecentRatingBasedOnDate()"
              type="submit"
            >
              Search
            </button>
          </form>

          <!-- Ratings Chart -->
          <div class="col-md-12" *ngIf="ratings && ratings.length > 0" style="margin-top: 5%">
            <div class="col-md-12">
              <h3 class="form-label-rating text-center">Ratings</h3>
              <div *ngIf="ratings.length === 0" style="text-align: center">
                This vet has no ratings.
              </div>
              <!-- Ratings Table -->
              <table class="table" style="margin-bottom: 10%">
                <thead>
                  <tr>
                    <th>Rating</th>
                    <th>Description</th>
                    <th>Update</th>
                    <th>Delete</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let rating of ratings; trackBy: trackByRatingId">
                    <td>{{ rating.rateScore }} / 5</td>
                    <td>{{ rating.rateDescription }}</td>

                    <!-- Rating Update Form -->
                    <td>
                      <form
                        enctype="multipart/form-data"
                        id="updateForm{{ rating.ratingId }}"
                        name="updateForm"
                      >
                        <div
                          class="col-sm-12"
                          name="ratingUpdate"
                          id="ratingUpdate{{ rating.ratingId }}"
                          style="display: none"
                        >
                          <select name="ratingOptions" id="ratingOptions{{ rating.ratingId }}">
                            <option value="1">1/5</option>
                            <option value="2">2/5</option>
                            <option value="3">3/5</option>
                            <option value="4">4/5</option>
                            <option value="5">5/5</option>
                          </select>
                          <br />
                          <h5 class="form-label">Review Description</h5>
                          <textarea
                            class="form-control"
                            id="updateDescription{{ rating.ratingId }}"
                            maxlength="350"
                            name="updateDescription"
                            [(ngModel)]="rating.rateDescription"
                            pattern="^(.*)"
                            placeholder="Update Description"
                            title="Maximum of 350 characters."
                          ></textarea>
                          <div class="col-sm-12 form-label-rating">
                            <label>Predefined Description:</label>
                            <input
                              type="checkbox"
                              name="predefinedDescriptionUpdate{{ rating.ratingId }}"
                              value="POOR"
                              (click)="handleCheckboxClick('POOR', rating.ratingId)"
                            />
                            Poor
                            <input
                              type="checkbox"
                              name="predefinedDescriptionUpdate{{ rating.ratingId }}"
                              value="GOOD"
                              (click)="handleCheckboxClick('GOOD', rating.ratingId)"
                            />
                            Good
                            <input
                              type="checkbox"
                              name="predefinedDescriptionUpdate{{ rating.ratingId }}"
                              value="EXCELLENT"
                              (click)="handleCheckboxClick('EXCELLENT', rating.ratingId)"
                            />
                            Excellent
                          </div>
                        </div>
                      </form>

                      <button
                        class="update-vet-button btn btn-success"
                        id="updateRatingBtn{{ rating.ratingId }}"
                        (click)="updateRating(rating.ratingId)"
                      >
                        Update
                      </button>
                    </td>
                    <td>
                      <a
                        class="btn btn-danger ratingButton"
                        href="javascript:void(0)"
                        (click)="deleteVetRating(rating.ratingId)"
                        >Delete Rating</a
                      >
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      <!-- Confirmation Modal -->
      <div class="confirmation-modal-backdrop modalOff" id="confirmationBackdrop">
        <div class="confirmation-modal" id="confirmationModal">
          <div class="confirmation-modal-header">
            <h5 id="confirmationModalTitle">Confirm Action</h5>
          </div>
          <div class="confirmation-modal-body">
            <p id="confirmationModalBody">Are you sure?</p>
          </div>
          <div class="confirmation-modal-footer">
            <button class="btn btn-secondary" id="confirmationModalCancelBtn">Cancel</button>
            <button class="btn btn-danger" id="confirmationModalConfirmBtn">Confirm</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      @import url('/css/vets/form.css');
      @import url('/css/vets/ratings.css');
      @import url('/css/vets/modal.css');
      @import url('/css/vets/sevenDaysCalendar.css');
    `,
  ],
})
export class VetDetailsComponent implements OnInit {
  private vetApi = inject(VetApiService);
  private route = inject(ActivatedRoute);

  vet: Vet | null = null;
  vetId: string = '';
  vetPhoto: Photo | null = null;
  badge: Badge | null = null;
  educations: Education[] = [];
  ratings: Rating[] = [];
  visitsList: unknown[] = [];
  addEducationFormVisible = false;
  newEducation: EducationRequest = {
    vetId: '',
    schoolName: '',
    degree: '',
    fieldOfStudy: '',
    startDate: '',
    endDate: '',
  };
  query: string = '';
  yearQuery: string = '';
  workdayToWorkHours = new Map();
  workHours: string[] = [];
  checkedCheckboxesUpdate: { [key: string]: string } = {};
  rating: RatingRequest = {
    vetId: '',
    rateScore: 0,
    rateDescription: '',
    predefinedDescription: null,
  };

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.vetId = params['vetId'];
      this.resetEducationForm(); // Initialize with vetId
      this.resetRatingForm(); // Initialize with vetId
      this.loadVetDetails();
      this.loadEducations();
      this.loadRatings();
      this.loadVisits();
    });

    setTimeout(() => {
      const backdrop = document.getElementById('confirmationBackdrop');
      if (backdrop) {
        backdrop.classList.remove('modalOn');
        backdrop.classList.add('modalOff');
      }
    }, 0);
  }

  loadVetDetails(): void {
    this.vetApi.getVetById(this.vetId).subscribe({
      next: vet => {
        this.vet = vet;
        if (vet.workHoursJson) {
          const workHoursObject = JSON.parse(vet.workHoursJson);
          for (const key in workHoursObject) {
            if (Object.prototype.hasOwnProperty.call(workHoursObject, key)) {
              this.workdayToWorkHours.set(key, workHoursObject[key]);
            }
          }
        }
        this.loadVetPhoto();
        this.loadVetBadge();
      },
      error: error => console.error('Error loading vet details:', error),
    });
  }

  loadVetPhoto(): void {
    this.vetApi.getVetDefaultPhoto(this.vetId).subscribe({
      next: photo => {
        this.vetPhoto = photo;
        if (this.vetPhoto.filename === 'vet_default.jpg') {
          this.vetPhoto.photo = this.vetPhoto.resourceBase64 || '';
        }
      },
      error: error => {
        console.error('Error loading default photo, trying regular photo:', error);
        this.vetApi.getVetPhoto(this.vetId).subscribe({
          next: photo => {
            this.vetPhoto = photo;
          },
          error: error => console.error('Error loading vet photo:', error),
        });
      },
    });
  }

  loadVetBadge(): void {
    this.vetApi.getVetBadge(this.vetId).subscribe({
      next: badge => {
        this.badge = badge;
      },
      error: error => console.error('Error loading vet badge:', error),
    });
  }

  loadEducations(): void {
    this.vetApi.getVetEducations(this.vetId).subscribe({
      next: educations => {
        this.educations = educations;
      },
      error: error => console.error('Error loading educations:', error),
    });
  }

  loadRatings(): void {
    this.vetApi.getVetRatings(this.vetId).subscribe({
      next: ratings => {
        this.ratings = ratings;
        this.percentageOfRatings();
      },
      error: error => {
        console.error('Error loading ratings:', error);
        this.ratings = [];
        this.percentageOfRatings();

        const ratingsContainer = document.getElementById('ratings-list');
        if (ratingsContainer) {
          ratingsContainer.innerHTML =
            'Ratings are currently not available. Please try again later.';
        }
      },
    });
  }

  loadVisits(): void {
    this.vetApi.getVetVisits(this.vetId).subscribe({
      next: visits => {
        this.visitsList = visits;
      },
      error: error => console.error('Error loading visits:', error),
    });
  }

  selectWorkday(selectedWorkday: string): void {
    this.workHours = [];

    if (this.workdayToWorkHours.has(selectedWorkday)) {
      this.workHours = this.workdayToWorkHours.get(selectedWorkday);
    } else {
    }

    const table = document.getElementById('workHoursTable') as HTMLElement;
    const tdElements = table.getElementsByTagName('td');

    if (table.style.display === 'none') {
      table.style.display = 'block';
    } else {
      table.style.display = 'none';
    }

    for (let i = 0; i < tdElements.length; i++) {
      const td = tdElements[i];
      const tdId = td.id;

      if (this.workHours.includes(tdId)) {
        td.style.border = '2px solid green';
      } else {
        td.style.border = '2px solid black';
      }
    }
  }

  deleteSpecialty(specialty: { name: string }): void {
    if (!confirm(`Are you sure you want to delete the specialty "${specialty.name}"?`)) return;
  }

  show(event: MouseEvent, vetId: string): void {
    const modal = document.querySelector(`.m${vetId}`) as HTMLElement;
    if (modal) {
      const left = event.pageX;
      const top = event.clientY;

      if (document.documentElement.clientWidth > 960) {
        modal.style.left = left + 221 + 'px';
      } else if (document.documentElement.clientWidth < 420) {
        modal.style.left = '170px';
      } else if (document.documentElement.clientWidth < 510) {
        modal.style.left = left + 334.5 / 2.5 + 'px';
      } else {
        modal.style.left = left + 200 + 'px';
      }

      modal.style.top = top + 'px';
      modal.classList.remove('modalOff');
      modal.classList.add('modalOn');
    }
  }

  hide(_event: MouseEvent, vetId: string): void {
    const modal = document.querySelector(`.m${vetId}`) as HTMLElement;
    if (modal) {
      modal.classList.remove('modalOn');
      modal.classList.add('modalOff');
    }
  }

  showConfirmationModal(title: string, body: string, confirmCallback: (() => void) | null): void {
    const backdrop = document.getElementById('confirmationBackdrop');
    const modalTitle = document.getElementById('confirmationModalTitle');
    const modalBody = document.getElementById('confirmationModalBody');
    const confirmBtn = document.getElementById('confirmationModalConfirmBtn');
    const cancelBtn = document.getElementById('confirmationModalCancelBtn');

    if (modalTitle) modalTitle.textContent = title;
    if (modalBody) modalBody.textContent = body;

    const hideModal = (): void => {
      if (backdrop) {
        backdrop.classList.remove('modalOn');
        backdrop.classList.add('modalOff');
      }
    };

    if (cancelBtn) cancelBtn.onclick = hideModal;
    if (confirmBtn) {
      confirmBtn.onclick = () => {
        if (confirmCallback) {
          confirmCallback();
        }
        hideModal();
      };
    }

    if (backdrop) {
      backdrop.classList.remove('modalOff');
      backdrop.classList.add('modalOn');
    }
  }

  handleCheckboxClick(value: string, ratingId: string): void {
    this.checkedCheckboxesUpdate[ratingId] = value;
    const checkboxes = document.querySelectorAll(
      `input[type="checkbox"][name="predefinedDescriptionUpdate${ratingId}"]`
    );
    Array.from(checkboxes).forEach(checkbox => {
      const input = checkbox as HTMLInputElement;
      if (input.value !== value) {
        input.checked = false;
      }
    });
  }

  onPredefinedDescriptionChange(value: PredefinedDescription): void {
    this.rating.predefinedDescription = value;
  }

  togglePredefinedDescription(rating: Record<string, unknown>, value: string): void {
    if (rating['predefinedDescription' + value] === value) {
      rating['predefinedDescription' + value] = null;
    } else {
      rating['predefinedDescription' + value] = value;
    }
  }

  trackByEducationId(_index: number, education: Education): string {
    return education.educationId;
  }

  trackByRatingId(_index: number, rating: Rating): string {
    return rating.ratingId;
  }

  updateEducation(educationId: string): void {
    const btn = document.getElementById(`updateEducationBtn${educationId}`);
    const updateContainer = document.getElementById(`educationUpdate${educationId}`);

    if (!btn || !updateContainer) return;

    const updatedDegree = (
      document.getElementById(`updateDegree${educationId}`) as HTMLInputElement
    )?.value;
    const updatedSchoolName = (
      document.getElementById(`updateSchoolName${educationId}`) as HTMLInputElement
    )?.value;
    const updatedFieldOfStudy = (
      document.getElementById(`updateFieldOfStudy${educationId}`) as HTMLInputElement
    )?.value;
    const updatedStartDate = (
      document.getElementById(`updateStartDate${educationId}`) as HTMLInputElement
    )?.value;
    const updatedEndDate = (
      document.getElementById(`updateEndDate${educationId}`) as HTMLInputElement
    )?.value;

    const updatedEducation: EducationRequest = {
      vetId: this.vetId,
      schoolName: updatedSchoolName,
      degree: updatedDegree,
      fieldOfStudy: updatedFieldOfStudy,
      startDate: updatedStartDate,
      endDate: updatedEndDate,
    };

    if (updateContainer.style.display === 'none') {
      updateContainer.style.display = 'block';
      btn.textContent = 'Save';
    } else if (btn.textContent === 'Save') {
      if (
        !updatedDegree ||
        !updatedSchoolName ||
        !updatedFieldOfStudy ||
        !updatedStartDate ||
        !updatedEndDate
      ) {
        this.showConfirmationModal(
          'Validation Error',
          'Please fill in all education fields.',
          null
        );
        return;
      }

      const confirmUpdate = (): void => {
        this.vetApi.updateVetEducation(this.vetId, educationId, updatedEducation).subscribe({
          next: () => {
            this.showConfirmationModal('Success', 'Your education was successfully updated!', null);
            this.loadEducations();
          },
          error: error => {
            this.showConfirmationModal(
              'Error',
              error.error?.errors || 'Could not update education.',
              null
            );
          },
        });

        updateContainer.style.display = 'none';
        btn.textContent = 'Update';
      };

      this.showConfirmationModal(
        'Confirm Update',
        'Are you sure you want to save these changes?',
        confirmUpdate
      );
    }
  }

  deleteVetEducation(educationId: string): void {
    const deleteAction = (): void => {
      this.vetApi.deleteVetEducation(this.vetId, educationId).subscribe({
        next: () => {
          this.showConfirmationModal(
            'Success',
            `Education ${educationId} was deleted successfully!`,
            null
          );
          this.loadEducations();
        },
        error: error => {
          this.showConfirmationModal(
            'Error',
            error.error?.errors || 'Could not delete education.',
            null
          );
        },
      });
    };

    this.showConfirmationModal(
      'Confirm Deletion',
      `Are you sure you want to delete education ${educationId}?`,
      deleteAction
    );
  }

  saveEducation(): void {
    const education: EducationRequest = {
      vetId: this.vetId,
      schoolName: this.newEducation.schoolName,
      degree: this.newEducation.degree,
      fieldOfStudy: this.newEducation.fieldOfStudy,
      startDate: this.newEducation.startDate,
      endDate: this.newEducation.endDate,
    };

    this.vetApi.createVetEducation(this.vetId, education).subscribe({
      next: () => {
        this.addEducationFormVisible = false;
        this.showConfirmationModal('Success', 'Your education was successfully added!', null);
        this.loadEducations();
        this.resetEducationForm();
      },
      error: error => {
        const errorMessage =
          error.error?.message || 'An error occurred while adding the education.';
        this.showConfirmationModal('Error', 'Error adding education: ' + errorMessage, null);
      },
    });
  }

  getRecentRatingBasedOnDate(): void {
    const wrongYearPattern = /^\d{4}$/;
    const year = new Date().getFullYear();

    if (!wrongYearPattern.test(this.yearQuery || '')) {
      alert('Invalid year format. Please enter a valid year.');
      return;
    } else if (!this.yearQuery || this.yearQuery === '') {
      const newYear = year - 2;
      this.vetApi.getVetRatingsByDate(this.vetId, newYear).subscribe({
        next: ratings => {
          this.ratings = ratings;
        },
        error: error => console.error('Error loading ratings by date:', error),
      });
    } else {
      this.vetApi.getVetRatingsByDate(this.vetId, parseInt(this.yearQuery || '2024')).subscribe({
        next: ratings => {
          this.ratings = ratings;
        },
        error: error => console.error('Error loading ratings by date:', error),
      });
    }
  }

  deleteVetRating(ratingId: string): void {
    const deleteAction = (): void => {
      this.vetApi.deleteVetRating(this.vetId, ratingId).subscribe({
        next: () => {
          this.showConfirmationModal('Success', `${ratingId} Deleted Successfully!`, null);
          this.loadRatings();
        },
        error: error => {
          this.showConfirmationModal(
            'Error',
            error.error?.errors || 'Could not delete rating.',
            null
          );
        },
      });
    };

    this.showConfirmationModal(
      'Confirm Deletion',
      `Are you sure you want to delete rating ${ratingId}?`,
      deleteAction
    );
  }

  updateRating(ratingId: string): void {
    const btn = document.getElementById(`updateRatingBtn${ratingId}`);
    const updateContainer = document.getElementById(`ratingUpdate${ratingId}`);
    const selectedValue = parseInt(
      (document.getElementById(`ratingOptions${ratingId}`) as HTMLSelectElement)?.value || '0'
    );

    if (selectedValue < 1 || selectedValue > 5) {
      alert('rateScore should be between 1 and 5 ' + selectedValue);
      return;
    }

    const updatedDescription = (
      document.getElementById(`updateDescription${ratingId}`) as HTMLTextAreaElement
    )?.value;
    const predefinedDesc = document.querySelector(
      `input[name="predefinedDescriptionUpdate${ratingId}"]:checked`
    ) as HTMLInputElement;

    const updatedRating: RatingRequest = {
      vetId: this.vetId,
      rateScore: selectedValue,
      rateDescription: updatedDescription?.trim() === '' ? null : updatedDescription,
      rateDate: new Date().toISOString(),
      predefinedDescription: (predefinedDesc?.value as PredefinedDescription) || null,
    };

    if (updateContainer && updateContainer.style.display === 'none') {
      updateContainer.style.display = 'block';
      if (btn) btn.textContent = 'Save';
    } else if (btn && btn.textContent === 'Save') {
      const confirmUpdate = (): void => {
        this.vetApi.updateVetRating(this.vetId, ratingId, updatedRating).subscribe({
          next: () => {
            this.showConfirmationModal('Success', 'Your review was successfully updated!', null);
            this.loadRatings();
            this.loadVetBadge();
          },
          error: error => {
            this.showConfirmationModal(
              'Error',
              error.error?.errors || 'Could not update rating.',
              null
            );
          },
        });

        if (updateContainer) updateContainer.style.display = 'none';
        if (btn) btn.textContent = 'Update';
        Array.from(
          document.querySelectorAll(`input[name="predefinedDescriptionUpdate${ratingId}"]`)
        ).forEach(radio => {
          const input = radio as HTMLInputElement;
          input.checked = false;
        });
      };

      this.showConfirmationModal(
        'Confirm Update',
        'Are you sure you want to save this rating?',
        confirmUpdate
      );
    }
  }

  submitRatingForm(): void {
    if (!this.rating.rateScore) {
      alert('Please select a rating score');
      return;
    }

    const ratingRequest: RatingRequest = {
      vetId: this.vetId,
      rateScore: this.rating.rateScore,
      rateDescription:
        this.rating.rateDescription?.trim() === '' ? '' : this.rating.rateDescription,
      rateDate: new Date().toISOString(),
      predefinedDescription: this.rating.predefinedDescription,
    };

    this.vetApi.createVetRating(this.vetId, ratingRequest).subscribe({
      next: () => {
        alert('Your review was successfully added!');
        this.loadRatings();
        this.loadVetBadge();
        this.resetRatingForm();
      },
      error: error => {
        const errorMessage =
          error.error?.errors || 'An error occurred while adding the rating. Please try again.';
        alert(errorMessage);
      },
    });
  }

  percentageOfRatings(): void {
    if (this.ratings && this.ratings.length > 0) {
      const ratingsContainer = document.getElementById('ratings-list');
      if (ratingsContainer) {
        const ratingCounts: { [key: number]: number } = {};
        this.ratings.forEach(rating => {
          const score = rating.rateScore;
          ratingCounts[score] = (ratingCounts[score] || 0) + 1;
        });

        const totalRatings = this.ratings.length;
        let html = '';
        const ratingsArray: unknown[] = [];

        for (const score in ratingCounts) {
          const count = ratingCounts[score];
          const percentage = (count / totalRatings) * 100;
          ratingsArray.push({ rating: parseFloat(score), percentage });
        }

        ratingsArray.sort(
          (a, b) =>
            (b as { rating: number; percentage: number }).rating -
            (a as { rating: number; percentage: number }).rating
        );

        for (const ratingObj of ratingsArray) {
          const obj = ratingObj as { rating: number; percentage: number };
          html += obj.rating + ' stars - ' + obj.percentage.toFixed(0) + '%';
          html += '<br>';
        }

        ratingsContainer.innerHTML = html.slice(0, -2);
      }
    } else {
      const ratingsContainer = document.getElementById('ratings-list');
      if (ratingsContainer) {
        ratingsContainer.innerHTML = 'No ratings available';
      }
    }
  }

  addEducation(): void {
    this.addEducationFormVisible = true;
  }

  cancelEducationForm(): void {
    this.resetEducationForm();
    this.addEducationFormVisible = false;
  }

  resetEducationForm(): void {
    this.newEducation = {
      vetId: this.vetId,
      schoolName: '',
      degree: '',
      fieldOfStudy: '',
      startDate: '',
      endDate: '',
    };
  }

  resetRatingForm(): void {
    this.rating = {
      vetId: this.vetId,
      rateScore: 0,
      rateDescription: '',
      predefinedDescription: null,
    };
  }
}
