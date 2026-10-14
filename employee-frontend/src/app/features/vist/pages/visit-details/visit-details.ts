import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { VisitResponseModel } from '@features/vist/models/VisitResponseModel';
import { VisitService } from '@features/vist/services/visit-service';

@Component({
  selector: 'app-visit-details',
  imports: [DatePipe, RouterLink],
  templateUrl: './visit-details.html',
  styleUrl: './visit-details.css',
})
export class VisitDetails implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly visitService = inject(VisitService);

  protected readonly visit = signal<VisitResponseModel | null>(null);
  protected readonly isLoading = signal(true);
  protected readonly errorMessage = signal<string | null>(null);

  ngOnInit(): void {
    const visitId = this.route.snapshot.paramMap.get('visitId');

    if (!visitId) {
      this.errorMessage.set($localize`No visit ID was provided.`);
      this.isLoading.set(false);
      return;
    }

    this.visitService.getVisitById(visitId).subscribe({
      next: (visit) => {
        this.visit.set(visit);
        this.isLoading.set(false);
      },
      error: (error: unknown) => {
        this.isLoading.set(false);

        this.errorMessage.set(
          error instanceof HttpErrorResponse && error.status === 404
            ? $localize`Visit not found.`
            : $localize`Unable to load visit details.`,
        );
      },
    });
  }
}
