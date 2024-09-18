import * as React from 'react';
import { FormEvent, useState, ChangeEvent } from 'react';
import { addVet } from '@/features/veterinarians/api/addVet.ts';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { Button, Modal, Form } from 'react-bootstrap';
import { Workday } from '@/features/veterinarians/models/Workday.ts';


const AddVet: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const [vet, setVet] = useState<VetRequestModel>({
    vetId: '',
    vetBillId: '',
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    resume: '',
    workday: [],
    workHoursJson: '',
    active: false,
    specialties: [],
    photoDefault: false,
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [show, setShow] = useState(false);

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    try {
      const response = await addVet(vet);
      if (response.status === 201) {
        handleClose();
        navigate(AppRoutePaths.Vet);
      } else {
        console.error('Failed to add vet');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  const handleChange = (
    e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ): void => {
    const { name, value } = e.target;
    setVet({ ...vet, [name]: value });
  };

  const handleCheckboxChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const { name, checked } = e.target;
    setVet({ ...vet, [name]: checked });
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!vet.firstName) newErrors.firstName = 'First name is required';
    if (!vet.lastName) newErrors.lastName = 'Last name is required';
    if (!vet.email) newErrors.email = 'Email is required';
    if (!vet.phoneNumber) newErrors.phoneNumber = 'Phone number is required';
    if (!vet.resume) newErrors.resume = 'Resume is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Modal control functions
  const handleClose = () => setShow(false);
  const handleShow = () => setShow(true);

  const handleWorkdayChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const { value, checked } = e.target;

    const selectedDay = value as Workday;

    if (checked) {
      setVet((prevVet) => ({
        ...prevVet,
        workday: [...prevVet.workday, selectedDay],
      }));
    } else {
      setVet((prevVet) => ({
        ...prevVet,
        workday: prevVet.workday.filter((day) => day !== selectedDay),
      }));
    }
  };

  const handleSpecialtiesChange = (e: ChangeEvent<any>): void => {
    const selectElement = e.target as HTMLSelectElement;
    const selectedOptions = Array.from(selectElement.selectedOptions);

    const selectedSpecialties = selectedOptions.map(option => ({
      specialtyId: option.value,
      name: option.textContent || ''
    }));

    setVet((prevVet) => ({
      ...prevVet,
      specialties: selectedSpecialties,
    }));
  };

  return (
    <>
      <Button variant="primary" onClick={handleShow}>
        Add Vet
      </Button>

      <Modal show={show} onHide={handleClose} backdrop="static" keyboard={false}>
        <Modal.Header closeButton>
          <Modal.Title>Add New Vet</Modal.Title>
        </Modal.Header>

        <Modal.Body>
          <Form id="addVetForm" onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>First Name</Form.Label>
              <Form.Control
                type="text"
                name="firstName"
                value={vet.firstName}
                onChange={handleChange}
                isInvalid={!!errors.firstName}
              />
              <Form.Control.Feedback type="invalid">
                {errors.firstName}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Specialties</Form.Label>
              <Form.Control
                as="select"
                multiple
                name="specialties"
                value={vet.specialties.map(s => s.specialtyId)}
                onChange={handleSpecialtiesChange}
              >
                <option value="1">Surgery</option>
                <option value="2">Dentistry</option>
                <option value="3">Dermatology</option>
              </Form.Control>
            </Form.Group>


            <Form.Group className="mb-3">
              <Form.Label>Work Days</Form.Label>
              {Object.values(Workday).map((day) => (
                <Form.Check
                  key={day}
                  type="checkbox"
                  label={day}
                  name="workday"
                  value={day}
                  checked={vet.workday.includes(day)}
                  onChange={handleWorkdayChange}
                />
              ))}
            </Form.Group>


            <Form.Group className="mb-3">
              <Form.Label>Last Name</Form.Label>
              <Form.Control
                type="text"
                name="lastName"
                value={vet.lastName}
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
                value={vet.email}
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
                value={vet.phoneNumber}
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
                value={vet.resume}
                onChange={handleChange}
                isInvalid={!!errors.resume}
              />
              <Form.Control.Feedback type="invalid">
                {errors.resume}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Work Hours JSON</Form.Label>
              <Form.Control
                type="text"
                name="workHoursJson"
                value={vet.workHoursJson}
                onChange={handleChange}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Check
                type="checkbox"
                label="Active"
                name="active"
                checked={vet.active}
                onChange={handleCheckboxChange}
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Check
                type="checkbox"
                label="Photo Default"
                name="photoDefault"
                checked={vet.photoDefault}
                onChange={handleCheckboxChange}
              />
            </Form.Group>
          </Form>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Close
          </Button>
          <Button form="addVetForm" variant="primary" type="submit">
            Add Vet
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
};

export default AddVet;
