import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, ValidatorFn } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { User } from '@features/users/models/user.model';
import { UserService } from '@features/users/services/user-service';

import { UserList } from './user-list';

// No translations are loaded in these tests, so the text is the French source text.
interface DialogConfig {
  data: {
    title: string;
    submitLabel: string;
    message?: string;
    destructive?: boolean;
    fields: {
      key: string;
      type: string;
      options?: { value: string; label: string }[];
      validators?: ValidatorFn[];
    }[];
    initialValue: Record<string, string>;
  };
}

describe('UserList', () => {
  let fixture: ComponentFixture<UserList>;
  let dialogResults: Subject<unknown>[];

  const vetUser: User = {
    userId: 'user-1',
    username: 'dr_vet',
    email: 'vet@petclinic.com',
    roles: [{ id: 2, name: 'VET' }],
  };

  const managerUser: User = {
    userId: 'user-2',
    username: 'stock_boss',
    email: 'stock@petclinic.com',
    roles: [{ id: 3, name: 'INVENTORY_MANAGER' }],
  };

  const adminUser: User = {
    userId: 'user-3',
    username: 'admin',
    email: 'admin@petclinic.com',
    roles: [{ id: 1, name: 'ADMIN' }],
  };

  const customerUser: User = {
    userId: 'user-4',
    username: 'jane_customer',
    email: 'jane@petclinic.com',
    roles: [{ id: 4, name: 'CUST' }],
  };

  const getUsers = vi.fn<UserService['getUsers']>();
  const updateUser = vi.fn<UserService['updateUser']>();
  const createUser = vi.fn<UserService['createUser']>();
  const deleteUser = vi.fn<UserService['deleteUser']>();

  // Every call to MatDialog.open() gets its own result, like a real dialog
  const openDialog = vi.fn(() => {
    const result = new Subject<unknown>();
    dialogResults.push(result);

    return { afterClosed: () => result.asObservable() };
  });

  // The second argument of MatDialog.open() is the dialog configuration
  const dialogConfig = (index: number): DialogConfig =>
    (openDialog.mock.calls[index] as unknown[] | undefined)?.[1] as DialogConfig;

  const closeDialog = (index: number, result: unknown): void => {
    dialogResults[index]?.next(result);
    dialogResults[index]?.complete();
  };

  const text = (): string => fixture.nativeElement.textContent as string;
  const rows = (): NodeListOf<HTMLElement> => fixture.nativeElement.querySelectorAll('tbody tr');
  const editButtons = (): NodeListOf<HTMLButtonElement> =>
    fixture.nativeElement.querySelectorAll('.edit-button');
  const deleteButtons = (): NodeListOf<HTMLButtonElement> =>
    fixture.nativeElement.querySelectorAll('.delete-button');
  const clickAdd = (): void =>
    (fixture.nativeElement.querySelector('.add-user-button') as HTMLButtonElement).click();

  // Chooses a role in the first "Add user" dialog, which opens the second dialog
  const chooseRole = (role: string): DialogConfig => {
    clickAdd();
    closeDialog(0, { value: { role } });

    return dialogConfig(1);
  };

  const fieldKeys = (config: DialogConfig): string[] =>
    config.data.fields.map((field) => field.key);

  // The errors of the email field of a form for an email value, or null when the email is valid
  const emailErrors = (config: DialogConfig, value: string): unknown => {
    const field = config.data.fields.find((item) => item.key === 'email');

    return new FormControl(value, field?.validators ?? []).errors;
  };

  async function setup(locale = 'en-US'): Promise<void> {
    TestBed.resetTestingModule();
    vi.clearAllMocks();
    dialogResults = [];

    getUsers.mockReturnValue(of([vetUser, managerUser, adminUser, customerUser]));
    updateUser.mockReturnValue(of({}));
    createUser.mockReturnValue(of({}));
    deleteUser.mockReturnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [UserList],
      providers: [
        { provide: UserService, useValue: { getUsers, updateUser, createUser, deleteUser } },
        { provide: LOCALE_ID, useValue: locale },
      ],
    })
      .overrideProvider(MatDialog, { useValue: { open: openDialog } })
      .compileComponents();

    fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();
  }

  beforeEach(async () => {
    await setup();
  });

  it('should display the name, email and role of every user', () => {
    expect(rows()).toHaveLength(4);
    expect(text()).toContain('dr_vet');
    expect(text()).toContain('vet@petclinic.com');
    expect(text()).toContain('Vétérinaire');
    expect(text()).toContain("Gestionnaire d'inventaire");
    expect(text()).toContain('Administrateur');
  });

  it('should show the CUST role as a customer', () => {
    expect(text()).toContain('Client');
    expect(text()).not.toContain('Owner');
    expect(text()).not.toContain('Propriétaire');
  });

  it('should show a message when there are no users', () => {
    getUsers.mockReturnValue(of([]));
    fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();

    expect(text()).toContain('Aucun utilisateur trouvé.');
    expect(rows()).toHaveLength(0);
  });

  it('should show an error message when the users cannot be loaded', () => {
    getUsers.mockReturnValue(throwError(() => new Error('boom')));
    fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();

    expect(text()).toContain('Impossible de charger les utilisateurs.');
    expect(rows()).toHaveLength(0);
  });

  it('should not offer to edit or delete administrators', () => {
    // The vet, the inventory manager and the customer can be managed, the administrator cannot
    expect(editButtons()).toHaveLength(3);
    expect(deleteButtons()).toHaveLength(3);
  });

  it('should give the buttons a label that names the user', () => {
    expect(editButtons().item(0).getAttribute('aria-label')).toBe('Modifier dr_vet');
    expect(deleteButtons().item(0).getAttribute('aria-label')).toBe('Supprimer dr_vet');
  });

  describe('editing a user', () => {
    it('should open the edit form with the current values of the user', () => {
      editButtons().item(0).click();

      expect(openDialog).toHaveBeenCalledTimes(1);
      expect(dialogConfig(0).data.title).toBe("Modifier l'utilisateur");
      expect(dialogConfig(0).data.initialValue).toEqual({
        username: 'dr_vet',
        email: 'vet@petclinic.com',
        role: 'VET',
      });
    });

    it('should save the changes, reload the users and confirm the success', () => {
      editButtons().item(0).click();
      closeDialog(0, {
        value: { username: ' new_vet ', email: 'new@petclinic.com', role: 'RECEPTIONIST' },
      });
      fixture.detectChanges();

      expect(updateUser).toHaveBeenCalledWith(vetUser, {
        username: 'new_vet',
        email: 'new@petclinic.com',
        role: 'RECEPTIONIST',
      });
      expect(getUsers).toHaveBeenCalledTimes(2);
      expect(text()).toContain('Utilisateur modifié avec succès.');
    });

    it('should show the error and no success message when saving fails', () => {
      updateUser.mockReturnValue(throwError(() => ({ code: 400, message: 'Email already used' })));

      editButtons().item(0).click();
      closeDialog(0, {
        value: { username: 'dr_vet', email: 'taken@petclinic.com', role: 'VET' },
      });
      fixture.detectChanges();

      expect(text()).toContain('Email already used');
      expect(text()).not.toContain('Utilisateur modifié avec succès.');
      expect(getUsers).toHaveBeenCalledTimes(1);
    });

    it('should show a generic error when saving fails without a message', () => {
      updateUser.mockReturnValue(throwError(() => new Error('network')));

      editButtons().item(0).click();
      closeDialog(0, { value: { username: 'dr_vet', email: 'vet@petclinic.com', role: 'VET' } });
      fixture.detectChanges();

      expect(text()).toContain("Impossible d'enregistrer l'utilisateur. Veuillez réessayer.");
    });

    it('should show the translated error to French users, not the English one of the backend', async () => {
      await setup('fr-CA');
      updateUser.mockReturnValue(throwError(() => ({ code: 400, message: 'Email already used' })));

      editButtons().item(0).click();
      closeDialog(0, {
        value: { username: 'dr_vet', email: 'taken@petclinic.com', role: 'VET' },
      });
      fixture.detectChanges();

      expect(text()).not.toContain('Email already used');
      expect(text()).toContain("Impossible d'enregistrer l'utilisateur. Veuillez réessayer.");
    });

    it('should reject an email without a domain ending when editing, like name@a', () => {
      editButtons().item(0).click();

      expect(emailErrors(dialogConfig(0), 'carterjames@a')).not.toBeNull();
      expect(emailErrors(dialogConfig(0), 'name@domain')).not.toBeNull();
      expect(emailErrors(dialogConfig(0), 'name@.com')).not.toBeNull();
      expect(emailErrors(dialogConfig(0), 'name@domain.c')).not.toBeNull();
      expect(emailErrors(dialogConfig(0), 'name surname@domain.com')).not.toBeNull();
      expect(emailErrors(dialogConfig(0), 'name@@domain.com')).not.toBeNull();
    });

    it('should accept a valid email when editing', () => {
      editButtons().item(0).click();

      expect(emailErrors(dialogConfig(0), 'vet@petclinic.com')).toBeNull();
      expect(emailErrors(dialogConfig(0), 'first.last@mail.example.co')).toBeNull();
    });

    it('should not save anything when the form is cancelled', () => {
      editButtons().item(0).click();
      closeDialog(0, undefined);
      fixture.detectChanges();

      expect(updateUser).not.toHaveBeenCalled();
      expect(createUser).not.toHaveBeenCalled();
    });
  });

  describe('deleting a user', () => {
    it('should ask for a confirmation that names the user before deleting', () => {
      deleteButtons().item(0).click();

      expect(openDialog).toHaveBeenCalledTimes(1);
      expect(dialogConfig(0).data.title).toBe("Supprimer l'utilisateur");
      expect(dialogConfig(0).data.message).toContain('dr_vet');
      expect(dialogConfig(0).data.destructive).toBe(true);
      expect(deleteUser).not.toHaveBeenCalled();
    });

    it('should delete the user, reload the users and confirm the success', () => {
      deleteButtons().item(0).click();
      closeDialog(0, { value: {} });
      fixture.detectChanges();

      expect(deleteUser).toHaveBeenCalledWith('user-1');
      expect(getUsers).toHaveBeenCalledTimes(2);
      expect(text()).toContain('Utilisateur supprimé avec succès.');
    });

    it('should not delete anything when the confirmation is cancelled', () => {
      deleteButtons().item(0).click();
      closeDialog(0, undefined);
      fixture.detectChanges();

      expect(deleteUser).not.toHaveBeenCalled();
      expect(getUsers).toHaveBeenCalledTimes(1);
      expect(text()).not.toContain('Utilisateur supprimé avec succès.');
    });

    it('should show the error and no success message when deleting fails', () => {
      deleteUser.mockReturnValue(throwError(() => new Error('server')));

      deleteButtons().item(0).click();
      closeDialog(0, { value: {} });
      fixture.detectChanges();

      expect(text()).toContain("Impossible de supprimer l'utilisateur. Veuillez réessayer.");
      expect(text()).not.toContain('Utilisateur supprimé avec succès.');
      expect(getUsers).toHaveBeenCalledTimes(1);
    });
  });

  describe('adding a user', () => {
    it('should first ask for the role, with the four roles to choose from', () => {
      clickAdd();

      expect(openDialog).toHaveBeenCalledTimes(1);
      expect(dialogConfig(0).data.title).toBe('Ajouter un utilisateur');
      expect(fieldKeys(dialogConfig(0))).toEqual(['role']);

      const labels = dialogConfig(0).data.fields[0]?.options?.map((option) => option.label);
      expect(labels).toEqual([
        "Gestionnaire d'inventaire",
        'Vétérinaire',
        'Réceptionniste',
        'Client',
      ]);
    });

    it('should not ask for anything else when the role choice is cancelled', () => {
      clickAdd();
      closeDialog(0, undefined);

      expect(openDialog).toHaveBeenCalledTimes(1);
      expect(createUser).not.toHaveBeenCalled();
    });

    it('should only ask for the account fields for an inventory manager', () => {
      const config = chooseRole('INVENTORY_MANAGER');

      expect(config.data.title).toBe("Ajouter un utilisateur : Gestionnaire d'inventaire");
      expect(fieldKeys(config)).toEqual(['username', 'email', 'password']);
    });

    it('should only ask for the account fields for a receptionist', () => {
      const config = chooseRole('RECEPTIONIST');

      expect(config.data.title).toBe('Ajouter un utilisateur : Réceptionniste');
      expect(fieldKeys(config)).toEqual(['username', 'email', 'password']);
    });

    it('should also ask for the vet profile when adding a vet', () => {
      const config = chooseRole('VET');

      expect(config.data.title).toBe('Ajouter un utilisateur : Vétérinaire');
      expect(fieldKeys(config)).toEqual([
        'username',
        'email',
        'password',
        'firstName',
        'lastName',
        'phoneNumber',
        'resume',
      ]);
    });

    it('should also ask for the customer profile when adding a customer', () => {
      const config = chooseRole('CUST');

      expect(config.data.title).toBe('Ajouter un utilisateur : Client');
      expect(fieldKeys(config)).toEqual([
        'username',
        'email',
        'password',
        'firstName',
        'lastName',
        'address',
        'city',
        'province',
        'telephone',
      ]);
    });

    it('should also reject an email without a domain ending when adding a user', () => {
      const config = chooseRole('RECEPTIONIST');

      expect(emailErrors(config, 'desk@a')).not.toBeNull();
      expect(emailErrors(config, 'desk@petclinic.com')).toBeNull();
    });

    it('should hide the password while it is typed', () => {
      const config = chooseRole('RECEPTIONIST');
      const password = config.data.fields.find((field) => field.key === 'password');

      expect(password?.type).toBe('password');
    });

    it('should create the user, reload the list and confirm the success', () => {
      chooseRole('RECEPTIONIST');
      closeDialog(1, {
        value: {
          username: ' front_desk ',
          email: 'desk@petclinic.com',
          role: 'RECEPTIONIST',
          password: 'Passw0rd!',
        },
      });
      fixture.detectChanges();

      expect(createUser).toHaveBeenCalledWith(
        expect.objectContaining({
          username: 'front_desk',
          email: 'desk@petclinic.com',
          password: 'Passw0rd!',
          role: 'RECEPTIONIST',
        }),
      );
      expect(getUsers).toHaveBeenCalledTimes(2);
      expect(text()).toContain('Utilisateur créé avec succès.');
    });

    it('should send the vet profile when creating a vet', () => {
      chooseRole('VET');
      closeDialog(1, {
        value: {
          username: 'dr_new',
          email: 'new.vet@petclinic.com',
          role: 'ignored',
          password: 'Passw0rd!',
          firstName: 'Nora',
          lastName: 'Vetter',
          phoneNumber: '(514) 555-0123 #1234',
          resume: 'Ten years of experience',
        },
      });

      expect(createUser).toHaveBeenCalledWith(
        expect.objectContaining({
          role: 'VET',
          firstName: 'Nora',
          lastName: 'Vetter',
          phoneNumber: '(514) 555-0123 #1234',
          resume: 'Ten years of experience',
        }),
      );
    });

    it('should send the customer profile when creating a customer', () => {
      chooseRole('CUST');
      closeDialog(1, {
        value: {
          username: 'new_customer',
          email: 'customer@petclinic.com',
          role: 'ignored',
          password: 'Passw0rd!',
          firstName: 'Olivia',
          lastName: 'Customer',
          address: '1 Main Street',
          city: 'Montreal',
          province: 'Quebec',
          telephone: '5145550123',
        },
      });

      expect(createUser).toHaveBeenCalledWith(
        expect.objectContaining({
          role: 'CUST',
          firstName: 'Olivia',
          address: '1 Main Street',
          city: 'Montreal',
          province: 'Quebec',
          telephone: '5145550123',
        }),
      );
    });

    it('should show the error and no success message when creating fails', () => {
      createUser.mockReturnValue(
        throwError(() => ({ code: 422, message: 'Username already taken' })),
      );

      chooseRole('INVENTORY_MANAGER');
      closeDialog(1, {
        value: {
          username: 'dr_vet',
          email: 'x@petclinic.com',
          role: 'INVENTORY_MANAGER',
          password: 'Passw0rd!',
        },
      });
      fixture.detectChanges();

      expect(text()).toContain('Username already taken');
      expect(text()).not.toContain('Utilisateur créé avec succès.');
      expect(getUsers).toHaveBeenCalledTimes(1);
    });
  });
});
