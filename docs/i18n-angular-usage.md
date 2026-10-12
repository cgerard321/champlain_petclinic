# Internationalization (i18n) Usage Guide — Employee Portal

This guide explains how translations work in the employee portal, how to use the language switcher, and how to translate a component step by step. The app uses `@angular/localize` and supports French (`fr`) and English (`en`). French is the **source locale**. The customer portal uses a different library; for that one see the [i18n Integration Guide](i18n_integration_guide.md).

---

## 1. Prerequisites

After pulling changes from Git, install any newly added packages:

```bash
git pull
npm install
```

`@angular/localize` must be present in `employee-frontend/package.json`, and `angular.json` must declare both of these on the `build` target:

```json
"i18n": { "sourceLocale": "fr" },
"polyfills": ["@angular/localize/init"]
```

Without the polyfill, `$localize` never exists at runtime and the app fails to start.

---

## 2. How It Works (Overview)

**The important difference from the customer portal:** there, English is one JSON file and French is another. Here, **French is the source locale.** The French text is written directly in the templates, and only English lives in a catalogue. There is no `fr.json`, and there should never be one.

- **Message ids:** every translatable element carries an `i18n="@@some.id"` attribute. Ids are flat: `settings.security.title` is a single string, and the dots are a reading convention, never a path — Angular does not split them. The convention is `module.key`.

- **The catalogue** is a plain JSON file, loaded over HTTP at runtime from:

  ```
  employee-frontend/public/i18n/en.json
  ```

  Its entries are **grouped by feature**, nested as deeply as their ids require:

  ```json
  {
    "locale": "en",
    "translations": {
      "footer": {
        "brand": "PetClinic Employee Portal"
      },
      "settings": {
        "title": "Settings",
        "security": { "title": "Security" }
      }
    }
  }
  ```

- **The grouping is flattened before Angular sees it.** `loadTranslations()` reads only the first level of the object and calls `.split()` on every value, so a nested catalogue makes it throw on its first key and install nothing at all. `flattenTranslations()` in `app.config.ts` walks the tree first and rebuilds the dotted ids: `{ settings: { security: { title: 'Security' } } }` becomes `{ 'settings.security.title': 'Security' }`.

  Grouping is what keeps the file readable as it grows, and keeps two teams off the same lines in a merge. Flattening is what makes it usable.

  > **The nesting path must reproduce the id exactly.** `i18n="@@settings.security.title"` needs `settings` → `security` → `title`: same words, same order, same number of levels. Rename a group and the id it produces no longer matches anything — silently, as always.

- **When it loads:** `loadActiveTranslations()` in `src/app/app.config.ts`, registered through `provideAppInitializer`. Angular waits for it before bootstrapping the app.

- **Why it must finish before bootstrap:** the Angular compiler turns every `i18n` attribute into a `$localize` tagged template, evaluated the first time its component renders. `loadTranslations()` only affects calls evaluated *after* it, so anything rendered earlier stays French for the rest of the session.

- **Files must live in `public/`, not `src/`.** The dev server and nginx both serve `public/` at the site root, which is why the loader fetches the absolute path `/i18n/en.json`.

- **Fallback:** a missing id shows the French source text. No error, no warning, and no raw id on screen — which is quieter than the customer portal and easy to miss.

### Folder structure

```
employee-frontend/
  public/
    i18n/
      en.json            <- English only. There is no fr.json.
  src/
    app/
      app.config.ts      <- getSavedLang(), flattenTranslations(), loadActiveTranslations()
      layout/
        header/
          header.ts      <- switchLang()
          header.html    <- the FR / EN buttons
```

> Every id used in a template must exist in `en.json`. Nothing checks this for you.

---

## 3. Using the Language Switcher

The switcher lets users toggle between French (`FR`) and English (`EN`).

- **Component path:** `src/app/layout/header/header.ts` and `header.html`

The header already renders it, so pages that sit inside the shell don't need to add anything.

### What happens on a click

```ts
protected switchLang(lang: string): void {
  if (localStorage.getItem('lang') === lang) {
    return;
  }
  localStorage.setItem('lang', lang);
  location.reload();
}
```

