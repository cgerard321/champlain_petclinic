import { VetResponse, Workday } from '@features/vets/models/vet-api.model';
import { VetProfile } from '@features/vets/models/vet-profile.model';

export function workdayLabels(): Record<Workday, string> {
  return {
    Monday: $localize`:@@vets.days.monday:Lundi`,
    Tuesday: $localize`:@@vets.days.tuesday:Mardi`,
    Wednesday: $localize`:@@vets.days.wednesday:Mercredi`,
    Thursday: $localize`:@@vets.days.thursday:Jeudi`,
    Friday: $localize`:@@vets.days.friday:Vendredi`,
    Saturday: $localize`:@@vets.days.saturday:Samedi`,
    Sunday: $localize`:@@vets.days.sunday:Dimanche`,
  };
}

function displayHours(vet: VetResponse): string[] {
  const labels = workdayLabels();
  let schedule: Record<string, unknown> = {};
  try {
    const parsed: unknown = JSON.parse(vet.workHoursJson || '{}');
    if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
      schedule = parsed as Record<string, unknown>;
    }
  } catch {
    // Legacy records may contain invalid JSON; still show their known workdays.
  }
  return (Object.keys(labels) as Workday[]).flatMap((day) => {
    const raw = schedule[day];
    const hours = Array.isArray(raw)
      ? raw.filter((hour): hour is string => typeof hour === 'string')
      : [];
    const formatted = hours.flatMap((hour) => {
      const match = /^Hour_(\d{1,2})_(\d{1,2})$/.exec(hour);
      return match ? [`${match[1]}:00–${match[2]}:00`] : [];
    });
    if (formatted.length) return [`${labels[day]} : ${formatted.join(', ')}`];
    return vet.workday?.includes(day) ? [labels[day]] : [];
  });
}

export function specialtyLabels(): Readonly<Record<string, string | undefined>> {
  return {
    radiology: $localize`:@@vets.specialty.radiology:Radiologie`,
    surgery: $localize`:@@vets.specialty.surgery:Chirurgie`,
    dentistry: $localize`:@@vets.specialty.dentistry:Dentisterie`,
    general: $localize`:@@vets.specialty.general:Médecine générale`,
  };
}

export function specialtyLabel(name: string): string {
  const labels = specialtyLabels();
  const key = name.trim().toLowerCase();

  return Object.hasOwn(labels, key) ? (labels[key] ?? name) : name;
}

export function toVetProfile(vet: VetResponse): VetProfile {
  return {
    vetId: vet.vetId,
    firstName: vet.firstName,
    lastName: vet.lastName,
    active: vet.active,
    specialties: (vet.specialties ?? []).map((specialty) => specialtyLabel(specialty.name)),
    email: vet.email || undefined,
    phoneNumber: vet.phoneNumber || undefined,
    biography: vet.resume || undefined,
    workingHours: displayHours(vet),
  };
}
