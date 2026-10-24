import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { of, Subject, throwError } from 'rxjs';
import { vi } from 'vitest';

import { User } from '@features/users/models/user.model';
import { UserService } from '@features/users/services/user-service';

import { UserList } from './user-list';

interface DialogConfig {
  data: {
    title: string;
    submitLabel: string;
    fields: { key: string; type: string; options?: unknown[] }[];
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

  const getUsers = vi.fn<UserService['getUsers']>();
  const updateUser = vi.fn<UserService['updateUser']>();
  const createUser = vi.fn<UserService['createUser']>();

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

  beforeEach(async () => {
    vi.clearAllMocks();
    dialogResults = [];

    getUsers.mockReturnValue(of([vetUser, managerUser, adminUser]));
    updateUser.mockReturnValue(of({}));
    createUser.mockReturnValue(of({}));

    await TestBed.configureTestingModule({
      imports: [UserList],
      providers: [{ provide: UserService, useValue: { getUsers, updateUser, createUser } }],
    })
      .overrideProvider(MatDialog, { useValue: { open: openDialog } })
      .compileComponents();

    fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();
  });

  it('should display the name, email and role of every user', () => {
    expect(rows()).toHaveLength(3);
    expect(text()).toContain('dr_vet');
    expect(text()).toContain('vet@petclinic.com');
    expect(text()).toContain('Vet');
    expect(text()).toContain('Inventory Manager');
    expect(text()).toContain('Administrator');
  });

  it('should show a message when there are no users', () => {
    getUsers.mockReturnValue(of([]));
    fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();

    expect(text()).toContain('No users found.');
    expect(rows()).toHaveLength(0);
  });

  it('should show an error message when the users cannot be loaded', () => {
    getUsers.mockReturnValue(throwError(() => new Error('boom')));
    fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();

    expect(text()).toContain('Unable to load the users.');
    expect(rows()).toHaveLength(0);
  });

  it('should not offer to edit administrators', () => {
    // Only the vet and the inventory manager can be edited
    expect(editButtons()).toHaveLength(2);
  });

  describe('editing a user', () => {
    it('should open the edit form with the current values of the user', () => {
      editButtons().item(0).click();

      expect(openDialog).toHaveBeenCalledTimes(1);
      expect(dialogConfig(0).data.title).toBe('Edit user');
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
      expect(text()).toContain('User updated successfully.');
    });

    it('should show the error and no success message when saving fails', () => {
      updateUser.mockReturnValue(throwError(() => ({ code: 400, message: 'Email already used' })));

      editButtons().item(0).click();
      closeDialog(0, {
        value: { username: 'dr_vet', email: 'taken@petclinic.com', role: 'VET' },
      });
      fixture.detectChanges();

      expect(text()).toContain('Email already used');
      expect(text()).not.toContain('User updated successfully.');
      expect(getUsers).toHaveBeenCalledTimes(1);
    });

    it('should show a generic error when saving fails without a message', () => {
      updateUser.mockReturnValue(throwError(() => new Error('network')));

      editButtons().item(0).click();
      closeDialog(0, { value: { username: 'dr_vet', email: 'vet@petclinic.com', role: 'VET' } });
      fixture.detectChanges();

      expect(text()).toContain('The user could not be saved. Please try again.');
    });

    it('should not save anything when the form is cancelled', () => {
      editButtons().item(0).click();
      closeDialog(0, undefined);
      fixture.detectChanges();

      expect(updateUser).not.toHaveBeenCalled();
      expect(createUser).not.toHaveBeenCalled();
    });
  });

  describe('adding a user', () => {
    it('should first ask for the role, with the four roles to choose from', () => {
      clickAdd();

      expect(openDialog).toHaveBeenCalledTimes(1);
      expect(dialogConfig(0).data.title).toBe('Add user');
      expect(fieldKeys(dialogConfig(0))).toEqual(['role']);
      expect(dialogConfig(0).data.fields[0]?.options).toHaveLength(4);
    });

    it('should not ask for anything else when the role choice is cancelled', () => {
      clickAdd();
      closeDialog(0, undefined);

      expect(openDialog).toHaveBeenCalledTimes(1);
      expect(createUser).not.toHaveBeenCalled();
    });

    it('should only ask for the account fields for an inventory manager', () => {
      const config = chooseRole('INVENTORY_MANAGER');

      expect(config.data.title).toBe('Add inventory manager');
      expect(fieldKeys(config)).toEqual(['username', 'email', 'password']);
    });

    it('should only ask for the account fields for a receptionist', () => {
      const config = chooseRole('RECEPTIONIST');

      expect(config.data.title).toBe('Add receptionist');
      expect(fieldKeys(config)).toEqual(['username', 'email', 'password']);
    });

    it('should also ask for the vet profile when adding a vet', () => {
      const config = chooseRole('VET');

      expect(config.data.title).toBe('Add vet');
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

    it('should also ask for the owner profile when adding an owner', () => {
      const config = chooseRole('CUST');

      expect(config.data.title).toBe('Add owner');
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
      expect(text()).toContain('User created successfully.');
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

    it('should send the owner profile when creating an owner', () => {
      chooseRole('CUST');
      closeDialog(1, {
        value: {
          username: 'new_owner',
          email: 'owner@petclinic.com',
          role: 'ignored',
          password: 'Passw0rd!',
          firstName: 'Olivia',
          lastName: 'Owner',
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
      expect(text()).not.toContain('User created successfully.');
      expect(getUsers).toHaveBeenCalledTimes(1);
    });
  });
});
