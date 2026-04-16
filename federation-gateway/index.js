const { ApolloServer } = require('@apollo/server');
const { startStandaloneServer } = require('@apollo/server/standalone');
const { ApolloGateway, IntrospectAndCompose } = require('@apollo/gateway');

// Initialize the Gateway
const gateway = new ApolloGateway({
  // IntrospectAndCompose tells the gateway to reach out to the subgraphs
  // on startup and stitch their schemas together automatically.
  supergraphSdl: new IntrospectAndCompose({
    subgraphs: [
      { name: 'catalog', url: process.env.CATALOG_URL || 'http://localhost:8081/graphql' },
      { name: 'user', url: process.env.USER_URL || 'http://localhost:8082/graphql' },
      { name: 'rating', url: process.env.RATING_URL || 'http://localhost:8083/graphql' },
    ],
  }),
});

// Start the Server
async function startGateway() {
  const server = new ApolloServer({ gateway });
  
  const { url } = await startStandaloneServer(server, {
    listen: { port: 4000 },
  });
  
  console.log(`🚀 StreamFlix Federation Gateway ready at ${url}`);
}

startGateway();