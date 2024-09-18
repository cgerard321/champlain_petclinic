import * as React from 'react';
import UpdateCustomerForm from '@/features/customers/components/UpdateCustomerForm.tsx';
import { IsOwner } from '@/context/UserContext.tsx';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { useNavigate } from 'react-router-dom';
import { useEffect } from 'react';

const ProfileEdit: React.FC = (): JSX.Element => {
  const navigate = useNavigate();

  useEffect(() => {
    if (!IsOwner()) {
      navigate(AppRoutePaths.Forbidden);
    }
  }, [navigate]);

  if (!IsOwner()) {
    return <></>; // Return an empty fragment while navigating
  }

  return (
    <div>
      <UpdateCustomerForm />
    </div>
  );
};

export default ProfileEdit;
