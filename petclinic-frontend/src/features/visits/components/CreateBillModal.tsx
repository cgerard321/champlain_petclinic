import { useEffect, useState } from 'react';
import BasicModal from '@/shared/components/BasicModal';
import { BillRequestModel } from '@/features/bills/models/BillRequestModel';
import { addBill } from '@/features/bills/api/addBill';
import { getAllOwners } from '@/features/customers/api/getAllOwners';
import { getAllVets } from '@/features/veterinarians/api/getAllVets';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';

interface CreateBillModalProps {
  showButton: JSX.Element;
  visitId: string;
  vetId?: string;
  vetFirstName?: string;
  vetLastName?: string;
  ownerFirstName?: string;
  ownerLastName?: string;
  visitDate?: string; // ISO string
  onCreated?: () => void;
}

const basePrices: Record<string, number> = {
  CHECKUP: 50,
  VACCINE: 30,
  SURGERY: 500,
  DENTAL: 200,
  REGULAR: 60,
  EMERGENCY: 150,
};

export default function CreateBillModal({
  showButton,
  visitId,
  vetId,
  vetFirstName,
  vetLastName,
  ownerFirstName,
  ownerLastName,
  visitDate,
  onCreated,
}: CreateBillModalProps): JSX.Element {
  const [owners, setOwners] = useState<OwnerResponseModel[]>([]);
  const [vets, setVets] = useState<VetResponseModel[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [form, setForm] = useState<BillRequestModel>({
    customerId: '',
    vetId: vetId || '',
    date: visitDate ? visitDate.split('T')[0] : '',
    amount: 0,
    visitType: '',
    billStatus: 'UNPAID',
    dueDate: '',
  });

  useEffect(() => {
    const fetch = async (): Promise<void> => {
      try {
        const ownersList = await getAllOwners();
        setOwners(ownersList || []);
        const vetsList = await getAllVets();
        setVets(vetsList || []);

        // try to preselect owner by name if available
        if (ownerFirstName && ownerLastName && ownersList?.length) {
          const match = ownersList.find(
            o => o.firstName === ownerFirstName && o.lastName === ownerLastName
          );
          if (match) setForm(prev => ({ ...prev, customerId: match.ownerId }));
        }

        // try to preselect vet id if provided or by name
        if (!vetId && vetFirstName && vetLastName && vetsList?.length) {
          const matchV = vetsList.find(
            v => v.firstName === vetFirstName && v.lastName === vetLastName
          );
          if (matchV) setForm(prev => ({ ...prev, vetId: matchV.vetId }));
        } else if (vetId) {
          setForm(prev => ({ ...prev, vetId }));
        }

        // default due date to same day + 30
        if (visitDate) {
          const d = new Date(visitDate);
          const due = new Date(d);
          due.setDate(due.getDate() + 30);
          setForm(prev => ({
            ...prev,
            dueDate: due.toISOString().split('T')[0],
          }));
        }
      } catch (err) {
        setError('Failed to load owners or vets.');
      }
    };
    fetch();
  }, [
    ownerFirstName,
    ownerLastName,
    vetFirstName,
    vetId,
    vetLastName,
    visitDate,
  ]);

  const validate = (): boolean => {
    if (!form.customerId) {
      setError('Customer is required.');
      return false;
    }
    if (!form.vetId) {
      setError('Vet is required.');
      return false;
    }
    if (!form.visitType) {
      setError('Visit type is required.');
      return false;
    }
    if (!form.date) {
      setError('Date is required.');
      return false;
    }
    if (!form.dueDate) {
      setError('Due date is required.');
      return false;
    }
    if (!form.amount || form.amount <= 0) {
      setError('Amount must be greater than zero.');
      return false;
    }
    setError(null);
    return true;
  };

  const handleVisitTypeChange = (value: string): void => {
    const upper = value.toUpperCase();
    const base = basePrices[upper] ?? 0;
    setForm(prev => ({ ...prev, visitType: upper, amount: base }));
  };

  const handleSubmit = async (): Promise<void> => {
    if (!validate()) return;
    setIsSubmitting(true);
    try {
      const payload: BillRequestModel = {
        ...form,
        billStatus: form.billStatus ? form.billStatus : 'UNPAID',
      };
      await addBill(payload);
      onCreated?.();
      setError(null);
      setIsSubmitting(false);
    } catch (err) {
      setError('Failed to create bill. Please try again.');
      setIsSubmitting(false);
    }
  };

  return (
    <BasicModal
      title="Create New Bill"
      showButton={showButton}
      formId={`create-bill-form-${visitId}`}
      validate={validate}
      confirmText={isSubmitting ? 'Creating...' : 'Create Bill'}
      errorMessage={error || ''}
    >
      <form
        id={`create-bill-form-${visitId}`}
        onSubmit={e => {
          e.preventDefault();
          e.stopPropagation();
          void handleSubmit();
        }}
      >
        <div className="form-group">
          <label>Customer</label>
          <select
            value={form.customerId}
            onChange={e => setForm({ ...form, customerId: e.target.value })}
            className="form-control"
          >
            <option value="">Select Customer</option>
            {owners.map(o => (
              <option key={o.ownerId} value={o.ownerId}>
                {o.firstName} {o.lastName}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Vet</label>
          <select
            value={form.vetId}
            onChange={e => setForm({ ...form, vetId: e.target.value })}
            className="form-control"
          >
            <option value="">
              {vetFirstName && vetLastName
                ? `Select Vet (${vetFirstName} ${vetLastName})`
                : 'Select Vet'}
            </option>
            {vets.map(v => (
              <option key={v.vetId} value={v.vetId}>
                {v.firstName} {v.lastName}
              </option>
            ))}
          </select>
        </div>

        <div className="form-group">
          <label>Visit Type</label>
          <select
            value={form.visitType}
            onChange={e => handleVisitTypeChange(e.target.value)}
            className="form-control"
          >
            <option value="">Select Visit Type</option>
            <option value="CHECKUP">Check-Up</option>
            <option value="VACCINE">Vaccine</option>
            <option value="SURGERY">Surgery</option>
            <option value="DENTAL">Dental</option>
            <option value="REGULAR">Regular</option>
            <option value="EMERGENCY">Emergency</option>
          </select>
        </div>

        <div className="form-group">
          <label>Date</label>
          <input
            type="date"
            className="form-control"
            value={form.date}
            onChange={e => setForm({ ...form, date: e.target.value })}
          />
        </div>

        <div className="form-group">
          <label>Amount ($)</label>
          <input
            type="number"
            min="0"
            step="0.01"
            className="form-control"
            value={form.amount}
            onChange={e =>
              setForm({ ...form, amount: parseFloat(e.target.value || '0') })
            }
          />
        </div>

        <div className="form-group">
          <label>Status</label>
          <select
            value={form.billStatus}
            onChange={e => setForm({ ...form, billStatus: e.target.value })}
            className="form-control"
          >
            <option value="UNPAID">UNPAID</option>
            <option value="PAID">PAID</option>
            <option value="OVERDUE">OVERDUE</option>
          </select>
        </div>

        <div className="form-group">
          <label>Due Date</label>
          <input
            type="date"
            className="form-control"
            value={form.dueDate}
            onChange={e => setForm({ ...form, dueDate: e.target.value })}
          />
        </div>
      </form>
    </BasicModal>
  );
}
