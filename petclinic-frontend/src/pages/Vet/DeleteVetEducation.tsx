import React from 'react';
import { deleteVetEducation } from '@/features/veterinarians/api/deleteVetEducation.ts';

interface DeleteVetEducationProps {
    vetId: string;
    educationId: string;
    onEducationDeleted: (educationId: string) => void;
}

const DeleteVetEducation: React.FC<DeleteVetEducationProps> = ({
                                                                   vetId,
                                                                   educationId,
                                                                   onEducationDeleted,
                                                               }) => {
    const handleDeleteEducation = async (
        event: React.MouseEvent
    ): Promise<void> => {
        event.stopPropagation();
        const confirmed = window.confirm(
            'Are you sure you want to delete this education entry?'
        );
        if (!confirmed) return;

        try {
            await deleteVetEducation(vetId, educationId);

            // Call the callback function to update the parent component
            onEducationDeleted(educationId);

            alert('Education entry deleted successfully.');
        } catch (error) {
            console.error('Failed to delete education entry:', error);
            alert('Failed to delete education entry.');
        }
    };

    return (
        <button onClick={handleDeleteEducation} className="btn btn-danger">
            Delete Education
        </button>
    );
};

export default DeleteVetEducation;