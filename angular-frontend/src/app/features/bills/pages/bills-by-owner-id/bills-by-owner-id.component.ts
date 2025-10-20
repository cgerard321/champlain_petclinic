import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BillApiService } from '../../api/bill-api.service';
import { Bill } from '../../models/bill.model';

@Component({
  selector: 'app-bills-by-owner-id',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <h2 class="titleOwner">
      Bill History for customer {{ owner.firstName }}, {{ owner.lastName }}
    </h2>

    <a class="newBtn btn btn-primary" [routerLink]="['/bills']">View all Bills</a>
    <a
      class="btn btn-danger float-end"
      href="javascript:void(0)"
      (click)="deleteBillsByOwnerId(owner.id)"
      >DELETE ALL BILLS FOR {{ owner.firstName }}, {{ owner.lastName }}</a
    >

    <table class="table table-striped">
      <thead>
        <tr>
          <td>Bill ID</td>
          <td>Customer ID</td>
          <td>Owner Details</td>
          <td>Details</td>
        </tr>
      </thead>

      <tr id="customerId" *ngFor="let bill of billsByOwnerId">
        <td>{{ bill.billId }}</td>
        <td>{{ bill.customerId }}</td>
        <td><a [routerLink]="['/owners/details', bill.customerId]">Owner Details</a></td>
        <td>
          <a [routerLink]="['/bills/details', bill.billId, 'owner', bill.customerId]"
            >Bill Details</a
          >
        </td>
      </tr>
    </table>
  `,
  styles: [],
})
export class BillsByOwnerIdComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private billApi = inject(BillApiService);

  customerId: string = '';
  billsByOwnerId: Bill[] = [];
  owner: Record<string, unknown> = {};

  ngOnInit(): void {
    this.customerId = this.route.snapshot.paramMap.get('customerId') || '';
    this.loadBills();
    this.loadOwner();
  }

  loadBills(): void {
    this.billApi.getBillsByCustomer(this.customerId).subscribe({
      next: bills => {
        this.billsByOwnerId = bills;
      },
      error: () => {},
    });
  }

  loadOwner(): void {
    this.owner = {
      id: this.customerId,
      firstName: 'John',
      lastName: 'Doe',
    };
  }

  deleteBillsByOwnerId(customerId: string): void {
    const varIsConf = confirm(
      'You are about to delete all the bills for the owner with ownerId ' +
        customerId +
        '. Are you sure this is what you want to do?'
    );
    if (varIsConf) {
      this.billApi.deleteAllBillsByVet(customerId).subscribe({
        next: () => {
          alert('The bills for owner with id ' + customerId + ' were deleted successfully');
          this.loadBills();
        },
        error: () => {
          alert('Could not delete bills');
        },
      });
    }
  }
}
