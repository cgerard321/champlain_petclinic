import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

import { PetDetails } from '@features/cust/components/customer-pet-details/customer-pet-details';
import { Customer } from '@features/cust/models/customer.model';
import { CustomerService } from '@features/cust/services/customer-service';
@Component({
  selector: 'app-customer-details',
  templateUrl: './customerDetails.html',
  styleUrl: './customerDetails.css',
})
export class CustomerDetails {


}
