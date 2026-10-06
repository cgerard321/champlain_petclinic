import { clearTranslations, loadTranslations } from '@angular/localize';

import { Roles } from '@shared/models/roles';

import { employeeRoleLabel } from './role-label';

const EN_ROLES = {
  'settings.security.role.admin': 'Administrator',
  'settings.security.role.vet': 'Veterinarian',
  'settings.security.role.receptionist': 'Receptionist',
  'settings.security.role.inventoryManager': 'Inventory Manager',
  'settings.security.role.unknown': 'Employee',
};

describe('employeeRoleLabel', () => {
  // Translations are installed globally, so a test that loads them has to put the environmentback as it found it or it would leak into the next ones.
  afterEach(() => clearTranslations());

  // CRITERION 1 - Each of the four employee roles reads as a name, not as a token.
  it('names an administrator', () => {
    expect(employeeRoleLabel([Roles.admin])).toBe('Administrateur');
  });

  it('names a veterinarian', () => {
    expect(employeeRoleLabel([Roles.vet])).toBe('Vétérinaire');
  });

  it('names a receptionist', () => {
    expect(employeeRoleLabel([Roles.receptionist])).toBe('Réceptionniste');
  });

  it('names an inventory manager', () => {
    expect(employeeRoleLabel([Roles.inventoryManager])).toBe("Gestionnaire d'inventaire");
  });

  // POSITIVE - Several roles at once resolve to a single, predictable label.
  // EMPLOYEE_ROLES lists ADMIN first, so the most privileged role wins whatever order the server sent them in.
  it('shows the most privileged role when an employee holds several', () => {
    // Assert
    expect(employeeRoleLabel([Roles.vet, Roles.admin])).toBe('Administrateur');
    expect(employeeRoleLabel([Roles.admin, Roles.vet])).toBe('Administrateur');
  });

  it('ignores a customer role sitting next to an employee one', () => {
    expect(employeeRoleLabel([Roles.owner, Roles.vet])).toBe('Vétérinaire');
  });

  // NEGATIVE - Never returns an empty label, which would look like a broken page.
  it('falls back to a neutral label on an empty list', () => {
    expect(employeeRoleLabel([])).toBe('Employé');
  });

  it('falls back to a neutral label on an unknown role', () => {
    expect(employeeRoleLabel(['SOMETHING_ELSE'])).toBe('Employé');
  });

  it('falls back to a neutral label for a customer-only account', () => {
    expect(employeeRoleLabel([Roles.owner])).toBe('Employé');
  });

//localisation
  describe('once the English catalogue is installed', () => {
    beforeEach(() => loadTranslations(EN_ROLES));

    it('translates the four employee roles', () => {
      // Assert
      expect(employeeRoleLabel([Roles.admin])).toBe('Administrator');
      expect(employeeRoleLabel([Roles.vet])).toBe('Veterinarian');
      expect(employeeRoleLabel([Roles.receptionist])).toBe('Receptionist');
      expect(employeeRoleLabel([Roles.inventoryManager])).toBe('Inventory Manager');
    });

    it('translates the fallback label too', () => {
      expect(employeeRoleLabel([])).toBe('Employee');
    });
  });
});
