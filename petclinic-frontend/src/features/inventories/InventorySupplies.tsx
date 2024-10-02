// import { useState, useEffect } from 'react';
// import { useParams } from 'react-router-dom';
// import axios from 'axios';
// import { SupplyModel } from '@/features/inventories/models/ProductModels/ProductModelINVT.ts';
// import './InventoriesListTable.css';
// import './InventoryProducts.css';
//
// const InventorySupplies: React.FC = () => {
//   const { inventoryName } = useParams<{ inventoryName: string }>();
//
//   // Declare state
//   const [supplies, setSupplies] = useState<SupplyModel[]>([]);
//   const [loading, setLoading] = useState<boolean>(true);
//   const [error, setError] = useState<string | null>(null);
//
//   // Fetch supplies from the backend
//   const fetchSupplies = async (): Promise<void> => {
//     setLoading(true);
//     setError(null);
//     try {
//       const response = await axios.get<SupplyModel[]>(
//         `http://localhost:8080/api/v2/gateway/inventories/${inventoryName}/supplies`
//       );
//       setSupplies(response.data);
//     } catch (err) {
//       setError('Failed to fetch supplies.');
//     } finally {
//       setLoading(false);
//     }
//   };
//
//   // useEffect with dependency
//   useEffect(() => {
//     if (inventoryName) {
//       fetchSupplies().catch(err => console.error(err));
//
//       const intervalId = setInterval(fetchSupplies, 5000); // Adjust the interval as needed
//
//       return () => clearInterval(intervalId);
//     }
//     // eslint-disable-next-line react-hooks/exhaustive-deps
//   }, [inventoryName]);
//
//   // Render loading, error, and supply table
//   if (loading) return <p>Loading supplies...</p>;
//   if (error) return <p>{error}</p>;
//
//   return (
//     <div className="inventory-supplies">
//       <h2 className="inventory-title">
//         Supplies in Inventory: <span>{inventoryName}</span>
//       </h2>
//       {supplies.length > 0 ? (
//         <table className="table table-striped">
//           <thead>
//             <tr>
//               <th>SupplyId</th>
//               <th>SupplyName</th>
//               <th>Description</th>
//               <th>Price</th>
//               <th>Quantity</th>
//               <th>Status</th>
//             </tr>
//           </thead>
//           <tbody>
//             {supplies.map((supply: SupplyModel) => (
//               <tr key={supply.supplyName}>
//                 <td>{supply.supplyName}</td>
//                 <td>{supply.supplyDescription}</td>
//                 <td>${supply.supplySalePrice}</td>
//                 <td>{supply.supplyQuantity}</td>
//                 <td>{supply.supplyStatus}</td>
//               </tr>
//             ))}
//           </tbody>
//         </table>
//       ) : (
//         <p>No supplies found for this inventory.</p>
//       )}
//     </div>
//   );
// };
//
// export default InventorySupplies;
