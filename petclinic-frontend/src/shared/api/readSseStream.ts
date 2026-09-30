import type { AxiosRequestConfig } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';

export async function* readSseStream<T>(
  url: string,
  signal?: AbortSignal,
  config?: Pick<AxiosRequestConfig, 'useV2'>
): AsyncGenerator<T> {
  const response = await axiosInstance.get<ReadableStream<Uint8Array>>(url, {
    adapter: 'fetch',
    responseType: 'stream',
    headers: {
      Accept: 'text/event-stream',
    },
    signal,
    ...config,
    handleLocally: true,
  });

  if (!response.data) {
    throw new Error('The response does not contain a readable stream');
  }

  const reader = response.data.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  try {
    while (true) {
      const { value, done } = await reader.read();

      if (done) {
        break;
      }
      buffer += decoder.decode(value, { stream: true });
      const events = buffer.split(/\r?\n\r?\n/);

      buffer = events.pop() ?? '';

      for (const event of events) {
        const data = event
          .split(/\r?\n/)
          .filter(line => line.startsWith('data:'))
          .map(line => line.slice(5).trim())
          .join('\n');

        if (!data || data === '[DONE]') {
          continue;
        }

        try {
          yield JSON.parse(data) as T;
        } catch (error) {
          console.error('Could not parse SSE event:', error);
        }
      }
    }
  } finally {
    reader.releaseLock();
  }
}
