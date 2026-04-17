const { ApolloServer } = require('@apollo/server');
const { startStandaloneServer } = require('@apollo/server/standalone');
const {
  ApolloGateway,
  IntrospectAndCompose,
  RemoteGraphQLDataSource,
} = require('@apollo/gateway');

const jwt = require('jsonwebtoken');

const JWT_SECRET = process.env.JWT_SECRET || "super-secret-streamflix-key";

const gateway = new ApolloGateway({
  supergraphSdl: new IntrospectAndCompose({
    subgraphs: [
      { name: 'catalog', url: process.env.CATALOG_URL || 'http://localhost:8081/graphql' },
      { name: 'user', url: process.env.USER_URL || 'http://localhost:8082/graphql' },
      { name: 'rating', url: process.env.RATING_URL || 'http://localhost:8083/graphql' },
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
    const authHeader = req.headers.authorization || '';

    if (authHeader.startsWith('Bearer ')) {
      const token = authHeader.substring(7);
      try {
        const decoded = jwt.verify(token, JWT_SECRET);
        console.log("Decoded JWT Token:", decoded);
        return { userId: decoded.userId };
      } catch (err) {
        console.error("Invalid JWT Token!");
      }
    }else {
      console.log("No JWT Token provided");
    }
    return {};
  },
}).then(({ url }) => {
  console.log(`Gateway ready at ${url}`);
});
