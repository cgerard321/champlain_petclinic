import {
  checkUsernameRules,
  isReserved,
  isSameAsCurrent,
  isValidUsername,
  matchesUsernameFormat,
  normalizeUsername,
  RESERVED_USERNAMES,
  suggestFromName,
  suggestUsernames,
  USERNAME_MAX_LENGTH,
  USERNAME_PATTERN,
  UsernameRuleId,
} from './username-rules';


const ACCEPTED_FORMATS = [
  'Admin',
  'Vet1',
  'Receptionist1',
  'InventoryManager',
  'dr_smith_2',
  'jean_dupont',
  'jeandupont',
  'jo123',
  'jo_vet',
  'marie_tremblay',
  'dr_tremblay',
  // Accepted because the rules judge the TRIMMED name (rule 7). Only a space inside the name
  // breaks the space rule, since trimming cannot fix that one.
  '  Vet1  ',
];

const REFUSED_FORMATS = [
  'a__b',
  '_ab',
  '1abc',
  'bob@x.com',
  'a b',
  'é_vet',
  'admin_',
  'a_',

  'jean.tremblay',
  'dr-smith_2',
];

function ruleFor(value: string, id: UsernameRuleId): boolean {
  const rule = checkUsernameRules(value).find((candidate) => candidate.id === id);

  if (!rule) {
    throw new Error(`No rule with id "${id}"`);
  }

  return rule.met;
}

