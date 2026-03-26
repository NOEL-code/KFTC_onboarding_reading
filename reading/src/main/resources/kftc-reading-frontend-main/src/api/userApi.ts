import axios from 'axios';

export function fetchEmployee(employeeNo: string) {
  return axios.get(`/api/employees/${employeeNo}`);
}

export function fetchUsersByCourse(courseId: string) {
  return axios.get('/api/admin/users', { params: { courseId } });
}

export function createUsers(users: { courseId: number; employeeNo: string; name: string; team: string }[]) {
  return axios.post('/api/admin/users', { users });
}

export function updateUsers(courseId: number, users: { userId: number; name?: string; team?: string }[]) {
  return axios.put('/api/admin/users', { courseId, users });
}

export function deleteUsers(courseId: number, userIds: number[]) {
  return axios.delete('/api/admin/users', { data: { courseId, userIds } });
}

export function uploadUsersExcel(courseId: string, file: File) {
  const formData = new FormData();
  formData.append('courseId', courseId);
  formData.append('file', file);
  return axios.post('/api/admin/users/upload', formData);
}
