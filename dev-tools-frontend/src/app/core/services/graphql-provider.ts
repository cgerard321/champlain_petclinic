import { inject } from '@angular/core';
import { HttpLink } from 'apollo-angular/http';
import { InMemoryCache } from '@apollo/client';
import { provideApollo } from 'apollo-angular';

export const graphqlProviders = [
  provideApollo(() => {
    const httpLink = inject(HttpLink);
    return {
      link: httpLink.create({ uri: '/graphql', withCredentials: true }),
      cache: new InMemoryCache(),
    };
  }),
];
