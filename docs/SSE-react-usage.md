
## Where things live

```text
petclinic-frontend/src/
├── shared/
│   └── api/
│       ├── axiosInstance.ts
│       │   └── Shared Axios configuration, authentication, XSRF, and v1 routing
│       └── readSseStream.ts
│           └── Shared SSE client: Axios request, decoding, parsing, and buffering
│
└── features/
    └── visits/
        ├── api/
        │   ├── getAllVisits.ts
        │   ├── getAllOwnerVisits.ts
        │   ├── getAllVetVisits.ts
        │   ├── getVisitsForPet.ts
        │   └── getVisitsForPractitioner.ts
        │       └── Visit-specific SSE requests using readSseStream
        │
        ├── VisitListTable.tsx
        ├── CustomerVisitListTable.tsx
        └── Calendar/
            ├── CalendarView.tsx
            └── CustomerCalendarView.tsx
                └── Consume streamed visits and update React state
```

## ReadSseStream


`readSseStream` requests visit data through the shared `Axios` instance. 

**(Axios https://www.digitalocean.com/community/tutorials/react-axios-react)**

`Axios` is here used for : 
- existing authentication, 
- XSRF settings, 
- interceptors, 
- and v1 routing are preserved (which is prefered over v2 see documentation).

Axios’s **fetch adapter** (axiosInstance.get()) sets the response as a readable stream, 

which allows to process data as it arrives instead of waiting for the full chunk of data before processing it (async.) 



**`TextDecoderStream`** converts incoming bytes into text (from bytes to a human readble buffer)



**(event source parser https://www.npmjs.com/package/eventsource-parser)**

Buffers partial chunks until complete SSE events are available. 
Each event is then parsed as JSON and passed to React immediately.
This allows the UI to load progressively while supporting cancellation through AbortSignal (allows to cancel the http request if the user leaves the page)


## Quick start

**Import readSseStream in an API**

`import { readSseStream } from '@/shared/api/readSseStream';`

Make sure to set v2 to false

`return readSseStream<VisitResponseModel>('/visits', signal, {
    useV2: false,
  });`

As said before we want to use v1 (see documentation for explanation)

**Consume the stream in a React component**

const abortController = new AbortController();

```typescript
try {
  for await (const visit of getAllVisits(abortController.signal)) {
    setVisits(currentVisits => [...currentVisits, visit]);
  }
} catch (error) {
  if (!abortController.signal.aborted) {
    setError('Unable to load visits');
  }
}
```
**(Example from petclinic-frontend/src/features/visits/Calendar/CalendarView.tsx)**

Each complete SSE event is parsed (converted to UTF-8) and passed as soon as it arrives,
allowing the component to update progressively instead of waiting for the full response.


**Cancel the stream when the component is destroyed**


```typescript
useEffect(() => {
  const controller = new AbortController();

  async function loadVisits() {
    try {
      for await (const visit of getAllVisits(controller.signal)) {
        setVisits(current => [...current, visit]);
      }
    } catch (error) {
      if (!controller.signal.aborted) {
        setError('Unable to load visits');
      }
    }
  }

  loadVisits();

  return () => controller.abort();
}, []);
```

The AbortController stops the request when the page is left or a newer request replaces it.
This prevents outdated streams from updating a destroyed component.



### Stream configuration

| Setting | Current value | Description |
| --- | --- | --- |
| Axios instance | `axiosInstance` | Reuses authentication, credentials, XSRF settings, and interceptors. |
| Adapter | `'fetch'` | Allows Axios to expose the response as a browser `ReadableStream`. |
| Response type | `'stream'` | Enables incremental response processing. (process data as it arrives |
| Accept header | `'text/event-stream'` | Requests an SSE response from the backend. |
| Routing | `{ useV2: false }` | Uses the v1 `/gateway` route through the Axios interceptor. |
| Cancellation | `AbortSignal` | Stops the stream when the component unmounts or a newer request starts. |


### Buffer and parsing

| Stage | Library/API | Purpose |
| --- | --- | --- |
| HTTP request | Axios | Sends the authenticated request. |
| Network transport | Axios fetch adapter | Exposes the response body as a stream. |
| Byte decoding | `TextDecoderStream` | Converts response bytes into text. |
| SSE buffering | `EventSourceParserStream` | Buffers partial chunks until a complete SSE event exists. |
| JSON conversion | `JSON.parse` | Converts each event’s `data` field into a visit object. |


## Errors and cancellation

| Situation | Stream behavior | React result |
| --- | --- | --- |
| Server closes the response normally | The async generator completes. It does not reconnect automatically. | The `for await...of` loop ends normally. |
| Network failure or request cancellation | Axios rejects the request. | The component’s `catch` block handles the error unless the request was intentionally aborted. |
| `401 Unauthorized` | Axios rejects the request because `handleLocally: true` prevents global conversion into a resolved response. | The component receives the error and can show an error or redirect. |
| `403 Forbidden` | Axios rejects the request locally. | The component receives the authorization error. |
| Other HTTP errors, such as `404` or `500` | Axios rejects the request. | The component’s error handler runs. |
| Invalid JSON in an SSE event | The event is logged and skipped. | Later valid events continue processing. |
| Component unmounts or dependencies change | `AbortController.abort()` cancels the stream. | The component ignores the expected cancellation error. |



## Testing


React SSE streams are tested with Playwright by recieving  Axios request route and returning an SSE-formatted response.

```TypeScript
await page.route('**/api/gateway/visits', async route => {
  await route.fulfill({
    status: 200,
    contentType: 'text/event-stream',
    body: [
      `data: ${JSON.stringify(visit1)}`,
      '',
      `data: ${JSON.stringify(visit2)}`,
      '',
    ].join('\n'),
  });
});
```

**Tests are located under C:\Users\ionas\champlain_petclinic\petclinic-frontend\tests**




  













