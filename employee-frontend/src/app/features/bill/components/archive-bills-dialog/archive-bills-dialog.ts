import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-archive-bills-dialog',
  imports: [MatButtonModule, MatDialogModule],
  templateUrl: './archive-bills-dialog.html',
  styleUrl: './archive-bills-dialog.css',
})
export class ArchiveBillsDialog {
  private readonly dialogRef = inject(MatDialogRef<ArchiveBillsDialog, boolean>);

  protected cancel(): void {
    this.dialogRef.close(false);
  }

  protected confirm(): void {
    this.dialogRef.close(true);
  }
}
