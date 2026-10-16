import { Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { VetResponse, VetUpdateRequest } from '@features/vets/models/vet-api.model';

@Component({
  selector: 'app-vet-edit-form',
  imports: [ReactiveFormsModule],
  templateUrl: './vet-edit-form.html',
  styleUrl: './vet-edit-form.css',
})
export class VetEditForm {
  readonly vet = input.required<VetResponse>();
  readonly saving = input(false);
  readonly error = input('');
  readonly submitted = output<VetUpdateRequest>();
  readonly cancelled = output<void>();
  private readonly fb = inject(FormBuilder);
  protected readonly form = this.fb.nonNullable.group({
    firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
    lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(30)]],
    email: [
      '',
      [Validators.required, Validators.email, Validators.minLength(6), Validators.maxLength(320)],
    ],
    phoneNumber: [
      '',
      [
        Validators.required,
        Validators.pattern(
          /^(?:\+?1[ .-]?)?\(?\d{3}\)?[ .-]?\d{3}[ .-]?\d{4}(?:\s*(?:#|ext\.?|x)\s*\d{1,6})?$/,
        ),
      ],
    ],
    resume: ['', [Validators.required, Validators.minLength(10)]],
  });
  protected readonly fields = [
    {
      key: 'firstName',
      label: $localize`:@@vets.edit.first_name:Prénom`,
      type: 'text',
      hint: $localize`:@@vets.edit.name_hint:De 2 à 30 caractères.`,
    },
    {
      key: 'lastName',
      label: $localize`:@@vets.edit.last_name:Nom`,
      type: 'text',
      hint: $localize`:@@vets.edit.name_hint:De 2 à 30 caractères.`,
    },
    {
      key: 'email',
      label: $localize`:@@vets.profile.email:Courriel`,
      type: 'email',
      hint: $localize`:@@vets.edit.email_hint:Une adresse courriel valide est requise.`,
    },
    {
      key: 'phoneNumber',
      label: $localize`:@@vets.profile.phone:Téléphone`,
      type: 'tel',
      hint: $localize`:@@vets.edit.phone_hint:Exemple : (514) 555-0123, avec un poste facultatif #1234.`,
    },
    {
      key: 'resume',
      label: $localize`:@@vets.profile.about:À propos`,
      type: 'textarea',
      hint: $localize`:@@vets.edit.biography_hint:Au moins 10 caractères.`,
    },
  ] as const;

  constructor() {
    effect(() => {
      const vet = this.vet();
      this.form.reset({
        firstName: vet.firstName ?? '',
        lastName: vet.lastName ?? '',
        email: vet.email ?? '',
        phoneNumber: vet.phoneNumber ?? '',
        resume: vet.resume ?? '',
      });
    });
  }

  hasUnsavedChanges(): boolean {
    return this.form.dirty;
  }

  protected save(): void {
    if (this.saving()) return;
    const values = this.form.getRawValue();
    this.form.setValue({
      firstName: values.firstName.trim(),
      lastName: values.lastName.trim(),
      email: values.email.trim(),
      phoneNumber: values.phoneNumber.trim(),
      resume: values.resume.trim(),
    });
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const vet = this.vet();
    this.submitted.emit({
      ...this.form.getRawValue(),
      vetId: vet.vetId,
      vetBillId: vet.vetBillId,
      workday: vet.workday ?? [],
      workHoursJson: vet.workHoursJson,
      active: vet.active,
      specialties: vet.specialties ?? [],
      photoDefault: false,
    });
  }
}
