export const contact = {
  hours: [
    { id:'weekdays', days: 'Mon–Fri', open: '8:00', close: '19:00' },
    { id:'saturday', days: 'Sat', open: '9:00', close: '16:00' },
    { id:'sunday',days: 'Sun', open: null, close: null, note: 'Closed' },
  ],

  phone: {
    display: '+1 (450) 672-7360',
    href: '+14506727360',
  },

  email: 'ChamplainPetClinic@gmail.com',
} as const;