describe('Username rules (VETS-CPC-2090)', () => {
  describe('matchesUsernameFormat', () => {
    // 1. POSITIVE - Every accepted name of the ticket's table passes.
    it.each(ACCEPTED_FORMATS)('accepts %s', (value) => {
      expect(matchesUsernameFormat(value)).toBe(true);
    });

    // 2. NEGATIVE - Every refused name of the ticket's table fails.
    it.each(REFUSED_FORMATS)('refuses %s', (value) => {
      expect(matchesUsernameFormat(value)).toBe(false);
    });

    // 3. NEGATIVE - Length boundaries (rule 1).
    it('refuses a name shorter than three characters', () => {
      expect(matchesUsernameFormat('ab')).toBe(false);
    });

    it('accepts a name of exactly thirty characters and refuses thirty-one', () => {
      // Arrange
      const thirty = 'a'.repeat(USERNAME_MAX_LENGTH);

      // Assert
      expect(matchesUsernameFormat(thirty)).toBe(true);
      expect(matchesUsernameFormat(`${thirty}b`)).toBe(false);
    });


    it.each([...ACCEPTED_FORMATS, ...REFUSED_FORMATS])(
      'keeps the checklist and the pattern in agreement for %s',
      (value) => {
        // Arrange
        const everyRuleMet = checkUsernameRules(value).every((rule) => rule.met);

        // Assert
        expect(everyRuleMet).toBe(matchesUsernameFormat(value));
      },
    );
  });

  // CRITERION 3 - The checklist has to say WHICH rule is broken, not just that one is.
  describe('checkUsernameRules', () => {
    it('reports five rules, always in the same order', () => {
      // Act
      const ids = checkUsernameRules('jean_dupont').map((rule) => rule.id);

      // Assert
      expect(ids).toEqual(['length', 'startsAlpha', 'charset', 'noSpaceNoAt', 'noTrailing']);
    });

    it('ticks nothing on an empty field', () => {
      // Assert
      expect(checkUsernameRules('').every((rule) => !rule.met)).toBe(true);
    });

    it('ticks every rule on a valid name', () => {
      // Assert
      expect(checkUsernameRules('jean_dupont').every((rule) => rule.met)).toBe(true);
    });

    it('breaks only the length rule on a short but otherwise clean name', () => {
      // Assert
      expect(ruleFor('ab', 'length')).toBe(false);
      expect(ruleFor('ab', 'startsAlpha')).toBe(true);
      expect(ruleFor('ab', 'charset')).toBe(true);
      expect(ruleFor('ab', 'noSpaceNoAt')).toBe(true);
      expect(ruleFor('ab', 'noTrailing')).toBe(true);
    });

    it('breaks only the first-letter rule on a name starting with a digit', () => {
      // Assert
      expect(ruleFor('1abc', 'startsAlpha')).toBe(false);
      expect(ruleFor('1abc', 'charset')).toBe(true);
      expect(ruleFor('1abc', 'length')).toBe(true);
    });

    it('breaks the character rule on a period and on a hyphen', () => {
      // Assert
      expect(ruleFor('jean.tremblay', 'charset')).toBe(false);
      expect(ruleFor('dr-smith_2', 'charset')).toBe(false);
    });

    it('breaks the character rule on an accented letter', () => {
      // Assert
      expect(ruleFor('é_vet', 'charset')).toBe(false);
    });

    it('breaks the space rule on an inner space and on an at sign', () => {
      // Assert
      expect(ruleFor('a b', 'noSpaceNoAt')).toBe(false);
      expect(ruleFor('bob@x.com', 'noSpaceNoAt')).toBe(false);
    });

    // Rule 5 is about a space INSIDE the name. Spaces at either end are rule 7's job, removed
    // on blur and on submit, so they must not light up the checklist as an error.
    it('does not break the space rule on a name merely padded with spaces', () => {
      // Assert
      expect(ruleFor('  Vet1  ', 'noSpaceNoAt')).toBe(true);
    });

    it('breaks the trailing rule on a trailing underscore and on two in a row', () => {
      // Assert
      expect(ruleFor('admin_', 'noTrailing')).toBe(false);
      expect(ruleFor('a__b', 'noTrailing')).toBe(false);
    });
  });

  // RULE 8 - Reserved names, shown as a message of their own.
  describe('isReserved', () => {
    it.each([...RESERVED_USERNAMES])('refuses the reserved name %s', (value) => {
      expect(isReserved(value)).toBe(true);
    });

    it('ignores the case', () => {
      // Assert
      expect(isReserved('Admin')).toBe(true);
      expect(isReserved('ROOT')).toBe(true);
      expect(isReserved('SyStEm')).toBe(true);
    });

    it('ignores surrounding spaces', () => {
      // Assert
      expect(isReserved('  admin  ')).toBe(true);
    });

    it('leaves a name that merely starts with a reserved word alone', () => {
      // Assert
      expect(isReserved('admin1')).toBe(false);
      expect(isReserved('administrator_2')).toBe(false);
    });
  });

  describe('isValidUsername', () => {
    it('accepts a well-formed name that is not reserved', () => {
      // Assert
      expect(isValidUsername('jean_dupont')).toBe(true);
    });

    // This is the contradiction in the ticket, settled here: "Admin" is a valid FORMAT but a
    // reserved name, so it can never be saved.
    it('refuses a well-formed name that is reserved', () => {
      // Assert
      expect(matchesUsernameFormat('Admin')).toBe(true);
      expect(isValidUsername('Admin')).toBe(false);
    });

    it('refuses a malformed name', () => {
      // Assert
      expect(isValidUsername('jean.tremblay')).toBe(false);
    });
  });

  // CRITERION 4 - Saving the name you already have is not a change.
  describe('isSameAsCurrent', () => {
    it('matches the same name', () => {
      expect(isSameAsCurrent('admin1', 'admin1')).toBe(true);
    });

    it('matches a different case', () => {
      expect(isSameAsCurrent('ADMIN1', 'admin1')).toBe(true);
    });

    it('matches once surrounding spaces are removed', () => {
      expect(isSameAsCurrent('  Vet1  ', 'Vet1')).toBe(true);
    });

    it('does not match a genuinely different name', () => {
      expect(isSameAsCurrent('vet2', 'Vet1')).toBe(false);
    });
  });

  // RULE 7 - Trimming happens on blur and on submit, so this helper only trims.
  describe('normalizeUsername', () => {
    it('removes leading and trailing spaces', () => {
      expect(normalizeUsername('  Vet1  ')).toBe('Vet1');
    });

    it('leaves an inner space in place, so the checklist can report it', () => {
      expect(normalizeUsername('  a b  ')).toBe('a b');
    });

    it('does not change the case', () => {
      expect(normalizeUsername('InventoryManager')).toBe('InventoryManager');
    });
  });

  // CRITERION 9 - Up to three corrected suggestions, all of which must themselves be valid.
  describe('suggestUsernames', () => {
    it.each(REFUSED_FORMATS)('only ever suggests valid names for %s', (value) => {
      // Act
      const suggestions = suggestUsernames(value);

      // Assert
      for (const suggestion of suggestions) {
        expect(isValidUsername(suggestion)).toBe(true);
        expect(USERNAME_PATTERN.test(suggestion)).toBe(true);
      }
    });

    it('returns at most three suggestions', () => {
      expect(suggestUsernames('jean.tremblay').length).toBeLessThanOrEqual(3);
    });

    it('honours a smaller maximum', () => {
      expect(suggestUsernames('jean.tremblay', 1)).toHaveLength(1);
    });

    it('turns a period into an underscore', () => {
      expect(suggestUsernames('jean.tremblay')).toContain('jean_tremblay');
    });

    it('turns a hyphen into an underscore', () => {
      expect(suggestUsernames('dr-smith')).toContain('dr_smith');
    });

    it('drops the accents', () => {
      expect(suggestUsernames('é_vet')).toContain('e_vet');
    });

    it('collapses a run of forbidden characters into a single underscore', () => {
      expect(suggestUsernames('a..b')).toContain('a_b');
    });

    it('drops whatever sits before the first letter', () => {
      expect(suggestUsernames('1abc')).toContain('abc');
    });

    it('never suggests a reserved name', () => {
      // Act
      const suggestions = suggestUsernames('admin.');

      // Assert
      expect(suggestions).not.toContain('admin');
      expect(suggestions.every((suggestion) => !isReserved(suggestion))).toBe(true);
    });

    it('returns nothing when there is no letter to build on', () => {
      // Assert
      expect(suggestUsernames('123')).toEqual([]);
      expect(suggestUsernames('   ')).toEqual([]);
      expect(suggestUsernames('')).toEqual([]);
    });

    it('returns no duplicates', () => {
      // Act
      const suggestions = suggestUsernames('jeandupont');

      // Assert
      expect(new Set(suggestions).size).toBe(suggestions.length);
    });

    it('keeps a suggestion within the maximum length', () => {
      // Act
      const suggestions = suggestUsernames(`${'a'.repeat(40)}.b`);

      // Assert
      for (const suggestion of suggestions) {
        expect(suggestion.length).toBeLessThanOrEqual(USERNAME_MAX_LENGTH);
      }
    });
  });

  // CRITERION 10 - Suggestions built from a vet's real name.
  describe('suggestFromName', () => {
    it('builds the expected names from a first and last name', () => {
      // Act
      const suggestions = suggestFromName('Jean', 'Dupont');

      // Assert
      expect(suggestions).toEqual(['jean_dupont', 'j_dupont', 'jean_d']);
    });

    it('drops the accents', () => {
      expect(suggestFromName('José', 'Gómez')).toContain('jose_gomez');
    });

    it('only ever returns valid names', () => {
      // Act
      const suggestions = suggestFromName('Jean-Luc', "O'Brien");

      // Assert
      expect(suggestions.length).toBeGreaterThan(0);
      for (const suggestion of suggestions) {
        expect(isValidUsername(suggestion)).toBe(true);
      }
    });

    it('returns nothing when either half is unusable', () => {
      // Assert
      expect(suggestFromName('', 'Dupont')).toEqual([]);
      expect(suggestFromName('Jean', '123')).toEqual([]);
    });
  });
});
