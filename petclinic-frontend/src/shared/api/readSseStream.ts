export async function* readSseStream<T>(
  url: string,
  signal?: AbortSignal
): AsyncGenerator<T> {
  // Initiates an HTTP request configured for Server-Sent Events streaming
  const response = await fetch(url, {
    // cookie credentials sent
    credentials: 'include',
    // we expect continuous txt event stream
    headers: {
      Accept: 'text/event-stream',
    },
    // can cancel the fetch if user decides to leave page
    signal,
  });

  if (!response.ok) {
    throw new Error(`Request failed with status ${response.status}`);
  }

  if (!response.body) {
    throw new Error('The response does not contain a readable stream');
  }

  // we now read data chunk by chunk (instead of all at nce as it used to be)
  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  try {
    while (true) {
      const { value, done } = await reader.read();

      if (done) {
        break;
      }
      // add readable data to buffer
      buffer += decoder.decode(value, { stream: true });
      const events = buffer.split(/\r?\n\r?\n/);

      // Keep the last part until the next chunk completes the event.
      buffer = events.pop() ?? '';

      for (const event of events) {
        const data = event
          // string to array every newline
          .split(/\r?\n/)
          //   keep only lines that start with data:
          .filter(line => line.startsWith('data:'))
          //   remove "data:
          .map(line => line.slice(5).trim())
          //   array to single string
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
    // other parts of the code can access the stream
    reader.releaseLock();
  }
}
