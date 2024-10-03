import {useNavigate, useParams} from "react-router-dom";
import React, {FormEvent, useEffect, useState} from "react";
import {BillRequestModel} from "@/features/bills/models/BillRequestModel.tsx";
import {getBill, updateBill} from "@/features/bills/api/updateBill.tsx";
import {Bill} from "@/features/bills/models/Bill.ts";
import {AppRoutePaths} from "@/shared/models/path.routes.ts";
import './UpdateBillForm.css'
import {getAllOwners} from "@/features/customers/api/getAllOwners.tsx";
import {getAllVets} from "@/features/veterinarians/api/getAllVets.tsx";
import {OwnerResponseModel} from "@/features/customers/models/OwnerResponseModel.ts";
import {VetResponseModel} from "@/features/veterinarians/models/VetResponseModel.ts";


const UpdateBillForm: React.FC = (): JSX.Element => {
    const { billId } = useParams<{billId: string}>();
    const navigate = useNavigate();
    const [formData, setFormData] = useState<BillRequestModel>({
        customerId: '',
        visitType: '',
        vetId: '',
        date: '',
        amount: 0.0,
        taxedAmount: 0.0,
        billStatus: '',
        dueDate: ''
    });
    const [errors, setErrors] = useState<{ [key: string]: string }>({})
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [owners, setOwners] = useState<OwnerResponseModel[]>([]);
    const [vets, setVets] = useState<VetResponseModel[]>([]);



    const fetchOwnersAndVets = async (): Promise<void> => {
        const ownersList = await getAllOwners();
        const vetsList = await getAllVets();
        setOwners(ownersList);
        setVets(vetsList);
    };


    useEffect(() => {
        const fetchBillData = async(): Promise<void> => {
            if(billId) {
                try {
                    const response = await getBill(billId);
                    const billData: Bill = response.data;
                    setFormData(billData);
                } catch (error) {
                    console.error('Error fetching bill data: ', error);
                }
            }
            if (billId) {
                fetchBillData();
            }
        }
    }, [billId]);


    useEffect(() => {
        fetchOwnersAndVets()
    }, []);




    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
    ): void => {
        setFormData(({
            ...formData,
            [e.target.name]: e.target.value,
        }))
    }

    const validate = (): boolean => {
        const newErrors: { [key: string]: string } = {};
        if(!formData.customerId) newErrors.customerId = 'Customer ID is required';
        if(!formData.visitType) newErrors.visitType = 'Visit type is required';
        if(!formData.vetId) newErrors.vetId = 'Vet ID is required';
        if(!formData.date) newErrors.date = 'Date is required';
        if(!formData.amount) newErrors.amount = 'Amount is required';
        if(!formData.taxedAmount) newErrors.taxedAmount = 'Taxed amount is required';
        if(!formData.billStatus) newErrors.billStatus = 'Bill status is required';
        if(!formData.dueDate) newErrors.dueDate = 'Due date is required';
        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };




    const handleSubmit = async (
        event: FormEvent<HTMLFormElement>
    ): Promise<void> => {
        event.preventDefault()
        if(!validate()) return;

        try {
            if(billId) {
                const response = await updateBill(billId, formData)
                setIsModalOpen(true)
                if (response.status === 200) {
                    navigate(AppRoutePaths.AdminBills);
                }
            }
        } catch(error) {
            console.error('Error: ', error)
        }
    };

    const closeModal = (): void => {
        setIsModalOpen(false)
        navigate(`/bills/admin}`)
    }

    return (
        <div>
            <form onSubmit={handleSubmit}>

                <label>Customer: </label>
                <select
                    value={formData.customerId}
                    onChange={e =>
                        setFormData({ ...formData, customerId: e.target.value })
                    }
                >
                    <option value="">Select Customer</option>
                    {owners.map(owner => (
                        <option key={owner.ownerId} value={owner.ownerId}>
                            {owner.firstName} {owner.lastName}
                        </option>
                    ))}
                </select>
                {errors.customerId && <span className="error">{errors.customerId}</span>}
                <label>Visit Type: </label>
                <input
                    type="text"
                    name="visitType"
                    value={formData.visitType}
                    onChange={handleChange}
                />
                {errors.visitType && <span className="error">{errors.visitType}</span>}
                <label>Vet ID: </label>
                <select
                    value={formData.vetId}
                    onChange={e =>
                    setFormData({ ...formData, vetId: e.target.value })
                }>
                <option value="">Select Vet</option>
                    {vets.map(vet => (
                        <option key={vet.vetId} value={vet.vetId}>
                            {vet.firstName} {vet.lastName}
                        </option>
                    ))}
                </select>
                {errors.vetId && <span className="error">{errors.vetId}</span>}
                <label>Date: </label>
                <input
                    type="text"
                    name="date"
                    value={formData.date}
                    onChange={handleChange}
                />
                {errors.date && <span className="error">{errors.date}</span>}
                <label>Amount: </label>
                <input
                    type="text"
                    name="amount"
                    value={formData.amount}
                    onChange={handleChange}
                />
                {errors.amount && <span className="error">{errors.amount}</span>}
                <label>Taxed Amount: </label>
                <input
                    type="text"
                    name="taxedAmount"
                    value={formData.taxedAmount}
                    onChange={handleChange}
                />
                {errors.taxedAmount && <span className="error">{errors.taxedAmount}</span>}
                <label>Bill Status: </label>
                <select
                    value={formData.billStatus}
                    onChange={e =>
                        setFormData({ ...formData, vetId: e.target.value })
                    }>
                    <option> PAID </option>
                    <option> UNPAID </option>
                    <option> OVERDUE </option>
                </select>
                {errors.billStatus && <span className="error">{errors.billStatus}</span>}
                <label>Due Date: </label>
                <input
                    type="text"
                    name="dueDate"
                    value={formData.dueDate}
                    onChange={handleChange}
                />
                {errors.dueDate && <span className="error">{errors.dueDate}</span>}

                <button type="submit">Update</button>
            </form>

            {isModalOpen && (
                <div className="admin-update-bill-modal">
                        <h2>Success!</h2>
                        <p>Bill has been successfully updated.</p>
                        <button onClick={closeModal}>Close</button>
                </div>
            )}
        </div>
    );
};

export default UpdateBillForm;
