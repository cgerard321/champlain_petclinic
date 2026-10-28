import { useState, useEffect } from 'react';
import { getAllVets } from '@/features/veterinarians/api/getAllVets';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import AddVet from '@/pages/Vet/AddVet.tsx';
import UploadVetPhoto from '@/pages/Vet/UploadVetPhoto.tsx';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import VetCardTable from '@/features/veterinarians/VetListCards';
import { IsInventoryManager, IsVet, IsAdmin } from '@/context/UserContext';

export default function Vet(): JSX.Element {
  const isInventoryManager = IsInventoryManager();
  const isVet = IsVet();
  const isAdmin = IsAdmin();
  const canSeeInactiveVets = isAdmin || isVet;
  const [searchQuery, setSearchQuery] = useState('');
  const [results, setResults] = useState<VetRequestModel[]>([]);
  const [allVets, setAllVets] = useState<VetRequestModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [formVisible, setFormVisible] = useState<boolean>(false);
  const [isSearchActive, setIsSearchActive] = useState<boolean>(false);

  const [loaded, setLoaded] = useState<boolean>(false);

  const fetchAllVets = async (): Promise<void> => {
    try {
      const response = await getAllVets();

      // Map VetResponseModel -> VetRequestModel since getAllVets returns ResponseModel but both the sets want RequestModel
      const vets: VetRequestModel[] = response.map(vet => ({
        vetId: vet.vetId,
        vetBillId: vet.vetBillId,
        firstName: vet.firstName,
        lastName: vet.lastName,
        email: vet.email,
        phoneNumber: vet.phoneNumber,
        resume: vet.resume ?? '',
        workday: vet.workday ?? [],
        workHoursJson: vet.workHoursJson ?? '',
        active: vet.active ?? false,
        specialties: vet.specialties ?? [],
        photoDefault: false,
        password: '',
        username: vet.username ?? '',
      }));

      const visibleVets = canSeeInactiveVets
        ? vets
        : vets.filter(vet => vet.active);
      setAllVets(visibleVets);
      setResults(visibleVets);
    } catch (err) {
      console.error('Error fetching all vets:', (err as Error)?.message ?? err);
      setError('Error fetching all vets.');
    } finally {
      setLoaded(true);
    }
  };

  useEffect(() => {
    fetchAllVets();
  }, [canSeeInactiveVets]);

  const handleSearch = (query: string): void => {
    const trimmed = query.trim();

    if (!trimmed) {
      setResults(allVets);
      setIsSearchActive(false);
      return;
    }

    const filteredVets = allVets.filter(vet =>
      `${vet.firstName} ${vet.lastName}`
        .toLowerCase()
        .includes(trimmed.toLowerCase())
    );

    setResults(filteredVets);
    setIsSearchActive(true);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    setSearchQuery(e.target.value);
    handleSearch(e.target.value);
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
          onChange={handleInputChange}
          placeholder="Search by first name, last name or both names"
          style={{ marginRight: '10px' }}
        />
        <button onClick={handleClear}>Clear</button>
      </div>

      {/* show error if present (after load) */}
      {loaded && error && <p style={{ color: 'red' }}>{error}</p>}

      {/* blank until initial load finishes; then render list or "No results" */}
      {!loaded ? null : results.length > 0 ? (
        <VetCardTable vets={results} onDeleteVet={setResults} />
      ) : (
        <p>No results found.</p>
      )}

      {!isSearchActive && !isInventoryManager && !isVet && (
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
