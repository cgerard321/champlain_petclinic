import { VisitResponseModel } from '../models/VisitResponseModel';
import { readSseStream } from '@/shared/api/readSseStream';

// Gets backend Url and removes trailing slashes (with regex)
const backendUrl = import.meta.env.VITE_BACKEND_URL.replace(/\/$/, '');

export function getAllOwnerVisits(
  //   requesting ownerId
  ownerId: string,
  // optional --> can cancel an ongoing async operation (if user changes page)
  signal?: AbortSignal
): AsyncGenerator<VisitResponseModel> {
  // remove whitespaces from ownerId
  const cleanOwnerId = ownerId.trim();

  // Initiates and returns the Server-Sent Events (sse)  stream consumer for the owner's visits endpoint
  return readSseStream<VisitResponseModel>(
    //   constructs sse endpoint (encodeURIConmponents escapces special characters)
    `${backendUrl}/gateway/visits/owners/${encodeURIComponent(cleanOwnerId)}/visits`,
    // Passes the cancellation signal to the stream reader
    signal
  );
}
