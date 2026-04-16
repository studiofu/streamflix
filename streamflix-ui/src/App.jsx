import { gql } from '@apollo/client';
import { useQuery } from '@apollo/client/react';

// The exact same query we tested in the Gateway
const GET_MOVIES_WITH_RATINGS = gql`
  query GetMovieWithRatings {
    movies {
      id
      title
      description
      ratings {
        stars
      }
    }
  }
`;

function App() {
  const { loading, error, data } = useQuery(GET_MOVIES_WITH_RATINGS);
  

  if (loading) return <p>Loading StreamFlix...</p>;
  if (error) return <p>Error loading movies: {error.message}</p>;

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px' }}>
      <h1>🎬 StreamFlix Catalog</h1>
      <div style={{ display: 'grid', gap: '20px' }}>
        {data.movies.map((movie) => (
          <div key={movie.id} style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
            <h2>{movie.title}</h2>
            <p>{movie.description}</p>
            
            <div style={{ backgroundColor: '#f9f9f9', padding: '10px', borderRadius: '5px' }}>
              <strong>Ratings: </strong>
              {movie.ratings && movie.ratings.length > 0 ? (
                movie.ratings.map((rating, index) => (
                  <span key={index}>⭐ {rating.stars}/5 </span>
                ))
              ) : (
                <span>No ratings yet</span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;