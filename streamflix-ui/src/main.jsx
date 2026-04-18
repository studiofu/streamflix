import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App.jsx';
import { ApolloClient, HttpLink, InMemoryCache } from '@apollo/client';
import { ApolloProvider } from '@apollo/client/react';
import { SetContextLink } from '@apollo/client/link/context';
import { ErrorLink } from '@apollo/client/link/error';
import { CombinedGraphQLErrors, ServerError } from '@apollo/client/errors';
import { from, throwError } from 'rxjs';
import { catchError, mergeMap } from 'rxjs/operators';
import {
  GRAPHQL_HTTP_URI,
  clearStoredSession,
  refreshAccessTokenSingleFlight,
  shouldProactiveRefresh,
} from './lib/authRefresh.js';

const httpLink = new HttpLink({
  uri: GRAPHQL_HTTP_URI,
});

const authLink = new SetContextLink(async (prevContext) => {
  let token = localStorage.getItem('token');
  const refreshToken = localStorage.getItem('refreshToken');
  if (refreshToken && (!token || shouldProactiveRefresh(token))) {
    try {
      token = await refreshAccessTokenSingleFlight();
    } catch {
      clearStoredSession();
      token = null;
    }
  }

  return {
    headers: {
      ...prevContext.headers,
      authorization: token ? `Bearer ${token}` : '',
    },
  };
});

function shouldRetryWithRefresh(error, operation) {
  if (operation.getContext()._retryAfterRefresh) return false;
  if (!localStorage.getItem('refreshToken')) return false;
  if (ServerError.is(error) && error.statusCode === 401) return true;
  if (CombinedGraphQLErrors.is(error)) {
    return error.errors.some((e) => e.extensions?.code === 'UNAUTHENTICATED');
  }
  return false;
}

const errorLink = new ErrorLink(({ error, operation, forward }) => {
  if (!shouldRetryWithRefresh(error, operation)) return;

  return from(refreshAccessTokenSingleFlight()).pipe(
    mergeMap(() => {
      operation.setContext({
        ...operation.getContext(),
        _retryAfterRefresh: true,
      });
      return forward(operation);
    }),
    catchError(() => {
      clearStoredSession();
      return throwError(() => error);
    })
  );
});

const client = new ApolloClient({
  link: errorLink.concat(authLink).concat(httpLink),
  cache: new InMemoryCache(),
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ApolloProvider client={client}>
      <App />
    </ApolloProvider>
  </React.StrictMode>,
);
