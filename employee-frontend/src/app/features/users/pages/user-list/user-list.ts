import { Component, computed, inject, OnInit, signal, TemplateRef, viewChild } from '@angular/core';
import { Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

import { isApiError } from '@core/models/api-error';
import {
  isManagedRole,
  roleLabel,
  User,
  USER_ROLE_OPTIONS,
  userRoleName,
} from '@features/users/models/user.model';
import { UserService } from '@features/users/services/user-service';
import { ModalForm } from '@shared/components/modal-form/modal-form';
import {
  ModalFormData,
  ModalFormField,
  ModalFormResult,
} from '@shared/components/modal-form/modal-form-models';
import { Table, TableColumn } from '@shared/components/table/table';

interface UserFormValue {
  username: string;
  email: string;
  role: string;
  password?: string;
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  resume?: string;
  address?: string;
  city?: string;
  province?: string;
  telephone?: string;
}

interface RoleChoice {
  role: string;
}

// Same rule as the backend: 8+ characters with a digit, a lowercase letter,
// an uppercase letter and a special character, and no spaces.
const PASSWORD_PATTERN =
  /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=*!()_.<>,{}])(?=\S+$).{8,}$/;

// The owner service only accepts a phone number made of 10 digits.
const OWNER_PHONE_PATTERN = /^[0-9]{10}$/;

