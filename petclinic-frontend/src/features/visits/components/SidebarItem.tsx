import * as React from 'react';

// Sidebar
interface SidebarItemProps {
  itemName: string;
  emergency?: boolean;
  currentTab: string;
  onClick: (n: string) => void;
}

const SidebarItem: React.FC<SidebarItemProps> = ({
  itemName,
  emergency = false,
  currentTab,
  onClick,
}): JSX.Element => (
  <li>
    <a
      className={[
        itemName === currentTab ? 'active' : '',
        emergency ? 'emergency' : '',
      ]
        .filter(Boolean)
        .join('-')}
      onClick={() => onClick(itemName)}
    >
      <span>{itemName}</span>
    </a>
  </li>
);

export default SidebarItem;
