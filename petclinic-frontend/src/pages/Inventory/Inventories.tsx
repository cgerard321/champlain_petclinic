import { NavBar } from '@/layouts/AppNavBar.tsx';
// import { useState } from 'react';
import InventoriesListTable from '@/features/inventories/InventoriesListTable.tsx';
import inventoryPageStyles from './Inventories.module.css';
// import AddInventoryForm from "@/features/inventories/AddInventoryForm.tsx";
// import AddInventoryType from "@/features/inventories/AddInventoryType.tsx";

export default function Inventories(): JSX.Element {
  // const [isMenuOpen, setIsMenuOpen] = useState(false);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  // const toggleMenu = () => {
  //   setIsMenuOpen(prevState => !prevState);
  // };

  return (
    <div>
      <NavBar />
      <div id={inventoryPageStyles.titleSection}>
        <h1 id={inventoryPageStyles.pageTitle}>Inventories</h1>
        {/*<div id={inventoryPageStyles.menuSection}>*/}
        {/*  <p id={inventoryPageStyles.menuText}>Menu</p>*/}
        {/*  <svg*/}
        {/*    id={inventoryPageStyles.menuIcon}*/}
        {/*    // onClick={toggleMenu}*/}
        {/*    xmlns="http://www.w3.org/2000/svg"*/}
        {/*    width="16"*/}
        {/*    height="16"*/}
        {/*    fill="currentColor"*/}
        {/*    className="bi bi-list"*/}
        {/*    viewBox="0 0 16 16"*/}
        {/*  >*/}
        {/*    <path*/}
        {/*      fillRule="evenodd"*/}
        {/*      d="M2.5 12a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5m0-4a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5m0-4a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5"*/}
        {/*    />*/}
        {/*  </svg>*/}
        {/*</div>*/}
        {/*{isMenuOpen && (*/}
        {/*  <div id={inventoryPageStyles.dropdownMenu}>*/}
        {/*    <AddInventoryForm showAddInventoryForm={} handleInventoryClose={} refreshInventoryTypes={}*/}
        {/*  </div>*/}
        {/*)}*/}
      </div>
      <InventoriesListTable />
    </div>
  );
}
