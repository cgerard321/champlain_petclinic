import { Visit } from '@/features/visits/models/Visit';
import { readSseStream } from '@/shared/api/readSseStream';

export function getVisitsForPet(
  petId: string,
  signal?: AbortSignal
): AsyncGenerator<Visit> {
  return readSseStream<Visit>(
    `/visits/pets/${encodeURIComponent(petId.trim())}`,
    signal,
    { useV2: false }
  );
}
