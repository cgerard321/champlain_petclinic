import { describe, expect, it } from 'vitest';

import { localizedProductType } from './product-localization';

describe('localizedProductType', () => {
  it('returns the raw backend name when a product type has no localized label', () => {
    expect(localizedProductType('NEW_BACKEND_TYPE')).toBe('NEW_BACKEND_TYPE');
  });
});
