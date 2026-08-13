// Use any icon from the SvgIcons.svg file by referencing its id
// Current icons include:
// archive, download, eye, pen-to-square, star-empty, star-full, xcross

import * as React from 'react';
import icon from './SvgIcons.svg';

interface IconProps {
  id: string;
  size?: number | string;
  className?: string;
}

const SvgIcon: React.FC<IconProps> = ({ id, size = 24, className }) => {
  const href = `${icon}#${id}`;
  return (
    <svg className={className} width={size} height={size} viewBox="0 0 24 24">
      <use href={href} />
    </svg>
  );
};

export default SvgIcon;
