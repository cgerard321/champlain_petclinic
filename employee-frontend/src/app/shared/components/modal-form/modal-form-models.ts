import { ValidatorFn } from '@angular/forms';

export type ModalFormFieldType = 'string' | 'number' | 'enum' | 'image';

export interface ModalFormOption<T = string> {
  value: T;
  label: string;
}

// Image Control
// changed == false && file == null -> untouched
// changed == true && file == File -> new image
// changed == true && file == null -> removed
export interface ModalFormImageValue {
  file: File | null;
  changed: boolean;
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

  currentImageUrl?: string | null; // existing picture to preview
  maxImageSizeBytes?: number;
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
