/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_ENV: string;
  readonly VITE_BACKEND_URL: string;
  readonly VITE_BACKEND_URL_V2: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
