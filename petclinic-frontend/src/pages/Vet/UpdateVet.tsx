import { useState, ChangeEvent, FormEvent } from 'react';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel';
import { updateVet } from '@/features/veterinarians/api/updateVet';
import { Button, Modal, Form } from 'react-bootstrap';
import { Workday } from '@/features/veterinarians/models/Workday';
import './UpdateVet.css';

interface UpdateVetProps {
  vet: VetRequestModel;
  onClose: () => void;
  refreshVetDetails: () => void;
}

export default function UpdateVet({
  vet,
  onClose,
  refreshVetDetails,
}: UpdateVetProps): JSX.Element {
  const [formData, setFormData] = useState<VetRequestModel>(vet);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  const handleChange = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleCheckboxChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const { name, checked } = e.target;
    setFormData({ ...formData, [name]: checked });
  };

  const handleWorkdayChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const { value, checked } = e.target;
    const selectedDay = value as Workday;

    if (checked) {
      setFormData(prev => ({
        ...prev,
        workday: [...prev.workday, selectedDay],
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        workday: prev.workday.filter(day => day !== selectedDay),
      }));
    }
  };

  const handleSpecialtiesChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ): void => {
    const selectElement = e.target as HTMLSelectElement;
    const selectedOptions = Array.from(selectElement.selectedOptions);

    const selectedSpecialties = selectedOptions.map(option => ({
      specialtyId: option.value,
      name: option.textContent || '',
    }));

    setFormData(prevVet => ({
      ...prevVet,
      specialties: selectedSpecialties,
    }));
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!formData.firstName) newErrors.firstName = 'First name is required';
    if (!formData.lastName) newErrors.lastName = 'Last name is required';
    if (!formData.email) newErrors.email = 'Email is required';
    if (!formData.phoneNumber)
      newErrors.phoneNumber = 'Phone number is required';
    if (!formData.resume) newErrors.resume = 'Resume is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent): Promise<void> => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await updateVet(vet.vetId, formData);
      alert('Vet updated successfully!');
      refreshVetDetails();
      onClose();
    } catch (error) {
      console.error('Error updating vet:', error);
    }
  };

  return (
    <Modal show={true} onHide={onClose} backdrop="static" keyboard={false}>
      <div className="modal-container">
        <Modal.Header closeButton>
          <Modal.Title className="modal-title">Update Vet</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form onSubmit={handleSubmit}>
            <Form.Group>
              <Form.Label>First Name</Form.Label>
              <Form.Control
                className="custom-form-control"
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                isInvalid={!!errors.firstName}
              />
              <Form.Control.Feedback type="invalid">
                {errors.firstName}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group>
              <Form.Label>Last Name</Form.Label>
              <Form.Control
                className="custom-form-control"
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                isInvalid={!!errors.lastName}
              />
              <Form.Control.Feedback type="invalid">
                {errors.lastName}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group>
              <Form.Label>Email</Form.Label>
              <Form.Control
                className="custom-form-control"
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                isInvalid={!!errors.email}
              />
              <Form.Control.Feedback type="invalid">
                {errors.email}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group>
              <Form.Label>Phone Number</Form.Label>
              <Form.Control
                className="custom-form-control"
                type="text"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                isInvalid={!!errors.phoneNumber}
              />
              <Form.Control.Feedback type="invalid">
                {errors.phoneNumber}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group>
              <Form.Label>Resume</Form.Label>
              <Form.Control
                className="custom-form-control"
                as="textarea"
                name="resume"
                value={formData.resume}
                onChange={handleChange}
                isInvalid={!!errors.resume}
              />
              <Form.Control.Feedback type="invalid">
                {errors.resume}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group>
              <Form.Label>Specialties</Form.Label>
              <Form.Select
                className="custom-form-control"
                as="select"
                multiple
                name="specialties"
                value={formData.specialties.map(s => s.specialtyId)}
                onChange={handleSpecialtiesChange}
              >
                <option value="1">Surgery</option>
                <option value="2">Dentistry</option>
                <option value="3">Dermatology</option>
              </Form.Select>
            </Form.Group>

            <Form.Group>
              <Form.Label>Work Hours</Form.Label>
              <Form.Control
                className="custom-form-control"
                type="text"
                name="workHoursJson"
                value={formData.workHoursJson}
                onChange={handleChange}
              />
            </Form.Group>

            <Form.Group className="custom-checkbox-aligned">
              <Form.Label className="custom-checkbox-label">Active</Form.Label>
              <Form.Check
                className="custom-form-check-input"
                type="checkbox"
                name="active"
                checked={formData.active}
                onChange={handleCheckboxChange}
              />
            </Form.Group>

            <Form.Group>
              <Form.Label>Work Days</Form.Label>
              {Object.values(Workday).map(day => (
                <div className="custom-checkbox-inline-group" key={day}>
                  <Form.Label className="custom-checkbox-label">
                    {day}
                  </Form.Label>
                  <Form.Check
                    className="custom-form-check-input"
                    name="workday"
                    value={day}
                    checked={formData.workday.includes(day)}
                    onChange={handleWorkdayChange}
                  />
                </div>
              ))}
            </Form.Group>

            <Form.Group className="custom-checkbox-aligned">
              <Form.Label className="custom-checkbox-label">
                Photo Default
              </Form.Label>
              <Form.Check
                className="custom-form-check-input"
                type="checkbox"
                name="photoDefault"
                checked={formData.photoDefault}
                onChange={handleCheckboxChange}
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer className="custom-modal-footer">
          <Button
            variant="secondary"
            className="custom-btn-secondary"
            onClick={onClose}
          >
            Close
          </Button>
          <Button
            variant="primary"
            className="custom-btn-primary-full"
            type="submit"
            onClick={handleSubmit}
          >
            Update Vet
          </Button>
        </Modal.Footer>
      </div>
    </Modal>
  );
}
