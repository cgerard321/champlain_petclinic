import { VisitResponseModel } from '../models/VisitResponseModel';
import { readSseStream } from '@/shared/api/readSseStream';

const backendUrl = import.meta.env.VITE_BACKEND_URL.replace(/\/$/, '');

export function getAllVisits(
  signal?: AbortSignal
): AsyncGenerator<VisitResponseModel> {
  // Read each visit as soon as the server sends it.
  return readSseStream<VisitResponseModel>(
    `${backendUrl}/gateway/visits`,
    signal
  );
}
