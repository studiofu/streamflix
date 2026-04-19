/**
 * JWT auth must align with user-service JwtAuthHelper (same JWT_SECRET env or default
 * super-secret-streamflix-key, userId on access tokens). Refresh tokens use typ "refresh"
 * and are ignored here so they cannot authorize API calls.
 *
 * W3C traceparent / tracestate from the client are copied into GraphQL context and forwarded
 * to each subgraph so Spring/Micrometer on the Java services can continue the trace.
 */
const { ApolloServer } = require('@apollo/server');
const { startStandaloneServer } = require('@apollo/server/standalone');
const {
  ApolloGateway,
  IntrospectAndCompose,
  RemoteGraphQLDataSource,
} = require('@apollo/gateway');

const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || "super-secret-streamflix-key";

/** First value if Express/Node coerces duplicate headers to an array. */
function singleHeader(value) {
  if (value == null) return undefined;
  return Array.isArray(value) ? value[0] : value;
}

const gateway = new ApolloGateway({
  supergraphSdl: new IntrospectAndCompose({
    subgraphs: [
      { name: 'catalog', url: process.env.CATALOG_URL || 'http://localhost:8081/graphql' },
      { name: 'user', url: process.env.USER_URL || 'http://localhost:8082/graphql' },
      { name: 'rating', url: process.env.RATING_URL || 'http://localhost:8083/graphql' },
      { name: 'playback', url: process.env.PLAYBACK_URL || 'http://localhost:8086/graphql' },
    ],
    // Re-introspect subgraphs periodically so new fields (e.g. Mutation.login) appear without a gateway restart.
    pollIntervalInMs: Number(process.env.SUPERGRAPH_POLL_MS) || 10_000,
  }),
  buildService({ name, url }) {
    return new RemoteGraphQLDataSource({
      url,
      willSendRequest({ request, context }) {
        if (context.userId) {
          request.http.headers.set('x-user-id', context.userId);
        }
        // W3C Trace Context — propagate from browser/UI so Java subgraphs join the same trace
        if (context.traceparent) {
          console.log("Setting traceparent:", context.traceparent);
          request.http.headers.set('traceparent', context.traceparent);
        }
        if (context.tracestate) {
          console.log("Setting tracestate:", context.tracestate);
          request.http.headers.set('tracestate', context.tracestate);
        }
      },
    });
  },
});

const server = new ApolloServer({
  gateway,
});

startStandaloneServer(server, {
  listen: { port: 4000 },
  context: async ({ req }) => {
    const baseContext = {
      traceparent: singleHeader(req.headers.traceparent),
      tracestate: singleHeader(req.headers.tracestate),
    };

    const authHeader = req.headers.authorization || '';

    if (authHeader.startsWith('Bearer ')) {
      const token = authHeader.substring(7);
      try {
        const decoded = jwt.verify(token, JWT_SECRET);
        console.log("Decoded JWT Token:", decoded);
        // Only accept access tokens at the gateway; refresh tokens must not authorize API calls.
        if (decoded.typ === "refresh") {
          console.warn("Ignoring refresh token in Authorization header");
          return baseContext;
        }
        return { ...baseContext, userId: decoded.userId };
      } catch (err) {
        console.error("Invalid JWT Token!");
      }
    } else {
      console.log("No JWT Token provided");
    }
    return baseContext;
  },
}).then(({ url }) => {
  console.log(`Gateway ready at ${url}`);
});