@Component({
  imports: [Table],
  selector: 'app-user-list',
  styleUrl: './user-list.css',
  templateUrl: './user-list.html',
})
export class UserList implements OnInit {
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);

  protected readonly users = signal<User[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly saving = signal(false);
  protected readonly saveError = signal('');
  protected readonly successMessage = signal('');

  protected readonly tableLabel = 'User accounts';
  protected readonly userRowId = (user: User): string => user.userId;
  protected readonly actionsCell = viewChild<TemplateRef<{ $implicit: User }>>('actionsCell');

  protected readonly columns = computed<TableColumn<User>[]>(() => [
    { id: 'name', header: 'Name', value: (user) => user.username },
    { id: 'email', header: 'Email', value: (user) => user.email },
    { id: 'role', header: 'Role', value: (user) => roleLabel(userRoleName(user)) },
    { id: 'actions', header: 'Actions', template: this.actionsCell() },
  ]);

  ngOnInit(): void {
    this.loadUsers();
  }

  protected loadUsers(): void {
    this.loading.set(true);
    this.error.set(false);

    this.userService.getUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      },
    });
  }

  // Administrators are shown in the list but cannot be edited here
  protected canEdit(user: User): boolean {
    return isManagedRole(userRoleName(user));
  }

  // Step 1 of adding a user: choose the role. The next step asks for the fields of that role.
  protected openAddDialog(): void {
    this.clearMessages();

    const dialogRef = this.dialog.open<
      ModalForm<RoleChoice>,
      ModalFormData<RoleChoice>,
      ModalFormResult<RoleChoice>
    >(ModalForm, {
      data: {
        title: 'Add user',
        submitLabel: 'Next',
        cancelLabel: 'Cancel',
        fields: [
          { key: 'role', label: 'Role', type: 'enum', required: true, options: USER_ROLE_OPTIONS },
        ],
        initialValue: { role: USER_ROLE_OPTIONS[0]?.value ?? '' },
      },
      ariaLabel: 'Add user',
      width: '560px',
      maxWidth: '95vw',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.openCreateDialog(result.value.role);
      }
    });
  }

  protected openEditDialog(user: User): void {
    if (!this.canEdit(user)) {
      return;
    }

    this.clearMessages();

    const dialogRef = this.openUserForm('Edit user', 'Save changes', this.editFields(), {
      username: user.username,
      email: user.email,
      role: userRoleName(user),
    });

    // The dialog closes with undefined when the user cancels.
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.updateUser(user, result.value);
      }
    });
  }

  // Step 2 of adding a user: the fields depend on the role
  private openCreateDialog(role: string): void {
    const dialogRef = this.openUserForm(
      `Add ${roleLabel(role).toLowerCase()}`,
      'Add user',
      this.createFields(role),
      this.emptyValue(role),
    );

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.createUser({ ...result.value, role });
      }
    });
  }

  private openUserForm(
    title: string,
    submitLabel: string,
    fields: readonly ModalFormField[],
    initialValue: UserFormValue,
  ): MatDialogRef<ModalForm<UserFormValue>, ModalFormResult<UserFormValue>> {
    return this.dialog.open<
      ModalForm<UserFormValue>,
      ModalFormData<UserFormValue>,
      ModalFormResult<UserFormValue>
    >(ModalForm, {
      data: { title, submitLabel, cancelLabel: 'Cancel', fields, initialValue },
      ariaLabel: title,
      width: '560px',
      maxWidth: '95vw',
    });
  }

  private emptyValue(role: string): UserFormValue {
    return {
      username: '',
      email: '',
      role,
      password: '',
      firstName: '',
      lastName: '',
      phoneNumber: '',
      resume: '',
      address: '',
      city: '',
      province: '',
      telephone: '',
    };
  }

  private emailField(): ModalFormField {
    return {
      key: 'email',
      label: 'Email',
      type: 'string',
      required: true,
      validators: [Validators.email, Validators.minLength(6)],
      errorMessages: {
        email: 'Enter a valid email address.',
        minlength: 'The email must have at least 6 characters.',
      },
    };
  }

  private editFields(): readonly ModalFormField[] {
    return [
      { key: 'username', label: 'Name', type: 'string', required: true },
      this.emailField(),
      { key: 'role', label: 'Role', type: 'enum', required: true, options: USER_ROLE_OPTIONS },
    ];
  }

  // The account fields come first, then the profile fields of the role
  private createFields(role: string): readonly ModalFormField[] {
    const account: ModalFormField[] = [
      { key: 'username', label: 'Name', type: 'string', required: true },
      this.emailField(),
      {
        key: 'password',
        label: 'Password',
        type: 'password',
        required: true,
        validators: [Validators.pattern(PASSWORD_PATTERN)],
        errorMessages: {
          pattern:
            'Use at least 8 characters with a number, an uppercase and a lowercase letter, and a special character (@#$%^&+=*!()_.<>,{}). No spaces.',
        },
      },
    ];

    if (role === 'VET') {
      return [...account, ...this.vetFields()];
    }

    if (role === 'CUST') {
      return [...account, ...this.ownerFields()];
    }

    return account;
  }

  private vetFields(): ModalFormField[] {
    return [
      {
        key: 'firstName',
        label: 'First name',
        type: 'string',
        required: true,
        validators: [Validators.minLength(2), Validators.maxLength(30)],
        errorMessages: {
          minlength: 'Use 2 to 30 characters.',
          maxlength: 'Use 2 to 30 characters.',
        },
      },
      {
        key: 'lastName',
        label: 'Last name',
        type: 'string',
        required: true,
        validators: [Validators.minLength(2), Validators.maxLength(30)],
        errorMessages: {
          minlength: 'Use 2 to 30 characters.',
          maxlength: 'Use 2 to 30 characters.',
        },
      },
      {
        key: 'phoneNumber',
        label: 'Phone number',
        type: 'string',
        required: true,
        placeholder: '(514) 555-0123 #1234',
        // The vet service only accepts a phone number of exactly 20 characters.
        validators: [Validators.minLength(20), Validators.maxLength(20)],
        errorMessages: {
          minlength: 'The phone number must have exactly 20 characters, like (514) 555-0123 #1234.',
          maxlength: 'The phone number must have exactly 20 characters, like (514) 555-0123 #1234.',
        },
      },
      {
        key: 'resume',
        label: 'Resume',
        type: 'string',
        required: true,
        validators: [Validators.minLength(10)],
        errorMessages: { minlength: 'The resume must have at least 10 characters.' },
      },
    ];
  }

  private ownerFields(): ModalFormField[] {
    return [
      { key: 'firstName', label: 'First name', type: 'string', required: true },
      { key: 'lastName', label: 'Last name', type: 'string', required: true },
      { key: 'address', label: 'Address', type: 'string', required: true },
      { key: 'city', label: 'City', type: 'string', required: true },
      { key: 'province', label: 'Province', type: 'string', required: true },
      {
        key: 'telephone',
        label: 'Phone number',
        type: 'string',
        required: true,
        placeholder: '5145550123',
        validators: [Validators.pattern(OWNER_PHONE_PATTERN)],
        errorMessages: { pattern: 'Enter 10 digits, without spaces or dashes.' },
      },
    ];
  }

  private clearMessages(): void {
    this.saveError.set('');
    this.successMessage.set('');
  }

  private updateUser(original: User, value: UserFormValue): void {
    this.save(
      this.userService.updateUser(original, {
        username: value.username.trim(),
        email: value.email.trim(),
        role: value.role,
      }),
      'User updated successfully.',
    );
  }

  private createUser(value: UserFormValue): void {
    this.save(
      this.userService.createUser({
        username: value.username.trim(),
        email: value.email.trim(),
        password: value.password ?? '',
        role: value.role,
        firstName: value.firstName?.trim(),
        lastName: value.lastName?.trim(),
        phoneNumber: value.phoneNumber?.trim(),
        resume: value.resume?.trim(),
        address: value.address?.trim(),
        city: value.city?.trim(),
        province: value.province?.trim(),
        telephone: value.telephone?.trim(),
      }),
      'User created successfully.',
    );
  }

  // Shows the success message only when the backend accepted the change
  private save(request$: ReturnType<UserService['updateUser']>, successMessage: string): void {
    if (this.saving()) {
      return;
    }

    this.saving.set(true);

    request$.subscribe({
      next: () => {
        this.saving.set(false);
        this.successMessage.set(successMessage);
        this.loadUsers();
      },
      error: (error: unknown) => {
        this.saving.set(false);
        this.saveError.set(
          isApiError(error) ? error.message : 'The user could not be saved. Please try again.',
        );
      },
    });
  }
}
