# Internationalization (i18n) Usage & Integration Guide

This guide explains how translations work in the PetClinic frontend, how to use the `LanguageSwitcher`, and how to translate a component step by step. The app uses `i18next`, `react-i18next` and `i18next-http-backend`, and supports English (`en`) and French (`fr`).

---

## 1. Prerequisites

After pulling changes from Git, install any newly added packages:

```bash
git pull
npm install
```

The following packages must be present in `package.json`:

- `i18next`
- `react-i18next`
- `i18next-http-backend`

---

## 2. How It Works (Overview)

- **Config file:** `i18n.js` initializes i18next. It must be imported once in `src/main.tsx`:

  ```tsx
  // src/main.tsx
  import './i18n';
  ```

  Without this import, nothing is translated and the UI shows the raw keys.

- **Translation files** are plain JSON, loaded over HTTP at runtime from:

  ```
  petclinic-frontend/public/locales/{language}/{namespace}.json
  ```

  This matches the config in `i18n.js`:

  ```js
  backend: { loadPath: '/locales/{{lng}}/{{ns}}.json' }
  ```

- **Files must live in `public/`, not `src/`.** Vite only serves files at the site root from `public/`. If the JSON is in `src/locales`, the requests fail (404 or an HTML page instead of JSON).

- **Namespaces:** each JSON file is a *namespace*, roughly one per feature area. The namespaces registered in `i18n.js` are:

  `bills`, `carts`, `customers`, `faq`, `home`, `inventories`, `products`, `promos`, `users`, `veterinarians`, `visits`

  The default namespace is `customers`.

- **Fallback:** if a key is missing in French, English is shown (`fallbackLng: 'en'`).

### Folder structure

```
petclinic-frontend/
  public/
    locales/
      en/
        customers.json
        home.json
        ...one file per namespace
      fr/
        customers.json
        home.json
        ...same files as en
  src/
    i18n.js
    main.tsx
    shared/
      components/
        language/
          LanguageSwitcher.tsx
```

> Every file in `en/` must have a matching file with the **same name and the same keys** in `fr/`.

---

## 3. Using the Language Switcher

The `LanguageSwitcher` component lets users toggle between English (`EN`) and French (`FR`).

- **Component path:** `src/shared/components/language/LanguageSwitcher.tsx`

### Import and render

```tsx
import { LanguageSwitcher } from '@/shared/components/language/LanguageSwitcher';

export function Header() {
  return (
    <header className="d-flex justify-content-between align-items-center p-3">
      <h1>Pet Clinic</h1>
      <LanguageSwitcher />
    </header>
  );
}
```

The navbar already renders it, so most pages don't need to add it again.

---

## 4. Translating Text in a Component

### Step 1: Pick the namespace

