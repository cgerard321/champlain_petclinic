import { useEffect, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { getAddressSuggestions } from '../api/getAddressSuggestions';
import { AddressSuggestionModel } from '../models/AddressSuggestionModel';
import './AddressAutocomplete.css';

const MIN_CHARS = 3;
const DEBOUNCE_MS = 300;

interface AddressAutocomplete {
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onSelect: (suggestion: AddressSuggestionModel) => void;
}

const AddressAutocomplete = ({
  value,
  onChange,
  onSelect,
}: AddressAutocomplete): JSX.Element => {
  const { t } = useTranslation('customers');
  const [suggestions, setSuggestions] = useState<AddressSuggestionModel[]>([]);
  const [isOpen, setIsOpen] = useState(false);
  const timerRef = useRef<number>();
  const requestIdRef = useRef(0);

  useEffect(() => {
    return () => window.clearTimeout(timerRef.current);
  }, []);

  const handleInput = (e: React.ChangeEvent<HTMLInputElement>): void => {
    onChange(e);
    const query = e.target.value.trim();
    window.clearTimeout(timerRef.current);

    if (query.length < MIN_CHARS) {
      setSuggestions([]);
      setIsOpen(false);
      return;
    }

    timerRef.current = window.setTimeout(async () => {
      const requestId = ++requestIdRef.current;
      const results = await getAddressSuggestions(query);
      if (requestId === requestIdRef.current) {
        setSuggestions(results);
        setIsOpen(true);
      }
    }, DEBOUNCE_MS);
  };

  const handleSelect = (suggestion: AddressSuggestionModel): void => {
    onSelect(suggestion);
    setSuggestions([]);
    setIsOpen(false);
  };

  return (
    <div className="address-autocomplete">
      <input
        type="text"
        name="address"
        value={value}
        onChange={handleInput}
        onBlur={() => setIsOpen(false)}
        onKeyDown={e => e.key === 'Escape' && setIsOpen(false)}
        autoComplete="off"
      />
      {isOpen && (
        <ul className="address-suggestions" role="listbox">
          {suggestions.length === 0 ? (
            <li className="address-suggestion-empty">
              {t('customerForm.noAddressSuggestions')}
            </li>
          ) : (
            suggestions.map((s, index) => (
              <li
                key={`${s.address}-${s.city}-${index}`}
                role="option"
                aria-selected={false}
                onMouseDown={e => {
                  e.preventDefault();
                  handleSelect(s);
                }}
              >
                <strong>{s.address}</strong>
                <span>
                  {s.city}, {s.province}
                </span>
              </li>
            ))
          )}
        </ul>
      )}
    </div>
  );
};

export default AddressAutocomplete;
