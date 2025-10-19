import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { BillApiService } from '../../api/bill-api.service';
import { BillRequest } from '../../models/bill.model';

@Component({
  selector: 'app-bill-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="bColor text-center"><h2 class="titleBill form" id="title">New Bill</h2></div>
    <div class="p-3 formColor m-0">
      <div id="billForm" class="form-horizontal">
        <div class="row">
          <div class="col-sm-6 form-group">
            <label class="control-label" for="owner">Owner</label>
            <select class="form-control" name="owner" id="owner" [(ngModel)]="bill.customerId" required title="Please select an owner.">
              <option *ngFor="let owner of owners; trackBy: trackByOwnerId" [value]="owner.ownerId">{{owner.firstName}} {{owner.lastName}}</option>
            </select>
          </div>
          <div class="col-sm-6 form-group">
            <label class="control-label" for="vetId">Veterinarian</label>
            <select class="form-control" name="visitType" id="vetId" [(ngModel)]="bill.vetId" required title="Please select a veterinarian.">
              <option *ngFor="let vet of vetList; trackBy: trackByVetId" [value]="vet.vetId">{{vet.firstName}} {{vet.lastName}}</option>
            </select>
          </div>
        </div>
        <div class="row">
          <div class="col-sm-6 form-group">
            <label class="control-label text-center" for="date">Date</label>
            <input class="form-control" id="date" [(ngModel)]="bill.date" name="date" type="date" required title="Please select a date."/>
          </div>

          <div class="col-sm-6 form-group">
            <label class="control-label" for="visitType">Visit Type</label>
            <select class="form-control" name="visitType" id="visitType" [(ngModel)]="bill.visitType" required title="Please select a visit type.">
              <option value="general">general</option>
              <option value="operation">operation</option>
              <option value="consultation">consultation</option>
              <option value="examination">examination</option>
              <option value="injury">injury</option>
              <option value="medical">medical</option>
              <option value="chronic">chronic</option>
            </select>
          </div>
        </div>
        <div class="row">
          <div class="form-group col-sm-12">
            <label class="control-label" for="amount">Amount</label>
            <div class="bill">
              <input class="form-control col-sm-4" [(ngModel)]="bill.amount" name="amount" id="amount" required data-type="currency" placeholder="0.00" title="Please enter the bill amount."/>
            </div>
          </div>
          <div class="col-sm-6 form-group">
            <label class="control-label text-center" for="billStatus">Status</label>
            <select class="form-control" name="billStatus" id="billStatus" [(ngModel)]="bill.billStatus" required title="Please select a status">
              <option value="PAID">Paid</option>
              <option value="UNPAID">Unpaid</option>
              <option value="OVERDUE">Overdue</option>
            </select>
          </div>
          <div class="col-sm-6 form-group">
            <label class="control-label text-center" for="date">Due Date</label>
            <input class="form-control" id="dueDate" [(ngModel)]="bill.dueDate" name="dueDate" type="date" required title="Please select a due date."/>
          </div>
        </div>
        <div class="form-group p-3">
          <div class="bill marg col-sm-12">
            <button id="newBtn" class="w-100 btn btn-primary btn-lg" type="button" (click)="submitBillForm()">Submit</button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class BillFormComponent implements OnInit {
  private billApi = inject(BillApiService);
  private router = inject(Router);

  bill: BillRequest = {
    customerId: '',
    vetId: '',
    visitType: '',
    date: new Date().toISOString().split('T')[0],
    amount: 0,
    billStatus: 'UNPAID' as any,
    dueDate: new Date().toISOString().split('T')[0]
  };

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

  vetList: any[] = [
    { vetId: '1', firstName: 'James', lastName: 'Carter' },
    { vetId: '2', firstName: 'Helen', lastName: 'Leary' },
    { vetId: '3', firstName: 'Linda', lastName: 'Douglas' },
    { vetId: '4', firstName: 'Rafael', lastName: 'Ortega' },
    { vetId: '5', firstName: 'Henry', lastName: 'Stevens' },
    { vetId: '6', firstName: 'Sharon', lastName: 'Jenkins' }
  ];

  query: string = '';

  ngOnInit(): void {
  }

  trackByOwnerId(_index: number, owner: any): string {
    return owner.ownerId;
  }

  trackByVetId(_index: number, vet: any): string {
    return vet.vetId;
  }

  submitBillForm(): void {
    this.billApi.createBill(this.bill).subscribe({
      next: () => {
        this.router.navigate(['/bills']);
      },
      error: (response) => {
        const error = response.error;
        error.errors = error.errors || [];
        alert(error.error + "\r\n" + error.errors.map((e: any) => {
          return e.field + ": " + e.defaultMessage;
        }).join("\r\n"));
      }
    });
  }
}

