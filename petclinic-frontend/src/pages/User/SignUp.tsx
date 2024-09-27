import * as React from 'react';
import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import '@/features/customers/components/UpdateCustomerForm.css';
import { OwnerModel } from '@/features/customers/models/OwnerModel.ts';
import { UserResponseModel } from '@/shared/models/UserResponseModel';
import { useSetUser } from '@/context/UserContext.tsx';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axios from 'axios';
import './SignUp.css';

const SignUp: React.FC = (): JSX.Element => {
  
  return (
    <div>
      <NavBar />
      
    </div>
  );
};

export default SignUp;
