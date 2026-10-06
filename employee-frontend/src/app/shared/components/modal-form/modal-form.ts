import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle,
} from '@angular/material/dialog';

import { ImageEditor } from '@shared/components/image-editor/image-editor';

import {
  ModalFormData,
  ModalFormField,
  ModalFormOption,
  ModalFormResult,
  ModalFormImageValue,
} from './modal-form-models';

type DynamicFormControl = FormControl<unknown>;

type DynamicFormControls = {
  [key: string]: DynamicFormControl;
};

type DynamicFormGroup = FormGroup<DynamicFormControls>;

@Component({
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogActions,
    MatDialogContent,
    MatDialogTitle,
    ImageEditor,
  ],
  selector: 'app-modal-form',
  templateUrl: './modal-form.html',
  styleUrl: './modal-form.css',
})
export class ModalForm<T extends object> {
  private readonly dialogRef = inject(MatDialogRef<ModalForm<T>, ModalFormResult<T>>);

  // MAT_DIALOG_DATA contains the configuration passed when the dialog is opened.
  // The generic type keeps the form data connected to the type of object being edited.
  protected readonly data = inject<ModalFormData<T>>(MAT_DIALOG_DATA);

  protected readonly form = this.createForm();

  protected readonly fields = this.data.fields;

  protected createForm(): DynamicFormGroup {
    const controls: DynamicFormControls = {};
    const initialValue = this.data.initialValue as Record<string, unknown>;

    for (const field of this.data.fields) {
      const fieldValue = initialValue[field.key];

      controls[field.key] = new FormControl(
        { value: fieldValue ?? this.defaultValue(field), disabled: field.disabled ?? false },
        this.createValidators(field),
      );
    }

    return new FormGroup(controls);
  }

  private defaultValue(field: ModalFormField): unknown {
    switch (field.type) {
      case 'number':
        return 0;

      case 'enum':
        return field.options?.[0]?.value ?? null;

      case 'image':
        return { file: null, changed: false } satisfies ModalFormImageValue;

      case 'string':
      default:
        return '';
    }
  }

  private createValidators(field: ModalFormField): ValidatorFn[] {
    const validators: ValidatorFn[] = [];

    if (field.required && field.type !== 'image') {
      validators.push(Validators.required);
    }

    // Validators.required would always pass for an image, because the value is an object
    if (field.required && field.type === 'image') {
      validators.push(this.requiredImage(field));
    }

    if (field.type === 'number') {
      if (field.min !== undefined) {
        validators.push(Validators.min(field.min));
      }

      if (field.max !== undefined) {
        validators.push(Validators.max(field.max));
      }
    }

    if (field.validators) {
      validators.push(...field.validators);
    }

    return validators;
  }

  private requiredImage(field: ModalFormField): ValidatorFn {
    return (control) => {
      const { file, changed } = control.value as ModalFormImageValue;
      const hasImage = changed ? file !== null : !!field.currentImageUrl;
      return hasImage ? null : { required: true };
    };
  }

  // Looks up the FormControl associated with a field.
  // This is used by the template because the controls are created dynamically.
  protected control(field: ModalFormField): DynamicFormControl {
    const control = this.form.controls[field.key];

    if (!control) {
      throw new Error(`No form control found for field "${field.key}".`);
    }

    return control;
  }

  protected options(field: ModalFormField): readonly ModalFormOption[] {
    return field.options ?? [];
  }

  protected errorMessage(field: ModalFormField): string {
    const control = this.control(field);

    if (!control.touched || !control.errors) {
      return '';
    }

    if (control.hasError('required')) {
      return `${field.label} is required.`;
    }

    if (control.hasError('min')) {
      const minimum = control.getError('min').min;
      return `${field.label} must be at least ${minimum}.`;
    }

    if (control.hasError('max')) {
      const maximum = control.getError('max').max;
      return `${field.label} must be at most ${maximum}.`;
    }

    return 'Invalid value.';
  }

  protected submit(): void {
    this.form.markAllAsTouched();

    if (this.form.invalid) {
      return;
    }

    const value = this.form.getRawValue() as T;

    this.dialogRef.close({
      value,
    });
  }

  protected cancel(): void {
    this.dialogRef.close();
  }

  protected onImageChange(field: ModalFormField, file: File | null): void {
    const control = this.control(field);
    control.setValue({ file, changed: true } satisfies ModalFormImageValue);
    control.markAsDirty();
    control.markAsTouched();
  }
}
