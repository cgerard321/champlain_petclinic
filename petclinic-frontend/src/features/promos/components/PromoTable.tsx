import {useEffect, useState} from "react";
import {Promo} from '@/features/promos/models/PromoModel.tsx';
import {useNavigate} from 'react-router-dom';
import './PromoTable.css'

export default function PromoListTable(): JSX.Element {
    const [promos, setPromos] = useState<Promo[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const navigate = useNavigate();


    const fetchPromos = async (): Promise<void> => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/v2/gateway/promos`,
                {
                    headers: {
                        Accept: 'application/json',
                    },
                    credentials: 'include',
                }
            );

            if (!response.ok) {
                throw new Error(`Error: ${response.status} ${response.statusText}`);
            }

            const data = await response.json();
            setPromos(data);
        } catch (err) {
            console.error('Error fetching promos:', err);
            setError('Failed to fetch promos');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchPromos();
    }, []);

    const handleUpdate = (promoId: string) => {
        navigate(`/promos/${promoId}/edit`);
    };

    const handleDelete = async (promoCode: string) => {
        const confirmed = window.confirm("Are you sure you want to delete this promo?");

        if (confirmed) {
            try {
                const response = await fetch(`http://localhost:8080/api/v2/gateway/promos/${promoCode}`, {
                    method: 'DELETE',
                    credentials: 'include',
                });

                if (!response.ok) {
                    throw new Error(`Error deleting promo: ${response.status} ${response.statusText}`);
                }


                fetchPromos();

            } catch (err) {
                console.error('Error deleting promo:', err);
                setError('Failed to delete promo');
            }
        }
    };

    if (loading) {
        return <p>Loading promos...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <div className="container">
            <h2>Promo List</h2>
            <div className="actions">
                <button className="add-button" onClick={() => navigate('/promos/add')}>
                    Add
                </button>
            </div>
            <table>
                <thead>
                <tr>
                    <th>Name</th>
                    <th>Code</th>
                    <th>Discount (%)</th>
                    <th>Expiration Date</th>
                    <th>Active</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>
                {promos.map((promo) => (
                    <tr key={promo.code}>
                        <td>{promo.name}</td>
                        <td>{promo.code}</td>
                        <td>{promo.discount}</td>
                        <td>{new Date(promo.expirationDate).toLocaleDateString()}</td>
                        <td>{promo.active ? 'Yes' : 'No'}</td>
                        <td>
                            <button onClick={() => handleUpdate(promo.id)} className="update"
                                    style={{marginRight: '10px'}}>Update
                            </button>
                            <button onClick={() => handleDelete(promo.id)} className="delete">Delete</button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}
