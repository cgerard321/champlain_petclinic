import { DatePipe, formatDate } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { form, FormField, pattern, required } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { isApiError } from '@core/models/api-error';
import { TimeSlot } from '@features/vist/models/TimeSlot';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';

interface FollowUpVisitDialogData {
  visit: Visit;
}

interface FollowUpVisitFormModel {
  date: Date | null;
  visitDate: string;
  description: string;
}

@Component({
  selector: 'app-follow-up-visit-dialog',
  imports: [
    DatePipe,
    FormField,
    MatButtonModule,
    MatDatepickerModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  providers: [provideNativeDateAdapter()],
  templateUrl: './FollowUpVisitDialog.html',
  styleUrl: './FollowUpVisitDialog.css',
})
export class FollowUpVisitDialog {
  private readonly data = inject<FollowUpVisitDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<FollowUpVisitDialog, Visit>);
  private readonly visitService = inject(VisitService);

  protected readonly visit = this.data.visit;
  protected readonly minDate = new Date();
  protected readonly timeSlots = signal<TimeSlot[]>([]);
  protected readonly isLoadingSlots = signal(false);
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly model = signal<FollowUpVisitFormModel>({
    date: null,
    visitDate: '',
    description: '',
  });

  protected readonly followUpForm = form(this.model, (schemaPath) => {
    required(schemaPath.date, {
      message: $localize`:@@visits.followUp.dateRequired:La date est requise`,
    });
    required(schemaPath.visitDate, {
      message: $localize`:@@visits.followUp.timeRequired:L'heure est requise`,
    });
    required(schemaPath.description, {
      message: $localize`:@@visits.followUp.descriptionRequired:La description est requise`,
    });
    pattern(schemaPath.description, /\S/, {
      message: $localize`:@@visits.followUp.descriptionInvalid:La description doit contenir du texte`,
    });
  });

  protected loadTimeSlots(date: Date | null): void {
    this.model.update((current) => ({ ...current, date, visitDate: '' }));
    this.timeSlots.set([]);
    this.errorMessage.set(null);

    if (!date) {
      return;
    }

    this.isLoadingSlots.set(true);
    const selectedDate = formatDate(date, 'yyyy-MM-dd', 'en-CA');
    this.visitService.getAvailableTimeSlots(this.visit.practitionerId, selectedDate).subscribe({
      next: (timeSlots) => {
        const now = Date.now();
        this.timeSlots.set(
          timeSlots.filter(
            (timeSlot) => timeSlot.available && new Date(timeSlot.startTime).getTime() > now,
          ),
        );
        this.isLoadingSlots.set(false);
      },
      error: (error: unknown) => {
        this.isLoadingSlots.set(false);
        this.errorMessage.set(
          isApiError(error)
            ? error.message
            : $localize`:@@visits.followUp.slotsError:Impossible de charger les heures disponibles.`,
        );
      },
    });
  }

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.followUpForm().invalid()) {
      return;
    }

    const formValue = this.model();
    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.visitService
      .addFollowUpVisit(this.visit, formValue.visitDate, formValue.description.trim())
      .subscribe({
        next: (visit) => {
          this.isSubmitting.set(false);
          this.dialogRef.close(visit);
        },
        error: (error: unknown) => {
          this.isSubmitting.set(false);
          this.errorMessage.set(
            isApiError(error)
              ? error.message
              : $localize`:@@visits.followUp.scheduleError:Impossible de planifier le suivi.`,
          );
        },
      });
  }

  protected cancel(): void {
    if (!this.isSubmitting()) {
      this.dialogRef.close();
    }
  }
}
