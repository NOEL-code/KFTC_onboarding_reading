import axios from 'axios';

// ─── User ──────────────────────────────────────────────────────────────────────

export function submitReport(formData: FormData) {
  return axios.post('/api/reports', formData);
}

export function updateReport(reportId: string, formData: FormData) {
  return axios.put(`/api/reports/${reportId}`, formData);
}

export function fetchReportDetail(reportId: string) {
  return axios.get(`/api/reports/${reportId}`);
}

export function downloadReport(reportId: number, filename: string) {
  return axios
    .get(`/api/reports/${reportId}/download`, { responseType: 'blob' })
    .then(({ data }) => ({ data, filename }));
}

export function deleteReport(reportId: number, employeeNo: string) {
  return axios.delete(`/api/reports/${reportId}`, { params: { employeeNo } });
}

export function fetchCourseReports(courseId: string, size = 100) {
  return axios.get(`/api/courses/${courseId}/reports`, { params: { size } });
}

// ─── Admin ─────────────────────────────────────────────────────────────────────

export function fetchAdminReports(params: {
  courseId: string;
  page: number;
  status?: string;
  name?: string;
}) {
  return axios.get('/api/admin/reports', { params });
}

export function downloadAdminReport(reportId: number) {
  return axios.get(`/api/admin/reports/${reportId}/download`, { responseType: 'blob' });
}

export function approveReports(reportIds: number[]) {
  return axios.patch('/api/admin/reports/approve', { reportIds });
}

export function supplementReports(reportIds: number[], reason: string) {
  return axios.patch('/api/admin/reports/supplement', { reportIds, reason });
}
