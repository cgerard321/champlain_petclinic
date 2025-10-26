import { useEffect, useState, useCallback, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import './VetDetails.css';
import DeleteVetPhoto from '@/pages/Vet/DeleteVetPhoto.tsx';
import UpdateVetEducation from '@/pages/Vet/UpdateVetEducation';
import AddEducation from '@/pages/Vet/AddEducation.tsx';
import DeleteVetEducation from '@/pages/Vet/DeleteVetEducation';
import { Workday } from '@/features/veterinarians/models/Workday.ts';
import UpdateVet from '@/pages/Vet/UpdateVet.tsx';
import UploadAlbumPhoto from '@/features/veterinarians/api/UploadAlbumPhoto';
import { getAlbumsByVetId } from '@/features/veterinarians/api/getAlbumByVetId.ts';
import { fetchVetPhoto } from '@/features/veterinarians/api/fetchPhoto';
import { fetchVet } from '@/features/veterinarians/api/fetchVetDetails.ts';
import { IsOwner, IsVet, IsAdmin, useUser } from '@/context/UserContext';
import { getOwner } from '@/features/customers/api/getOwner';
import { deleteVetRating } from '@/features/veterinarians/api/deleteVetRating';
import AddVetRatingModal from '@/pages/Vet/AddVetRatingModal';
import { format } from 'date-fns';
import { fetchVetRatings } from '@/features/veterinarians/api/fetchVetRatings';
import { fetchEducationDetails } from '@/features/veterinarians/api/fetchEducationDetails';
import { updateVetProfilePhoto } from '@/features/veterinarians/api/updateVetProfilePhoto';
import { deleteAlbumPhoto } from '@/features/veterinarians/api/deleteAlbumPhoto';
import { addSpecialty } from '@/features/veterinarians/api/addSpecialty';
import { deleteSpecialty } from '@/features/veterinarians/api/deleteSpecialty';

interface VetResponseType {
  vetId: string;
  vetBillId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  resume: string;
  workday: string[];
  workHoursJson: string;
  active: boolean;
  specialties: { specialtyId: string; name: string }[];
}

interface VetRequestModel {
  vetId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  resume: string;
  workday: Workday[];
  workHoursJson: string;
  active: boolean;
  specialties: { specialtyId: string; name: string }[];
  photoDefault: boolean;
  username: string;
  password: string;
  vetBillId: string;
}

interface AlbumPhotoType {
  id: number;
  data: string;
  imgType: string;
}

interface EducationResponseType {
  educationId: string;
  vetId: string;
  schoolName: string;
  degree: string;
  fieldOfStudy: string;
  startDate: string;
  endDate: string;
}

interface RatingResponseType {
  ratingId: string;
  rateScore: number;
  rateDescription: string;
  predefinedDescription: string;
  rateDate: string;
  customerName: string;
}

const formatRatingDate = (rateDate?: string): string => {
  if (!rateDate) {
    return 'No date available';
  }

  try {
    const parsedDate = new Date(rateDate);
    if (Number.isNaN(parsedDate.getTime())) {
      return rateDate;
    }
    return format(parsedDate, 'yyyy-MM-dd');
  } catch {
    return rateDate;
  }
};

export default function VetDetails(): JSX.Element {
  const { vetId } = useParams<{ vetId: string }>();
  const { user } = useUser();
  const isOwner = IsOwner();
  const isVet = IsVet();
  const isAdmin = IsAdmin();
  const [vet, setVet] = useState<VetResponseType | null>(null);
  const [education, setEducation] = useState<EducationResponseType[] | null>(
    null
  );
  const [photo, setPhoto] = useState<string | null>(null);
  const [isDefaultPhoto, setIsDefaultPhoto] = useState(false);
  const [albumPhotos, setAlbumPhotos] = useState<AlbumPhotoType[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false); // To handle form visibility
  const [specialtyName, setSpecialtyName] = useState('');
  const [enlargedPhoto, setEnlargedPhoto] = useState<string | null>(null);
  const [formVisible, setFormVisible] = useState<boolean>(false);
  const [isRatingModalOpen, setIsRatingModalOpen] = useState<boolean>(false);

  // Confirm-delete modal state
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [pendingPhotoId, setPendingPhotoId] = useState<number | null>(null);
  const canManageVet = isVet || isAdmin; // Only allow Vets and Admins to manage vet details

  const [notification, setNotification] = useState<{
    show: boolean;
    message: string;
    type: 'success' | 'error';
  }>({
    show: false,
    message: '',
    type: 'success',
  });

  const [selectedEducation, setSelectedEducation] =
    useState<EducationResponseType | null>(null);
  const [ratings, setRatings] = useState<RatingResponseType[] | null>(null);
  const [selectedVet, setSelectedVet] = useState<VetRequestModel | null>(null);
  const [currentCustomerName, setCurrentCustomerName] = useState<string>('');
  const canSubmitReview = Boolean(user.userId) && isOwner;

  const showNotification = (
    message: string,
    type: 'success' | 'error'
  ): void => {
    setNotification({ show: true, message, type });
    setTimeout(() => {
      setNotification({ show: false, message: '', type: 'success' });
    }, 3000);
  };

  const refreshVetDetails = useCallback(async (): Promise<void> => {
    try {
      if (!vetId)
        throw new Error('Vet ID is required for fetching vet details');
      const vetData = await fetchVet(vetId);
      setVet(vetData);
    } catch (error) {
      console.error('Failed to fetch vet details:', error);
      setError('Failed to fetch vet details');
    }
  }, [vetId]);

  const loadAlbumPhotos = useCallback(async (): Promise<void> => {
    if (!vetId) return;
    try {
      const photos = await getAlbumsByVetId(vetId);
      setAlbumPhotos(photos);
    } catch (e) {
      console.error('Failed to fetch album photos:', e);
      setAlbumPhotos([]);
    }
  }, [vetId]);

  const mapVetResponseToRequest = (vet: VetResponseType): VetRequestModel => ({
    vetId: vet.vetId,
    firstName: vet.firstName,
    lastName: vet.lastName,
    email: vet.email,
    phoneNumber: vet.phoneNumber,
    resume: vet.resume,
    workday: vet.workday.map(day => day as Workday),
    workHoursJson: vet.workHoursJson,
    active: vet.active,
    specialties: vet.specialties,
    photoDefault: true,
    username: 'defaultUsername',
    password: 'defaultPassword',
    vetBillId: vet.vetBillId,
  });

  const fetchRatings = useCallback(async (): Promise<void> => {
    if (!vetId) return;
    try {
      const ratingsData = await fetchVetRatings(vetId);
      setRatings(ratingsData ?? []);
    } catch (error) {
      console.error('Failed to fetch vet ratings:', error);
      setError('Failed to fetch vet ratings');
      setRatings([]);
    }
  }, [vetId]);
  const requestDeleteAlbumPhoto = (photoId: number): void => {
    setPendingPhotoId(photoId);
    setConfirmOpen(true);
  };

  const confirmDelete = (): void => {
    if (pendingPhotoId != null) {
      void handleDeleteAlbumPhoto(pendingPhotoId);
    }
    setConfirmOpen(false);
    setPendingPhotoId(null);
  };

  const cancelDelete = (): void => {
    setConfirmOpen(false);
    setPendingPhotoId(null);
  };

  useEffect(() => {
    void fetchRatings();
  }, [fetchRatings]);

  const handleRatingSubmitSuccess = useCallback((): void => {
    void fetchRatings();
  }, [fetchRatings]);

  useEffect(() => {
    const fetchPhoto = async (): Promise<void> => {
      try {
        if (!vetId) throw new Error('Vet ID undefined');
        const imageUrl = await fetchVetPhoto(vetId);
        setPhoto(imageUrl);
        setIsDefaultPhoto(false);
      } catch (error) {
        setError('Failed to fetch vet photo');
        setPhoto('/images/vet_default.jpg');
        setIsDefaultPhoto(true); // This indicates the default photo is being used
      }
    };

    fetchPhoto();
  }, [vetId]);

  useEffect(() => {
    const fetchCurrentCustomerName = async (): Promise<void> => {
      try {
        if (user.userId) {
          const ownerResponse = await getOwner(user.userId);
          const customerName = `${ownerResponse.data.firstName} ${ownerResponse.data.lastName}`;
          setCurrentCustomerName(customerName);
        }
      } catch (error) {
        console.error('Failed to fetch customer name:', error);
        setCurrentCustomerName('');
      }
    };

    fetchCurrentCustomerName();
  }, [user.userId]);

  const handleEducationDeleted = (deletedEducationId: string): void => {
    setEducation(prevEducation =>
      prevEducation
        ? prevEducation.filter(edu => edu.educationId !== deletedEducationId)
        : null
    );
  };

  const handleRatingDeleted = async (): Promise<void> => {
    try {
      if (vetId) {
        await deleteVetRating(vetId);
        await fetchRatings();
      }
    } catch (error) {
      console.error('Error deleting rating:', error);
      setError('Failed to delete rating');
    }
  };

  const handlePhotoDeleted = (): void => {
    setIsDefaultPhoto(true);
  };
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const fetchVetDetails = async (): Promise<void> => {
      try {
        if (!vetId) throw new Error('Vet ID undefined');
        const vetData = await fetchVet(vetId);
        setVet(vetData);
      } catch (error) {
        setError('Failed to fetch vet details');
      }
    };

    const fetchEducation = async (): Promise<void> => {
      try {
        if (!vetId) throw new Error('Vet ID undefined');
        const educationData = await fetchEducationDetails(vetId);
        setEducation(educationData);
      } catch (error) {
        setError('Failed to fetch education details');
      }
    };

    fetchVetDetails().then(async () => {
      await fetchEducation();
      await loadAlbumPhotos();
      setLoading(false);
    });
  }, [vetId, loadAlbumPhotos]);

  const handleImageClick = (): void => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  const handleUpdateVetProfilePhoto = async (
    e: React.ChangeEvent<HTMLInputElement>
  ): Promise<void> => {
    const file = e.target.files?.[0];
    if (!file) return;

    const localImageUrl = URL.createObjectURL(file);
    setPhoto(localImageUrl);
    setIsDefaultPhoto(false); // Set to false because a new photo is uploaded

    try {
      const blob = await updateVetProfilePhoto(vetId!, file);
      const url = URL.createObjectURL(blob);
      setPhoto(url);
      setIsDefaultPhoto(false);
    } catch (error) {
      setError('Failed to update vet photo');
    }
  };
  const handleDeleteAlbumPhoto = async (photoId: number): Promise<void> => {
    try {
      await deleteAlbumPhoto(vetId!, photoId);

      setAlbumPhotos(prev => prev.filter(photo => photo.id !== photoId));
    } catch (error) {
      setError('Failed to delete album photo');
    }
  };

  const renderWorkHours = (workHoursJson: string): JSX.Element => {
    try {
      const workHours: Record<string, string[]> = JSON.parse(workHoursJson);
      const daysOfWeek = [
        'Monday',
        'Tuesday',
        'Wednesday',
        'Thursday',
        'Friday',
        'Saturday',
        'Sunday',
      ];

      const mergeHours = (hours: string[]): string => {
        if (hours.length === 0) return '';

        // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
        const formatHour = (hour: number) => {
          const isPM = hour >= 12;
          const adjustedHour = hour > 12 ? hour - 12 : hour;
          return `${adjustedHour} ${isPM ? 'PM' : 'AM'}`;
        };

        // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
        const extractHour = (hourString: string) =>
          parseInt(hourString.split('_')[1], 10);

        const hourRanges: [number, number][] = hours.map(hour => {
          const start = extractHour(hour);
          const end = start + 1;
          return [start, end];
        });

        const mergedRanges: string[] = [];
        let currentRange = hourRanges[0];

        for (let i = 1; i < hourRanges.length; i++) {
          if (hourRanges[i][0] === currentRange[1]) {
            currentRange[1] = hourRanges[i][1];
          } else {
            mergedRanges.push(
              `${formatHour(currentRange[0])} - ${formatHour(currentRange[1])}`
            );
            currentRange = hourRanges[i];
          }
        }
        mergedRanges.push(
          `${formatHour(currentRange[0])} - ${formatHour(currentRange[1])}`
        );

        return mergedRanges.join(', ');
      };

      return (
        <div className="work-hours-calendar">
          <table>
            <thead>
              <tr>
                <th>Day</th>
                <th>Hours</th>
              </tr>
            </thead>
            <tbody>
              {daysOfWeek.map(day => (
                <tr key={day}>
                  <td>
                    <strong>{day}</strong>
                  </td>
                  <td>
                    {workHours[day]?.length
                      ? mergeHours(workHours[day])
                      : 'No hours available'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    } catch (error) {
      console.error('Error parsing work hours:', error);
      return <p>Invalid work hours data</p>;
    }
  };

  const openPhotoModal = (photoUrl: string): void => {
    setEnlargedPhoto(photoUrl);
  };

  const closePhotoModal = (): void => {
    setEnlargedPhoto(null);
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleAddSpecialty = async () => {
    const specialtyDTO = {
      specialtyId: '', // Will be generated by backend
      name: specialtyName,
    };

    try {
      await addSpecialty(vetId!, specialtyDTO);
      showNotification('Specialty added successfully!', 'success');
      setIsFormOpen(false); // Close form on success
      setSpecialtyName(''); // Clear fields

      if (vetId) {
        const updatedVetData = await fetchVet(vetId);
        setVet(updatedVetData);
      }
    } catch (error) {
      showNotification('Failed to add specialty', 'error');
      console.error('Failed to add specialty:', error);
    }
  };

  const handleDeleteSpecialty = async (specialtyId: string): Promise<void> => {
    try {
      await deleteSpecialty(vetId!, specialtyId);
      showNotification('Specialty deleted successfully!', 'success');

      // Refresh the vet data to get the updated specialties list
      if (vetId) {
        const updatedVetData = await fetchVet(vetId);
        setVet(updatedVetData);
      }
    } catch (error) {
      console.error('Failed to delete specialty:', error);
      showNotification('Failed to delete specialty', 'error');
    }
  };

  if (loading) return <p>Loading vet details...</p>;
  if (error) return <p>{error}</p>;
  if (!vetId) throw new Error('Vet ID undefined');

  return (
    <div>
      <NavBar />
      <div className="vet-details-container">
        <h1>Vet Information</h1>

        {photo && (
          <section className="vet-photo-container">
            <img
              src={photo}
              alt="Vet"
              className="vet-photo"
              onClick={handleImageClick}
            />
            <input
              type="file"
              ref={fileInputRef}
              style={{ display: 'none ' }}
              onChange={handleUpdateVetProfilePhoto}
              accept="image/*"
            />
            {canManageVet && (
              <>
                {!isDefaultPhoto && (
                  <DeleteVetPhoto
                    vetId={vetId!}
                    onPhotoDeleted={handlePhotoDeleted}
                  />
                )}

                <button
                  className="btn btn-primary"
                  onClick={() => setSelectedVet(mapVetResponseToRequest(vet!))}
                >
                  Update Profile
                </button>

                {selectedVet && (
                  <UpdateVet
                    vet={selectedVet}
                    onClose={() => setSelectedVet(null)}
                    refreshVetDetails={refreshVetDetails}
                  />
                )}
              </>
            )}
          </section>
        )}

        <section className="vet-ratings-info">
          <div className="vet-ratings-header">
            <h2>Ratings</h2>
            {canSubmitReview && (
              <button
                className="add-rating-button"
                onClick={() => setIsRatingModalOpen(true)}
                type="button"
              >
                Write a Review
              </button>
            )}
          </div>
          {ratings && ratings.length > 0 ? (
            ratings.map((rating, index) => (
              <div key={index} className="rating-card">
                <p>
                  <strong>Customer:</strong>{' '}
                  {rating.customerName || 'Anonymous'}
                </p>
                <p>
                  <strong>Rating:</strong> {rating.rateScore} / 5
                </p>
                <p>
                  <strong>Experience:</strong> {rating.predefinedDescription}
                </p>
                <p>
                  <strong>Description:</strong> {rating.rateDescription}
                </p>
                <p>
                  <strong>Rate Date:</strong>{' '}
                  {formatRatingDate(rating.rateDate)}
                </p>
                {(currentCustomerName &&
                  rating.customerName === currentCustomerName) ||
                isAdmin ? (
                  <button
                    onClick={handleRatingDeleted}
                    className="delete-rating-button"
                    style={{
                      backgroundColor: '#dc3545',
                      color: 'white',
                      border: 'none',
                      padding: '8px 16px',
                      borderRadius: '4px',
                      cursor: 'pointer',
                      marginTop: '10px',
                      fontSize: '14px',
                    }}
                  >
                    {isAdmin ? 'Delete Rating' : 'Delete My Rating'}
                  </button>
                ) : null}
                <hr />
              </div>
            ))
          ) : (
            <p>No ratings available</p>
          )}
        </section>
        {isRatingModalOpen && canSubmitReview && vetId && (
          <AddVetRatingModal
            vetId={vetId}
            onClose={() => setIsRatingModalOpen(false)}
            onSubmitSuccess={handleRatingSubmitSuccess}
          />
        )}

        {vet && (
          <>
            <section className="vet-info">
              <h2>Veterinarian</h2>
              <p>
                <strong>Name:</strong> {vet.firstName} {vet.lastName}
              </p>
            </section>

            <section className="contact-info">
              <h2>Contact Information</h2>
              <p>
                <strong>Email Address:</strong> {vet.email}
              </p>
              <p>
                <strong>Phone Number:</strong> {vet.phoneNumber}
              </p>
            </section>

            <section className="work-info">
              <h2>Work Information</h2>
              <p>
                <strong>Resume:</strong> {vet.resume}
              </p>
              <p>
                <strong>Workdays:</strong>
                {vet.workday && vet.workday.length > 0 ? (
                  <ul>
                    {vet.workday.map((workday, index) => (
                      <li key={index}>{workday}</li>
                    ))}
                  </ul>
                ) : (
                  <p>No workdays available</p>
                )}
              </p>
              <p>
                <strong>Work Hours:</strong>{' '}
                {renderWorkHours(vet.workHoursJson)}
              </p>
              <p>
                <strong>Active:</strong> {vet.active ? 'Yes' : 'No'}
              </p>
            </section>

            <section className="specialties-info">
              <h2>Specialties</h2>
              {vet.specialties && vet.specialties.length > 0 ? (
                <div className="specialties-list">
                  {vet.specialties.map((specialty, index) => (
                    <div key={index} className="specialty-item">
                      <span className="specialty-name">{specialty.name}</span>
                      {canManageVet && (
                        <button
                          className="btn-delete-specialty"
                          onClick={() =>
                            handleDeleteSpecialty(specialty.specialtyId)
                          }
                          title="Delete this specialty"
                        >
                          Delete
                        </button>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <p className="no-specialties">No specialties available</p>
              )}

              {/* Button to open the form */}
              {canManageVet && (
                <button
                  className="btn-add-specialty"
                  onClick={() => setIsFormOpen(true)}
                >
                  + Add Specialty
                </button>
              )}

              {/* Conditionally render the form */}
              {isFormOpen && (
                <div
                  className="specialty-form-overlay"
                  onClick={() => setIsFormOpen(false)}
                >
                  <div
                    className="specialty-form-popup"
                    onClick={e => e.stopPropagation()}
                  >
                    <h3>Add New Specialty</h3>
                    <form
                      onSubmit={e => {
                        e.preventDefault();
                        handleAddSpecialty();
                      }}
                    >
                      <div className="form-group">
                        <label htmlFor="specialtyName">Specialty Name:</label>
                        <input
                          type="text"
                          id="specialtyName"
                          value={specialtyName}
                          onChange={e => setSpecialtyName(e.target.value)}
                          placeholder="Enter specialty name"
                          required
                        />
                      </div>
                      <div className="form-buttons">
                        <button type="submit" className="btn-submit">
                          Add
                        </button>
                        <button
                          type="button"
                          className="btn-cancel"
                          onClick={() => setIsFormOpen(false)}
                        >
                          Cancel
                        </button>
                      </div>
                    </form>
                  </div>
                </div>
              )}
            </section>

            <section className="vet-education-info">
              <h2>Vet Education</h2>
              {education && education.length > 0 ? (
                education.map((edu, index) => (
                  <div key={index}>
                    <p>
                      <strong>School Name:</strong> {edu.schoolName}
                    </p>
                    <p>
                      <strong>Degree:</strong> {edu.degree}
                    </p>
                    <p>
                      <strong>Field of Study:</strong> {edu.fieldOfStudy}
                    </p>
                    <p>
                      <strong>Start Date:</strong> {edu.startDate}
                    </p>
                    <p>
                      <strong>End Date:</strong> {edu.endDate}
                    </p>
                    {canManageVet && (
                      <>
                        <div
                          style={{ marginBottom: '20px', textAlign: 'right' }}
                        >
                          <button
                            onClick={() => setFormVisible(prev => !prev)}
                            style={{
                              backgroundColor: formVisible
                                ? '#ff6347'
                                : '#4CAF50',
                            }}
                          >
                            {formVisible ? 'Cancel' : 'Add Education'}
                          </button>
                          {vetId && formVisible && (
                            <AddEducation
                              vetId={vetId}
                              onClose={() => setFormVisible(false)}
                            />
                          )}
                        </div>

                        <button
                          className="btn btn-primary"
                          onClick={event => {
                            event.stopPropagation();
                            setSelectedEducation(edu);
                          }}
                        >
                          Update Education
                        </button>
                        <DeleteVetEducation
                          vetId={vetId!}
                          educationId={edu.educationId}
                          onEducationDeleted={handleEducationDeleted}
                        />
                      </>
                    )}
                    <hr />
                  </div>
                ))
              ) : (
                // When there are no education entries
                <div>
                  <p>No education details available</p>

                  {canManageVet && (
                    <div style={{ marginBottom: '20px', textAlign: 'right' }}>
                      <button
                        onClick={() => setFormVisible(prev => !prev)}
                        style={{
                          backgroundColor: formVisible ? '#ff6347' : '#4CAF50',
                        }}
                      >
                        {formVisible ? 'Cancel' : 'Add Education'}
                      </button>
                      {vetId && formVisible && (
                        <AddEducation
                          vetId={vetId}
                          onClose={() => setFormVisible(false)}
                        />
                      )}
                    </div>
                  )}
                </div>
              )}
              {(isVet || isAdmin) && selectedEducation && vetId && (
                <UpdateVetEducation
                  vetId={vetId}
                  education={selectedEducation}
                  educationId={selectedEducation.educationId}
                  onClose={() => setSelectedEducation(null)}
                />
              )}
            </section>

            {/* Only show album photos section if:
                1. There are photos to display, OR
                2. User is vet or admin */}
            {(albumPhotos.length > 0 || isVet || isAdmin) && (
              <section className="album-photos">
                <div className="d-flex justify-content-between align-items-center">
                  <h2>Album Photos</h2>

                  {vetId && (isVet || isAdmin) && (
                    <UploadAlbumPhoto
                      vetId={vetId}
                      onUploadComplete={loadAlbumPhotos}
                    />
                  )}
                </div>

                {albumPhotos.length > 0 ? (
                  <div className="album-photo-grid">
                    {albumPhotos.map(photo => (
                      <div
                        key={photo.id}
                        className="album-photo-card"
                        onClick={() =>
                          openPhotoModal(
                            `data:${photo.imgType};base64,${photo.data}`
                          )
                        }
                      >
                        <img
                          src={`data:${photo.imgType};base64,${photo.data}`}
                          alt={`Album Photo ${photo.id}`}
                          className="album-photo-thumbnail"
                        />
                        {(isVet || isAdmin) && (
                          <button
                            style={{
                              backgroundColor: '#f93142ff',
                              borderColor: '#f93142ff',
                            }}
                            className="delete-photo-button"
                            onClick={e => {
                              e.stopPropagation();
                              requestDeleteAlbumPhoto(photo.id);
                            }}
                          >
                            Delete Image
                          </button>
                        )}
                      </div>
                    ))}
                  </div>
                ) : (
                  <p>No album photos available</p>
                )}
              </section>
            )}
          </>
        )}
      </div>

      {/* Modal for enlarged photo */}
      {enlargedPhoto && (
        <div className="photo-modal" onClick={closePhotoModal}>
          <img src={enlargedPhoto} alt="Enlarged Vet Album Photo" />
        </div>
      )}
      {confirmOpen && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0,0,0,0.45)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
          onClick={cancelDelete}
        >
          <div
            style={{
              background: '#fff',
              padding: 20,
              borderRadius: 12,
              width: 'min(520px, 92vw)',
              boxShadow: '0 10px 30px rgba(0,0,0,0.25)',
            }}
            onClick={e => e.stopPropagation()}
          >
            <h3 style={{ marginTop: 0 }}>Delete this photo?</h3>
            <p>
              You&apos;re about to delete{' '}
              <strong>photo #{pendingPhotoId}</strong>.
            </p>
            <p>
              <strong>Vet:</strong>{' '}
              {vet ? `${vet.firstName} ${vet.lastName}` : 'Loading...'}
            </p>
            <p style={{ fontSize: 13, opacity: 0.85 }}>
              This can&apos;t be undone. Make sure this is the correct
              veterinarian profile.
            </p>
            <div
              style={{
                display: 'flex',
                gap: 12,
                justifyContent: 'flex-end',
                marginTop: 18,
              }}
            >
              <button
                onClick={cancelDelete}
                style={{
                  padding: '8px 14px',
                  borderRadius: 8,
                  border: '1px solid #ddd',
                }}
              >
                Cancel
              </button>
              <button
                onClick={confirmDelete}
                style={{
                  padding: '8px 14px',
                  borderRadius: 8,
                  border: '1px solid #f93142',
                  background: '#f93142',
                  color: '#fff',
                }}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Notification Modal */}
      {notification.show && (
        <div className="notification-overlay">
          <div className={`notification-modal ${notification.type}`}>
            <div className="notification-icon">
              {notification.type === 'success' ? '✓' : '✕'}
            </div>
            <div className="notification-content">
              <h4>{notification.type === 'success' ? 'Success' : 'Error'}</h4>
              <p>{notification.message}</p>
            </div>
            <button
              className="notification-close"
              onClick={() =>
                setNotification({ show: false, message: '', type: 'success' })
              }
              aria-label="Close notification"
            >
              ✕
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
