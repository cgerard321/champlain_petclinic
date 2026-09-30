import { Visit } from '@/features/visits/models/Visit';
import { readSseStream } from '@/shared/api/readSseStream';

export function getVisitsForPractitioner(
  practitionerId: string,
  signal?: AbortSignal
): AsyncGenerator<Visit> {
  return readSseStream<Visit>(
    `/visits/vets/${encodeURIComponent(practitionerId.trim())}/visits`,
    signal,
    { useV2: false }
  );
}
