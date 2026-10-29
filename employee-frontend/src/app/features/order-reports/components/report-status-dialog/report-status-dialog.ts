import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface ReportStatusDialogData {
  action: 'RESOLVED' | 'REJECTED';
}

// returns note, undefined on cancel
@Component({
  selector: 'app-report-status-dialog',
  imports: [MatButtonModule, MatDialogModule, MatFormFieldModule, MatInputModule],
  templateUrl: './report-status-dialog.html',
  styleUrl: './report-status-dialog.css',
})
export class ReportStatusDialog {
  private readonly data = inject<ReportStatusDialogData>(MAT_DIALOG_DATA);
  private readonly dialogRef = inject<MatDialogRef<ReportStatusDialog, string>>(MatDialogRef);

  protected readonly isReject = this.data.action === 'REJECTED';
  protected readonly note = signal('');
  protected readonly showNoteError = signal(false);

  protected readonly title: string;
  protected readonly message: string;
  protected readonly noteLabel: string;
  protected readonly confirmLabel: string;

  constructor() {
    if (this.isReject) {
      this.title = $localize`:@@orderReports.statusDialog.rejectTitle:Rejeter le signalement`;
      this.message = $localize`:@@orderReports.statusDialog.rejectMessage:Le signalement sera fermé comme rejeté. Indiquez la raison du rejet.`;
      this.noteLabel = $localize`:@@orderReports.statusDialog.rejectNote:Raison du rejet`;
      this.confirmLabel = $localize`:@@orderReports.statusDialog.rejectConfirm:Rejeter`;
    } else {
      this.title = $localize`:@@orderReports.statusDialog.resolveTitle:Résoudre le signalement`;
      this.message = $localize`:@@orderReports.statusDialog.resolveMessage:Le signalement sera fermé comme résolu. Vous pouvez ajouter une note de résolution.`;
      this.noteLabel = $localize`:@@orderReports.statusDialog.resolveNote:Note de résolution (facultatif)`;
      this.confirmLabel = $localize`:@@orderReports.statusDialog.resolveConfirm:Résoudre`;
    }
  }

  protected onNoteInput(event: Event): void {
    const textarea = event.target as HTMLTextAreaElement;
    this.note.set(textarea.value);
    this.showNoteError.set(false);
  }

  protected confirm(): void {
    const trimmedNote = this.note().trim();
    if (this.isReject && trimmedNote === '') {
      this.showNoteError.set(true);
      return;
    }
    this.dialogRef.close(trimmedNote);
  }
}
