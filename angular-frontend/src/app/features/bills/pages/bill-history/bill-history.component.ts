import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BillApiService } from '../../api/bill-api.service';
import { Bill } from '../../models/bill.model';

@Component({
  selector: 'app-bill-history',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="bill-history-container">
      <h2 class="titleOwner">Bill History</h2>

      <div class="btn-toolbar mb-3 toolbar-gap" role="toolbar" aria-label="Bills toolbar">
        <div class="btn-group" role="group">
          <button class="btn btn-primary" [class.active]="toolbar.open==='search'" (click)="togglePanel('search')">Search</button>
        </div>
        <div class="btn-group" role="group">
          <button class="btn btn-primary" [class.active]="toolbar.open==='filter'" (click)="togglePanel('filter')">Filter</button>
        </div>
        <div class="btn-group" role="group">
          <a routerLink="/bills/new" class="btn btn-success">New Bill</a>
        </div>
        <div class="btn-group" role="group">
          <button class="btn btn-outline-danger" (click)="deleteAllBills()">Delete All Bills</button>
        </div>
      </div>

      <div class="card mb-3" *ngIf="toolbar.open==='search'">
        <div class="card-body d-flex gap-2 flex-wrap">
          <input type="text" class="form-control" style="max-width:420px" placeholder="Search bills…" [(ngModel)]="searchQuery">
          <button class="btn btn-primary">Apply</button>
          <button class="btn btn-light" (click)="searchQuery=''">Clear</button>
        </div>
      </div>

      <div class="card mb-3" *ngIf="toolbar.open==='filter'">
        <div class="card-body">
          <h5 class="mb-3">Filter Bills</h5>
          <div class="d-flex flex-wrap align-items-end gap-3">
            <div class="form-group">
              <label for="statusSel" class="form-label">Status</label>
              <select id="statusSel" class="form-select" style="min-width: 160px" [(ngModel)]="tempStatus">
                <option value="">Any</option>
                <option value="paid">Paid</option>
                <option value="unpaid">Unpaid</option>
                <option value="overdue">Overdue</option>
              </select>
            </div>

            <div class="form-group">
              <label for="yearSel" class="form-label">Year</label>
              <select id="yearSel" class="form-select" style="min-width: 120px" [(ngModel)]="tempYear">
                <option value="">Any</option>
                <option *ngFor="let y of availableYears" [value]="y">{{y}}</option>
              </select>
            </div>

            <div class="form-group">
              <label for="visitSel" class="form-label">Visit Type</label>
              <select id="visitSel" class="form-select" style="min-width: 180px" [(ngModel)]="tempVisitType">
                <option value="">Any</option>
                <option value="regular">Regular</option>
                <option value="emergency">Emergency</option>
              </select>
            </div>

            <div class="ms-auto d-flex gap-2">
              <button class="btn btn-primary" (click)="applyFilter()">Apply</button>
              <button class="btn btn-light" (click)="resetFilter()">Reset</button>
            </div>
          </div>
        </div>
      </div>

      <table class="table table-striped">
        <thead>
          <tr>
            <td>Bill Id</td>
            <td>Owner Name</td>
            <td>Owner Details</td>
            <td>Vet Name</td>
            <td>Visit Type</td>
            <td class="text-end">Amount</td>
            <td>Status</td>
            <td>Due Date</td>
            <td>Date</td>
            <td>Details</td>
            <td>Delete</td>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let bill of getRows(); trackBy: trackByBillId">
            <td>{{bill.billId || bill.id}}</td>
            <td>{{getCustomerDetails(bill.customerId)}}</td>
            <td>
              <a class="btn btn-sm btn-outline-secondary" [routerLink]="['/owners', getOwnerUUIDByCustomerId(bill.customerId)]">
                View Owner
              </a>
            </td>
            <td>{{getVetDetails(bill.vetId)}}</td>
            <td>{{bill.visitType}}</td>
            <td class="text-end">{{bill.amount | currency}}</td>
            <td>
              <span class="badge" [ngClass]="{'bg-success': (activeStatus=='paid') || ((bill.status||'').toLowerCase()=='paid'), 'bg-secondary': (activeStatus=='unpaid') || ((bill.status||'').toLowerCase()=='unpaid'), 'bg-danger': (activeStatus=='overdue') || ((bill.status||'').toLowerCase()=='overdue')}">
                {{(activeStatus || (bill.status || '')).toUpperCase() || '—'}}
              </span>
            </td>
            <td>{{bill.dueDate || bill.due}}</td>
            <td><code>{{bill.date}}</code></td>
            <td>
              <a class="btn btn-sm btn-outline-primary" [routerLink]="['/bills', (bill.billId || bill.id)]">Details</a>
            </td>
            <td>
              <button class="btn btn-sm btn-outline-danger" (click)="deleteBill(bill.billId || bill.id)">Delete</button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="d-flex align-items-center gap-3">
        <button class="btn btn-light" (click)="goPreviousPage()">« Prev</button>
        <span>Page {{page}} of {{totalPages}}</span>
        <button class="btn btn-light" (click)="goNextPage()">Next »</button>
      </div>
    </div>
  `,
  styles: [`
    .table > thead > tr > th {
      background-color: #3c3834;
      color: #f1f1f1;
    }
  `]
})
export class BillHistoryComponent implements OnInit {
  private billApi = inject(BillApiService);

  toolbar = { open: null as string | null };
  searchQuery: string = '';
  
  tempStatus: string = '';
  tempVisitType: string = '';
  tempYear: string = '';
  activeStatus: string = '';
  activeVisitType: string = '';
  filterYear: string = '';
  
  availableYears: number[] = [];
  
  billHistory: Bill[] = [];
  paidBills: Bill[] = [];
  unpaidBills: Bill[] = [];
  overdueBills: Bill[] = [];
  
  currentPage: number = 0;
  pageSize: number = 10;
  currentPageOnSite: number = 1;
  page: number = 1;
  totalPages: number = 1;
  totalItems: number = 0;

  owners: any[] = [
    { ownerId: '1', firstName: 'George', lastName: 'Franklin' },
    { ownerId: '2', firstName: 'Betty', lastName: 'Davis' },
    { ownerId: '3', firstName: 'Eduardo', lastName: 'Rodriguez' },
    { ownerId: '4', firstName: 'Harold', lastName: 'Davis' },
    { ownerId: '5', firstName: 'Peter', lastName: 'McTavish' },
    { ownerId: '6', firstName: 'Jean', lastName: 'Coleman' },
    { ownerId: '7', firstName: 'Jeff', lastName: 'Black' },
    { ownerId: '8', firstName: 'Maria', lastName: 'Escobito' },
    { ownerId: '9', firstName: 'David', lastName: 'Schroeder' },
    { ownerId: '10', firstName: 'Carlos', lastName: 'Esteban' }
  ];

  ownersUUID: any[] = [
    { ownerId: 'f470653d-05c5-4c45-b7a0-7d70f003d2ac', firstName: 'George', lastName: 'Franklin' },
    { ownerId: 'e6c7398e-8ac4-4e10-9ee0-03ef33f0361a', firstName: 'Betty', lastName: 'Davis' },
    { ownerId: '3f59dca2-903e-495c-90c3-7f4d01f3a2aa', firstName: 'Eduardo', lastName: 'Rodriguez' },
    { ownerId: 'a6e0e5b0-5f60-45f0-8ac7-becd8b330486', firstName: 'Harold', lastName: 'Davis' },
    { ownerId: 'c6a0fb9d-fc6f-4c21-95fc-4f5e7311d0e2', firstName: 'Peter', lastName: 'McTavish' },
    { ownerId: 'b3d09eab-4085-4b2d-a121-78a0a2f9e501', firstName: 'Jean', lastName: 'Coleman' },
    { ownerId: '5fe81e29-1f1d-4f9d-b249-8d3e0cc0b7dd', firstName: 'Jeff', lastName: 'Black' },
    { ownerId: '48f9945a-4ee0-4b0b-9b44-3da829a0f0f7', firstName: 'Maria', lastName: 'Escobito' },
    { ownerId: '9f6accd1-e943-4322-932e-199d93824317', firstName: 'David', lastName: 'Schroeder' },
    { ownerId: '7c0d42c2-0c2d-41ce-bd9c-6ca67478956f', firstName: 'Carlos', lastName: 'Esteban' }
  ];

  vetList: any[] = [];
  ownersInfoArray: any[] = [];
  customerNameMap: { [key: string]: string } = {};
  customerNameMap2: { [key: string]: string } = {};

  ngOnInit(): void {
    this.buildYears();
    this.loadDefaultData();
    this.loadVetsAndOwners();
    this.setupCustomerMaps();
  }

  buildYears(): void {
    this.availableYears = [];
    const now = new Date().getFullYear();
    for (let y = now; y >= now - 14; y--) {
      this.availableYears.push(y);
    }
  }

  togglePanel(which: string): void {
    this.toolbar.open = (this.toolbar.open === which) ? null : which;
    if (this.toolbar.open === 'filter') {
      this.tempStatus = this.activeStatus;
      this.tempVisitType = this.activeVisitType;
      this.tempYear = this.filterYear;
    }
  }

  applyFilter(): void {
    this.activeStatus = (this.tempStatus || '').toLowerCase();
    this.activeVisitType = (this.tempVisitType || '').toLowerCase();
    this.filterYear = this.tempYear || '';
    this.toolbar.open = null;
  }

  resetFilter(): void {
    this.tempStatus = this.tempVisitType = '';
    this.tempYear = '';
    this.activeStatus = this.activeVisitType = '';
    this.filterYear = '';
  }

  getRows(): Bill[] {
    switch (this.activeStatus) {
      case 'paid': return this.paidBills;
      case 'unpaid': return this.unpaidBills;
      case 'overdue': return this.overdueBills;
      default: return this.billHistory;
    }
  }

  visitTypePredicate(bill: Bill): boolean {
    if (!this.activeVisitType) return true;
    const v = (bill && bill.visitType) ? String(bill.visitType).toLowerCase() : '';
    return v === this.activeVisitType;
  }

  loadDefaultData(): void {
    this.loadTotalItemForDefaultData().then((totalItems) => {
      this.totalItems = totalItems;
      this.totalPages = Math.ceil(this.totalItems / parseInt(this.pageSize.toString()));
      this.billApi.getAllBills(this.currentPage, this.pageSize).subscribe({
        next: (bills) => {
          this.billHistory = bills;
        },
        error: () => {}
      });
      this.updateCurrentPageOnSite();
    });
  }

  loadTotalItemForDefaultData(): Promise<number> {
    return new Promise((resolve) => {
      this.billApi.getBillsCount().subscribe({
        next: (count) => resolve(count),
        error: () => resolve(0)
      });
    });
  }

  goNextPage(): void {
    if (parseInt(this.currentPage.toString()) + 1 < this.totalPages) {
      this.currentPage = (parseInt(this.currentPage.toString()) + 1).toString() as any;
      this.updateCurrentPageOnSite();
      this.loadDefaultData();
    }
  }

  goPreviousPage(): void {
    if (this.currentPage - 1 >= 0) {
      this.currentPage = (parseInt(this.currentPage.toString()) - 1).toString() as any;
      this.updateCurrentPageOnSite();
      this.loadDefaultData();
    }
  }

  updateCurrentPageOnSite(): void {
    this.currentPageOnSite = parseInt(this.currentPage.toString()) + 1;
    this.page = this.currentPageOnSite;
  }

  loadVetsAndOwners(): void {
  }

  setupCustomerMaps(): void {
    this.owners.forEach((c) => {
      this.customerNameMap[c.ownerId] = c.firstName + ' ' + c.lastName;
    });
    this.ownersUUID.forEach((c) => {
      this.customerNameMap2[c.ownerId] = c.firstName + ' ' + c.lastName;
    });
  }

  getOwnerUUIDByCustomerId(customerId: string): string {
    let foundOwner: any;
    this.owners.forEach((o) => {
      if (o.ownerId === String(customerId)) foundOwner = o;
    });
    this.ownersUUID.forEach((o) => {
      if (o.ownerId === String(customerId)) foundOwner = o;
    });
    if (foundOwner && this.ownersInfoArray) {
      const match = this.ownersInfoArray.find((o) => {
        return o.firstName === foundOwner.firstName && o.lastName === foundOwner.lastName;
      });
      if (match) return match.ownerId;
    }
    return customerId || 'Unknown Owner';
  }

  getVetDetails(vetId: string): string {
    if (!this.vetList) return 'Unknown Vet';
    const viaBillId = this.vetList.find((v) => v.vetBillId === vetId);
    const viaVetId = this.vetList.find((v) => v.vetId === vetId);
    const v = viaBillId || viaVetId;
    return v ? (v.firstName + ' ' + v.lastName) : 'Unknown Vet';
  }

  getCustomerDetails(customerId: string): string {
    return this.customerNameMap[customerId] || this.customerNameMap2[customerId] || 'Unknown Customer';
  }

  deleteAllBills(): void {
    const ok = confirm('Are you sure you want to delete all the bills in the bill history');
    if (!ok) return;
    
    this.billApi.deleteAllBillHistory().subscribe({
      next: () => {
        alert("bill history was deleted successfully");
        this.billApi.getAllBills().subscribe({
          next: (bills) => {
            this.billHistory = bills;
          },
          error: () => {}
        });
      },
      error: () => {}
    });
  }

  deleteBill(billId: string): void {
    const ok = confirm('You are about to delete billId ' + billId + '. Is it what you want to do ? ');
    if (!ok) return;
    
    this.billApi.deleteBill(billId).subscribe({
      next: () => {
        alert(billId + " bill was deleted successfully");
        this.billApi.getAllBills().subscribe({
          next: (bills) => {
            this.billHistory = bills;
          },
          error: () => {}
        });
      },
      error: () => {}
    });
  }

  trackByBillId(_index: number, bill: Bill): string {
    return bill.billId || bill.id || '';
  }
}

