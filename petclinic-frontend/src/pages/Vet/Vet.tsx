import { useState, useEffect } from 'react';
import axios from 'axios';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import AddVet from '@/pages/Vet/AddVet.tsx';
import UploadVetPhoto from '@/pages/Vet/UploadVetPhoto.tsx';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import VetCardTable from '@/features/veterinarians/VetListCards';

export default function Vet(): JSX.Element {
  const [searchQuery, setSearchQuery] = useState('');
  const [results, setResults] = useState<VetRequestModel[]>([]);
  const [allVets, setAllVets] = useState<VetRequestModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [formVisible, setFormVisible] = useState<boolean>(false);
  const [isSearchActive, setIsSearchActive] = useState<boolean>(false);

  const fetchAllVets = async (): Promise<void> => {
    try {
      const response = await axios.get(
        'http://localhost:8080/api/gateway/vets'
      );
      if (response.status === 200) {
        setAllVets(response.data);
        setResults(response.data);
      }
    } catch (err) {
      console.error('Error fetching all vets:', err);
      setError('Error fetching all vets.');
    }
  };

  useEffect(() => {
    fetchAllVets();
  }, []);

  const handleSearch = (): void => {
    if (!searchQuery) {
      setResults(allVets);
      setIsSearchActive(false);
      return;
    }

    const filteredVets = allVets.filter(vet =>
      `${vet.firstName} ${vet.lastName}`
        .toLowerCase()
        .includes(searchQuery.toLowerCase())
    );

    setResults(filteredVets);
    setIsSearchActive(true);
  };

  const handleClear = (): void => {
    setSearchQuery('');
    setResults(allVets);
    setIsSearchActive(false);
  };

  return (
    <div style={{ padding: '20px' }}>
      <NavBar />
      <div style={{ marginBottom: '20px', textAlign: 'right' }}>
        <input
          type="text"
          value={searchQuery}
          onChange={e => setSearchQuery(e.target.value)}
          placeholder="Search by first name, last name or both names"
          style={{ marginRight: '10px' }}
        />
        <button onClick={handleSearch} style={{ marginRight: '5px' }}>
          Search
        </button>
        <button onClick={handleClear}>Clear</button>
      </div>
      {error && <p style={{ color: 'red' }}>{error}</p>}

      {results.length > 0 ? (
        <VetCardTable vets={results} onDeleteVet={setResults} />
      ) : (
        <p>No results found.</p>
      )}

      {!isSearchActive && (
        <div style={{ marginBottom: '20px', textAlign: 'right' }}>
          <button onClick={() => setFormVisible(prev => !prev)}>
            {formVisible ? 'Cancel' : 'Add Vet'}
          </button>
          {formVisible && <AddVet />}
          <UploadVetPhoto vets={allVets} />
        </div>
      )}
    </div>
  );
}
