import { Component, inject, signal } from '@angular/core';
import { form, FormField, pattern, required } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { isApiError } from '@core/models/api-error';
import { Visit } from '@features/vist/models/Visit';
import { VisitService } from '@features/vist/services/visit-service';

interface VetNoteDialogData {
  visit: Visit;
}

@Component({
  selector: 'app-vet-note-dialog',
  imports: [
    FormField,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './VetNoteDialog.html',
  styleUrl: '../FollowUpVisit/FollowUpVisitDialog.css',
})
export class VetNoteDialog {
  private readonly data = inject<VetNoteDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject(MatDialogRef<VetNoteDialog, Visit>);
  private readonly visitService = inject(VisitService);

  protected readonly visit = this.data.visit;
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);

  protected readonly model = signal({ vetNote: this.visit.vetNote ?? '' });

  protected readonly noteForm = form(this.model, (schemaPath) => {
    required(schemaPath.vetNote, {
      message: $localize`:@@visits.note.noteRequired:La note est requise`,
    });
    // at least 5 characters, not counting spaces at the start or end
    pattern(schemaPath.vetNote, /\S[\s\S]{3,}\S/, {
      message: $localize`:@@visits.note.noteInvalid:La note doit contenir au moins 5 caractères`,
    });
  });

  protected submit(event: Event): void {
    event.preventDefault();
    if (this.noteForm().invalid()) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.visitService.addVetNote(this.visit.visitId, this.model().vetNote.trim()).subscribe({
      next: (visit) => {
        this.isSubmitting.set(false);
        this.dialogRef.close(visit);
      },
      error: (error: unknown) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(
          isApiError(error)
            ? error.message
            : $localize`:@@visits.note.saveError:Impossible d'enregistrer la note.`,
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
