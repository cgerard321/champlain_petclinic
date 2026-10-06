import { EMPLOYEE_ROLES, Roles } from '@shared/models/roles';

export function employeeRoleLabel(roles: readonly string[]): string {
  const match = EMPLOYEE_ROLES.find((role) => roles.includes(role));

  switch (match) {
    case Roles.admin:
      return $localize`:@@settings.security.role.admin:Administrateur`;
    case Roles.vet:
      return $localize`:@@settings.security.role.vet:Vétérinaire`;
    case Roles.receptionist:
      return $localize`:@@settings.security.role.receptionist:Réceptionniste`;
    case Roles.inventoryManager:
      return $localize`:@@settings.security.role.inventoryManager:Gestionnaire d'inventaire`;
    default:
      return $localize`:@@settings.security.role.unknown:Employé`;
  }
}
