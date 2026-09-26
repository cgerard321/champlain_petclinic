export async function* readSseStream<T>(
  url: string,
  signal?: AbortSignal
): AsyncGenerator<T> {
  const response = await fetch(url, {
    credentials: 'include',
    headers: {
      Accept: 'text/event-stream',
    },
    signal,
  });

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }

  if (!response.body) {
    throw new Error('The response does not contain a readable stream');
  }

  const reader = response.body.getReader();
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

      // Keep the last part until the next chunk completes the event.
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
          // Ignore one bad event without stopping the rest of the stream.
          console.error('Could not parse SSE event:', error);
        }
      }
    }
  } finally {
    reader.releaseLock();
  }
}
