import {CancellationReason} from "@/features/visits/models/CancellationReason.ts";

export interface CancellationRequest{
    cancellationReason:CancellationReason;
    cancellationReasonDetails?: string;
}