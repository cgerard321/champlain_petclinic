import { useState } from 'react';
import axios from 'axios';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import AddVet from '@/pages/Vet/AddVet.tsx';
import { useState } from 'react';

// Define the interfaces for the DTOs
interface SpecialtyDTO {
    specialtyId: string;
    name: string;
}

interface VetResponseDTO {
    vetId: string;
    vetBillId: string;
    firstName: string;
    lastName: string;
    email: string;
    phoneNumber: string;
    resume: string;
    workday: Workday[];
    workHoursJson: string;
    active: boolean;
    specialties: SpecialtyDTO[];
}

enum Workday {
    Monday = 'Monday',
    Tuesday = 'Tuesday',
    Wednesday = 'Wednesday',
    Thursday = 'Thursday',
    Friday = 'Friday',
}

interface WorkHoursData {
    [day: string]: string[];
}

export default function Vet(): JSX.Element {
    const [searchQuery, setSearchQuery] = useState('');
    const [searchType, setSearchType] = useState<'firstName' | 'lastName'>(
        'firstName'
    );
    const [result, setResult] = useState<VetResponseDTO | null>(null);
    const [error, setError] = useState<string | null>(null);

    // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
    const handleSearch = async () => {
        try {
            setError(null);
            const response = await axios.get(
                `http://localhost:8080/api/gateway/vets/${searchType}/${searchQuery}`
            );

            if (response.status === 200) {
                const data: VetResponseDTO = response.data;
                setResult(data);
            } else if (response.status === 404) {
                setResult(null);
            }
        } catch (err: unknown) {
            console.error('Error during fetch:', err); // Log error for debugging
            setError('An error occurred while fetching the data.');
            setResult(null);
        }
    };

    const parseWorkHours = (workHoursJson: string): string => {
        try {
            const workHours: WorkHoursData = JSON.parse(workHoursJson);
            return Object.entries(workHours)
                .map(([day, hours]) => {
                    // Ensure hours is an array and format it
                    if (Array.isArray(hours)) {
                        return `${day}: ${hours.join(', ') || 'No data'}`;
                    }
                    return `${day}: No data`;
                })
                .join(' | ');
        } catch (error) {
            console.error('Error parsing work hours:', error);
            return 'No data';
        }
    };

    const renderSpecialties = (specialties: SpecialtyDTO[]): string => {
        if (specialties) {
            return specialties.map(sp => sp.name).join(', ') || 'None';
        }
        return 'None';
    };

    const renderWorkdays = (workdays: Workday[]): string => {
        if (workdays) {
            return workdays.join(', ') || 'None';
        }
        return 'None';
    };

    return (
        <div style={{ padding: '20px' }}>
            <NavBar />
            <div style={{ marginBottom: '20px', textAlign: 'right' }}>
                <input
                    type="text"
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    placeholder={`Search by ${searchType}`}
                    style={{ marginRight: '10px' }}
                />
                <select
                    value={searchType}
                    onChange={e =>
                        setSearchType(e.target.value as 'firstName' | 'lastName')
                    }
                    style={{ marginRight: '10px' }}
                >
                    <option value="firstName">First Name</option>
                    <option value="lastName">Last Name</option>
                </select>
                <button onClick={handleSearch}>Search</button>
            </div>
            {error && <p>{error}</p>}
            <div>
                {result ? (
                    <div>
                        <p>Vet ID: {result.vetId}</p>
                        <p>Vet Bill ID: {result.vetBillId}</p>
                        <p>First Name: {result.firstName}</p>
                        <p>Last Name: {result.lastName}</p>
                        <p>Email: {result.email}</p>
                        <p>Phone: {result.phoneNumber}</p>
                        <p>Resume: {result.resume}</p>
                        <p>Active: {result.active ? 'Yes' : 'No'}</p>
                        <p>Specialties: {renderSpecialties(result.specialties)}</p>
                        <p>Workdays: {renderWorkdays(result.workday)}</p>
                        <p>Work Hours: {parseWorkHours(result.workHoursJson)}</p>
                    </div>
                ) : (
                    <p>No results found.</p>
                )}
            </div>
        </div>
    );
  const [formVisible, setFormVisible] = useState(false);

  return (
    <div>
      <NavBar />
      <h1>Hello dear vets</h1>
      <button onClick={() => setFormVisible(prev => !prev)}>
        {formVisible ? 'Cancel' : 'Add Vet'}
      </button>
      {formVisible && <AddVet />}
    </div>
  );
}
