export const clinic = {
  name: 'PetClinic',
  description:
    'At Champlain Pet Clinic, we offer a wide range of services to ensure the health and well-being of your beloved pets. Our experienced veterinarians and staff are dedicated to providing the best care possible.',
  message:
    'Providing compassionate veterinary care to keep your pets healthy and happy.',

  address: {
    street: '900 Rue Riverside',
    city: 'Saint-Lambert',
    province: 'QC',
    country: 'Canada',
    mapsUrl:
      'https://www.google.com/maps/search/?api=1&query=900+Rue+Riverside,+Saint-Lambert,+QC',
  },

  services: [
    {
      icon: '\uD83E\uDE7A',
      title: 'Wellness Exams',
      desc: 'Annual check-ups & preventive care.',
    },
    {
      icon: '\uD83D\uDC89',
      title: 'Vaccinations',
      desc: 'Core & lifestyle vaccines.',
    },
    {
      icon: '\uD83E\uDDB7',
      title: 'Dental Care',
      desc: 'Cleaning, polishing & dental X-rays.',
    },
    {
      icon: '\uD83E\uDDBB',
      title: 'Diagnostics',
      desc: 'Digital radiology & in-house lab.',
    },
    {
      icon: '\u2702\uFE0F',
      title: 'Surgery',
      desc: 'Routine & soft-tissue procedures.',
    },
    {
      icon: '\uD83D\uDE91',
      title: 'Emergency',
      desc: 'Urgent care during open hours.',
    },
  ],
} as const;
