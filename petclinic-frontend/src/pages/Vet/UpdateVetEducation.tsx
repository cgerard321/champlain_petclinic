import { EducationRequestModel } from '@/features/veterinarians/models/EducationRequestModel';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { ChangeEvent, FormEvent, useState } from 'react';
import { Button, Modal, Form } from 'react-bootstrap';
import { updateVetEducation } from '@/features/veterinarians/api/updateVetEducation'; // Adjust the import path as necessary
import { useNavigate } from 'react-router-dom';

interface UpdateVetEducationProps {
  vetId: string;
  education: EducationRequestModel;
  educationId: string;
  onClose: () => void;
}

export default function UpdateVetEducation({
  vetId,
  education,
  educationId,
  onClose,
}: UpdateVetEducationProps): JSX.Element {
  const [formData, setFormData] = useState<EducationRequestModel>(education);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  const handleChange = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const dateFormat = (date: string | undefined): string => {
    if (!date) return '';
    const parsedDate = new Date(date);
    return parsedDate.toISOString().split('T')[0]; // Format the date as YYYY-MM-DD
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!formData.schoolName) newErrors.schoolName = 'School name is required';
    if (!formData.fieldOfStudy)
      newErrors.fieldOfStudy = 'Fields of Study is required';
    if (!formData.degree) newErrors.degree = 'Degree is required';
    if (!formData.startDate) newErrors.startDate = 'Start Date is required';
    if (!formData.endDate) newErrors.endDate = 'End Date is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };
  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent): Promise<void> => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await updateVetEducation(vetId, educationId, formData);
      onClose();
      navigate(AppRoutePaths.Vet); //Change to VetDetails after figuring out the issue.
      window.location.reload();
    } catch (e) {
      console.error('Error updating education:', e);
      setErrors({ ...errors, form: 'Update Failed' });
    }
  };
  return (
    <Modal show={true} onHide={onClose} backdrop="static" keyboard={false}>
      <Modal.Header closeButton>
        <Modal.Title>Update Education</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>School Name</Form.Label>
            <Form.Control
              type="text"
              name="schoolName"
              value={formData.schoolName}
              onChange={handleChange}
              isInvalid={!!errors.schoolName}
            />
            <Form.Control.Feedback type="invalid">
              {errors.schoolName}
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Field of study</Form.Label>
            <Form.Control
              type="text"
              name="fieldOfStudy"
              value={formData.fieldOfStudy}
              onChange={handleChange}
              isInvalid={!!errors.fieldOfStudy}
            />
            <Form.Control.Feedback type="invalid">
              {errors.fieldOfStudy}
            </Form.Control.Feedback>
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Degree</Form.Label>
            <Form.Control
              type="text"
              name="degree"
              value={formData.degree}
              onChange={handleChange}
              isInvalid={!!errors.degree}
            />
            <Form.Control.Feedback type="invalid">
              {errors.degree}
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Start Date</Form.Label>
            <Form.Control
              type="date"
              name="startDate"
              value={dateFormat(formData.startDate)}
              onChange={handleChange}
              isInvalid={!!errors.startDate}
            />
            <Form.Control.Feedback type="invalid">
              {errors.startDate}
            </Form.Control.Feedback>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>End Date</Form.Label>
            <Form.Control
              type="date"
              name="endDate"
              value={dateFormat(formData.endDate)}
              onChange={handleChange}
              isInvalid={!!errors.endDate}
            />
            <Form.Control.Feedback type="invalid">
              {errors.endDate}
            </Form.Control.Feedback>
          </Form.Group>
        </Form>
      </Modal.Body>
      <Modal.Footer>
        <Button variant="secondary" onClick={onClose}>
          Close
        </Button>
        <Button variant="primary" type="submit" onClick={handleSubmit}>
          Update Vet
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
