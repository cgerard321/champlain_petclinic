// eslint-disable-next-line import/default
import React, { useState } from 'react';
import { Button, Modal, Form } from 'react-bootstrap';
import { addVetEducation } from '@/features/veterinarians/api/addVetEducation';

interface AddEducationProps {
  vetId: string;
  onClose: () => void;
}

const AddEducation: React.FC<AddEducationProps> = ({ vetId, onClose }) => {
  const [schoolName, setSchoolName] = useState('');
  const [degree, setDegree] = useState('');
  const [fieldOfStudy, setFieldOfStudy] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [show, setShow] = useState(false);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleShow = () => setShow(true);
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleClose = () => {
    setShow(false);
    onClose();
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!schoolName) newErrors.schoolName = 'School Name is required';
    if (!degree) newErrors.degree = 'Degree is required';
    if (!fieldOfStudy) newErrors.fieldOfStudy = 'Field of Study is required';
    if (!startDate) newErrors.startDate = 'Start Date is required';
    if (!endDate) newErrors.endDate = 'End Date is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    const educationData = {
      vetId,
      schoolName,
      degree,
      fieldOfStudy,
      startDate,
      endDate,
    };

    try {
      await addVetEducation(educationData);
      handleClose();
      window.location.reload();
    } catch (error) {
      console.error('Failed to add education:', error);
    }
  };

  return (
    <>
      <Button variant="primary" onClick={handleShow}>
        Add Education
      </Button>

      <Modal
        show={show}
        onHide={handleClose}
        backdrop="static"
        keyboard={false}
      >
        <Modal.Header closeButton>
          <Modal.Title>Add Education</Modal.Title>
        </Modal.Header>

        <Modal.Body>
          <Form id="addEducationForm" onSubmit={handleSubmit}>
            <Form.Group className="mb-3">
              <Form.Label>School Name</Form.Label>
              <Form.Control
                type="text"
                value={schoolName}
                onChange={e => setSchoolName(e.target.value)}
                isInvalid={!!errors.schoolName}
              />
              <Form.Control.Feedback type="invalid">
                {errors.schoolName}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Degree</Form.Label>
              <Form.Control
                type="text"
                value={degree}
                onChange={e => setDegree(e.target.value)}
                isInvalid={!!errors.degree}
              />
              <Form.Control.Feedback type="invalid">
                {errors.degree}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Field of Study</Form.Label>
              <Form.Control
                type="text"
                value={fieldOfStudy}
                onChange={e => setFieldOfStudy(e.target.value)}
                isInvalid={!!errors.fieldOfStudy}
              />
              <Form.Control.Feedback type="invalid">
                {errors.fieldOfStudy}
              </Form.Control.Feedback>
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Start Date</Form.Label>
              <Form.Control
                type="date"
                value={startDate}
                onChange={e => setStartDate(e.target.value)}
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
                value={endDate}
                onChange={e => setEndDate(e.target.value)}
                isInvalid={!!errors.endDate}
              />
              <Form.Control.Feedback type="invalid">
                {errors.endDate}
              </Form.Control.Feedback>
            </Form.Group>
          </Form>
        </Modal.Body>

        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Close
          </Button>
          <Button form="addEducationForm" variant="primary" type="submit">
            Add Education
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
};

export default AddEducation;
