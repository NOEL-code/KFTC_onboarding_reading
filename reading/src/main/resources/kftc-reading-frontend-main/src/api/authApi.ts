import axios from 'axios';

export function login(loginId: string, password: string) {
  return axios.post('/api/admin/login', { loginId, password });
}
