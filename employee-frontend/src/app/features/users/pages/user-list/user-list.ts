import {
  Component,
  computed,
  inject,
  LOCALE_ID,
  OnInit,
  signal,
  TemplateRef,
  viewChild,
} from '@angular/core';
import { Validators } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';

import { isApiError } from '@core/models/api-error';
import {
  isManagedRole,
  roleLabel,
  User,
  userRoleName,
  userRoleOptions,
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

// The confirmation dialog has no fields
type ConfirmValue = Record<string, never>;

// Same rule as the backend: 8+ characters with a digit, a lowercase letter,
// an uppercase letter and a special character, and no spaces.
const PASSWORD_PATTERN =
  /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=*!()_.<>,{}])(?=\S+$).{8,}$/;

// The customer service only accepts a phone number made of 10 digits.
const CUSTOMER_PHONE_PATTERN = /^[0-9]{10}$/;

@Component({
  imports: [Table],
  selector: 'app-user-list',
  styleUrl: './user-list.css',
  templateUrl: './user-list.html',
})
export class UserList implements OnInit {
  private readonly userService = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly locale = inject(LOCALE_ID);

  // The backend sends its error messages in English only
  private readonly isFrench = this.locale.startsWith('fr');

  protected readonly users = signal<User[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal(false);
  protected readonly saving = signal(false);
  protected readonly saveError = signal('');
  protected readonly successMessage = signal('');

  protected readonly tableLabel = $localize`:@@users.table.label:Comptes utilisateurs`;
  protected readonly userRowId = (user: User): string => user.userId;
  protected readonly actionsCell = viewChild<TemplateRef<{ $implicit: User }>>('actionsCell');

  protected readonly columns = computed<TableColumn<User>[]>(() => [
    {
      id: 'name',
      header: $localize`:@@users.fields.name:Nom`,
      value: (user) => user.username,
    },
    {
      id: 'email',
      header: $localize`:@@users.fields.email:Courriel`,
      value: (user) => user.email,
    },
    {
      id: 'role',
      header: $localize`:@@users.fields.role:Rôle`,
      value: (user) => roleLabel(userRoleName(user)),
    },
    {
      id: 'actions',
      header: $localize`:@@users.fields.actions:Actions`,
      template: this.actionsCell(),
    },
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

  // Administrators are shown in the list but cannot be edited or deleted here
  protected canEdit(user: User): boolean {
    return isManagedRole(userRoleName(user));
  }

  protected editLabel(user: User): string {
    return $localize`:@@users.actions.editAria:Modifier ${user.username}:name:`;
  }

  protected deleteLabel(user: User): string {
    return $localize`:@@users.actions.deleteAria:Supprimer ${user.username}:name:`;
  }

  // Step 1 of adding a user: choose the role. The next step asks for the fields of that role.
  protected openAddDialog(): void {
    this.clearMessages();

    const title = $localize`:@@users.actions.add:Ajouter un utilisateur`;
    const options = userRoleOptions();

    const dialogRef = this.dialog.open<
      ModalForm<RoleChoice>,
      ModalFormData<RoleChoice>,
      ModalFormResult<RoleChoice>
    >(ModalForm, {
      data: {
        title,
        submitLabel: $localize`:@@users.actions.next:Suivant`,
        cancelLabel: $localize`:@@users.actions.cancel:Annuler`,
        fields: [
          {
            key: 'role',
            label: $localize`:@@users.fields.role:Rôle`,
            type: 'enum',
            required: true,
            options,
          },
        ],
        initialValue: { role: options[0]?.value ?? '' },
      },
      ariaLabel: title,
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

    const dialogRef = this.openUserForm(
      $localize`:@@users.actions.editTitle:Modifier l'utilisateur`,
      $localize`:@@users.actions.saveChanges:Enregistrer les modifications`,
      this.editFields(),
      { username: user.username, email: user.email, role: userRoleName(user) },
    );

    // The dialog closes with undefined when the user cancels.
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.updateUser(user, result.value);
      }
    });
  }

  protected openDeleteDialog(user: User): void {
    if (!this.canEdit(user)) {
      return;
    }

    this.clearMessages();

    const title = $localize`:@@users.actions.deleteTitle:Supprimer l'utilisateur`;

    const dialogRef = this.dialog.open<
      ModalForm<ConfirmValue>,
      ModalFormData<ConfirmValue>,
      ModalFormResult<ConfirmValue>
    >(ModalForm, {
      data: {
        title,
        message: $localize`:@@users.actions.deleteConfirm:Voulez-vous vraiment supprimer ${user.username}:name: ? Cette action est irréversible.`,
        fields: [],
        initialValue: {},
        submitLabel: $localize`:@@users.actions.delete:Supprimer`,
        cancelLabel: $localize`:@@users.actions.cancel:Annuler`,
        destructive: true,
      },
      ariaLabel: title,
      width: '440px',
      maxWidth: '95vw',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.deleteUser(user);
      }
    });
  }

  // Step 2 of adding a user: the fields depend on the role
  private openCreateDialog(role: string): void {
    const dialogRef = this.openUserForm(
      $localize`:@@users.actions.addRole:Ajouter un utilisateur : ${roleLabel(role)}:role:`,
      $localize`:@@users.actions.add:Ajouter un utilisateur`,
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
      data: {
        title,
        submitLabel,
        cancelLabel: $localize`:@@users.actions.cancel:Annuler`,
        fields,
        initialValue,
      },
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

  private nameField(): ModalFormField {
    return {
      key: 'username',
      label: $localize`:@@users.fields.name:Nom`,
      type: 'string',
      required: true,
    };
  }

  private emailField(): ModalFormField {
    return {
      key: 'email',
      label: $localize`:@@users.fields.email:Courriel`,
      type: 'string',
      required: true,
      validators: [Validators.email, Validators.minLength(6)],
      errorMessages: {
        email: $localize`:@@users.validation.email:Entrez une adresse courriel valide.`,
        minlength: $localize`:@@users.validation.emailLength:Le courriel doit contenir au moins 6 caractères.`,
      },
    };
  }

  private editFields(): readonly ModalFormField[] {
    return [
      this.nameField(),
      this.emailField(),
      {
        key: 'role',
        label: $localize`:@@users.fields.role:Rôle`,
        type: 'enum',
        required: true,
        options: userRoleOptions(),
      },
    ];
  }

  // The account fields come first, then the profile fields of the role
  private createFields(role: string): readonly ModalFormField[] {
    const account: ModalFormField[] = [
      this.nameField(),
      this.emailField(),
      {
        key: 'password',
        label: $localize`:@@users.fields.password:Mot de passe`,
        type: 'password',
        required: true,
        validators: [Validators.pattern(PASSWORD_PATTERN)],
        errorMessages: {
          pattern: $localize`:@@users.validation.password:Utilisez au moins 8 caractères avec un chiffre, une majuscule, une minuscule et un caractère spécial. Aucun espace.`,
        },
      },
    ];

    if (role === 'VET') {
      return [...account, ...this.vetFields()];
    }

    if (role === 'CUST') {
      return [...account, ...this.customerFields()];
    }

    return account;
  }

  private personFields(): ModalFormField[] {
    const lengthMessage = $localize`:@@users.validation.nameLength:Utilisez de 2 à 30 caractères.`;

    return [
      {
        key: 'firstName',
        label: $localize`:@@users.fields.firstName:Prénom`,
        type: 'string',
        required: true,
        validators: [Validators.minLength(2), Validators.maxLength(30)],
        errorMessages: { minlength: lengthMessage, maxlength: lengthMessage },
      },
      {
        key: 'lastName',
        label: $localize`:@@users.fields.lastName:Nom de famille`,
        type: 'string',
        required: true,
        validators: [Validators.minLength(2), Validators.maxLength(30)],
        errorMessages: { minlength: lengthMessage, maxlength: lengthMessage },
      },
    ];
  }

  private vetFields(): ModalFormField[] {
    const phoneMessage = $localize`:@@users.validation.vetPhone:Le numéro de téléphone doit contenir exactement 20 caractères, par exemple (514) 555-0123 #1234.`;

    return [
      ...this.personFields(),
      {
        key: 'phoneNumber',
        label: $localize`:@@users.fields.phone:Numéro de téléphone`,
        type: 'string',
        required: true,
        placeholder: '(514) 555-0123 #1234',
        // The vet service only accepts a phone number of exactly 20 characters.
        validators: [Validators.minLength(20), Validators.maxLength(20)],
        errorMessages: { minlength: phoneMessage, maxlength: phoneMessage },
      },
      {
        key: 'resume',
        label: $localize`:@@users.fields.resume:Résumé`,
        type: 'string',
        required: true,
        validators: [Validators.minLength(10)],
        errorMessages: {
          minlength: $localize`:@@users.validation.resumeLength:Le résumé doit contenir au moins 10 caractères.`,
        },
      },
    ];
  }

  private customerFields(): ModalFormField[] {
    return [
      ...this.personFields(),
      {
        key: 'address',
        label: $localize`:@@users.fields.address:Adresse`,
        type: 'string',
        required: true,
      },
      {
        key: 'city',
        label: $localize`:@@users.fields.city:Ville`,
        type: 'string',
        required: true,
      },
      {
        key: 'province',
        label: $localize`:@@users.fields.province:Province`,
        type: 'string',
        required: true,
      },
      {
        key: 'telephone',
        label: $localize`:@@users.fields.phone:Numéro de téléphone`,
        type: 'string',
        required: true,
        placeholder: '5145550123',
        validators: [Validators.pattern(CUSTOMER_PHONE_PATTERN)],
        errorMessages: {
          pattern: $localize`:@@users.validation.customerPhone:Entrez 10 chiffres, sans espaces ni tirets.`,
        },
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
      $localize`:@@users.success.updated:Utilisateur modifié avec succès.`,
      $localize`:@@users.errors.save:Impossible d'enregistrer l'utilisateur. Veuillez réessayer.`,
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
      $localize`:@@users.success.created:Utilisateur créé avec succès.`,
      $localize`:@@users.errors.save:Impossible d'enregistrer l'utilisateur. Veuillez réessayer.`,
    );
  }

  private deleteUser(user: User): void {
    this.save(
      this.userService.deleteUser(user.userId),
      $localize`:@@users.success.deleted:Utilisateur supprimé avec succès.`,
      $localize`:@@users.errors.delete:Impossible de supprimer l'utilisateur. Veuillez réessayer.`,
    );
  }

  // Shows the success message only when the backend accepted the change
  private save(
    request$: ReturnType<UserService['updateUser']>,
    successMessage: string,
    errorMessage: string,
  ): void {
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
        // English users see the message of the backend, French users see the translated one
        this.saveError.set(!this.isFrench && isApiError(error) ? error.message : errorMessage);
      },
    });
  }
}
