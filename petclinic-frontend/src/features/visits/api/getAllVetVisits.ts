import { VisitResponseModel } from '../models/VisitResponseModel';
import { readSseStream } from '@/shared/api/readSseStream';

// champlain_petclinic\petclinic-frontend\src\environments\.env.dev
const backendUrl = import.meta.env.VITE_BACKEND_URL.replace(/\/$/, '');

export function getAllVetVisits(
  vetId: string,
  // user can cancel an http request if he leaves the page
  signal?: AbortSignal
  //   We are now using async generator to send data bit by bit so we can load it as it arrives
  //   instead of waiting for the whole response and loading it all at once
): AsyncGenerator<VisitResponseModel> {
  return readSseStream<VisitResponseModel>(
    `${backendUrl}/gateway/visits/vets/${encodeURIComponent(vetId)}/visits`,
    signal
  );
}
