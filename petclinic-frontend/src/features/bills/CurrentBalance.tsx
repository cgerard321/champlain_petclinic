import { useEffect, useState } from 'react';
import { useUser } from '@/context/UserContext';

export default function CurrentBalance(): JSX.Element {
  const { user } = useUser();
  const [currentBalance, setCurrentBalance] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user.userId) return;

    const fetchCurrentBalance = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/customers/${user.userId}/current-balance`,
          {
            headers: {
              'Content-Type': 'application/json',
            },
            credentials: 'include',
          }
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const balance = await response.json();
        setCurrentBalance(balance);
      } catch (err) {
        console.error('Error fetching current balance:', err);
        setError('Failed to fetch current balance');
      }
    };

    fetchCurrentBalance();
  }, [user.userId]);

  return (
    <div className="balance-container">
      <h3>Current Balance</h3>
      {error ? <p>{error}</p> : <p>${currentBalance?.toFixed(2) || '0.00'}</p>}
    </div>
  );
}
