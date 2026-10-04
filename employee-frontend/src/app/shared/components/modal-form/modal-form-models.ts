import { ValidatorFn } from '@angular/forms';

// Right now, modal forms only support string, number and enum (More can be added later on)
export type ModalFormFieldType = 'string' | 'number' | 'enum';

export interface ModalFormOption<T = string> {
  value: T;
  label: string;
}

export interface ModalFormField {
  key: string;
  label: string;
  type: ModalFormFieldType;

  required?: boolean;
  disabled?: boolean;

  min?: number;
  max?: number;

  placeholder?: string;

  options?: readonly ModalFormOption[];

  // Allows a component using the modal to provide additional Angular validators for a specific field.
  validators?: readonly ValidatorFn[];
}

export interface ModalFormData<T extends object> {
  title: string;

  fields: readonly ModalFormField[];

  // Partial<T> allows the caller to provide only the initial values that are relevant to the fields being displayed.
  initialValue: Partial<T>;

  submitLabel?: string;

  cancelLabel?: string;
}

export interface ModalFormResult<T extends object> {
  value: T;
}
