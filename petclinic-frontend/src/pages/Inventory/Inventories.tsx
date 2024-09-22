import { NavBar } from '@/layouts/AppNavBar.tsx';
import InventoriesListTable from '@/features/inventories/InventoriesListTable.tsx';
import { useState } from 'react';
import MockPage from '@/pages/Inventory/MockPage.tsx';

export default function Inventories(): JSX.Element {
  const [showMockPage, setShowMockPage] = useState(false);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleButtonClick = () => {
    setShowMockPage(prev => !prev);
  };

  return (
    <div>
      <NavBar />
      <h1>InventoriesCARLOS</h1>
      <InventoriesListTable />
      <button onClick={handleButtonClick}>
        {showMockPage ? 'Hide MockPage' : 'Show MockPage'}
      </button>
      {showMockPage && <MockPage />}
    </div>
  );
}
