import { gql } from '@apollo/client';
import { useQuery, useMutation } from '@apollo/client/react';
import { useState } from 'react';

// --- GRAPHQL QUERIES & MUTATIONS ---
const GET_MOVIES = gql`
  query GetMovies {
    movies { id title description ratings { stars } }
  }
`;

const LOGIN_USER = gql`
  mutation Login($username: String!, $password: String!) {
    login(username: $username, password: $password) { token userId }
  }
`;

const ADD_RATING = gql`
  mutation AddRating($movieId: ID!, $stars: Int!) {
    addRating(movieId: $movieId, stars: $stars) { stars }
  }
`;

function App() {
  const [usernameInput, setUsernameInput] = useState('');
  const [passwordInput, setPasswordInput] = useState('');
  const [loggedInUser, setLoggedInUser] = useState(localStorage.getItem('userId'));

  // Apollo Hooks
  const { loading, error, data, refetch } = useQuery(GET_MOVIES);
  const [loginMutation] = useMutation(LOGIN_USER);
  const [addRatingMutation] = useMutation(ADD_RATING);

  // --- HANDLERS ---
  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await loginMutation({ variables: { username: usernameInput, password: passwordInput } });
      const { token, userId } = response.data.login;
      
      // Save to browser storage
      localStorage.setItem('token', token);
      localStorage.setItem('userId', userId);
      setLoggedInUser(userId);
    } catch (err) {
      alert("Login failed! Try 'password123'");
      console.error("Login failed: " + err.message);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('userId');
    setLoggedInUser(null);
  };

  const handleRateMovie = async (movieId, stars) => {
    try {
      await addRatingMutation({ variables: { movieId, stars } });
      alert("Rating saved successfully!");
      refetch(); // Reload the movies to see the new rating
    } catch (err) {
      alert("Error saving rating: " + err.message);
    }
  };

  // --- UI RENDERING ---
  if (loading) return <p>Loading StreamFlix...</p>;
  if (error) return <p>Error: {error.message}</p>;

  return (
    <div style={{ fontFamily: 'Arial, sans-serif', padding: '20px', maxWidth: '800px', margin: '0 auto' }}>
      
      {/* HEADER SECTION */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
        <h1>🎬 StreamFlix Catalog</h1>
        {loggedInUser ? (
          <div>
            <span style={{ marginRight: '15px' }}>Welcome, <strong>{loggedInUser}</strong>!</span>
            <button onClick={handleLogout} style={{ padding: '8px 12px' }}>Logout</button>
          </div>
        ) : (
          <form onSubmit={handleLogin} style={{ display: 'flex', gap: '10px' }}>
            <input type="text" placeholder="Username" value={usernameInput} onChange={(e) => setUsernameInput(e.target.value)} required />
            <input type="password" placeholder="Password (password123)" value={passwordInput} onChange={(e) => setPasswordInput(e.target.value)} required />
            <button type="submit" style={{ padding: '8px 12px' }}>Login</button>
          </form>
        )}
      </div>

      {/* MOVIE LIST SECTION */}
      <div style={{ display: 'grid', gap: '20px' }}>
        {data.movies.map((movie) => (
          <div key={movie.id} style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
            <h2>{movie.title}</h2>
            <p>{movie.description}</p>
            
            <div style={{ backgroundColor: '#f9f9f9', padding: '10px', borderRadius: '5px', marginBottom: '10px' }}>
              <strong>Ratings: </strong>
              {movie.ratings && movie.ratings.length > 0 
                ? movie.ratings.map((r, i) => <span key={i}>⭐ {r.stars}/5 </span>) 
                : <span>No ratings yet</span>}
            </div>

            {/* SHOW RATING BUTTONS ONLY IF LOGGED IN */}
            {loggedInUser && (
              <div>
                <strong style={{ marginRight: '10px' }}>Rate this: </strong>
                {[1, 2, 3, 4, 5].map((star) => (
                  <button key={star} onClick={() => handleRateMovie(movie.id, star)} style={{ margin: '0 2px' }}>
                    {star}
                  </button>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

export default App;