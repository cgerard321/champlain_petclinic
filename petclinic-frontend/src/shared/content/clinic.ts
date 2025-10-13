export const contact = {
  name: 'Champlain PetClinic',
  description:
    'At Champlain Pet Clinic, we offer a wide range of services to ensure the health and well-being of your beloved pets. Our experienced veterinarians and staff are dedicated to providing the best care possible.',
  message:
    'Providing compassionate veterinary care to keep your pets healthy and happy.',
  hours: [
    { days: 'Mon–Fri', open: '8:00', close: '19:00' },
    { days: 'Sat', open: '9:00', close: '16:00' },
    { days: 'Sun', open: null, close: null, note: 'Closed' },
  ],
} as const;
