import { useState, ChangeEvent, FormEvent } from 'react';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel';
import { updateVet } from '@/features/veterinarians/api/updateVet';
import { Button, Modal, Form } from 'react-bootstrap';
import { Workday } from '@/features/veterinarians/models/Workday';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';

interface UpdateVetProps {
  vet: VetRequestModel;
  onClose: () => void;
}

export default function UpdateVet({
  vet,
  onClose,
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

  const handleSpecialtiesChange = (e: ChangeEvent<unknown>): void => {
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

  const navigate = useNavigate();

  const handleSubmit = async (e: FormEvent): Promise<void> => {
    e.preventDefault();
    if (!validate()) return;

    try {
      await updateVet(vet.vetId, formData);
      alert('Vet updated successfully!');
      onClose();

      navigate(AppRoutePaths.Vet);
      window.location.reload();
    } catch (error) {
      console.error('Error updating vet:', error);
    }
  };

  return (
    <Modal show={true} onHide={onClose} backdrop="static" keyboard={false}>
      <Modal.Header closeButton>
        <Modal.Title>Update Vet</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form onSubmit={handleSubmit}>
          <Form.Group className="mb-3">
            <Form.Label>First Name</Form.Label>
            <Form.Control
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

          <Form.Group className="mb-3">
            <Form.Label>Last Name</Form.Label>
            <Form.Control
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

          <Form.Group className="mb-3">
            <Form.Label>Email</Form.Label>
            <Form.Control
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

          <Form.Group className="mb-3">
            <Form.Label>Phone Number</Form.Label>
            <Form.Control
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

          <Form.Group className="mb-3">
            <Form.Label>Resume</Form.Label>
            <Form.Control
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

          <Form.Group className="mb-3">
            <Form.Label>Specialties</Form.Label>
            <Form.Control
              as="select"
              multiple
              name="specialties"
              value={formData.specialties.map(s => s.specialtyId)}
              onChange={handleSpecialtiesChange}
            >
              <option value="1">Surgery</option>
              <option value="2">Dentistry</option>
              <option value="3">Dermatology</option>
            </Form.Control>
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Work Hours</Form.Label>
            <Form.Control
              type="text"
              name="workHoursJson"
              value={formData.workHoursJson}
              onChange={handleChange}
            />
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Check
              type="checkbox"
              label="Active"
              name="active"
              checked={formData.active}
              onChange={handleCheckboxChange}
            />
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Work Days</Form.Label>
            {Object.values(Workday).map(day => (
              <Form.Check
                key={day}
                type="checkbox"
                label={day}
                name="workday"
                value={day}
                checked={formData.workday.includes(day)}
                onChange={handleWorkdayChange}
              />
            ))}
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Check
              type="checkbox"
              label="Photo Default"
              name="photoDefault"
              checked={formData.photoDefault}
              onChange={handleCheckboxChange}
            />
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
