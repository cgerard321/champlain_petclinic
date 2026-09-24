# Internationalization (i18n) Usage & Integration Guide

This guide explains how to use the `LanguageSwitcher` component and how to add translations to your components using `react-i18next`.

---

## 1. Prerequisites (For Team Members)

When pulling changes from Git, make sure to install any newly added packages:

```bash
git pull
npm install
```

---

## 2. Using the Language Switcher Component

The `LanguageSwitcher` component allows users to toggle between English (`EN`) and French (`FR`).

* **Component Path:** `src/shared/components/language/LanguageSwitcher.tsx`

### How to Import & Render

In your component or layout file (e.g., Navbar, Header, Footer, or Page):

```tsx
import { LanguageSwitcher } from '@/shared/components/language/LanguageSwitcher';

export function Header() {
  return (
    <header className="flex justify-between items-center p-4">
      <h1>Pet Clinic</h1>
      {/* Add the LanguageSwitcher button here */}
      <LanguageSwitcher />
    </header>
  );
}
```

---

## 3. Translating Text in Your Components

To make text in your component translatable, follow these steps:

### Step A: Import `useTranslation`
Import the hook from `react-i18next`:

```tsx
import { useTranslation } from 'react-i18next';
```

### Step B: Initialize the Hook & Replace Text
Call `const { t } = useTranslation();` inside your component and replace hardcoded text with `t('yourTranslationKey')`.

#### Example:
```tsx
import React from 'react';
import { useTranslation } from 'react-i18next';

export function WelcomeBanner() {
  const { t } = useTranslation();

  return (
    <div className="banner">
      <h2>{t('welcomeMessage')}</h2>
      <button>{t('login')}</button>
    </div>
  );
}
```

---

## 4. Adding New Translation Keys

Whenever you add a new `t('key')` to a component, you must add the corresponding translation key-value pair to the dictionary files.

### Location of Translation Files
Depending on your project setup, locate your translation JSON files:
* **English:** `src/locales/en/translation.json` (or `public/locales/en/translation.json`)
* **French:** `src/locales/fr/translation.json` (or `public/locales/fr/translation.json`)

### Example:

**`en/translation.json`**
```json
{
  "welcomeMessage": "Welcome to the Pet Clinic",
  "login": "Log In",
  "signup": "Sign Up"
}
```

**`fr/translation.json`**
```json
{
  "welcomeMessage": "Bienvenue à la Clinique Vétérinaire",
  "login": "Se connecter",
  "signup": "S'inscrire"
}
```

---

## 5. Summary Checklist for Developers

1. [ ] Import `<LanguageSwitcher />` where language selection is needed.
2. [ ] Import `useTranslation` in any component containing display text.
3. [ ] Replace static text strings with `{t('keyName')}`.
4. [ ] Add `keyName` to both `en/translation.json` and `fr/translation.json`.
5. [ ] Verify that switching languages toggles the text correctly in the browser.