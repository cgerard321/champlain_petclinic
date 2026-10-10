import { useCallback, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill';
import { getAllBillsStream } from '@/features/bills/api/getAllBillsStream';

export default function useGetAllBillsStream(): {
  bills: Bill[];
  loading: boolean;
  error: string | null;
  getBillsStream: (
    billId?: string,
    customerId?: string,
    customerFirstName?: string,
    customerLastName?: string,
    visitType?: string,
    vetId?: string,
    vetFirstName?: string,
    vetLastName?: string
  ) => Promise<Bill[]>;
} {
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getBillsStream = useCallback(
    async (
      billId?: string,
      customerId?: string,
      customerFirstName?: string,
      customerLastName?: string,
      visitType?: string,
      vetId?: string,
      vetFirstName?: string,
      vetLastName?: string
    ): Promise<Bill[]> => {
      setLoading(true);
      setError(null);

      try {
        const streamedBills = await getAllBillsStream(
          billId,
          customerId,
          customerFirstName,
          customerLastName,
          visitType,
          vetId,
          vetFirstName,
          vetLastName
        );

        setBills(streamedBills);

        return streamedBills;
      } catch (err) {
        console.error('Failed to stream bills:', err);

        setBills([]);
        setError('Failed to load bills.');

        throw err;
      } finally {
        setLoading(false);
      }
    },
    []
  );

  return {
    bills,
    loading,
    error,
    getBillsStream,
  };
}
