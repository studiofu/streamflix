import { gql } from '@apollo/client';
import { useQuery, useMutation } from '@apollo/client/react';
import { useState, useEffect } from 'react';

// --- GRAPHQL QUERIES & MUTATIONS ---
const GET_MOVIES = gql`
  query GetMovies {
    movies {
      id
      title
      description
      releaseYear
      ratings { stars }
      myPlayback {
        positionSeconds
        durationSeconds
        completed
        updatedAt
      }
    }
  }
`;

// Login mutation: access token, refresh token, and user id
const LOGIN_USER = gql`
  mutation Login($username: String!, $password: String!) {
    login(username: $username, password: $password) {
      token
      refreshToken
      userId
    }
  }
`;

function readLoggedInUserFromStorage() {
  const uid = localStorage.getItem('userId');
  if (!uid) return null;
  if (localStorage.getItem('refreshToken') || localStorage.getItem('token')) return uid;
  return null;
}

// Add rating mutation to add a rating to a movie
const ADD_RATING = gql`
  mutation AddRating($movieId: ID!, $stars: Int!) {
    addRating(movieId: $movieId, stars: $stars) { stars }
  }
`;

const ADD_MOVIE = gql`
  mutation AddMovie($title: String!, $description: String, $releaseYear: Int) {
    addMovie(title: $title, description: $description, releaseYear: $releaseYear) {
      id
      title
    }
  }
`;

const UPDATE_PLAYBACK = gql`
  mutation UpdatePlayback($movieId: ID!, $positionSeconds: Int!, $durationSeconds: Int) {
    updatePlaybackProgress(
      movieId: $movieId
      positionSeconds: $positionSeconds
      durationSeconds: $durationSeconds
    ) {
      positionSeconds
      durationSeconds
      completed
      updatedAt
    }
  }
`;

const RECORD_PLAY = gql`
  mutation RecordPlay($movieId: ID!) {
    recordPlay(movieId: $movieId) {
      id
      movieId
      playedAt
    }
  }
`;

const GET_PLAY_HISTORY = gql`
  query PlayHistory($userId: ID!) {
    user(id: $userId) {
      id
      playHistory(limit: 20) {
        id
        movieId
        playedAt
      }
    }
  }
`;

