import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { provideNativeDateAdapter, MatOption } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelect } from '@angular/material/select';

import { AuthState } from '@core/services/auth-state';

@Component({
  imports: [
    MatFormFieldModule,
    MatCardModule,
    MatDatepickerModule,
    MatInputModule,
    MatSelect,
    MatOption,
    MatDividerModule,
    MatListModule,
    MatButtonModule,
  ],
  providers: [provideNativeDateAdapter()],
  selector: 'app-bill',
  styleUrl: './createBill.css',
  templateUrl: './createBill.html',
})
export class CreateBill {
  protected auth = inject(AuthState);
}
