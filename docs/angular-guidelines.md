# Frontend Architecture Guide

This document explains how `dev-tools-frontend` is organized, what goes where, and the import rules that keep features
decoupled. Examples use domain names from your actual product (visits, bills, customers, products, inventories, vets)
rather than made-up ones, so this maps directly onto what you'll actually build. This **should** be used for the
employee-frontend as well.

---

## 1. Top-level folders

```
src/
├── app/
│   ├── core/       → app-wide singletons (services, guards, interceptors)
│   ├── shared/     → reusable, business-logic-free building blocks
│   ├── layout/     → the app "shell" (header, sidenav, footer, page frame)
│   └── features/   → business domains (e.g. visits, bills, customers, products)
├── environments/   → per-build config (API base URL, etc.)
└── testing/        → helpers/mocks/fixtures reused across .spec.ts files

```

| Folder         | Loaded                 | Knows about business logic? | Imported by                                          |
|----------------|------------------------|-----------------------------|------------------------------------------------------|
| `core`         | once, at bootstrap     | Yes (app-wide concerns)     | Only `app.ts` and `app.config.ts`                    |
| `shared`       | anywhere it's used     | No                          | `features`, `layout`                                 |
| `layout`       | once, in the app shell | A little (composes routes)  | `app.ts` only                                        |
| `features`     | lazily, per route      | Yes (that domain only)      | **nothing** — see rules below                        |
| `environments` | build time             | No (config values only)     | `core/interceptors/api-base-url-interceptor.ts` only |
| `testing`      | test runs only         | No (fakes/fixtures only)    | `*.spec.ts` files only — never `app/`                |

---

## 2. `core/` — app-wide singletons

Things that exist exactly once for the whole app and aren't tied to any feature's UI.

```
core/
├── guards/          # e.g. auth.guard.ts, unsaved-changes.guard.ts
├── interceptors/     # e.g. auth.interceptor.ts, error.interceptor.ts
└── services/         # e.g. auth.service.ts, config.service.ts
```

Rule of thumb: if two instances of it existed would cause issues (auth state, HTTP interceptor, global error handler),
it's `core`.

`core` services are registered once. Nothing outside of the app bootstrap should need to import from `core` directly —
features consume `core` services via DI, not via explicit imports of implementation details.

---

## 3. `shared/` — reusable building blocks

`shared/` holds things with **no knowledge of a specific business domain**. A shared component doesn't know what a
"visit" or a "bill" is — it just renders whatever data/inputs it's given.

```
shared/
├── components/   # e.g. button, modal, data-table, badge, empty-state
├── directives/   # e.g. click-outside, autofocus, tooltip
├── models/       # generic types: PaginatedResult<T>, ApiError, SortState
└── pipes/        # e.g. truncate, relative-time, file-size
```

**Test for "should this be in shared?"**

1. Does it contain any domain/business logic (talks to a feature-specific service, knows about feature-specific models
   like `Visit` or `Bill`)? → **Yes**, keep it in the feature.
2. Is it purely presentational (inputs/outputs only, no feature imports)? → **Yes candidate**.
3. Is it used by 2+ features, or clearly *going to be*? → **Yes**, move it to `shared`.

If a component is domain-specific but *visually* generic (e.g. a
`visit-status-badge`), don't shortcut it into `shared` just because it's small. Only the logic-free shell belongs
there — e.g. a generic
`status-badge` that takes a `color` and `label` input, which the feature then wraps/configures with its own
`VisitStatus` → color mapping.

`shared` never imports from `features` or `layout`. It's the bottom of the dependency graph.

It is important to think and plan before creating a component for a feature, ask yourself : is this something unique to
this feature?, is this something that could be reused by another feature? Sometimes you might even notice that a
component in feature might need to be promoted to `shared` and made **generic**, the generic part is very important,
remember, shared should never know anything about the business logic.

---

## 4. `layout/` — the app shell

The basics of every page: navbar, sidenav, footer, the `<router-outlet>`
wrapper.

```
layout/
├── shell/
│   ├── shell.ts
│   └── shell.html
├── header/
└── sidenav/
```

`layout` composes `shared` components and reads `core` services (e.g. to show the logged-in user), but it does not
import feature components directly — features are rendered into it via the router.

---

## 5. `features/` — business domains

Each feature is a self-contained slice of the app, normally to be lazy-loaded via routes.

```
features/
└── visits/
    ├── visits.routes.ts
    ├── pages/
    │   ├── visit-list/
    │   │   ├── visit-list.ts
    │   │   ├── visit-list.html
    │   │   └── visit-list.css
    │   └── visit-detail/
    ├── components/
    │   └── visit-card/
    ├── services/
    │   └── visits.service.ts
    └── models/
        └── visit.model.ts
```

### `pages/`