function App() {
  const [usernameInput, setUsernameInput] = useState('');
  const [passwordInput, setPasswordInput] = useState('');
  const [loggedInUser, setLoggedInUser] = useState(readLoggedInUserFromStorage);
  const [newMovieTitle, setNewMovieTitle] = useState('');
  const [newMovieDescription, setNewMovieDescription] = useState('');
  const [newMovieReleaseYear, setNewMovieReleaseYear] = useState('');

  useEffect(() => {
    const onAuthFailed = () => setLoggedInUser(null);
    window.addEventListener('streamflix-auth-failed', onAuthFailed);
    return () => window.removeEventListener('streamflix-auth-failed', onAuthFailed);
  }, []);

  // Apollo Hooks
  const { loading, error, data, refetch } = useQuery(GET_MOVIES);
  const { data: historyData, refetch: refetchPlayHistory, loading: historyLoading } = useQuery(GET_PLAY_HISTORY, {
    variables: { userId: loggedInUser },
    skip: !loggedInUser,
  });
  const [loginMutation] = useMutation(LOGIN_USER);
  const [addRatingMutation] = useMutation(ADD_RATING);
  const [addMovieMutation, { loading: addingMovie }] = useMutation(ADD_MOVIE);
  const [updatePlaybackMutation] = useMutation(UPDATE_PLAYBACK);
  const [recordPlayMutation] = useMutation(RECORD_PLAY);

  // --- HANDLERS ---
  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await loginMutation({ variables: { username: usernameInput, password: passwordInput } });
      const { token, refreshToken, userId } = response.data.login;

      localStorage.setItem('token', token);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('userId', userId);
      setLoggedInUser(userId);
    } catch (err) {
      alert("Login failed! Try 'password123'");
      console.error("Login failed: " + err.message);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
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

  const handleRecordPlay = async (movieId) => {
    try {
      await recordPlayMutation({ variables: { movieId } });
      await refetchPlayHistory();
    } catch (err) {
      alert('Error recording play: ' + err.message);
    }
  };

  const handleSavePlayback = async (movieId, positionStr, durationStr) => {
    const positionSeconds = parseInt(positionStr, 10);
    if (Number.isNaN(positionSeconds) || positionSeconds < 0) {
      alert('Position (seconds) must be a non-negative number');
      return;
    }
    let durationSeconds = null;
    const d = durationStr.trim();
    if (d) {
      const parsed = parseInt(d, 10);
      if (Number.isNaN(parsed) || parsed <= 0) {
        alert('Duration (seconds) must be a positive number if provided');
        return;
      }
      durationSeconds = parsed;
    }
    try {
      await updatePlaybackMutation({
        variables: {
          movieId,
          positionSeconds,
          durationSeconds,
        },
      });
      alert('Playback progress saved');
      refetch();
    } catch (err) {
      alert('Error saving playback: ' + err.message);
    }
  };

  const handleAddMovie = async (e) => {
    e.preventDefault();
    const title = newMovieTitle.trim();
    if (!title) {
      alert('Title is required');
      return;
    }
    const desc = newMovieDescription.trim();
    const yearStr = newMovieReleaseYear.trim();
    let releaseYear = null;
    if (yearStr) {
      const y = parseInt(yearStr, 10);
      if (Number.isNaN(y)) {
        alert('Release year must be a number');
        return;
      }
      releaseYear = y;
    }
    try {
      await addMovieMutation({
        variables: {
          title,
          description: desc || null,
          releaseYear,
        },
      });
      alert('Movie added successfully!');
      setNewMovieTitle('');
      setNewMovieDescription('');
      setNewMovieReleaseYear('');
      refetch();
    } catch (err) {
      alert('Error adding movie: ' + err.message);
      console.error('Error adding movie: ' + err.message);
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

      {loggedInUser && (
        <div style={{ border: '1px solid #ddd', padding: '15px', borderRadius: '8px', marginBottom: '20px', backgroundColor: '#fafafa' }}>
          <h2 style={{ marginTop: 0 }}>Play history</h2>
          <p style={{ color: '#666', marginTop: 0, fontSize: '14px' }}>Movies you clicked &quot;Log play&quot; (newest first).</p>
          {historyLoading ? (
            <p style={{ color: '#888' }}>Loading history…</p>
          ) : historyData?.user?.playHistory?.length ? (
            <ul style={{ margin: 0, paddingLeft: '20px' }}>
              {historyData.user.playHistory.map((entry) => {
                const title = data.movies.find((m) => m.id === entry.movieId)?.title ?? entry.movieId;
                return (
                  <li key={entry.id} style={{ marginBottom: '6px' }}>
                    <strong>{title}</strong>
                    <span style={{ color: '#666', marginLeft: '8px', fontSize: '13px' }}>{entry.playedAt}</span>
                  </li>
                );
              })}
            </ul>
          ) : (
            <p style={{ color: '#888', marginBottom: 0 }}>No plays logged yet. Use &quot;Log play&quot; on a movie below.</p>
          )}
        </div>
      )}

      {loggedInUser && (
        <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px', marginBottom: '20px' }}>
          <h2 style={{ marginTop: 0 }}>Add a movie</h2>
          <form onSubmit={handleAddMovie} style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            <input
              type="text"
              placeholder="Title"
              value={newMovieTitle}
              onChange={(e) => setNewMovieTitle(e.target.value)}
              required
            />
            <textarea
              placeholder="Description (optional)"
              value={newMovieDescription}
              onChange={(e) => setNewMovieDescription(e.target.value)}
              rows={3}
            />
            <input
              type="number"
              placeholder="Release year (optional)"
              value={newMovieReleaseYear}
              onChange={(e) => setNewMovieReleaseYear(e.target.value)}
            />
            <button type="submit" disabled={addingMovie} style={{ padding: '8px 12px', alignSelf: 'flex-start' }}>
              {addingMovie ? 'Adding…' : 'Add movie'}
            </button>
          </form>
        </div>
      )}

      {/* MOVIE LIST SECTION */}
      <div style={{ display: 'grid', gap: '20px' }}>
        {data.movies.map((movie) => (
          <MovieCard
            key={movie.id}
            movie={movie}
            loggedInUser={loggedInUser}
            onRateMovie={handleRateMovie}
            onSavePlayback={handleSavePlayback}
            onRecordPlay={handleRecordPlay}
          />
        ))}
      </div>
    </div>
  );
}

function MovieCard({ movie, loggedInUser, onRateMovie, onSavePlayback, onRecordPlay }) {
  const [positionInput, setPositionInput] = useState('');
  const [durationInput, setDurationInput] = useState('');

  return (
    <div style={{ border: '1px solid #ccc', padding: '15px', borderRadius: '8px' }}>
      <h2>{movie.title}</h2>
      {movie.releaseYear != null && (
        <p style={{ color: '#666', marginTop: '-8px' }}>Released: {movie.releaseYear}</p>
      )}
      <p>{movie.description}</p>

      <div style={{ backgroundColor: '#f9f9f9', padding: '10px', borderRadius: '5px', marginBottom: '10px' }}>
        <strong>Ratings: </strong>
        {movie.ratings && movie.ratings.length > 0
          ? movie.ratings.map((r, i) => <span key={i}>⭐ {r.stars}/5 </span>)
          : <span>No ratings yet</span>}
      </div>

      {movie.myPlayback && (
        <div style={{ backgroundColor: '#f0f8ff', padding: '10px', borderRadius: '5px', marginBottom: '10px' }}>
          <strong>Your playback (simulated): </strong>
          {movie.myPlayback.positionSeconds}s
          {movie.myPlayback.durationSeconds != null && ` / ${movie.myPlayback.durationSeconds}s`}
          {movie.myPlayback.completed ? ' — completed' : ''}
          <span style={{ color: '#888', marginLeft: '8px', fontSize: '12px' }}>
            (updated {movie.myPlayback.updatedAt})
          </span>
        </div>
      )}

      {loggedInUser && (
        <div style={{ display: 'flex', flexWrap: 'wrap', alignItems: 'center', gap: '10px' }}>
          <button type="button" onClick={() => onRecordPlay(movie.id)} style={{ padding: '6px 12px' }}>
            Log play
          </button>
          <span>
            <strong style={{ marginRight: '10px' }}>Rate this: </strong>
            {[1, 2, 3, 4, 5].map((star) => (
              <button key={star} onClick={() => onRateMovie(movie.id, star)} style={{ margin: '0 2px' }}>
                {star}
              </button>
            ))}
          </span>
        </div>
      )}

      {loggedInUser && (
        <div style={{ marginTop: '12px', paddingTop: '10px', borderTop: '1px solid #eee' }}>
          <strong>Simulate playback: </strong>
          <label style={{ marginLeft: '8px' }}>
            Position (s)
            <input
              type="number"
              min="0"
              value={positionInput}
              onChange={(e) => setPositionInput(e.target.value)}
              style={{ width: '72px', marginLeft: '6px' }}
            />
          </label>
          <label style={{ marginLeft: '8px' }}>
            Duration (s, optional)
            <input
              type="number"
              min="1"
              value={durationInput}
              onChange={(e) => setDurationInput(e.target.value)}
              style={{ width: '72px', marginLeft: '6px' }}
            />
          </label>
          <button
            type="button"
            onClick={() => onSavePlayback(movie.id, positionInput, durationInput)}
            style={{ marginLeft: '10px', padding: '6px 10px' }}
          >
            Save progress
          </button>
        </div>
      )}
    </div>
  );
}

export default App;