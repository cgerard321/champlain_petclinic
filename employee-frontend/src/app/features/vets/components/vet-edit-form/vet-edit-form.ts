import { Component, effect, inject, input, output, signal } from '@angular/core';
import { FormControl, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { VetResponse, VetEditSubmission, VetSpecialty } from '@features/vets/models/vet-api.model';
import { specialtyLabel } from '@features/vets/models/vet-profile.mapper';

@Component({
  selector: 'app-vet-edit-form',
  imports: [ReactiveFormsModule],
  templateUrl: './vet-edit-form.html',
  styleUrl: './vet-edit-form.css',
})
export class VetEditForm {
  readonly vet = input.required<VetResponse>();
  readonly availableSpecialties = input.required<readonly string[]>();
  readonly saving = input(false);
  readonly error = input('');
  readonly submitted = output<VetEditSubmission>();
  readonly cancelled = output<void>();
  protected readonly customSpecialty = new FormControl('', {
    nonNullable: true,
  });
  protected readonly specialtyLabel = specialtyLabel;
  private readonly fb = inject(FormBuilder);
  protected readonly pendingSpecialtyNames = signal<string[]>([]);
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
    specialties: this.fb.nonNullable.control<VetSpecialty[]>([]),
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

  protected addSpecialtyByName(value: string): void {
    if (this.saving()) return;

    const name = value.trim();
    if (!name) return;

    const selectedNames = [
      ...this.form.controls.specialties.value.map((specialty) => specialty.name),
      ...this.pendingSpecialtyNames(),
    ];

    const alreadySelected = selectedNames.some(
      (selected) => selected.trim().toLowerCase() === name.toLowerCase(),
    );

    if (alreadySelected) return;

    this.pendingSpecialtyNames.update((names) => [...names, name]);
    this.form.controls.specialties.markAsDirty();
  }

  protected removePendingSpecialty(name: string): void {
    if (this.saving()) return;

    this.pendingSpecialtyNames.update((names) => names.filter((item) => item !== name));
    this.form.controls.specialties.markAsDirty();
  }

  protected isSpecialtySelected(specialtyId: string): boolean {
    return this.form.controls.specialties.value.some(
      (specialty) => specialty.specialtyId === specialtyId,
    );
  }

  protected toggleSpecialty(specialty: VetSpecialty, checked: boolean): void {
    if (this.saving()) return;

    const control = this.form.controls.specialties;
    const current = control.value;

    const updated = checked
      ? current.some((item) => item.specialtyId === specialty.specialtyId)
        ? current
        : [...current, specialty]
      : current.filter((item) => item.specialtyId !== specialty.specialtyId);

    control.setValue(updated);
    control.markAsDirty();
    control.markAsTouched();
  }

  protected remainingSpecialties(): readonly string[] {
    const selectedNames = new Set(
      [
        ...this.form.controls.specialties.value.map((specialty) => specialty.name),
        ...this.pendingSpecialtyNames(),
      ].map((name) => name.trim().toLowerCase()),
    );

    return this.availableSpecialties().filter(
      (name) => !selectedNames.has(name.trim().toLowerCase()),
    );
  }

  protected addCustomSpecialty(): void {
    this.addSpecialtyByName(this.customSpecialty.value);

    if (!this.saving()) {
      this.customSpecialty.reset();
    }
  }

  constructor() {
    effect(() => {
      const vet = this.vet();
      this.form.reset({
        firstName: vet.firstName ?? '',
        lastName: vet.lastName ?? '',
        email: vet.email ?? '',
        phoneNumber: vet.phoneNumber ?? '',
        resume: vet.resume ?? '',
        specialties: [...(vet.specialties ?? [])],
      });
      this.pendingSpecialtyNames.set([]);
      this.customSpecialty.reset();
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
      specialties: values.specialties,
    });
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    const vet = this.vet();
    this.submitted.emit({
      request: {
        ...this.form.getRawValue(),
        vetId: vet.vetId,
        vetBillId: vet.vetBillId,
        workday: vet.workday ?? [],
        workHoursJson: vet.workHoursJson,
        active: vet.active,
        photoDefault: false,
      },
      newSpecialtyNames: [...this.pendingSpecialtyNames()],
    });
  }
}
