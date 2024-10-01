import {useNavigate, useParams} from "react-router-dom";
import React, {FormEvent, useEffect, useState} from "react";
import {BillRequestModel} from "@/features/bills/models/BillRequestModel.tsx";
import {getBill, updateBill} from "@/features/bills/api/updateBill.tsx";
import {Bill} from "@/features/bills/models/Bill.ts";
import {AppRoutePaths} from "@/shared/models/path.routes.ts";


const UpdateBillForm: React.FC = (): JSX.Element => {
    const { billId } = useParams<{billId: string}>();
    const navigate = useNavigate();
    const [bill, setBill] = useState<BillRequestModel>({
        customerId: '',
        ownerFirstName: '',
        ownerLastName: '',
        visitType: '',
        vetId: '',
        vetFirstName: '',
        vetLastName: '',
        date: '',
        amount: 0.0,
        taxedAmount: 0.0,
        billStatus: '',
        dueDate: ''
    });
    const [errors, setErrors] = useState<{ [key: string]: string }>({})

    useEffect(() => {
        const fetchBillData = async(): Promise<void> => {
            if(billId) {
                try {
                    const response = await getBill(billId);
                    const billData: Bill = response.data;
                    setBill({
                        customerId: billData.customerId,
                        ownerFirstName: billData.ownerFirstName,
                        ownerLastName: billData.ownerLastName,
                        visitType: billData.visitType,
                        vetId: billData.vetId,
                        vetFirstName: billData.vetFirstName,
                        vetLastName: billData.vetLastName,
                        date: billData.date,
                        amount: billData.amount,
                        taxedAmount: billData.taxedAmount,
                        billStatus: billData.billStatus,
                        dueDate: billData.dueDate
                    });
                } catch (error) {
                    console.error('Error fetching owner data: ', error);
                }
            }
            if (billId) {
                fetchBillData();
            }
        }
    }, [billId]);

    const handleChange = (
        e: React.ChangeEvent<HTMLInputElement>
    ): void => {
        const {name, value} = e.target;
        setBill({...bill, [name]: value});
    }

    const validate = (): boolean => {
        const newErrors: { [key: string]: string } = {};
        if(!bill.customerId) newErrors.customerId = 'Customer ID is required';
        if(!bill.ownerFirstName) newErrors.ownerFirstName = 'Owner first name is required';
        if(!bill.ownerLastName) newErrors.ownerLastName = 'Owner last name is required';
        if(!bill.visitType) newErrors.visitType = 'Visit type is required';
        if(!bill.vetId) newErrors.vetId = 'Vet ID is required';
        if(!bill.vetFirstName) newErrors.vetFirstName = 'Vet first name is required';
        if(!bill.vetLastName) newErrors.vetLastName = 'Vet last name is required';
        if(!bill.date) newErrors.date = 'Date is required';
        if(!bill.amount) newErrors.amount = 'Amount is required';
        if(!bill.taxedAmount) newErrors.taxedAmount = 'Taxed amount is required';
        if(!bill.billStatus) newErrors.billStatus = 'Bill status is required';
        if(!bill.dueDate) newErrors.dueDate = 'Due date is required';
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
                const response = await updateBill(billId, bill)
                if (response.status === 200) {
                    navigate(AppRoutePaths.AdminBills);
                }
            }
        } catch(error) {
            console.error('Error: ', error)
        }
    };

    return (
        <div className="update-customer-form">
            <h1>Edit Profile</h1>
            <form onSubmit={handleSubmit}>
                <label>First Name: </label>
                <input
                    type="text"
                    name="firstName"
                    value={bill.ownerFirstName}
                    onChange={handleChange}
                />
                {errors.ownerFirstName && <span className="error">{errors.firstName}</span>}
                <br />
                <label>Last Name: </label>
                <input
                    type="text"
                    name="lastName"
                    value={bill.ownerLastName}
                    onChange={handleChange}
                />
                {errors.lastName && <span className="error">{errors.lastName}</span>}
                <br />
                <label>Address: </label>
                <input
                    type="text"
                    name="address"
                    value={bill.visitType}
                    onChange={handleChange}
                />
                {errors.address && <span className="error">{errors.address}</span>}
                <br />
                <label>City: </label>
                <input
                    type="text"
                    name="city"
                    value={bill.vetFirstName}
                    onChange={handleChange}
                />
                {errors.city && <span className="error">{errors.city}</span>}
                <br />
                <label>Province: </label>
                <input
                    type="text"
                    name="province"
                    value={bill.vetLastName}
                    onChange={handleChange}
                />
                {errors.province && <span className="error">{errors.province}</span>}
                <br />
                <label>Telephone: </label>
                <input
                    type="text"
                    name="telephone"
                    value={bill.dueDate}
                    onChange={handleChange}
                />
                {errors.telephone && <span className="error">{errors.telephone}</span>}
                <br />
                <button type="submit">Update</button>
            </form>
        </div>
    );
};

export default UpdateBillForm;
