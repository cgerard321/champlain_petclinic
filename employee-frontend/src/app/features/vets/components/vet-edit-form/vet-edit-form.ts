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
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';

import {
  DAYS,
  emptySchedule,
  hourLabel,
  readSchedule,
  RemovedSlot,
  Schedule,
  visitsAffectedBy,
} from '@features/vets/models/availability';
import { VetResponse, VetUpdateRequest, Workday } from '@features/vets/models/vet-api.model';
import { workdayLabels } from '@features/vets/models/vet-profile.mapper';
import { Visit } from '@features/vist/models/Visit';
import { ConfirmDialog } from '@shared/components/confirm-dialog/confirm-dialog';

export interface VetSavePayload {
  readonly request: VetUpdateRequest;
  readonly visitsToCancel: readonly Visit[];
}

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
  readonly visits = input<readonly Visit[]>([]);
  readonly visitsChecked = input(false);
  readonly submitted = output<VetSavePayload>();
  readonly cancelled = output<void>();
  private readonly fb = inject(FormBuilder);
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
  protected readonly availability = computed(() => {
    const days = this.days();
    const schedule = this.schedule();
    return DAYS.filter((day) => days.includes(day) || schedule[day].length > 0).map((day) => ({
      day,
      label: this.labels[day],
      hours: schedule[day].map((hour) => ({ value: hour, label: hourLabel(hour) })),
    }));
  });
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
      const workday = vet.workday ?? [];
      const days = DAYS.filter((day) => workday.includes(day));
      const schedule = readSchedule(vet.workHoursJson);
      this.initialDays.set(days);
      this.initialSchedule.set(schedule);
      this.days.set(days);
      this.schedule.set(schedule);
      this.announcement.set('');
    });
  }

  hasUnsavedChanges(): boolean {
    return this.form.dirty || this.removed().length > 0;
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
        target?.focus();
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
    });
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
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
      $localize`:@@vets.availability.confirm_final:Cette action est définitive une fois enregistrée.`,
    );
    return parts.join(' ');
  }

  private emit(): void {
    const vet = this.vet();
    const days = this.days();
    const schedule = this.schedule();
    const untouched = !this.removed().length;
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
        workday: untouched
          ? (vet.workday ?? [])
          : DAYS.filter((day) => days.includes(day) || schedule[day].length > 0),
        workHoursJson: untouched ? vet.workHoursJson : JSON.stringify(workHours),
        active: vet.active,
        specialties: vet.specialties ?? [],
        photoDefault: false,
      },
    });
  }
}
