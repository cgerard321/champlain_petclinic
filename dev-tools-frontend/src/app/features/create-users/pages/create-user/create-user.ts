import { Component, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { email, form, FormField, required } from '@angular/forms/signals';
import { ASSIGNABLE_ROLES } from '@features/create-users/models/roles';
import { CreateUsers } from '@features/create-users/services/create-users';


interface CreateUserFormModel {
  email: string;
  password: string;
  displayName: string;
  roleIds: string[];
}

@Component({
  selector: 'app-create-user',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    FormField,
  ],
  templateUrl: './create-user.html',
  styleUrl: './create-user.css',
})
export class CreateUser {
  private readonly createUserService = inject(CreateUsers);

  protected readonly roles = ASSIGNABLE_ROLES;

  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly successMessage = signal<string | null>(null);

  private readonly model = signal<CreateUserFormModel>({
    email: '',
    password: '',
    displayName: '',
    roleIds: [],
  });

  protected readonly createUserForm = form(this.model, (schemaPath) => {
    required(schemaPath.email, { message: 'Email is required' });
    email(schemaPath.email, { message: 'Enter a valid email address' });
    required(schemaPath.password, { message: 'Password is required' });
    required(schemaPath.displayName, { message: 'Display name is required' });
  });

  protected toggleRole(roleId: string, checked: boolean): void {
    this.model.update((m) => ({
      ...m,
      roleIds: checked ? [...m.roleIds, roleId] : m.roleIds.filter((id) => id !== roleId),
    }));
  }

  protected isRoleChecked(roleId: string): boolean {
    return this.model().roleIds.includes(roleId);
  }

  protected submit(event: Event): void {
    event.preventDefault();

    if (this.createUserForm().invalid() || this.model().roleIds.length === 0) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const { email, password, displayName, roleIds } = this.model();

    this.createUserService
      .createUser({ email, password, display_name: displayName, roles: roleIds })
      .subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.successMessage.set(`User ${email} created.`);
          this.model.set({ email: '', password: '', displayName: '', roleIds: [] });
        },
        error: () => {
          this.isSubmitting.set(false);
          this.errorMessage.set('Failed to create user.');
        },
      });
  }
}
