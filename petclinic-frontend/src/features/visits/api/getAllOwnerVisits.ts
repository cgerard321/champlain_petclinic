import { VisitResponseModel } from '../models/VisitResponseModel';
import { readSseStream } from '@/shared/api/readSseStream';

const backendUrl = import.meta.env.VITE_BACKEND_URL.replace(/\/$/, '');

export function getAllOwnerVisits(
  ownerId: string,
  signal?: AbortSignal
): AsyncGenerator<VisitResponseModel> {
  const cleanOwnerId = ownerId.trim();

  return readSseStream<VisitResponseModel>(
    `${backendUrl}/gateway/visits/owners/${encodeURIComponent(cleanOwnerId)}/visits`,
    signal
  );
}
