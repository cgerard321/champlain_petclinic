import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { BillApiService } from '../../api/bill-api.service';
import { Bill, BillRequest } from '../../models/bill.model';

@Component({
  selector: 'app-bill-update-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container">
      <h2 class="titleBill form">Update Bill</h2>
      <div id="billUpdateForm" class="form-horizontal" *ngIf="bill">
        <div class="form-group">
          <label class="col-sm-2 control-label">Customer ID</label>
          <div class="col-sm-6">
            <input
              class="form-control"
              [(ngModel)]="bill.customerId"
              name="customerId"
              [disabled]="checked"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label class="col-sm-2 control-label">Vet ID</label>
          <div class="col-sm-6">
            <input
              class="form-control"
              [(ngModel)]="bill.vetId"
              name="vetId"
              [disabled]="checked"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label class="col-sm-2 control-label">Visit Type</label>
          <div class="col-sm-6">
            <input
              class="form-control"
              [(ngModel)]="bill.visitType"
              name="visitType"
              [disabled]="checked"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label class="control-label text-center" for="billStatus">Status</label>
          <div class="col-sm-6">
            <select
              class="form-control"
              name="billStatus"
              id="billStatus"
              [(ngModel)]="bill.billStatus"
              required
              title="Please select a status"
            >
              <option value="PAID">Paid</option>
              <option value="UNPAID">Unpaid</option>
              <option value="OVERDUE">Overdue</option>
            </select>
          </div>
        </div>

        <div class="form-group">
          <label class="col-sm-2 control-label">Due Date</label>
          <div class="col-sm-6">
            <input
              class="form-control"
              [(ngModel)]="bill.dueDate"
              name="dueDate"
              type="date"
              [disabled]="checked"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label class="col-sm-2 control-label">Amount</label>
          <div class="col-sm-6">
            <input
              class="form-control"
              [(ngModel)]="bill.amount"
              name="amount"
              [disabled]="checked"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <label class="col-sm-2 control-label">Date</label>
          <div class="col-sm-6">
            <input
              class="form-control"
              [(ngModel)]="bill.date"
              name="date"
              type="date"
              [disabled]="checked"
              required
            />
          </div>
        </div>

        <div class="form-group">
          <div class="col-sm-6">
            <button class="btn btn-default" type="button" (click)="submitBillForm()">
              Update Bill
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class BillUpdateFormComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private billApi = inject(BillApiService);

  bill: Bill | null = null;
  billId: string = '';
  method: string = '';
  checked: boolean = false;

  ngOnInit(): void {
    this.billId = this.route.snapshot.paramMap.get('billId') || '';
    this.method = this.route.snapshot.paramMap.get('method') || '';

    if (!this.billId) {
      this.bill = {} as Bill;
    } else {
      this.loadBill();
    }
  }

  loadBill(): void {
    this.billApi.getBillById(this.billId).subscribe({
      next: bill => {
        this.bill = bill;
      },
      error: () => {},
    });
  }

  submitBillForm(): void {
    if (!this.bill) return;

    const id = this.bill.billId;

    if (id) {
      if (this.method === 'edit') {
        const billRequest: BillRequest = {
          customerId: this.bill.customerId,
          vetId: this.bill.vetId,
          visitType: this.bill.visitType,
          date: this.bill.date,
          amount: this.bill.amount,
          billStatus: this.bill.billStatus,
          dueDate: this.bill.dueDate,
          description: this.bill.description,
        };
        this.billApi.updateBill(id, billRequest).subscribe({
          next: () => {
            this.router.navigate(['/bills']);
          },
          error: (response: {
            error?: { errors?: Array<{ field: string; defaultMessage: string }>; error?: string };
          }) => {
            const error = response.error;
            if (error) {
              error.errors = error.errors || [];
              alert(
                error.error +
                  '\r\n' +
                  error.errors
                    .map((e: { field: string; defaultMessage: string }) => {
                      return e.field + ': ' + e.defaultMessage;
                    })
                    .join('\r\n')
              );
            }
          },
        });
      } else {
        this.billApi.deleteBill(id).subscribe({
          next: () => {
            this.router.navigate(['/bills']);
          },
          error: (response: {
            error?: { errors?: Array<{ field: string; defaultMessage: string }>; error?: string };
          }) => {
            const error = response.error;
            if (error) {
              error.errors = error.errors || [];
              alert(
                error.error +
                  '\r\n' +
                  error.errors
                    .map((e: { field: string; defaultMessage: string }) => {
                      return e.field + ': ' + e.defaultMessage;
                    })
                    .join('\r\n')
              );
            }
          },
        });
      }
    } else {
      const billRequest: BillRequest = {
        customerId: this.bill.customerId,
        vetId: this.bill.vetId,
        visitType: this.bill.visitType,
        date: this.bill.date,
        amount: this.bill.amount,
        billStatus: this.bill.billStatus,
        dueDate: this.bill.dueDate,
        description: this.bill.description,
      };
      this.billApi.createBill(billRequest).subscribe({
        next: () => {
          this.router.navigate(['/bills']);
        },
        error: (response: {
          error?: { errors?: Array<{ field: string; defaultMessage: string }>; error?: string };
        }) => {
          const error = response.error;
          if (error) {
            error.errors = error.errors || [];
            alert(
              error.error +
                '\r\n' +
                error.errors
                  .map((e: { field: string; defaultMessage: string }) => {
                    return e.field + ': ' + e.defaultMessage;
                  })
                  .join('\r\n')
            );
          }
        },
      });
    }
  }
}
