import * as React from 'react';
import * as ReactDOM from 'react-dom/client';
import App from './App.tsx';
import './index.css';
import { UserProvider } from '@/context/UserContext.tsx';
import { CartProvider } from '@/context/CartContext.tsx';
import { ToastProvider } from '@/shared/components/toast/ToastProvider.tsx';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <ToastProvider>
      <UserProvider>
        <CartProvider>
          <App />
        </CartProvider>
      </UserProvider>
    </ToastProvider>
  </React.StrictMode>
);
