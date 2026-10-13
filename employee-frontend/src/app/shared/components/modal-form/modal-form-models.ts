import { ValidatorFn } from '@angular/forms';

export type ModalFormFieldType = 'string' | 'number' | 'enum' | 'image';

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
  step?: number;

  placeholder?: string;

  options?: readonly ModalFormOption[];

  // Allows a component using the modal to provide additional Angular validators for a specific field.
  validators?: readonly ValidatorFn[];

  errorMessages?: Record<string, string>;

  currentImageUrl?: string | null; // existing picture to preview
  maxImageSizeBytes?: number;
}

export interface ModalFormData<T extends object> {
  title: string;

  fields: readonly ModalFormField[];

  message?: string; // optional text shown above the fields
  destructive?: boolean; // styles the submit button as a danger action

  // Partial<T> allows the caller to provide only the initial values that are relevant to the fields being displayed.
  initialValue: Partial<T>;

  submitLabel?: string;

  cancelLabel?: string;
}

export interface ModalFormResult<T extends object> {
  value: T;
}
