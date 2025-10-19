import { Component, OnInit, inject, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { VisitApiService } from '../../api/visit-api.service';
import { Visit } from '../../models/visit.model';

@Component({
  selector: 'app-visit-details-info',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="container mt-5">
      <div class="row">
        <div class="col-md-6">
          <a class="btn btn-success" routerLink="/visit-list">Back to Visits</a>
        </div>
      </div>
      <br>
      <div class="alert alert-danger" *ngIf="visit?.status === 'CANCELLED'">
        NOTICE: This visit is cancelled.
      </div>
      <br>
      <div class="row">
        <div class="col-md-4 text-start">
          <h1 class="visit-title">VISIT(<span [ngStyle]="{'color': visit?.status === 'CANCELLED' ? 'red' : visit?.status === 'UPCOMING' ? 'blue' : visit?.status === 'CONFIRMED' ? 'green' : 'black'}">{{visit?.status}}</span>)</h1>
        </div>
        <div class="col-md-4"></div> <!-- Empty column for spacing -->
        <div class="col-md-4 text-end">
          <h3 class="visit-id">Visit ID: {{visit?.visitId}}</h3>
          <h5>Date: {{visit?.visitDate | date:'yyyy-MM-ddTHH:mm:ss'}}</h5>
        </div>
        <div class="col-md-4 text-start">
          <h5>Visit Description: {{visit?.description}}</h5>
        </div>
      </div>
      <hr>
      <div class="row g-5">
        <div class="col-md-6">
          <h3 class="text-center">Pet Information</h3>
          <h5>Pet ID: {{visit?.petId}}</h5>
          <h5>Pet Name: {{visit?.petName}} </h5>
          <h5>Birth Date: {{visit?.petBirthDate | date: 'yyyy-MM-dd'}}</h5>
          <hr>
          <h3 class="text-center">Vet Information</h3>
          <h5>Vet ID: {{visit?.practitionerId}}</h5>
          <h5>Vet Name: {{visit?.vetFirstName}} {{visit?.vetLastName}} </h5>
          <h5>Email: {{visit?.vetEmail}}</h5>
          <h5>Phone Number: {{visit?.vetPhoneNumber}}</h5>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @import url('/css/petclinic.css');
    
    .visit-title, .visit-id {
      font-size: 24px;
      font-weight: bold;
    }
  `],
  encapsulation: ViewEncapsulation.None
})
export class VisitDetailsInfoComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private visitApi = inject(VisitApiService);

  visit: Visit | null = null;
  visitId: string = '';

  ngOnInit(): void {
    this.visitId = this.route.snapshot.paramMap.get('visitId') || '';
    this.loadVisit();
  }

  loadVisit(): void {
    this.visitApi.getVisitById(this.visitId).subscribe({
      next: (visit) => this.visit = visit,
      error: (error) => {
        console.error('Error loading visit:', error);
        alert('Failed to load visit');
      }
    });
  }
}

