import { VisitResponseModel } from '../models/VisitResponseModel';
import { readSseStream } from '@/shared/api/readSseStream';

const backendUrl = import.meta.env.VITE_BACKEND_URL.replace(/\/$/, '');

export function getAllVetVisits(
  vetId: string,
  signal?: AbortSignal
): AsyncGenerator<VisitResponseModel> {
  return readSseStream<VisitResponseModel>(
    `${backendUrl}/gateway/visits/vets/${encodeURIComponent(vetId)}/visits`,
    signal
  );
}
