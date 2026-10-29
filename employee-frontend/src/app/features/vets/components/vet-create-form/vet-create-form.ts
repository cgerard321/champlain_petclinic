import { Component, DestroyRef, computed, inject, output, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize, of, switchMap, tap } from 'rxjs';

import { AuthState } from '@core/services/auth-state';
import { VetResponse, Workday } from '@features/vets/models/vet-api.model';
import { specialtyLabel } from '@features/vets/models/vet-profile.mapper';
import { VetService, vetErrorMessage } from '@features/vets/services/vet.service';
import { Roles } from '@shared/models/roles';

@Component({
  selector: 'app-vet-create-form',
  imports: [ReactiveFormsModule],
  templateUrl: './vet-create-form.html',
  styleUrl: './vet-create-form.css',
})
export class VetCreateForm {
  readonly completed = output<void>();
  readonly cancelled = output<void>();
  private readonly service = inject(VetService);
  private readonly auth = inject(AuthState);
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  protected readonly canCreate = computed(() => this.auth.hasRole(Roles.admin));
  protected readonly saving = signal(false);
  protected readonly error = signal('');
  protected readonly created = signal<VetResponse | null>(null);
  protected readonly days: Workday[] = [
    'Monday',
    'Tuesday',
    'Wednesday',
    'Thursday',
    'Friday',
    'Saturday',
    'Sunday',
  ];
  protected readonly specialtyLabel = specialtyLabel;
  protected readonly dayLabels: Record<Workday, string> = {
    Monday: $localize`:@@vets.create.Monday:Lundi`,
    Tuesday: $localize`:@@vets.create.Tuesday:Mardi`,
    Wednesday: $localize`:@@vets.create.Wednesday:Mercredi`,
    Thursday: $localize`:@@vets.create.Thursday:Jeudi`,
    Friday: $localize`:@@vets.create.Friday:Vendredi`,
    Saturday: $localize`:@@vets.create.Saturday:Samedi`,
    Sunday: $localize`:@@vets.create.Sunday:Dimanche`,
  };
  protected readonly specialties = ['radiology', 'surgery', 'dentistry', 'general'];
  protected readonly form = this.fb.nonNullable.group({
    firstName: [
      '',
      [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(2),
        Validators.maxLength(30),
      ],
    ],
    lastName: [
      '',
      [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.minLength(2),
        Validators.maxLength(30),
      ],
    ],
    email: [
      '',
      [Validators.required, Validators.email, Validators.minLength(6), Validators.maxLength(320)],
    ],
    username: ['', [Validators.required, Validators.pattern(/^\S+$/)]],
    password: [
      '',
      [
        Validators.required,
        Validators.pattern(
          /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=*!()_.<>,{}])(?=\S+$).{8,}$/,
        ),
      ],
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
    resume: ['', [Validators.required, Validators.pattern(/\S/), Validators.minLength(10)]],
    specialties: this.fb.nonNullable.control<string[]>([], Validators.required),
    workday: this.fb.nonNullable.control<Workday[]>([]),
    active: [true],
  });
  protected readonly education = this.fb.nonNullable.group({
    schoolName: [''],
    degree: [''],
    fieldOfStudy: [''],
    startDate: [''],
    endDate: [''],
  });
  protected readonly fields = [
    {
      key: 'firstName',
      label: $localize`:@@vets.create.firstName:Prénom`,
      type: 'text',
      hint: $localize`:@@vets.create.nameHint:De 2 à 30 caractères.`,
    },
    {
      key: 'lastName',
      label: $localize`:@@vets.create.lastName:Nom`,
      type: 'text',
      hint: $localize`:@@vets.create.nameHint:De 2 à 30 caractères.`,
    },
    {
      key: 'email',
      label: $localize`:@@vets.create.email:Courriel`,
      type: 'email',
      hint: $localize`:@@vets.create.emailHint:Entrez une adresse courriel valide.`,
    },
    {
      key: 'username',
      label: $localize`:@@vets.create.username:Nom d’utilisateur`,
      type: 'text',
      hint: $localize`:@@vets.create.usernameHint:Obligatoire, sans espaces.`,
    },
    {
      key: 'password',
      label: $localize`:@@vets.create.password:Mot de passe`,
      type: 'password',
      hint: $localize`:@@vets.create.passwordHint:Au moins 8 caractères : majuscule, minuscule, chiffre et caractère spécial (@#$%^&+=*!()_.<>,{}), sans espaces.`,
    },
    {
      key: 'phoneNumber',
      label: $localize`:@@vets.create.phone: Téléphone`,
      type: 'tel',
      hint: $localize`:@@vets.create.phoneHint:Exemple : (514) 555-0123, poste facultatif #1234.`,
    },
    {
      key: 'resume',
      label: $localize`:@@vets.create.resume:À propos`,
      type: 'text',
      hint: $localize`:@@vets.create.resumeHint:Au moins 10 caractères.`,
    },
  ] as const;
  protected readonly educationFields = [
    { key: 'schoolName', label: $localize`:@@vets.create.school:Établissement`, type: 'text' },
    { key: 'degree', label: $localize`:@@vets.create.degree:Diplôme`, type: 'text' },
    { key: 'fieldOfStudy', label: $localize`:@@vets.create.study:Domaine d’études`, type: 'text' },
    { key: 'startDate', label: $localize`:@@vets.create.start:Date de début`, type: 'date' },
    { key: 'endDate', label: $localize`:@@vets.create.end:Date de fin`, type: 'date' },
  ] as const;

  protected educationValid(): boolean {
    const e = this.education.getRawValue();
    const values = Object.values(e).map((v) => v.trim());
    return values.every((v) => !v) || (values.every(Boolean) && e.startDate <= e.endDate);
  }

  protected cancel(): void {
    if (!this.saving()) {
      if (this.created()) this.completed.emit();
      else this.cancelled.emit();
    }
  }

  protected save(): void {
    if (!this.canCreate() || this.saving()) return;
    const v = this.form.getRawValue();
    for (const key of [
      'firstName',
      'lastName',
      'email',
      'username',
      'phoneNumber',
      'resume',
    ] as const) {
      this.form.controls[key].setValue(v[key].trim());
    }
    this.form.markAllAsTouched();
    this.education.markAllAsTouched();
    if ((!this.created() && this.form.invalid) || !this.educationValid()) return;
    this.saving.set(true);
    this.error.set('');
    const value = this.form.getRawValue();
    const existing = this.created();
    const request = existing
      ? of(existing)
      : this.service.createVet({
          username: value.username,
          password: value.password,
          email: value.email,
          vet: {
            firstName: value.firstName,
            lastName: value.lastName,
            email: value.email,
            phoneNumber: value.phoneNumber,
            resume: value.resume,
            active: value.active,
            workday: value.workday,
            workHoursJson: null,
            photoDefault: false,
            specialties: value.specialties.map((name) => ({ specialtyId: '', name })),
          },
        });
    request
      .pipe(
        tap((vet) => {
          this.created.set(vet);
          this.form.controls.password.reset();
        }),
        switchMap((vet) => {
          const e = this.education.getRawValue();
          return e.schoolName.trim()
            ? this.service.addEducation(vet.vetId, {
                ...e,
                schoolName: e.schoolName.trim(),
                degree: e.degree.trim(),
                fieldOfStudy: e.fieldOfStudy.trim(),
              })
            : of(null);
        }),
        finalize(() => this.saving.set(false)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: () => this.completed.emit(),
        error: (err: unknown) => this.error.set(vetErrorMessage(err)),
      });
  }
}