The choice is persisted, then the page reloads. The reload is deliberate: it is the simplest way to guarantee the catalogue is installed before any component renders (see section 2). Clicking the language that is already active does nothing, so the user never gets a pointless flash.

`LOCALE_ID` is provided from the same stored value, so `DatePipe`, `CurrencyPipe` and `DecimalPipe` follow the chosen language too.

---

## 4. Translating Text in a Template

### Step 1: Pick the id

Use `module.key`. Prefixes already in use are `login.`, `footer.`, `settings.`, `notFound.` and `forbidden.`.

### Step 2: Write the French straight into the template

```html
<h2 i18n="@@vets.list.title">Liste des vétérinaires</h2>
```

For text that has no element of its own, wrap it in `<ng-container>`:

```html
<button mat-button>
  <ng-container i18n="@@vets.list.add">Ajouter un vétérinaire</ng-container>
</button>
```

### Step 3: Add the English to the catalogue

**`public/i18n/en.json`**

Add your entries inside their feature group, creating the group if it does not exist yet. The nesting has to spell out the id:

```json
{
  "locale": "en",
  "translations": {
    "vets": {
      "list": {
        "title": "Veterinarians",
        "add": "Add a veterinarian"
      }
    }
  }
}
```

Both ids above come out of `flattenTranslations()` as `vets.list.title` and `vets.list.add`, which is what the templates ask for. Never mix the two styles in the same file — write the groups, not the dotted keys.

### Key rules

- **Always write an explicit `@@id`.** A bare `i18n` attribute makes Angular generate a hash from the text, so the id changes the moment somebody edits the French — silently breaking the English.
- **Ids are global, so prefix them by module.** `vets.list.title`, not `title`.
- **The catalogue nesting must reproduce the id.** `vets.list.title` lives at `vets` → `list` → `title`. Reorganising the groups changes the ids they produce, so it breaks the translations even though no template changed.
- **Never create a `fr.json`.** French comes from the templates.
- **A missing id shows French, not an error.** If a string refuses to translate, that is the first thing to check.
- **Attributes need their own marker.** Translate `placeholder`, `aria-label`, `title` and `alt` too, not only visible text:

  ```html
  <input matInput placeholder="Rechercher" i18n-placeholder="@@vets.list.search" />
  <button mat-icon-button aria-label="Fermer" i18n-aria-label="@@common.close">
  ```

---

## 5. Translating Text Written in TypeScript

`i18n=` is a template attribute — it does nothing inside a `.ts` file. For strings built in code, use the `$localize` tagged template instead:

```ts
protected readonly navBarItems = [
  { label: $localize`:@@nav.home:Accueil`, route: '/home' },
  { label: $localize`:@@nav.vets:Vétérinaires`, route: '/vets' },
];
```

The id block is delimited by colons: `` $localize`:@@id:source text` ``. `$localize` is a global installed by the polyfill, so there is nothing to import.

> Keep these strings inside a class field or a method. A translatable string in a module-level `const` is evaluated when the file is first imported, which happens before the loader runs, so it would stay French no matter which language is active.

---

## 6. Regenerating the Catalogue

Angular can scan the templates and list every id it finds:

```bash
cd employee-frontend
npx ng extract-i18n --format=json --output-path=.tmp-i18n --out-file=extracted.json
```

The result reports how many messages were found and writes:

```json
{
  "locale": "fr",
  "translations": {
    "footer.brand": "Portail Employé Clinique Vétérinaire"
  }
}
```

Note the `"locale": "fr"` and the French values: extraction gives you the **source**, not the English file. Treat it as a checklist — compare it against `en.json`, copy over the ids you forgot, and fill in the English yourself.

> Never point `--out-file` at `en.json`. It would overwrite every English string with the French source. Extract to a scratch folder and delete it afterwards.

---

## 7. Testing

Specs run on Vitest. Install the catalogue yourself rather than relying on the loader:

```ts
import { clearTranslations, loadTranslations } from '@angular/localize';

describe('Footer', () => {
  afterEach(() => {
    clearTranslations();
  });

  it('renders the English text', async () => {
    loadTranslations({ 'footer.brand': 'PetClinic Employee Portal' });
    // render only after loading, never before
  });
});
```

