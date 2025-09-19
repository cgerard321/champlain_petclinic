import { useEffect, useState } from 'react';
import { useUser } from '@/context/UserContext';
import CountUp from 'react-countup';
import axiosInstance from '@/shared/api/axiosInstance';

export default function CurrentBalance(): JSX.Element {
  const { user } = useUser();
  const [currentBalance, setCurrentBalance] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user.userId) return;

    const fetchCurrentBalance = async (): Promise<void> => {
      try {
        /*
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/customers/${user.userId}/bills/current-balance`,
          {
            headers: {
              'Content-Type': 'application/json',
            },
            credentials: 'include',
          }
            */

        const response = await axiosInstance.get(
          `/customers/${user.userId}/bills/current-balance`,
          {
            withCredentials: true,
            headers: { 'Content-Type': 'application/json' },
          }
        );

        if (!response || response.status !== 200 || !response.data) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const balance = response.data;
        setCurrentBalance(balance);
      } catch (err) {
        console.error('Error fetching current balance:', err);
        setError('Failed to fetch current balance');
      }
    };

    fetchCurrentBalance();
  }, [user.userId]);

  return (
    <div
      style={{
        padding: '20px',
        borderRadius: '8px',
        boxShadow: '0px 4px 12px rgba(0, 0, 0, 0.1)',
        maxWidth: '300px',
        margin: 'auto',
        textAlign: 'center',
        backgroundColor: '#f7f7f7',
      }}
    >
      <h3>Current Balance</h3>
      {error ? (
        <p>{error}</p>
      ) : (
        <h1 style={{ fontSize: '2rem', color: '#4caf50' }}>
          {currentBalance !== null ? (
            <CountUp
              start={0}
              end={currentBalance}
              duration={2}
              prefix="$"
              decimals={2}
            />
          ) : (
            '$0.00'
          )}
        </h1>
      )}
    </div>
  );
}
