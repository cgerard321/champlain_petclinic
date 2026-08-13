import { useEffect, useState } from 'react';
import { useUser } from '@/context/UserContext';
import CountUp from 'react-countup';
import axiosInstance from '@/shared/api/axiosInstance';
import { Currency, convertCurrency } from './utils/convertCurrency';

interface CurrentBalanceProps {
  currency: Currency;
}

export default function CurrentBalance(
  props: CurrentBalanceProps
): JSX.Element {
  const { currency } = props;
  const { user } = useUser();
  const [currentBalance, setCurrentBalance] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCurrentBalance = async (): Promise<void> => {
      if (!user.userId) return;

      try {
        const response = await axiosInstance.get(
          `/customers/${user.userId}/bills/current-balance`,
          {
            headers: { 'Content-Type': 'application/json' },
            useV2: true,
          }
        );

        if (!response || response.status !== 200) {
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

    const handlePaymentSuccess = (): void => {
      setTimeout(() => {
        fetchCurrentBalance();
      }, 500);
    };

    window.addEventListener('paymentSuccess', handlePaymentSuccess);

    return () => {
      window.removeEventListener('paymentSuccess', handlePaymentSuccess);
    };
  }, [user.userId]);

  const displayBalance =
    currentBalance !== null
      ? convertCurrency(currentBalance, 'CAD', currency)
      : 0;

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
          <CountUp
            start={0}
            end={displayBalance}
            duration={2}
            prefix={currency === 'CAD' ? 'CAD $' : 'USD $'}
            decimals={2}
          />
        </h1>
      )}
    </div>
  );
}
