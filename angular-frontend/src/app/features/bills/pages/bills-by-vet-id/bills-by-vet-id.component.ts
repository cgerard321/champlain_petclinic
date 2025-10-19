import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BillApiService } from '../../api/bill-api.service';
import { Bill } from '../../models/bill.model';

@Component({
  selector: 'app-bills-by-vet-id',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <h2 class="titleOwner">Bill History for vet {{vet.firstName}}, {{vet.lastName}}</h2>

    <table class="table table-striped">
      <thead>
        <tr>
          <td>Bill ID</td>
          <td>Customer ID</td>
          <td>Vet ID</td>
          <td>Vet Details</td>
          <td>Details</td>
        </tr>
      </thead>

      <tr id="vetId" *ngFor="let bill of billsByVetId">
        <td>{{bill.billId}}</td>
        <td>{{bill.customerId}}</td>
        <td>{{bill.vetId}}</td>
        <td><a [routerLink]="['/vets/details', bill.vetId]">Vet Details</a></td>
        <td><a [routerLink]="['/bills/details', bill.billId, 'owner', bill.customerId]">Bill Details</a></td>
      </tr>
    </table>
    <a class="newBtn btn btn-primary" [routerLink]="['/bills']">
      View all Bills</a> <br>
    <a class="btn btn-danger" href="javascript:void(0)" (click)="deleteBillsByVetId(bill.vetId)">Delete All Bills</a>
  `,
  styles: []
})
export class BillsByVetIdComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private billApi = inject(BillApiService);

  vetId: string = '';
  billsByVetId: Bill[] = [];
  vet: any = {};

  ngOnInit(): void {
    this.vetId = this.route.snapshot.paramMap.get('vetId') || '';
    this.loadBills();
    this.loadVet();
  }

  loadBills(): void {
    this.billApi.getBillsByVet(this.vetId).subscribe({
      next: (bills) => {
        this.billsByVetId = bills;
      },
      error: () => {}
    });
  }

  loadVet(): void {
    this.vet = {
      id: this.vetId,
      firstName: 'Dr. John',
      lastName: 'Smith'
    };
  }

  deleteBillsByVetId(vetId: string): void {
    const varIsConf = confirm('You are about to all bills by vet ' + vetId + '. Is it what you want to do ? ');
    if (varIsConf) {
      this.billApi.deleteAllBillsByVet(vetId).subscribe({
        next: () => {
          alert(vetId + " bills were deleted successfully");
          this.loadBills();
        },
        error: () => {
          alert("Could not delete bills");
        }
      });
    }
  }
}

