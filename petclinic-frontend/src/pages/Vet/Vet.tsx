import { NavBar } from '@/layouts/AppNavBar.tsx';
import AddVet from '@/pages/Vet/AddVet.tsx';
import { useState } from 'react';

export default function Vet(): JSX.Element {
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
