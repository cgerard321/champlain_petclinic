import {useEffect, useState} from "react";
import {Bill} from "@/features/bills/models/Bill.ts";
import {getAllBills} from "@/features/bills/api/getAllBills.tsx";


export default function AdminBillsListTable(): JSX.Element {
    const [bills, setBills] = useState<Bill[]>([]);

    const fetchBills = async (): Promise<void> => {
        getAllBills()
            .then(r => setBills(r));
    };

    useEffect(() => {
        fetchBills()
    }, []);


    return (
        <div>
            {bills
                .filter(data => data != null)
                .map((bill: Bill) => (
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
                        </tr>
                        </thead>
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
                            <td>{bill.billStatus}</td>
                            <td>{bill.dueDate}</td>
                        </tr>
                    </table>
                ))
            }
        </div>
    )

}