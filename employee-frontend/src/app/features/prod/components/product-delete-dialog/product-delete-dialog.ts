import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';

export interface ProductDeleteDialogData {
  title: string;
  message: string;
  confirmLabel: string;
}

@Component({
  selector: 'app-product-delete-dialog',
  imports: [MatButtonModule, MatDialogModule, MatIconModule],
  templateUrl: './product-delete-dialog.html',
  styleUrl: './product-delete-dialog.css',
})
export class ProductDeleteDialog {
  private readonly dialogRef = inject(MatDialogRef<ProductDeleteDialog, boolean>);
  protected readonly data = inject<ProductDeleteDialogData>(MAT_DIALOG_DATA);

  protected confirm(): void {
    this.dialogRef.close(true);
  }

  protected cancel(): void {
    this.dialogRef.close(false);
  }
}
