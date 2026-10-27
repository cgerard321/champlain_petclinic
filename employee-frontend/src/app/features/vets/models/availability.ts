import { Workday } from '@features/vets/models/vet-api.model';
import { Status } from '@features/vist/models/Status';
import { Visit } from '@features/vist/models/Visit';

export const DAYS = [
  'Monday',
  'Tuesday',
  'Wednesday',
  'Thursday',
  'Friday',
  'Saturday',
  'Sunday',
] as const satisfies readonly Workday[];

export const ALL_WORK_HOURS = [
  'Hour_8_9',
  'Hour_9_10',
  'Hour_10_11',
  'Hour_11_12',
  'Hour_12_13',
  'Hour_13_14',
  'Hour_14_15',
  'Hour_15_16',
  'Hour_16_17',
  'Hour_17_18',
  'Hour_18_19',
  'Hour_19_20',
] as const;

export type Schedule = Record<Workday, readonly string[]>;

export interface RemovedSlot {
  readonly day: Workday;
  readonly hour: string;
}

const ACTIVE_STATUSES: readonly Status[] = [Status.SCHEDULED, Status.CONFIRMED, Status.UPCOMING];

const DAY_OF_WEEK: Record<Workday, number> = {
  Sunday: 0,
  Monday: 1,
  Tuesday: 2,
  Wednesday: 3,
  Thursday: 4,
  Friday: 5,
  Saturday: 6,
};

export function emptySchedule(): Schedule {
  return Object.fromEntries(DAYS.map((day) => [day, [] as readonly string[]])) as Schedule;
}

export function readSchedule(workHoursJson: string | null): Schedule {
  const schedule = emptySchedule();
  let parsed: unknown;
  try {
    parsed = JSON.parse(workHoursJson || '{}');
  } catch {
    return schedule;
  }
  if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return schedule;
  const record = parsed as Record<string, unknown>;
  for (const day of DAYS) {
    const raw = record[day];
    if (Array.isArray(raw)) {
      schedule[day] = raw.filter((hour): hour is string => typeof hour === 'string');
    }
  }
  return schedule;
}

export function hourRange(hour: string): readonly [number, number] | null {
  const match = /^Hour_(\d{1,2})_(\d{1,2})$/.exec(hour);
  return match ? [Number(match[1]), Number(match[2])] : null;
}

export function hourLabel(hour: string): string {
  const range = hourRange(hour);
  return range ? `${range[0]}:00–${range[1]}:00` : hour;
}

/**
 * Jackson serialises LocalDateTime either as an ISO string or, when
 * WRITE_DATES_AS_TIMESTAMPS is on, as [year, month, day, hour, minute].
 * `new Date()` cannot read the array form and yields an invalid date.
 */
export function toVisitDate(value: unknown): Date {
  if (value instanceof Date) return value;
  if (Array.isArray(value)) {
    const parts = value.map(Number);
    if (parts.length < 3 || parts.slice(0, 3).some(Number.isNaN)) return new Date(NaN);
    const [y, mo, d] = parts as [number, number, number];
    return new Date(y, mo - 1, d, parts[3] ?? 0, parts[4] ?? 0, parts[5] ?? 0);
  }
  if (typeof value === 'string' || typeof value === 'number') return new Date(value);
  return new Date(NaN);
}

export function visitsAffectedBy(
  visits: readonly Visit[],
  removed: readonly RemovedSlot[],
  now: Date = new Date(),
): Visit[] {
  if (!removed.length) return [];
  return visits.filter((visit) => {
    if (!ACTIVE_STATUSES.includes(visit.status)) return false;
    const start = toVisitDate(visit.visitDate);
    if (Number.isNaN(start.getTime()) || start <= now) return false;
    const end = toVisitDate(visit.visitEndDate);
    const spanMs = Number.isNaN(end.getTime()) ? 0 : end.getTime() - start.getTime();
    const duration = spanMs > 0 ? Math.min(spanMs / 3_600_000, 24) : 1;
    const startHour = start.getHours() + start.getMinutes() / 60;
    const endHour = startHour + duration;
    return removed.some((slot) => {
      if (DAY_OF_WEEK[slot.day] !== start.getDay()) return false;
      const range = hourRange(slot.hour);
      return !!range && startHour < range[1] && endHour > range[0];
    });
  });
}
