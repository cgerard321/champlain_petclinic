import { Component, computed, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

import { AuthState } from '@core/services/auth-state';
import { CreateBill } from '@features/bill/components/createBill/create-bill';
import { Roles } from '@shared/models/roles';



@Component({
  selector: 'app-bill',
  styleUrl: './bill.css',
  templateUrl: './bill.html',
})
export class Bill {
  protected readonly auth = inject(AuthState);
  protected readonly dialog = inject(MatDialog);

  // Only vets can start a bill. The CRUD work is handled in the follow-up stories.
  protected readonly canCreateBill = computed(() => this.auth.hasRole(Roles.vet));

  showDialog = false;
  openCreateBill(): void {
    this.dialog.open(CreateBill);
  }
}
