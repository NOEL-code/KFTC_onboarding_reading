import axios from 'axios';

export function fetchCourses() {
  return axios.get('/api/courses');
}

export function fetchCourseDetail(courseId: string) {
  return axios.get(`/api/courses/${courseId}`);
}

export function createCourse(form: { name: string; startDate: string; endDate: string }) {
  return axios.post('/api/admin/courses', form);
}

export function updateCourse(courseId: number, form: { name: string; startDate: string; endDate: string }) {
  return axios.put(`/api/admin/courses/${courseId}`, form);
}

export function deleteCourse(courseId: number) {
  return axios.delete(`/api/admin/courses/${courseId}`);
}
