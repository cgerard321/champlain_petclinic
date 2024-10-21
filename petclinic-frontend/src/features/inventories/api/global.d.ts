declare global {
  interface GoogleTranslateOptions {
    pageLanguage: string;
    autoDisplay: boolean;
    includedLanguages: string;
    layout?: number;
  }

  interface TranslateElement {
    new (
      options: GoogleTranslateOptions,
      elementId: string
    ): TranslateElementInstance;
  }

  interface TranslateElementInstance {}

  interface GoogleTranslate {
    TranslateElement: TranslateElement;
    InlineLayout: {
      SIMPLE: number;
      HORIZONTAL: number;
      VERTICAL: number;
    };
  }

  interface Window {
    google: {
      translate: GoogleTranslate; // Google Translate API reference
    };
    googleTranslateElementInit: () => void; // Initialization function for Google Translate
  }
}

export {};