- One folder per **route**. A page is a route-level container: it fetches data (via the feature's service), and composes
  `components/` +
  `shared/` components into a screen.
- Pages are **never imported by anything** — not by other pages, not by other features, not even by other pages in the
  *same* feature. If
  `visit-list` and `visit-detail` need the same chunk of UI, extract that chunk into `components/` (or `shared/` if it's
  generic) and have both pages use it.
- A page is the "leaf" of the import graph — things point *into* it via routing, nothing points *out* of it via
  `import`.

### `components/`

- UI pieces specific to this feature's domain — they know about `Visit`, call `VisitsService`, etc.
- Used **within this feature only** (by its own pages or other components in the same feature).
- **Do not import a feature's `components/` from a different feature.**
  See below.

### `services/`

- Feature-scoped data access / state (e.g. `VisitsService` calling the visits API).

### `<feature>.routes.ts`

- Lives at the root of the feature folder, exports the route config for that feature, normally to be lazy-loaded from
  `app.routes.ts`:

```ts
// app.routes.ts
export const routes: Routes = [
    {
        path: 'visits',
        loadChildren: () =>
            import('./features/visits/visits.routes')
                .then(m => m.VISITS_ROUTES),
    },
    {
        path: 'bills',
        loadChildren: () =>
            import('./features/bills/bills.routes')
                .then(m => m.BILLS_ROUTES),
    },
];
```

```ts
// features/visits/visits.routes.ts
export const VISITS_ROUTES: Routes = [
    {path: '', component: VisitList},
    {path: ':id', component: VisitDetail},
];
```

---

## 6. Import rules

**Features can depend on other features' `services/` (and `models/`), but never on another feature's `pages/` or
`components/`.**

A service is a *contract* (methods in, typed data out) — depending on it is a normal, controlled dependency, same as
depending on `HttpClient`. A component or page is an *implementation* (DOM structure, styling, internal state, its own
service calls) — depending on it means you inherit all of that, and it can shift under you without warning.

Why not allow feature → feature **component** imports?

- **Coupling**: `features/bills` importing a component from
  `features/visits` means you can no longer delete, replace, or independently version either feature without touching
  the other.
- **Ownership gets fuzzy**: if `visit-card` is used by `bills`, is it still the visits team's to change freely? Reused
  components need to live somewhere ownerless and generic — that's what `shared` is for.

**The fix when a feature component is needed elsewhere:** promote it. Strip out anything domain-specific, move it to
`shared/components/`, and have both features consume the shared version. See below for a worked example.

### Allowed vs. not allowed

```
✅ features/visits/pages/visit-list
     imports features/visits/components/visit-card

✅ features/visits/components/visit-card
     imports shared/components/status-badge

✅ features/bills/pages/bills-list
     imports shared/components/data-table

✅ features/customers/pages/customer-detail
     imports features/visits/services/visits.service   (data dependency)

❌ features/customers/pages/customer-detail
     imports features/visits/components/visit-card
   → build customers' own summary component fed by VisitsService instead
   
❌ features/bills/components/bills-list-table
     imports features/visits/components/visit-list-table
   → both are "a table of domain rows" — promote the generic table shell
     to shared instead

❌ features/visits/pages/visit-detail
     imports features/visits/pages/visit-list
   → pages are never imported; extract the shared piece into components/
```

### Quick reference table

Reading it: find the row for **where the import starts**, then look at the column for **what it's importing**. `✅` =
allowed, `❌` = not allowed,
`—` = doesn't apply (that folder has no such thing to import from/into).

| Importing from ↓ \\ Importing what → |          `core`          |   `shared`    | its own `pages`/`components` |           another feature's `pages`/`components`           | another feature's `services`/`models` |               `layout`                |
|--------------------------------------|:------------------------:|:-------------:|:----------------------------:|:----------------------------------------------------------:|:-------------------------------------:|:-------------------------------------:|
| `core`                               |      ✅ (internal)       |      ❌       |              —               |                             ❌                             |                  ❌                   |                  ❌                   |
| `shared`                             |            ❌            | ✅ (internal) |              —               |                             ❌                             |                  ❌                   |                  ❌                   |
| `features/*`                         |       ✅ (via DI)        |      ✅       |              ✅              |                             ❌                             |            ✅ (sparingly)             |                  ❌                   |
| `layout`                             |       ✅ (via DI)        |      ✅       |              —               |                             ❌                             |                  ❌                   |             ✅ (internal)             |
| `app.config.ts`                      | ✅ (registers providers) |       —       |              —               |                             —                              |                   —                   |                   —                   |
| `app.routes.ts`                      |    ✅ (route guards)     |       —       |              —               | ✅ (lazy `loadChildren` of a feature's `*.routes.ts` only) |                   —                   | ✅ (root route nests under the shell) |
| `testing/*` (`.spec.ts` files only)  |       ✅ (via DI)        |      ✅       |     ✅ (the spec's own)      |          — (specs don't share UI across features)          |              ✅ (mocked)              |                   —                   |

The two rules that matter most: **`features/*` → another feature's `pages`/`components` is always ❌.**
**`features/*` → another feature's `services`/`models` is ✅, but keep it sparing** (see the "smell to watch for" in 6b).

---

### 6a. Two features need "the same table" (e.g. `bills` and `visits`)

`bills` has a `BillsListTable`; `visits` has a `VisitListTable`. They render different columns and different row data,
but structurally they're the same concept: rows, columns, sorting. Don't let one feature import the other's table —
promote a generic, config-driven table shell to `shared/`:

```ts
// shared/components/data-table/data-table.ts
@Component({
    selector: 'app-data-table',
    standalone: true,
    imports: [NgTemplateOutlet],
    template: `
    <table>
      <tr *ngFor="let row of rows">
        <td *ngFor="let col of columns">
          <ng-container
            [ngTemplateOutlet]="col.cellTemplate ?? defaultCell"
            [ngTemplateOutletContext]="{ $implicit: row, col }">
          </ng-container>
        </td>
      </tr>
    </table>
  `
})
export class DataTable<T> {
    @Input() rows: T[] = [];
    @Input() columns: ColumnDef<T>[] = [];
}
```

Each feature supplies its own column config, with its own domain-specific formatting and cell templates, without the
shared table ever knowing what a `Bill` or a `Visit` is:

```ts
// features/bills/pages/bills-list/bills-list.ts
columns: ColumnDef<Bill>[] = [
    {key: 'ownerName', header: 'Owner'},
    {key: 'status', header: 'Status', cellTemplate: this.statusCell},
];
```

```ts
// features/visits/pages/visit-list/visit-list.ts
columns: ColumnDef<Visit>[] = [
    {key: 'petName', header: 'Pet'},
    {key: 'date', header: 'Date'},
    {key: 'status', header: 'Status', cellTemplate: this.statusCell},
];
```

This scales to sorting/pagination/selection too — the shared table emits events, and each feature decides what those
events mean for its own data.

If instead a feature needs the actual table from another feature — say `vets` wants a "vet schedule" page that uses the
real `VisitListTable`, wired to the real `VisitsService`, filtered to one vet — promoting a generic shell doesn't help,
because there's no domain-agnostic version to extract; the page fundamentally is visits data.

The fix: put the page inside `visits`, not `vets`, since that's where the component and service it needs already live.

```
features/
└── visits/
    ├── pages/
    │   ├── visit-list/          (existing — all visits)
    │   └── vet-schedule/         (new — visits filtered by vet)
    ├── components/
    │   └── visit-list-table/     (already here — no import needed)
    └── services/
        └── visits.service.ts     (already here — no import needed)
```

vets reaches it by routing, not importing:

```html
<a routerLink="/visits/vet-schedule" [queryParams]="{ vetId: vet.id }">
    View schedule
</a>
```

No cross-feature import happens at all — vets just links to a URL that visits owns.

---

### 6b. A page aggregates multiple features (e.g. a customer detail page)

`features/customers/pages/customer-detail` needs to show an owner's profile, plus their recent visits and outstanding
bills. That's three domains on one screen.

Give the aggregating page its own feature-local components, and let it depend on the *services* of the other features —
never their pages or components:

```
features/
├── customers/
│   ├── customers.routes.ts
│   ├── pages/
│   │   └── customer-detail/
│   │       ├── customer-detail.ts      ← injects VisitsService, BillsService
│   │       └── customer-detail.html
│   └── components/
│       ├── recent-visits-panel/         ← customers' OWN component
│       └── outstanding-bills-panel/     ← customers' OWN component
├── visits/
│   └── services/visits.service.ts
└── bills/
    └── services/bills.service.ts
```

```ts
// features/customers/pages/customer-detail/customer-detail.ts
export class CustomerDetail {
    private visits = inject(VisitsService);   // ✅ service import — OK
    private bills = inject(BillsService);     // ✅ service import — OK

    recentVisits = toSignal(this.visits.getRecentForOwner(this.ownerId));
    outstandingBills = toSignal(this.bills.getUnpaidForOwner(this.ownerId));
}
```

```html
<!-- customer-detail.html -->
<app-recent-visits-panel [visits]="recentVisits()"/>
<app-outstanding-bills-panel [bills]="outstandingBills()"/>
```

Don't import `visit-card` from `visits` or `bills-list-table` from `bills`
here — build `recent-visits-panel` and `outstanding-bills-panel` as
`customers`' own components, styled for this context, fed by data from the injected services. If a panel turns out to be
visually identical to something already in `shared`, use that instead.

**One smell to watch for:** if a page ends up injecting five or six other features' services just to stitch together a
view, that's a sign the *data shape* itself should be aggregated one layer down — e.g. a `core` or
`shared/data-access` service that composes calls to the individual feature APIs and exposes a single `CustomerSummary`
shape. That keeps the page itself simple (one injected dependency) even though multiple domains feed it.

---

## 7. `environments/` — per-build configuration

`environments/` sits alongside `app/` and `testing/`, not inside either. It holds the one thing that's meant to differ
between builds: config values like the API base URL. It never holds application logic.

```
environments/
├── environment.ts               # production default — apiUrl: ''
└── environment.development.ts   # dev — apiUrl: 'http://localhost:4242'
```

**How it's wired in:** `angular.json`'s `development` build configuration swaps `environment.ts` out for
`environment.development.ts` via
`fileReplacements`. Since `ng serve`'s default configuration is
`development`, this happens automatically — no flags needed. To point at a different backend, either edit `apiUrl` in
`environment.development.ts`, or add a new `angular.json` configuration with its own `fileReplacements`
entry and matching `environment.<name>.ts`, then run
`ng serve --configuration <name>`.

---

## 8. `testing/` — shared test utilities

`testing/` sits alongside `app/`, not inside it. It holds things that exist purely to make `.spec.ts` files shorter and
more consistent — never application code, and never anything imported by the app itself at runtime.

```
testing/
├── mocks/          # e.g. mock-visits.service.ts, mock-auth.service.ts
├── fixtures/        # e.g. visit.fixture.ts, owner.fixture.ts (sample data)
├── builders/         # e.g. visit-builder.ts (fluent test-data builders)
└── test-utils.ts     # e.g. renderWithProviders(), a shared TestBed setup helper
```

**What belongs here:**

- Anything created *only* to be imported by `*.spec.ts` files, and reused by more than one of them. If a mock, fixture,
  or helper is only ever used by a single spec file, keep it next to that spec instead of promoting it — same "don't
  move it until it's actually shared" rule as `shared/`.
- Fakes/mocks for `core` and feature `services/`, so specs don't have to hand-roll a `MockVisitsService` in three
  different files:

```ts
// testing/mocks/mock-visits.service.ts
export class MockVisitsService {
    getRecentForOwner = jasmine.createSpy().and.returnValue(of([]));
    getById = jasmine.createSpy().and.returnValue(of(mockVisit));
}
```

```ts
// features/visits/pages/visit-detail/visit-detail.spec.ts
import {MockVisitsService} from '../../../../testing/mocks/mock-visits.service';

TestBed.configureTestingModule({
    providers: [{provide: VisitsService, useClass: MockVisitsService}],
});
```

- Sample domain data (fixtures) so tests across features aren't each inventing their own slightly-different fake `Visit`
  or `Bill`:

```ts
// testing/fixtures/visit.fixture.ts
export const mockVisit: Visit = {
    id: 'v1', petName: 'Rex', date: '2026-08-01', status: 'scheduled',
};
```

- Generic TestBed/harness setup that every spec repeats (e.g. a
  `renderWithProviders()` that wires up common providers/router stubs).

**What does not belong here:**

- Real application logic. `testing/` should never be imported by anything in `app/` — only by `*.spec.ts` files. If you
  find `app/` code importing from `testing/`, that's a sign a fake leaked into production code.
- Feature-specific test data that only one spec cares about — that stays local to the spec, not promoted to `testing/`.

Same promotion logic as `shared/components/`: a mock or fixture starts out living next to the one spec that needs it,
and only moves to `testing/`
once a second spec — in the same feature or a different one — needs the same thing.

---

## 9. Summary checklist

When adding something new, ask:

1. **Is it a route-level screen?** → `features/<x>/pages/<name>/`
2. **Is it UI-specific to one feature's domain, used only inside that feature?** → `features/<x>/components/<name>/`
3. **Is it UI with no domain knowledge, or needed by 2+ features?**
   → `shared/components/<name>/`
4. **Is it a single app-wide instance (auth, HTTP interceptor, global config)?** → `core/`
5. **Is it part of the app's skeleton (nav, header, footer)?**
   → `layout/`
6. **Does a page need data owned by another feature?** → inject that feature's `services/`, never import its `pages/`/
   `components/`
7. **Is it a mock, fixture, or test helper reused by more than one `.spec.ts` file?** →
   `testing/<mocks|fixtures|builders>/<name>`
8. **Is it a value that should differ per build (a host, a flag)?** → `environments/environment.*.ts`

And the hard rules:

- **Pages are never imported anywhere.**
- **Feature UI (`pages/`, `components/`) is never imported by another feature** — promote to `shared` instead, or
  aggregate via services.
- **Feature `services/`/`models/` can be imported across features**, but sparingly — if a page needs many of them, push
  the aggregation down into a shared/core data-access layer.
