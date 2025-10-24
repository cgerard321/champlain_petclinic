import * as React from 'react';
import './Sidebar.css';

// Sidebar
interface SidebarProps {
  title: string;
  children?: React.ReactNode; //The body of the modal
}

const Sidebar: React.FC<SidebarProps> = ({ title, children }): JSX.Element => (
  <aside id="sidebar">
    <ul>
      <li>
        <h2>{title}</h2>
      </li>
      {children}
    </ul>
  </aside>
);

export default Sidebar;
