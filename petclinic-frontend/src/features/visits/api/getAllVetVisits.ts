import { VisitResponseModel } from '../models/VisitResponseModel';
import { readSseStream } from '@/shared/api/readSseStream';

export function getAllVetVisits(
  vetId: string,
  signal?: AbortSignal
): AsyncGenerator<VisitResponseModel> {
  return readSseStream<VisitResponseModel>(
    `/visits/vets/${encodeURIComponent(vetId)}/visits`,
    signal,
    { useV2: false }
  );
}
