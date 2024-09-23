import { NavBar } from '@/layouts/AppNavBar.tsx';
import InventoriesListTable from '@/features/inventories/InventoriesListTable.tsx';
import { useNavigate } from 'react-router-dom'; // Import useNavigate
import { AppRoutePaths } from '@/shared/models/path.routes'; // Import your route paths

export default function Inventories(): JSX.Element {
  const navigate = useNavigate(); // Get the navigate function

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleButtonClick = () => {
    navigate(AppRoutePaths.MockPage); // Navigate to MockPage
  };

  return (
    <div>
      <NavBar />
      <h1>InventoriesGG</h1>
      <InventoriesListTable />
      <button onClick={handleButtonClick}>Go to MockPage</button>
    </div>
  );
}
