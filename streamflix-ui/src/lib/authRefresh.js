/** Default matches {@code HttpLink} in main.jsx; override with Vite env if needed. */
export const GRAPHQL_HTTP_URI =
  import.meta.env.VITE_GRAPHQL_URI ?? 'http://localhost:4000/';

const REFRESH_LEEWAY_SEC = 300;

export function parseJwtPayload(token) {
  if (!token) return null;
  try {
    const part = token.split('.')[1];
    if (!part) return null;
    const json = atob(part.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json);
  } catch {
    return null;
  }
}

export function shouldProactiveRefresh(accessToken) {
  const p = parseJwtPayload(accessToken);
  if (!p?.exp) return true;
  const now = Date.now() / 1000;
  return p.exp - now < REFRESH_LEEWAY_SEC;
}

let refreshInFlight = null;

export function refreshAccessTokenSingleFlight() {
  const rt = localStorage.getItem('refreshToken');
  if (!rt) {
    return Promise.reject(new Error('No refresh token'));
  }
  if (!refreshInFlight) {
    refreshInFlight = fetch(GRAPHQL_HTTP_URI, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: `mutation Refresh($refreshToken: String!) {
          refresh(refreshToken: $refreshToken) {
            token
            refreshToken
            userId
            username
          }
        }`,
        variables: { refreshToken: rt },
      }),
    })
      .then((r) => r.json())
      .then((body) => {
        if (body.errors?.length) {
          throw new Error(body.errors[0].message);
        }
        const { token, refreshToken, userId, username } = body.data.refresh;
        localStorage.setItem('token', token);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('userId', userId);
        localStorage.setItem('username', username);
        window.dispatchEvent(new Event('streamflix-auth-refreshed'));
        return token;
      })
      .finally(() => {
        refreshInFlight = null;
      });
  }
  return refreshInFlight;
}

export function clearStoredSession() {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
  window.dispatchEvent(new Event('streamflix-auth-failed'));
}
