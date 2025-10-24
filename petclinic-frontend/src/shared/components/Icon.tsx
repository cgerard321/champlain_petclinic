// This Component allows you to create an Icon  without the hassle of importing everything
// Feel free to create new icons for this for others to use. New icons should be put in the assets/Icons folder
// You're welcome :3

import * as React from 'react';

import archiveD from '@/assets/Icons/archiveDark.svg';
import archiveL from '@/assets/Icons/archiveLight.svg';

import downloadD from '@/assets/Icons/downloadDark.svg';
import downloadL from '@/assets/Icons/downloadLight.svg';

import eyeD from '@/assets/Icons/eyeDark.svg';
import eyeL from '@/assets/Icons/eyeLight.svg';

import pencilD from '@/assets/Icons/pencilDark.svg';
import pencilL from '@/assets/Icons/pencilLight.svg';

import pentosquareD from '@/assets/Icons/pentosquareDark.svg';
import pentosquareL from '@/assets/Icons/pentosquareLight.svg';

import starEmptyD from '@/assets/Icons/starEmptyDark.svg';
import starEmptyL from '@/assets/Icons/starEmptyLight.svg';

import trashD from '@/assets/Icons/trashDark.svg';
import trashL from '@/assets/Icons/trashLight.svg';

import xcrossD from '@/assets/Icons/xcrossDark.svg';
import xcrossL from '@/assets/Icons/xcrossLight.svg';

interface IconProps {
  title?: string; // The title of the Icon when you hover over it
  light?: boolean; // Set to true if it is in light mode, normally it is false
  className?: string;
}

const renderIcon = (
  src: string,
  title?: string,
  className?: string
): JSX.Element => <img className={className} src={src} title={title} />;

export const ArchiveIcon: React.FC<IconProps> = ({
  title,
  light,
  className,
}) => {
  return renderIcon(light ? archiveL : archiveD, title, className);
};

export const DownloadIcon: React.FC<IconProps> = ({
  title,
  light,
  className,
}) => {
  return renderIcon(light ? downloadL : downloadD, title, className);
};

export const EyeIcon: React.FC<IconProps> = ({ title, light, className }) => {
  return renderIcon(light ? eyeL : eyeD, title, className);
};

export const PenIcon: React.FC<IconProps> = ({ title, light, className }) => {
  return renderIcon(light ? pencilL : pencilD, title, className);
};

export const StarEmptyIcon: React.FC<IconProps> = ({
  title,
  light,
  className,
}) => {
  return renderIcon(light ? starEmptyL : starEmptyD, title, className);
};

export const PenToSquareIcon: React.FC<IconProps> = ({
  title,
  light,
  className,
}) => {
  return renderIcon(light ? pentosquareL : pentosquareD, title, className);
};

export const XCrossIcon: React.FC<IconProps> = ({
  title,
  light,
  className,
}) => {
  return renderIcon(light ? xcrossL : xcrossD, title, className);
};

export const TrashIcon: React.FC<IconProps> = ({ title, light, className }) => {
  return renderIcon(light ? trashL : trashD, title, className);
};
