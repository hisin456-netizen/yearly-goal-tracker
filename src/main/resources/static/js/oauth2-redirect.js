import { tokenStorage } from './api.js';

const params = new URLSearchParams(location.search);
const token = params.get('token');

if (token) {
  tokenStorage.setToken(token);
  window.location.replace('/');
} else {
  window.location.replace('/login.html?error=1');
}
