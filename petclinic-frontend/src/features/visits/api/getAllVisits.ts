import { VisitResponseModel } from '../models/VisitResponseModel';
import { readSseStream } from '@/shared/api/readSseStream';

export function getAllVisits(
  signal?: AbortSignal
): AsyncGenerator<VisitResponseModel> {
  return readSseStream<VisitResponseModel>('/visits', signal, {
    useV2: false,
  });
}
