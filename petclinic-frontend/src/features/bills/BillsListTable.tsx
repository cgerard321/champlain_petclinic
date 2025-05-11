import { useEffect, useState, useCallback } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { useUser } from '@/context/UserContext';
import { payBill } from '@/features/bills/api/payBill.tsx';
import './BillsListTable.css';
import axiosInstance from '@/shared/api/axiosInstance';

export default function BillsListTable(): JSX.Element {
  const { user } = useUser();
  const [bills, setBills] = useState<Bill[]>([]);
  const [filteredBills, setFilteredBills] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<string>('all');

  const fetchBills = useCallback(async (): Promise<void> => {
    if (!user.userId) return;

    try {
      const response = await axiosInstance.get(
        `/v2/gateway/customers/${user.userId}/bills`,
      {
        Headers: {
          Accept: 'text/event-stream',
        }
      });

        //TODO: delete once sure is working
      // const response = await fetch(
      //   `http://localhost:8080/api/v2/gateway/customers/${user.userId}/bills`,
      //   {
      //     headers: {
      //       Accept: 'text/event-stream',
      //     },
      //     credentials: 'include',
      //   }
      // );

      if (response.status != 200) {
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }

      const reader = response.body?.getReader();
      const decoder = new TextDecoder('utf-8');

      let done = false;
      const billsArray: Bill[] = [];

      while (!done) {
        const { value, done: streamDone } = (await reader?.read()) || {};
        done = streamDone ?? true;

        if (value) {
          const chunk = decoder.decode(value, { stream: true });
          const formattedChunks = chunk.trim().split(/\n\n/);

          formattedChunks.forEach(formattedChunk => {
            const cleanChunk = formattedChunk.trim().replace(/^data:\s*/, '');

            if (cleanChunk) {
              try {
                const newBill: Bill = JSON.parse(cleanChunk);
                billsArray.push(newBill);
              } catch (e) {
                console.error('Error parsing chunk:', e);
              }
            }
          });
        }
      }

      setBills(billsArray);
    } catch (err) {
      console.error('Error fetching bills:', err);
      setError('Failed to fetch bills');
    }
  }, [user.userId]);

  useEffect(() => {
    fetchBills();
  }, [fetchBills]);

  useEffect(() => {
    if (selectedStatus === 'all') {
      setFilteredBills(bills);
    } else {
      setFilteredBills(
        bills.filter(
          bill => bill.billStatus.toLowerCase() === selectedStatus.toLowerCase()
        )
      );
    }
  }, [selectedStatus, bills]);

  // Function to handle downloading the PDF for a bill
  const handleDownloadPdf = async (
    customerId: string,
    billId: string
  ): Promise<void> => {
    try {

      const response = await axiosInstance.get(
        `/v2/gateway/customers/${customerId}/bills/${billId}/pdf`,
      {
        Headers: {
          'Content-Type': 'application/pdf',
        }
      });
      // const response = await fetch(
      //   `http://localhost:8080/api/v2/gateway/customers/${customerId}/bills/${billId}/pdf`,
      //   {
      //     method: 'GET',
      //     headers: {
      //       'Content-Type': 'application/pdf',
      //     },
      //     credentials: 'include',
      //   }
      // );

      if (response.status != 200) {
        throw new Error('Failed to download PDF');
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `bill-${billId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Error downloading PDF:', error);
    }
  };

  const handlePayBillClick = async (billId: string): Promise<void> => {
    let cardNumber: string = '';
    let cvv: string = '';
    let expirationDate: string = '';
    let isExpired = true;

    while (isExpired) {
      while (
        !cardNumber ||
        cardNumber.length !== 16 ||
        isNaN(Number(cardNumber))
      ) {
        const inputCardNumber = window.prompt(
          'Enter your card number (16 digits):'
        );
        if (!inputCardNumber) {
          alert('Payment cancelled.');
          return;
        }
        if (inputCardNumber.length !== 16 || isNaN(Number(inputCardNumber))) {
          alert(
            'Invalid card number. Please enter a valid 16-digit card number.'
          );
        } else {
          cardNumber = inputCardNumber;
        }
      }

      while (!cvv || cvv.length !== 3 || isNaN(Number(cvv))) {
        const inputCvv = window.prompt('Enter your CVV (3 digits):');
        if (!inputCvv) {
          alert('Payment cancelled.');
          return;
        }
        if (inputCvv.length !== 3 || isNaN(Number(inputCvv))) {
          alert('Invalid CVV. Please enter a valid 3-digit CVV.');
        } else {
          cvv = inputCvv;
        }
      }

      const expirationDatePattern = /^(0[1-9]|1[0-2])\/\d{2}$/;
      while (!expirationDate || !expirationDatePattern.test(expirationDate)) {
        const inputExpirationDate = window.prompt(
          'Enter your expiration date (MM/YY):'
        );
        if (!inputExpirationDate) {
          alert('Payment cancelled.');
          return;
        }
        if (!expirationDatePattern.test(inputExpirationDate)) {
          alert(
            'Invalid expiration date. Please enter a valid date in MM/YY format.'
          );
        } else {
          expirationDate = inputExpirationDate;
        }
      }

      const [expMonth, expYear] = expirationDate.split('/');
      const currentDate = new Date();
      const expiryYearFull = `20${expYear}`;
      const expiryDate = new Date(Number(expiryYearFull), Number(expMonth) - 1);

      if (expiryDate < currentDate) {
        alert('Your card is expired. Please enter a new card.');
        cardNumber = '';
        cvv = '';
        expirationDate = '';
      } else {
        isExpired = false;
      }
    }

    try {
      await payBill(user.userId, billId, {
        cardNumber,
        cvv,
        expirationDate,
      });
      alert('Payment successful!');
      fetchBills();
    } catch (error) {
      alert('Payment failed');
      console.error('Payment error:', error);
    }
  };

  return (
    <div>
      {/* Dropdown to filter bills by status */}
      <div className="filterContainer">
        <label htmlFor="statusFilter">Filter by Status:</label>
        <select
          id="statusFilter"
          value={selectedStatus}
          onChange={e => setSelectedStatus(e.target.value)}
          style={{ width: '150px' }}
        >
          <option value="all">All</option>
          <option value="overdue">Overdue</option>
          <option value="paid">Paid</option>
          <option value="unpaid">Unpaid</option>
        </select>
      </div>

      {error ? (
        <p>{error}</p>
      ) : (
        <div className="billsListTableContainer">
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Bill ID</th>
                <th>Owner Name</th>
                <th>Visit Type</th>
                <th>Vet Name</th>
                <th>Date</th>
                <th>Amount</th>
                <th>Taxed Amount</th>
                <th>Status</th>
                <th>Due Date</th>
                <th>Time Remaining</th>
                <th>Download PDF</th>
                <th>Pay Bill</th>
              </tr>
            </thead>
            <tbody>
              {filteredBills.map(bill => (
                <tr key={bill.billId}>
                  <td>{bill.billId}</td>
                  <td>
                    {bill.ownerFirstName} {bill.ownerLastName}
                  </td>
                  <td>{bill.visitType}</td>
                  <td>
                    {bill.vetFirstName} {bill.vetLastName}
                  </td>
                  <td>{bill.date}</td>
                  <td>{bill.amount}</td>
                  <td>{bill.taxedAmount}</td>
                  <td>
                    {bill.billStatus === 'OVERDUE' ? (
                      <span style={{ color: 'red' }}>Overdue</span>
                    ) : bill.billStatus === 'PAID' ? (
                      <span style={{ color: 'green' }}>{bill.billStatus}</span>
                    ) : (
                      bill.billStatus
                    )}
                  </td>
                  <td>{bill.dueDate}</td>
                  <td>
                    {bill.billStatus === 'PAID' ? (
                      <span style={{ color: 'green' }}>This bill is paid</span>
                    ) : bill.timeRemaining === 0 ? (
                      <span style={{ color: 'red' }}>
                        0 days remaining to pay bill
                      </span>
                    ) : (
                      `${bill.timeRemaining} days remaining to pay bill`
                    )}
                  </td>
                  <td>
                    <button
                      onClick={() =>
                        handleDownloadPdf(user.userId, bill.billId)
                      }
                    >
                      Download PDF
                    </button>
                  </td>
                  <td>
                    {bill.billStatus !== 'PAID' && (
                      <button onClick={() => handlePayBillClick(bill.billId)}>
                        Pay Bill
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
