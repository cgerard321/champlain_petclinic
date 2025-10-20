import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BillApiService } from '../../api/bill-api.service';
import { Bill } from '../../models/bill.model';

@Component({
  selector: 'app-bill-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <link
      crossorigin="anonymous"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.0/dist/css/bootstrap.min.css"
      integrity="sha384-KyZXEAg3QhqLMpG8r+8fhAXLRk2vvoC2f3B09zVXn8CA5QIVfZOJ3BCsw2P0p/We"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />

    <div class="container mt-5" *ngIf="bills">
      <div class="row">
        <div class="col-md-6">
          <a class="btn btn-primary" href="http://localhost:8080/#!/bills">Back to History</a>
        </div>
        <div class="col-md-6 text-end">
          <a class="newBtn btn btn-primary" [routerLink]="['/bills', bills.billId, 'edit']"
            >Update Bill</a
          >
        </div>
      </div>
      <br />
      <div class="alert alert-warning" *ngIf="bills.billStatus === 'OVERDUE'">
        NOTICE: This bill is overdue. Please notify client to pay as soon as possible.
      </div>
      <br />
      <div class="row">
        <div class="col-md-6 text-start">
          <h1 class="invoice-title">Invoice</h1>
        </div>
        <div class="col-md-6 text-end">
          <h3 class="bill-id">Bill ID: {{ bills.billId }}</h3>
          <br />
          <h5>Date: {{ bills.date }}</h5>
        </div>
      </div>
      <hr />
      <div class="row g-5">
        <div class="col-md-6">
          <br />
          <h5>Customer ID: {{ getOwnerUUIDByCustomerId(bills.customerId) }}</h5>
          <h5>Customer Name: {{ getCustomerDetails(bills.customerId) }}</h5>
          <h5>Address: 505 Lincoln Str. Queens, New York</h5>
          <hr />
          <br />
          <h5>
            Bill Status:
            <span [ngStyle]="{ color: bills.billStatus === 'OVERDUE' ? 'red' : 'black' }">{{
              bills.billStatus
            }}</span>
          </h5>
          <hr />
          <br />
          <h5>
            Due Date: <strong>{{ bills.dueDate }}</strong>
          </h5>
          <hr />
          <br />
          <h5>Final Amount Due: {{ bills.amount }}</h5>
          <h5 *ngIf="show">Final Amount Including Taxes: {{ bills.taxedAmount }}</h5>
          <button class="newBtn btn btn-primary" (click)="show = !show">
            {{ show ? 'Hide amount including taxes' : 'Show amount including taxes' }}
          </button>
          <hr />
          <br />
          <div>
            <a class="newBtn btn btn-primary" [routerLink]="['/bills/owner', bills.customerId]">
              All bills of customer</a
            >
            <a class="newBtn btn btn-primary" [routerLink]="['/bills/vet', bills.vetId]">
              All bills of veterinarian</a
            >
          </div>
        </div>
      </div>
      <hr />
      <br />
      <div class="row">
        <div class="col-md-6 text-start">
          <button class="btn btn-primary" (click)="printPage()">Print this page</button>
        </div>
      </div>
    </div>

    <style>
      .invoice-title,
      .bill-id {
        font-size: 24px;
        font-weight: bold;
      }
    </style>
  `,
})
export class BillDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private billApi = inject(BillApiService);

  bills: Bill | null = null;
  billId: string = '';
  show: boolean = false;

  owners: Array<{ ownerId: string; firstName: string; lastName: string }> = [
    { ownerId: '1', firstName: 'George', lastName: 'Franklin' },
    { ownerId: '2', firstName: 'Betty', lastName: 'Davis' },
    { ownerId: '3', firstName: 'Eduardo', lastName: 'Rodriguez' },
    { ownerId: '4', firstName: 'Harold', lastName: 'Davis' },
    { ownerId: '5', firstName: 'Peter', lastName: 'McTavish' },
    { ownerId: '6', firstName: 'Jean', lastName: 'Coleman' },
    { ownerId: '7', firstName: 'Jeff', lastName: 'Black' },
    { ownerId: '8', firstName: 'Maria', lastName: 'Escobito' },
    { ownerId: '9', firstName: 'David', lastName: 'Schroeder' },
    { ownerId: '10', firstName: 'Carlos', lastName: 'Esteban' },
  ];

  ownersUUID: Array<{ ownerId: string; firstName: string; lastName: string }> = [
    { ownerId: 'f470653d-05c5-4c45-b7a0-7d70f003d2ac', firstName: 'George', lastName: 'Franklin' },
    { ownerId: 'e6c7398e-8ac4-4e10-9ee0-03ef33f0361a', firstName: 'Betty', lastName: 'Davis' },
    {
      ownerId: '3f59dca2-903e-495c-90c3-7f4d01f3a2aa',
      firstName: 'Eduardo',
      lastName: 'Rodriguez',
    },
    { ownerId: 'a6e0e5b0-5f60-45f0-8ac7-becd8b330486', firstName: 'Harold', lastName: 'Davis' },
    { ownerId: 'c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2', firstName: 'Peter', lastName: 'McTavish' },
    { ownerId: 'b3d09eab-4085-4b2d-a121-78a0a2f9e501', firstName: 'Jean', lastName: 'Coleman' },
    { ownerId: '5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd', firstName: 'Jeff', lastName: 'Black' },
    { ownerId: '48f9945a-4ee0-4b0b-9b44-3da829a0f0f7', firstName: 'Maria', lastName: 'Escobito' },
    { ownerId: '9f6accd1-e943-4322-932e-199d93824317', firstName: 'David', lastName: 'Schroeder' },
    { ownerId: '7c0d42c2-0c2d-41ce-bd9c-6ca67478956f', firstName: 'Carlos', lastName: 'Esteban' },
  ];

  vetList: unknown[] = [];
  ownersInfoArray: unknown[] = [];

  ngOnInit(): void {
    this.billId = this.route.snapshot.paramMap.get('billId') || '';
    this.loadBill();
    this.loadOwners();
  }

  loadBill(): void {
    this.billApi.getBillById(this.billId).subscribe({
      next: bill => {
        this.bills = bill;
      },
      error: () => {},
    });
  }

  loadOwners(): void {}

  getOwnerUUIDByCustomerId(customerId: string): string {
    let foundOwner: { ownerId: string; firstName: string; lastName: string } | undefined;
    this.owners.forEach(owner => {
      if (owner.ownerId === customerId.toString()) {
        foundOwner = owner;
      }
    });
    this.ownersUUID.forEach(owner => {
      if (owner.ownerId === customerId.toString()) {
        foundOwner = owner;
      }
    });
    if (foundOwner) {
      const firstName = foundOwner.firstName;
      const lastName = foundOwner.lastName;

      const ownerInfo = this.ownersInfoArray.find((owner: any) => {
        return owner.firstName === firstName && owner.lastName === lastName;
      });
      if (ownerInfo) {
        return (ownerInfo as any).ownerId;
      }
    }

    return 'Unknown Owner';
  }

  getCustomerDetails(customerId: string): string {
    const customerName = this.owners.find(c => c.ownerId === customerId);
    const customerName2 = this.ownersUUID.find(c => c.ownerId === customerId);
    if (customerName) {
      return customerName.firstName + ' ' + customerName.lastName;
    } else if (customerName2) {
      return customerName2.firstName + ' ' + customerName2.lastName;
    } else {
      return 'Unknown Customer';
    }
  }

  printPage(): void {
    window.print();
  }
}
