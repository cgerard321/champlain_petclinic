// src/shared/api/environment.ts

import axiosInstance from '@/shared/api/axiosInstance';

export interface EnvironmentInfoModel {
  application: string;
  environment: string;
  activeProfiles: string;
  port: number;
  hostName: string;
  buildVersion: string;
  buildLabel: string;
  buildTime: string;
  startedAt: string;
  uptime: string;
  javaVersion: string;
  allowedFrontendOrigins: string;
  gitCommit: string;
  gitBranch: string;
  serverTime: string;
  markers: Record<string, string>;
}

/**
 * The backend did not answer with the environment endpoint. On a deployed
 * environment that is the expected result and proves the change has not been
 * rolled out there yet.
 */
export const ENVIRONMENT_ENDPOINT_MISSING = 'not deployed';

/**
 * Fetches the deployment metadata of the api-gateway this frontend is talking to.
 *
 * The endpoint is unauthenticated, so no token is attached and `handleLocally`
 * is set so the global error interceptor does not redirect the user away from
 * this page when the endpoint is missing.
 */
export async function getEnvironmentInfo(): Promise<EnvironmentInfoModel | null> {
  try {
    const res = await axiosInstance.get<EnvironmentInfoModel | null>(
      '/environment',
      {
        useV2: true,
        handleLocally: true,
      }
    );

    return res.data ?? null;
  } catch {
    return null;
  }
}
