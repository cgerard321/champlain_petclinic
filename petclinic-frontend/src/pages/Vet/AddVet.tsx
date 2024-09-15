import React, { FormEvent, useState } from 'react';
import { addVet } from '@/features/veterinarians/api/addVet.ts'; // Adjust import path as needed
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel.ts'; // Adjust import path as needed
import { Workday } from '@/features/veterinarians/models/Workday.ts'; // Adjust import path as needed

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
    active: true,
    specialties: [],
    photoDefault: false,
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [specialtyInput, setSpecialtyInput] = useState<{ specialtyId: string; name: string }>({ specialtyId: '', name: '' });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>): void => {
    const { name, value } = e.target;
    setVet(prev => ({ ...prev, [name]: value }));
  };


  const handleWorkdayChange = (e: React.ChangeEvent<HTMLSelectElement>): void => {
    const value = e.target.value as Workday;
    setVet(prev => ({
      ...prev,
      workday: prev.workday.includes(value) ? prev.workday.filter(day => day !== value) : [...prev.workday, value]
    }));
  };

  const handleSpecialtyChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    setSpecialtyInput({ ...specialtyInput, [name]: value });
  };

  const addSpecialty = (): void => {
    if (specialtyInput.specialtyId && specialtyInput.name) {
      setVet(prev => ({
        ...prev,
        specialties: [...prev.specialties, { ...specialtyInput }]
      }));
      setSpecialtyInput({ specialtyId: '', name: '' });
    }
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!vet.firstName) newErrors.firstName = 'First name is required';
    if (!vet.lastName) newErrors.lastName = 'Last name is required';
    if (!vet.email) newErrors.email = 'Email is required';
    if (!vet.phoneNumber) newErrors.phoneNumber = 'Phone number is required';
    if (!vet.resume) newErrors.resume = 'Resume is required';
    if (vet.workday.length === 0) newErrors.workday = 'At least one workday must be selected';
    if (!vet.workHoursJson) newErrors.workHoursJson = 'Work hours JSON is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    try {
      const response = await addVet(vet);
      if (response.status === 201) {
        navigate(AppRoutePaths.Home);
      } else {
        console.error('Failed to add vet');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="form-container">
      <h2>Add Vet</h2>
      <form onSubmit={handleSubmit}>
        <label>First Name: </label>
        <input
          type="text"
          name="firstName"
          value={vet.firstName}
          onChange={handleChange}
        />
        {errors.firstName && <span className="error">{errors.firstName}</span>}
        <br />
        <label>Last Name: </label>
        <input
          type="text"
          name="lastName"
          value={vet.lastName}
          onChange={handleChange}
        />
        {errors.lastName && <span className="error">{errors.lastName}</span>}
        <br />
        <label>Email: </label>
        <input
          type="email"
          name="email"
          value={vet.email}
          onChange={handleChange}
        />
        {errors.email && <span className="error">{errors.email}</span>}
        <br />
        <label>Phone Number: </label>
        <input
          type="text"
          name="phoneNumber"
          value={vet.phoneNumber}
          onChange={handleChange}
        />
        {errors.phoneNumber && <span className="error">{errors.phoneNumber}</span>}
        <br />
        <label>Resume: </label>
        <textarea
          name="resume"
          value={vet.resume}
          onChange={handleChange}
        />
        {errors.resume && <span className="error">{errors.resume}</span>}
        <br />
        <label>Workdays: </label>
        <select multiple onChange={handleWorkdayChange}>
          {Object.values(Workday).map(day => (
            <option key={day} value={day}>
              {day}
            </option>
          ))}
        </select>
        {errors.workday && <span className="error">{errors.workday}</span>}
        <br />
        <label>Work Hours JSON: </label>
        <input
          type="text"
          name="workHoursJson"
          value={vet.workHoursJson}
          onChange={handleChange}
        />
        {errors.workHoursJson && <span className="error">{errors.workHoursJson}</span>}
        <br />
        <label>Active: </label>
        <input
          type="checkbox"
          name="active"
          checked={vet.active}
          onChange={() => setVet(prev => ({ ...prev, active: !prev.active }))}
        />
        <br />
        <label>Photo Default: </label>
        <input
          type="checkbox"
          name="photoDefault"
          checked={vet.photoDefault}
          onChange={() => setVet(prev => ({ ...prev, photoDefault: !prev.photoDefault }))}
        />
        <br />
        <div>
          <h3>Add Specialty</h3>
          <label>Specialty ID: </label>
          <input
            type="text"
            name="specialtyId"
            value={specialtyInput.specialtyId}
            onChange={handleSpecialtyChange}
          />
          <br />
          <label>Specialty Name: </label>
          <input
            type="text"
            name="name"
            value={specialtyInput.name}
            onChange={handleSpecialtyChange}
          />
          <button type="button" onClick={addSpecialty}>
            Add Specialty
          </button>
        </div>
        <div>
          <h3>Specialties</h3>
          {vet.specialties.map(spec => (
            <div key={spec.specialtyId}>{spec.name}</div>
          ))}
        </div>
        <button type="submit">Add Vet</button>
      </form>
    </div>
  );
};

export default AddVet;