Choose the namespace that fits your feature (e.g. `customers`, `bills`, `visits`). If none fits, see [section 6](#6-adding-a-new-namespace).

### Step 2: Add the keys to both JSON files

**`public/locales/en/customers.json`**

```json
{
  "title": "Customer Details",
  "subtitle": "Manage your customer profile and registered pets.",
  "buttons": {
    "save": "Save Changes",
    "cancel": "Cancel"
  }
}
```

**`public/locales/fr/customers.json`**

```json
{
  "title": "Détails du client",
  "subtitle": "Gérez votre profil et vos animaux enregistrés.",
  "buttons": {
    "save": "Enregistrer les modifications",
    "cancel": "Annuler"
  }
}
```

### Step 3: Use the hook in your component

Pass the namespace to `useTranslation`, then replace hard-coded text with `t('key')`:

```tsx
import { useTranslation } from 'react-i18next';

export function CustomerDetails(): React.JSX.Element {
  const { t } = useTranslation('customers');

  return (
    <div>
      <h2>{t('title')}</h2>
      <p>{t('subtitle')}</p>
      <button type="button">{t('buttons.save')}</button>
      <button type="button">{t('buttons.cancel')}</button>
    </div>
  );
}
```

### Key rules

- **Nested keys use dot notation.** If the JSON has `"buttons": { "save": "..." }`, the key is `t('buttons.save')`, not `t('saveBtn')`. A key that doesn't exist renders as the literal key text.
- **Call `useTranslation('namespace')` with the right namespace.** Plain `useTranslation()` uses the default namespace (`customers`), so keys stored in other files won't be found.
- **Use a different namespace for a single key** without changing the hook:

  ```tsx
  t('nav.home', { ns: 'home' });
  // or
  t('home:nav.home');
  ```

- **Use the same key names in `en` and `fr`.** Only the values differ.

---

## 5. Variables and Plurals

### Variables

```json
{ "welcome": "Welcome, {{name}}!" }
```

```tsx
t('welcome', { name: user.username });
```

### Plurals

Use `_one` and `_other` suffixes and pass `count`:

**en**

```json
{
  "items_one": "Cart has {{count}} item",
  "items_other": "Cart has {{count}} items"
}
```

**fr**

```json
{
  "items_one": "Le panier contient {{count}} article",
  "items_other": "Le panier contient {{count}} articles"
}
```

```tsx
t('items', { count: cartCount });
```

i18next chooses the right form automatically for each language.

### Attributes and non-visible text

Translate `title`, `placeholder`, `aria-label` and `alt` text too, not only visible text:

```tsx
<input placeholder={t('search.placeholder')} />
<FaShoppingCart aria-label={t('nav.cart.icon')} />
```

---

## 6. Adding a New Namespace

Use this when a feature area has no JSON file yet (for example `bills`).

1. Create `public/locales/en/bills.json` and `public/locales/fr/bills.json` with the same keys.
2. Make sure the namespace is listed in the `ns` array in `i18n.js`. The namespaces listed in section 2 are already registered, so this only matters for a brand-new name.
3. Use it in your component with `useTranslation('bills')`.

> i18next requests **every** namespace listed in `ns` at startup. A namespace with no JSON file causes a 404 in the browser console. It is harmless, but create the `en` and `fr` files as you migrate each area to keep the console clean.

---

## 7. Testing

1. Start the dev server (restart it if you just created `public/locales`):

   ```bash
   npm run dev
   ```

2. Check that the files are served. Open (use your actual port):

   ```
   http://localhost:5173/locales/en/customers.json
   http://localhost:5173/locales/fr/customers.json
   ```

   You should see raw JSON. If you see the app's HTML page instead, the files are in the wrong folder or the server needs a restart.

3. Open the test page at `/i18n-test`. You should see "Customer Details" with **Save Changes** and **Cancel** buttons.

4. Click the language switcher (`FR`). The text should change to "Détails du client" with **Enregistrer les modifications** and **Annuler**, and the Network tab should show `/locales/fr/customers.json` returning `200`.

5. Navbar keys (`nav.*`) come from the `home` namespace. Some links only appear for certain roles, so log in as `ADMIN` to check all of them.

---

## 8. Troubleshooting

| Symptom | Likely cause | Fix |
| --- | --- | --- |
| Screen shows raw keys like `saveBtn` or `nav.home` | Key doesn't exist in the JSON, wrong namespace, or the JSON isn't loading | Compare the key to the JSON (nested keys need dots), check `useTranslation('namespace')`, and open the JSON URL in the browser |
| Nothing translates anywhere | `i18n.js` isn't imported | Add `import './i18n';` to `src/main.tsx` |
| `404` for `/locales/...` or JSON parse error (`Unexpected token <`) | Files are in `src/locales` instead of `public/locales` | Move them to `public/locales` and restart the dev server |
| French text is missing but English shows | Key missing in the `fr` file (fallback is `en`) | Add the key to `public/locales/fr/<namespace>.json` |
| Console `404` for a namespace | Namespace is in `ns` but has no JSON file | Create the `en` and `fr` files for it |
| Brief flash of raw keys on first load | Translations load over HTTP and `useSuspense` is `false` | Optional: `const { t, ready } = useTranslation('ns'); if (!ready) return null;` |
| Keys still old after editing JSON | Browser cached the old file | Hard refresh (Ctrl+Shift+R) |

---

## 9. Summary Checklist for Developers

1. [ ] `npm install` after pulling.
2. [ ] `import './i18n';` exists in `src/main.tsx`.
3. [ ] Translation files are in `public/locales/en/` and `public/locales/fr/` (not `src/`).
4. [ ] Choose the namespace and call `useTranslation('namespace')` in your component.
5. [ ] Replace static text with `{t('key')}`, using dot notation for nested keys.
6. [ ] Add each key to **both** `en/<namespace>.json` and `fr/<namespace>.json` with identical key names.
7. [ ] Translate `title`, `placeholder`, `aria-label` and `alt` text as well.
8. [ ] Render `<LanguageSwitcher />` only if the page has no navbar that already includes it.
9. [ ] Test in the browser: switch to `FR` and confirm the text changes with no raw keys and no red 404s in the Network tab.