* **`loadTranslations()`** takes a simple translations object, not an object with `{ locale, translations }`. If you read a real file, extract just the translations part first.
* **In a spec, write the ids out flat.** `loadTranslations({ 'settings.security.title': 'Security' })`, not the grouped shape of `en.json`. A spec installs the catalogue directly, so nothing flattens it for you. `flattenTranslations()` is covered on its own in `app.config.spec.ts`, including an integration case that feeds the loader a grouped catalogue and checks the English actually lands.
* **Call it before `TestBed.createComponent`.** Loading after the component renders has no effect.
* **`clearTranslations()` in `afterEach` is mandatory.** The catalogue is global memory, so translations loaded by one test leak into the next and test order becomes important.
* To test the loader itself instead of a component, mock `fetch` with `vi.stubGlobal`. See `src/app/app.config.spec.ts` for the success cases and failure paths.

```bash
npm test
```

---

## 8. Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| A string stays French after switching to `EN` | The id is missing from `en.json`, or its group nesting does not spell the id out | Add it, or fix the group names. Use the extraction in section 6 as a checklist |
| **Nothing** translates after the catalogue was reorganised, and the console shows `Could not load en translations` | A group was renamed, a level was added or removed, or the `flattenTranslations()` call was dropped from the loader. `loadTranslations()` throws on the first value that is not a string, so one bad group kills the whole catalogue | Make every nesting path spell its id out exactly, and check `loadTranslations(flattenTranslations(translations))` is still in `app.config.ts` |
| Nothing translates, `$localize is not defined` | The polyfill is not declared | Add `"polyfills": ["@angular/localize/init"]` to the `build` target in `angular.json` |
| `Cannot find name '$localize'` when compiling | The global is not typed. The polyfill makes it exist at runtime, but TypeScript needs to be told about it | Add `"@angular/localize"` to `types` in **both** `tsconfig.app.json` and `tsconfig.spec.json` |
| Part of the page is English and part French | Something rendered before the loader finished | Move the string out of module scope (section 5) |
| App starts in French, console shows `Could not load en translations` | `en.json` is missing or not valid JSON. nginx serves `index.html` for unknown paths, so this arrives as a parse error rather than a 404 | Confirm the file is in `public/i18n/` and open `/i18n/en.json` in the browser — you should see raw JSON, not the app |
| `Missing locale data for the locale "xx"` | `localStorage.lang` holds something other than `fr` or `en` | Clear the key. The stored value is not validated today |
| The English text changed by itself after somebody edited the French | The element uses `i18n` with no `@@id`, so the generated hash moved | Give it an explicit `@@id` |
| The language resets on every reload | `localStorage` is blocked or cleared by the browser | Check the browser settings, and that `switchLang` actually ran |
| A spec renders English when it should render French | An earlier test left the catalogue loaded | Add `clearTranslations()` in `afterEach` |

---

## 9. Summary Checklist for Developers

1. [ ] `npm install` after pulling.
2. [ ] `angular.json` has `i18n.sourceLocale: "fr"` and the `@angular/localize/init` polyfill.
3. [ ] Write the French straight into the template with an explicit `i18n="@@module.key"`.
4. [ ] Wrap bare text in `<ng-container i18n="...">` when it has no element of its own.
5. [ ] Mark attributes separately with `i18n-placeholder`, `i18n-aria-label`, `i18n-title`.
6. [ ] For strings in a `.ts` file use `` $localize`:@@id:texte` ``, keep them out of module scope, and make sure `types` lists `"@angular/localize"` in both `tsconfig.app.json` and `tsconfig.spec.json`.
7. [ ] Add every new id to `public/i18n/en.json`, inside its feature group, with the nesting spelling the id out. Never create a `fr.json`.
8. [ ] Run the extraction to a scratch folder to catch ids you missed, then delete the folder.
9. [ ] In specs, `loadTranslations()` before rendering and `clearTranslations()` in `afterEach`.
10. [ ] Test in the browser: switch to `EN`, reload the page, and confirm the choice sticks with no French left behind.
