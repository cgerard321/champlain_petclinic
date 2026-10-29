import {
  afterNextRender,
  Component,
  computed,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  Injector,
  input,
  output,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  FormArray,
  FormControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';

import {
  ALL_WORK_HOURS,
  STANDARD_SHIFT_HOURS,
  DAYS,
  emptySchedule,
  hourRange,
  hourLabel,
  readSchedule,
  RemovedSlot,
  Schedule,
  visitsAffectedBy,
} from '@features/vets/models/availability';
import {
  VetEditSubmission,
  VetResponse,
  VetSpecialty,
  Workday,
} from '@features/vets/models/vet-api.model';
import { specialtyLabel, workdayLabels } from '@features/vets/models/vet-profile.mapper';
import { Visit } from '@features/vist/models/Visit';
import { ConfirmDialog } from '@shared/components/confirm-dialog/confirm-dialog';

export interface VetSavePayload extends VetEditSubmission {
  readonly visitsToCancel: readonly Visit[];
}

type EducationFormGroup = FormGroup<{
  schoolName: FormControl<string>;
  degree: FormControl<string>;
  fieldOfStudy: FormControl<string>;
  startDate: FormControl<string>;
  endDate: FormControl<string>;
}>;

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
  readonly visits = input<readonly Visit[]>([]);
  readonly visitsChecked = input(false);
  readonly submitted = output<VetSavePayload>();
  readonly cancelled = output<void>();
  protected readonly customSpecialty = new FormControl('', {
    nonNullable: true,
  });
  protected readonly specialtyLabel = specialtyLabel;
  private readonly fb = inject(FormBuilder);

  protected showEducationForm = false;
  protected readonly educationDraft = this.createEducationGroup();
  protected educationDateError = false;

  protected readonly educationFields = [
    {
      key: 'schoolName',
      label: $localize`:@@vets.education.school:École ou établissement`,
      type: 'text',
    },
    {
      key: 'degree',
      label: $localize`:@@vets.education.degree:Diplôme ou qualification`,
      type: 'text',
    },
    {
      key: 'fieldOfStudy',
      label: $localize`:@@vets.education.field:Domaine d’études`,
      type: 'text',
    },
    {
      key: 'startDate',
      label: $localize`:@@vets.education.start_date:Date de début`,
      type: 'date',
    },
    {
      key: 'endDate',
      label: $localize`:@@vets.education.end_date:Date de fin`,
      type: 'date',
    },
  ] as const;
  protected readonly education = new FormArray<EducationFormGroup>([]);
  private createEducationGroup(): EducationFormGroup {
    return this.fb.nonNullable.group({
      schoolName: ['', [Validators.required, Validators.pattern(/\S/)]],
      degree: ['', [Validators.required, Validators.pattern(/\S/)]],
      fieldOfStudy: ['', [Validators.required, Validators.pattern(/\S/)]],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
    });
  }

  protected openEducationForm(): void {
    if (this.saving()) return;

    this.showEducationForm = true;
  }

  protected addEducation(): void {
    if (this.saving()) return;

    const values = this.educationDraft.getRawValue();

    this.educationDraft.setValue({
      schoolName: values.schoolName.trim(),
      degree: values.degree.trim(),
      fieldOfStudy: values.fieldOfStudy.trim(),
      startDate: values.startDate,
      endDate: values.endDate,
    });

    this.educationDraft.markAllAsTouched();
    this.educationDateError = false;

    if (this.educationDraft.invalid) return;

    if (values.endDate < values.startDate) {
      this.educationDateError = true;
      return;
    }

    const entry = this.createEducationGroup();
    entry.setValue(this.educationDraft.getRawValue());

    this.education.push(entry);
    this.education.markAsDirty();

    this.educationDraft.reset();
    this.showEducationForm = false;
    this.educationDateError = false;
  }

  acknowledgeEducationSaved(): void {
    if (!this.education.length) return;

    this.education.removeAt(0);

    if (!this.education.length) {
      this.education.markAsPristine();
    }
  }
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  private readonly labels = workdayLabels();
  private readonly initialDays = signal<readonly Workday[]>([]);
  private readonly initialSchedule = signal<Schedule>(emptySchedule());
  private readonly days = signal<readonly Workday[]>([]);
  private readonly schedule = signal<Schedule>(emptySchedule());
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);
  protected readonly announcement = signal('');
  protected readonly pendingSpecialtyNames = signal<string[]>([]);
  protected readonly showAllAvailabilities = signal(false);

  protected toggleShowAllAvailabilities(): void {
    this.showAllAvailabilities.update((value) => !value);
  }

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

  protected readonly allAvailabilities = computed(() => {
    const days = this.days();
    const schedule = this.schedule();
    const initialSchedule = this.initialSchedule();

    const allHours = Array.from(
      new Set([...ALL_WORK_HOURS, ...DAYS.flatMap((day) => initialSchedule[day] || [])]),
    ).sort((a, b) => {
      const rangeA = hourRange(a)?.[0] ?? 0;
      const rangeB = hourRange(b)?.[0] ?? 0;
      return rangeA - rangeB;
    });

    return DAYS.map((day) => {
      const isDaySelected = days.includes(day);
      const activeHours = schedule[day] || [];

      return {
        day,
        label: this.labels[day],
        selected: isDaySelected,
        hours: allHours.map((hour) => ({
          value: hour,
          label: hourLabel(hour),
          selected: activeHours.includes(hour),
        })),
      };
    });
  });

  protected addDay(day: Workday): void {
    this.days.update((current) => (current.includes(day) ? current : [...current, day]));

    const initialSchedule = this.initialSchedule();
    const defaultHours = initialSchedule[day]?.length ? initialSchedule[day] : STANDARD_SHIFT_HOURS;

    if (!this.schedule()[day]?.length) {
      this.schedule.update((current) => ({ ...current, [day]: defaultHours }));
    }
    this.announcement.set(
      $localize`:@@vets.availability.added_day:Journée ${this.labels[day]}:day: ajoutée.`,
    );
    this.restoreFocus(`#add-day-${day}`);
  }

  protected addHour(day: Workday, hour: string): void {
    const currentHours = this.schedule()[day] || [];
    if (!currentHours.includes(hour)) {
      const updatedHours = [...currentHours, hour].sort();
      this.schedule.update((current) => ({ ...current, [day]: updatedHours }));
    }
    if (!this.days().includes(day)) {
      this.days.update((current) => [...current, day]);
    }
    this.announcement.set(
      $localize`:@@vets.availability.added_hour:${this.labels[day]}:day: ${hourLabel(hour)}:hour: ajouté.`,
    );
    this.restoreFocus(`#add-hour-${day}-${hour}`);
  }

  protected readonly availability = computed(() => {
    return this.allAvailabilities()
      .filter((entry) => entry.selected && entry.hours.some((h) => h.selected))
      .map((entry) => ({
        ...entry,
        hours: entry.hours.filter((h) => h.selected),
      }));
  });

  protected resetSchedule(): void {
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: $localize`:@@vets.availability.reset_title:Réinitialiser les modifications ?`,
          message: $localize`:@@vets.availability.reset_message:Voulez-vous restaurer les disponibilités initiales ? Cela annulera les modifications en cours, mais ne replanifiera pas les rendez-vous annulés.`,
          confirmLabel: $localize`:@@vets.availability.reset_confirm:Réinitialiser`,
          cancelLabel: $localize`:@@vets.actions.cancel:Annuler`,
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (confirmed) {
          this.days.set([...this.initialDays()]);
          this.schedule.set(JSON.parse(JSON.stringify(this.initialSchedule())));
          this.announcement.set(
            $localize`:@@vets.availability.reset:Disponibilités réinitialisées.`,
          );
          this.restoreFocus('#availability-title');
        }
      });
  }
  protected readonly removedSlots = computed<readonly RemovedSlot[]>(() => {
    const initialSchedule = this.initialSchedule();
    const schedule = this.schedule();
    return DAYS.flatMap((day) =>
      initialSchedule[day]
        .filter((hour) => !schedule[day].includes(hour))
        .map((hour) => ({ day, hour })),
    );
  });
  protected readonly affectedVisits = computed(() =>
    visitsAffectedBy(this.visits(), this.removedSlots()),
  );
  private readonly removed = computed(() => {
    const initialDays = this.initialDays();
    const initialSchedule = this.initialSchedule();
    const days = this.days();
    const schedule = this.schedule();
    return DAYS.flatMap((day) => {
      const hours = initialSchedule[day].filter((hour) => !schedule[day].includes(hour));
      const dayGone = initialDays.includes(day) && !days.includes(day);
      if (!hours.length && !dayGone) return [];
      const label = this.labels[day];
      return [hours.length ? `${label} : ${hours.map(hourLabel).join(', ')}` : label];
    });
  });

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
      const workday = vet.workday ?? [];
      const days = DAYS.filter((day) => workday.includes(day));
      const schedule = readSchedule(vet.workHoursJson);
      this.initialDays.set(days);
      this.initialSchedule.set(schedule);
      this.days.set(days);
      this.schedule.set(schedule);
      this.announcement.set('');
      this.pendingSpecialtyNames.set([]);
      this.customSpecialty.reset();
      this.education.clear();
      this.education.markAsPristine();
      this.education.markAsUntouched();
    });
  }

  hasUnsavedChanges(): boolean {
    return (
      this.form.dirty ||
      this.education.dirty ||
      this.educationDraft.dirty ||
      this.removed().length > 0
    );
    const days = this.days();
    const schedule = this.schedule();
    const initialDays = this.initialDays();
    const initialSchedule = this.initialSchedule();
    const hasScheduleChanges = DAYS.some(
      (day) =>
        initialDays.includes(day) !== days.includes(day) ||
        JSON.stringify(initialSchedule[day]) !== JSON.stringify(schedule[day]),
    );
    return this.form.dirty || hasScheduleChanges;
  }

  protected removeHour(day: Workday, hour: string): void {
    const hours = this.schedule()[day];
    const index = hours.indexOf(hour);
    const rest = hours.filter((item) => item !== hour);
    const neighbour = rest[index] ?? rest[index - 1];
    this.schedule.update((current) => ({ ...current, [day]: rest }));
    const messages = [
      $localize`:@@vets.availability.removed_hour:${this.labels[day]}:day: ${hourLabel(hour)}:hour: retiré.`,
    ];
    if (!rest.length) {
      this.days.update((current) => current.filter((item) => item !== day));
      messages.push(
        $localize`:@@vets.availability.removed_day:Journée ${this.labels[day]}:day: retirée.`,
      );
    }
    this.announcement.set(messages.join(' '));
    this.restoreFocus(neighbour ? `#remove-hour-${day}-${neighbour}` : `#remove-day-${day}`);
  }

  protected removeDay(day: Workday): void {
    this.days.update((current) => current.filter((item) => item !== day));
    this.schedule.update((current) => ({ ...current, [day]: [] }));
    this.announcement.set(
      $localize`:@@vets.availability.removed_day:Journée ${this.labels[day]}:day: retirée.`,
    );
    this.restoreFocus(null);
  }

  private restoreFocus(preferred: string | null): void {
    afterNextRender(
      () => {
        const root = this.host.nativeElement;
        const target =
          (preferred && root.querySelector<HTMLElement>(preferred)) ||
          root.querySelector<HTMLElement>('.remove-day') ||
          root.querySelector<HTMLElement>('#availability-title');
        target?.focus({ preventScroll: true });
      },
      { injector: this.injector },
    );
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
    this.education.markAllAsTouched();
    if (this.form.invalid || this.education.invalid) return;
    const removed = this.removed();
    if (!removed.length) {
      this.emit();
      return;
    }
    this.dialog
      .open(ConfirmDialog, {
        data: {
          title: $localize`:@@vets.availability.confirm_title:Retirer ces disponibilités ?`,
          message: this.confirmMessage(removed),
          confirmLabel: $localize`:@@vets.availability.confirm:Retirer`,
          cancelLabel: $localize`:@@vets.actions.cancel:Annuler`,
        },
      })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (confirmed) this.emit();
      });
  }

  private confirmMessage(removed: readonly string[]): string {
    const parts = [
      $localize`:@@vets.availability.confirm_message:Ces disponibilités seront retirées du profil : ${removed.join(' ; ')}:slots:.`,
    ];
    const count = this.affectedVisits().length;
    if (!this.visitsChecked()) {
      parts.push(
        $localize`:@@vets.availability.visits_unknown:Les visites réservées sur ces créneaux n'ont pas pu être vérifiées.`,
      );
    } else if (count === 1) {
      parts.push(
        $localize`:@@vets.availability.visits_one:1 visite réservée sur ces créneaux sera annulée.`,
      );
    } else if (count > 1) {
      parts.push(
        $localize`:@@vets.availability.visits_many:${count}:count: visites réservées sur ces créneaux seront annulées.`,
      );
    }
    parts.push(
      $localize`:@@vets.availability.confirm_visits_cancel:--- AVIS : Cela annulera toutes les visites réservées pendant cette disponibilité.`,
    );
    return parts.join(' ');
  }

  private emit(): void {
    const vet = this.vet();
    const days = this.days();
    const schedule = this.schedule();
    const initialDays = this.initialDays();
    const initialSchedule = this.initialSchedule();

    const hasScheduleChanges = DAYS.some(
      (day) =>
        initialDays.includes(day) !== days.includes(day) ||
        JSON.stringify(initialSchedule[day]) !== JSON.stringify(schedule[day]),
    );

    const workHours: Record<string, readonly string[]> = {};
    for (const day of DAYS) {
      if (schedule[day].length) workHours[day] = schedule[day];
    }

    this.submitted.emit({
      visitsToCancel: this.affectedVisits(),
      request: {
        ...this.form.getRawValue(),
        vetId: vet.vetId,
        vetBillId: vet.vetBillId,
        workday: !hasScheduleChanges
          ? (vet.workday ?? [])
          : DAYS.filter((day) => days.includes(day) || schedule[day].length > 0),
        workHoursJson: !hasScheduleChanges ? vet.workHoursJson : JSON.stringify(workHours),
        active: vet.active,
        photoDefault: false,
      },
      newSpecialtyNames: [...this.pendingSpecialtyNames()],
      newEducation: this.education.getRawValue().map((entry) => ({
        vetId: vet.vetId,
        schoolName: entry.schoolName.trim(),
        degree: entry.degree.trim(),
        fieldOfStudy: entry.fieldOfStudy.trim(),
        startDate: entry.startDate,
        endDate: entry.endDate,
      })),
    });
  }
}
