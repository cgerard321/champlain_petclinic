import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthApiService } from '../../api/auth-api.service';
import { UserDetails } from '../../models/user.model';

@Component({
  selector: 'app-admin-panel',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <h2 class="admin-panel">Users</h2>

    <div
      class="dropdown"
      style="float: right; margin-bottom: 1%; margin-right: 10px; position: relative;"
    >
      <button
        type="button"
        class="btn btn-success dropdown-toggle"
        data-toggle="dropdown"
        aria-haspopup="true"
        aria-expanded="false"
        (click)="toggleDropdown()"
      >
        <i class="glyphicon glyphicon-user"></i> Create User <span class="caret"></span>
      </button>
      <div
        class="dropdown-menu p-2"
        [class.show]="showDropdown"
        style="min-width: 240px; right: 0; left: auto; transform: translateX(0%);"
      >
        <a
          routerLink="/vet-form"
          class="btn btn-block text-left mb-2"
          style="background-color: #d4edda; color: #155724; border: 1px solid #c3e6cb;"
        >
          Veterinarian
        </a>
        <a
          routerLink="/manager-form"
          class="btn btn-block text-left mb-2"
          style="background-color: #e2e3e5; color: #383d41; border: 1px solid #d6d8db;"
        >
          Inventory Manager
        </a>
        <a
          routerLink="/signup"
          class="btn btn-block text-left mb-2"
          style="background-color: #d1ecf1; color: #0c5460; border: 1px solid #bee5eb;"
        >
          Owner
        </a>
        <div
          class="btn btn-block text-left mb-2"
          style="background-color: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; opacity: 0.7; cursor: not-allowed;"
          title="Coming soon"
        >
          Receptionist
        </div>
      </div>
    </div>

    <form onsubmit="javascript:void(0)" style="max-width: 20em; margin-top: 2em;">
      <div class="form-group">
        <input
          type="text"
          class="form-control"
          placeholder="Search Filter"
          [(ngModel)]="query"
          name="query"
          [ngModelOptions]="{ standalone: true }"
        />
        <br />
      </div>
    </form>

    <table id="users" class="table table-striped">
      <thead>
        <tr>
          <th>Username</th>
          <th>Email</th>
          <th>Role</th>
          <th>Options</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let user of filteredUsers" id="userId">
          <td>
            <a style="text-decoration: none;" [routerLink]="['/user-details', user.userId]">
              <span>{{ user.username }}</span>
            </a>
          </td>
          <td>{{ user.email }}</td>
          <td>
            <span *ngFor="let role of user.roles; let last = last">
              {{ role.name }}<span *ngIf="!last">, </span>
            </span>
          </td>
          <td>
            <input
              class="add-vet-button btn btn-success"
              type="button"
              value="Delete"
              (click)="removeUser(user.userId)"
              style="margin-right: 8px"
            />
            <button class="btn btn-warning" [routerLink]="['/role-update', user.userId]">
              Update Role
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  `,
  styles: [
    `
      .admin-panel {
        margin-bottom: 20px;
        font-weight: bold;
      }

      .dropdown-menu.show {
        display: block;
      }

      .mb-2 {
        margin-bottom: 8px;
      }

      .table {
        width: 100%;
        margin-bottom: 1rem;
        color: #212529;
      }

      .table-striped tbody tr:nth-of-type(odd) {
        background-color: rgba(0, 0, 0, 0.05);
      }

      .table th,
      .table td {
        padding: 0.75rem;
        vertical-align: top;
        border-top: 1px solid #dee2e6;
      }

      .table thead th {
        vertical-align: bottom;
        border-bottom: 2px solid #dee2e6;
      }

      .btn {
        display: inline-block;
        font-weight: 400;
        text-align: center;
        white-space: nowrap;
        vertical-align: middle;
        user-select: none;
        border: 1px solid transparent;
        padding: 0.375rem 0.75rem;
        font-size: 1rem;
        line-height: 1.5;
        border-radius: 0.25rem;
        transition:
          color 0.15s ease-in-out,
          background-color 0.15s ease-in-out,
          border-color 0.15s ease-in-out,
          box-shadow 0.15s ease-in-out;
      }

      .btn-success {
        color: #fff;
        background-color: #28a745;
        border-color: #28a745;
      }

      .btn-warning {
        color: #212529;
        background-color: #ffc107;
        border-color: #ffc107;
      }

      .dropdown-toggle::after {
        display: inline-block;
        margin-left: 0.255em;
        vertical-align: 0.255em;
        content: '';
        border-top: 0.3em solid;
        border-right: 0.3em solid transparent;
        border-bottom: 0;
        border-left: 0.3em solid transparent;
      }

      .form-control {
        display: block;
        width: 100%;
        padding: 0.375rem 0.75rem;
        font-size: 1rem;
        line-height: 1.5;
        color: #495057;
        background-color: #fff;
        background-clip: padding-box;
        border: 1px solid #ced4da;
        border-radius: 0.25rem;
        transition:
          border-color 0.15s ease-in-out,
          box-shadow 0.15s ease-in-out;
      }
    `,
  ],
})
export class AdminPanelComponent implements OnInit {
  private authApi = inject(AuthApiService);

  users: UserDetails[] = [];
  filteredUsers: UserDetails[] = [];
  query: string = '';
  showDropdown = false;

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.authApi.getAllUsers().subscribe({
      next: users => {
        this.users = users;
        this.filteredUsers = users;
      },
      error: () => {},
    });
  }

  ngDoCheck(): void {
    if (this.query) {
      this.filteredUsers = this.users.filter(user =>
        user.username.toLowerCase().includes(this.query.toLowerCase())
      );
    } else {
      this.filteredUsers = this.users;
    }
  }

  removeUser(userId: string): void {
    if (!confirm('Are you sure you want to delete this user?')) return;

    this.authApi.deleteUser(userId).subscribe({
      next: () => {
        alert('User deleted successfully!');
        this.loadUsers();
      },
      error: error => {
        const errorMsg = error.error?.message || error.error?.error || 'Failed to delete user.';
        alert('Failed to delete user: ' + errorMsg);
      },
    });
  }

  toggleDropdown(): void {
    this.showDropdown = !this.showDropdown;
  }
}
