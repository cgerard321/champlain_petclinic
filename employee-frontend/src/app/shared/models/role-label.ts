import { EMPLOYEE_ROLES, Roles } from '@shared/models/roles';

export function employeeRoleLabel(roles: readonly string[]): string {
  const match = EMPLOYEE_ROLES.find((role) => roles.includes(role));

  switch (match) {
    case Roles.admin:
      return $localize`:@@role.admin:Administrateur`;
    case Roles.vet:
      return $localize`:@@role.vet:Vétérinaire`;
    case Roles.receptionist:
      return $localize`:@@role.receptionist:Réceptionniste`;
    case Roles.inventoryManager:
      return $localize`:@@role.inventoryManager:Gestionnaire d'inventaire`;
    default:
      return $localize`:@@role.unknown:Employé`;
  }
}
