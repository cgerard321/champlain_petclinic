import { EventSourceParserStream } from 'eventsource-parser/stream';
import { AxiosRequestConfig } from 'axios';
import axiosInstance from '@/shared/api/axiosInstance.ts';

export async function* readSseStream<T>(
  url: string,
  signal?: AbortSignal,
  config?: Pick<AxiosRequestConfig, 'useV2'>
): AsyncGenerator<T> {
  const response = await axiosInstance.get<ReadableStream<Uint8Array>>(url, {
    adapter: 'fetch',
    responseType: 'stream',
    headers: { Accept: 'text/event-stream' },
    signal,
    timeout: 0,
    ...config,
    handleLocally: true,
  });

  if (!response.data)
    throw new Error('The response does not contain a readable stream');

  const reader = response.data
    .pipeThrough(new TextDecoderStream())
    .pipeThrough(new EventSourceParserStream())
    .getReader();

  try {
    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      if (value.data === '[DONE]') continue;
      try {
        yield JSON.parse(value.data) as T;
      } catch (error) {
        console.error('Could not parse SSE event:', error);
      }
    }
  } finally {
    await reader.cancel().catch(() => {});
    reader.releaseLock();
  }
}
