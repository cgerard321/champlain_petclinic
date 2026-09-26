import { Visit } from '@/features/visits/models/Visit';
import { readSseStream } from '@/shared/api/readSseStream';

const backendUrl = import.meta.env.VITE_BACKEND_URL.replace(/\/$/, '');

export function getVisitsForPractitioner(
  practitionerId: string,
  signal?: AbortSignal
): AsyncGenerator<Visit> {
  return readSseStream<Visit>(
    `${backendUrl}/gateway/visits/vets/${encodeURIComponent(practitionerId.trim())}/visits`,
    signal
  );
}
