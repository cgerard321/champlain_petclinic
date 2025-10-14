import type { FaqItem } from '../models/FaqItem';
import { contact } from '@/shared/content';

// Formatted hours string for display in the contact info and footer
export const formattedHours = contact.hours
  .map(h =>
    'note' in h && h.note
      ? `${h.days}: ${h.note}`
      : `${h.days}: ${h.open ?? '–'}–${h.close ?? '–'}`
  )
  .join('\n');

// All the sample FAQ questions, answers, and so on.
export const FAQ_ITEMS: readonly FaqItem[] = [
  {
    id: 'hours',
    question: 'What are your hours?',
    answer: `${formattedHours}. For urgent issues outside these hours, visit our Emergency page.`,
    tags: ['schedule', 'open', 'close', 'weekend'],
    category: 'General',
  },
  {
    id: 'walkins',
    question: 'Do you accept walk-ins?',
    answer:
      'Appointments are recommended, but we try to accommodate walk-ins when possible.',
    tags: ['walk in', 'walk-in', 'appointments'],
    category: 'General',
  },
  {
    id: 'booking',
    question: 'How do I book an appointment?',
    answer:
      'Use the “Book Appointment” button on the site or call the clinic during business hours.',
    tags: ['book', 'schedule', 'online'],
    category: 'Appointments',
  },
  {
    id: 'first-visit',
    question: 'What should I bring to my first visit?',
    answer:
      'Please bring medical records, vaccination history, a list of medications, and your pet’s favorite treat or toy.',
    tags: ['new client', 'documents', 'records'],
    category: 'Appointments',
  },
  {
    id: 'vaccines',
    question: 'Do you offer vaccinations?',
    answer:
      'Yes. We provide core and lifestyle vaccines tailored to your pet’s needs.',
    tags: ['shots', 'immunization'],
    category: 'Services',
  },
  {
    id: 'dental',
    question: 'Do you provide dental care?',
    answer:
      'We offer dental exams, cleaning and polishing, and dental X-rays when indicated.',
    tags: ['teeth', 'cleaning', 'xray', 'x-ray'],
    category: 'Services',
  },
  {
    id: 'emergency',
    question: 'Is there emergency care?',
    answer:
      'Yes during open hours. For after-hours emergencies, please see our Emergency page for partner locations.',
    tags: ['urgent', 'after hours', '24/7'],
    category: 'Services',
  },
  {
    id: 'payment',
    question: 'Which payment methods do you accept?',
    answer:
      'Visa, MasterCard, debit, and cash. We can help with claims for select pet insurers.',
    tags: ['billing', 'insurance', 'credit card'],
    category: 'Billing',
  },
  {
    id: 'refills',
    question: 'Can I get prescription refills?',
    answer:
      'Request refills through your account or call us. Some medications require an exam before renewal.',
    tags: ['rx', 'medication', 'renewal'],
    category: 'Medical',
  },
  {
    id: 'microchip',
    question: 'Do you microchip pets?',
    answer:
      'Yes. Microchipping can be done during a regular appointment or while your pet is under anesthesia.',
    tags: ['chip', 'id', 'identification'],
    category: 'Services',
  },
] as const;

// Map of FAQ items by ID for quick lookup (Literally just a dictionary with ID's as keys)
export const FAQ_INDEX: Readonly<Record<string, FaqItem>> = Object.freeze(
  Object.fromEntries(FAQ_ITEMS.map(i => [i.id, i]))
);

// Get a FAQ item by its ID
export function getFaqById(id: string): FaqItem | undefined {
  return FAQ_INDEX[id];
}

// Group FAQ items by their category for categorized display and searching for the future
export function groupFaqByCategory(
  items: readonly FaqItem[] = FAQ_ITEMS
): Readonly<Record<string, FaqItem[]>> {
  return items.reduce<Record<string, FaqItem[]>>((acc, item) => {
    const key = item.category ?? 'Other';

    (acc[key] ||= []).push(item);

    return acc;
  }, {});
}
