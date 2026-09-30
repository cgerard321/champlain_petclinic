import { VisitResponseModel } from '../models/VisitResponseModel';
import { readSseStream } from '@/shared/api/readSseStream';

export function getAllOwnerVisits(
  ownerId: string,
  signal?: AbortSignal
): AsyncGenerator<VisitResponseModel> {
  const cleanOwnerId = ownerId.trim();

  return readSseStream<VisitResponseModel>(
    `/visits/owners/${encodeURIComponent(cleanOwnerId)}/visits`,
    signal,
    { useV2: false }
  );
}
