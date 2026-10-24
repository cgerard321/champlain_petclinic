import { CommonModule, formatNumber } from '@angular/common';
import { Component, inject, LOCALE_ID } from '@angular/core';
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

import { ImageEditor, ImageAction } from '@shared/components/image-editor/image-editor';

import {
  ModalFormData,
  ModalFormField,
  ModalFormOption,
  ModalFormResult,
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
export type ModalFormFieldType = 'string' | 'number' | 'enum' | 'image' | 'date' | 'password';
export class ModalForm<T extends object> {
  private readonly dialogRef = inject(MatDialogRef<ModalForm<T>, ModalFormResult<T>>);
  private readonly locale = inject(LOCALE_ID);

  // MAT_DIALOG_DATA contains the configuration passed when the dialog is opened.
  // The generic type keeps the form data connected to the type of object being edited.
  protected readonly data = inject<ModalFormData<T>>(MAT_DIALOG_DATA);

  protected readonly form = this.createForm();

  protected readonly fields = this.data.fields;
  protected readonly cancelLabel = $localize`:@@modal.actions.cancel:Annuler`;
  protected readonly saveLabel = $localize`:@@modal.actions.save:Enregistrer`;

  protected createForm(): DynamicFormGroup {
    const controls: DynamicFormControls = {};
    const initialValue = structuredClone(this.data.initialValue) as Record<string, unknown>;

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
        return { action: 'keep' } satisfies ImageAction;

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

    if (field.type === 'date' && field.minDate) {
      validators.push(this.minDate(field.minDate));
    }

    if (field.validators) {
      validators.push(...field.validators);
    }

    return validators;
  }

  // -------- Validator Methods --------
  private requiredImage(field: ModalFormField): ValidatorFn {
    return (control) => {
      const change = control.value as ImageAction;
      const hasImage =
        change.action === 'replace' || (change.action === 'keep' && !!field.currentImageUrl);
      return hasImage ? null : { required: true };
    };
  }

  private minDate(min: string): ValidatorFn {
    return (control) => {
      const value = control.value as string | null;

      return value && value < min ? { minDate: { min } } : null;
    };
  }
  // ----------------------------------------

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

    for (const key of Object.keys(control.errors)) {
      const message = field.errorMessages?.[key];
      if (message) return message;
    }

    if (control.hasError('required')) {
      return $localize`:@@modal.validation.required:Le champ ${field.label}:label: est obligatoire.`;
    }

    if (control.hasError('min')) {
      const minimum = formatNumber(control.getError('min').min, this.locale);
      return $localize`:@@modal.validation.min:${field.label}:label: doit être au moins ${minimum}:minimum:.`;
    }

    if (control.hasError('max')) {
      const maximum = formatNumber(control.getError('max').max, this.locale);
      return $localize`:@@modal.validation.max:${field.label}:label: doit être au plus ${maximum}:maximum:.`;
    }

    if (control.hasError('minDate')) {
      const earliest = control.getError('minDate').min;
      return $localize`:@@modal.validation.minDate:${field.label}:label: ne peut pas être antérieur au ${earliest}:earliest:.`;
    }

    return $localize`:@@modal.validation.invalid:Valeur invalide.`;
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

  protected onImageChange(field: ModalFormField, change: ImageAction): void {
    const control = this.control(field);
    control.setValue(change);
    control.markAsDirty();
    control.markAsTouched();
  }
}
