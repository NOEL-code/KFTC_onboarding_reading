import axios from 'axios';

export function fetchTemplateInfo() {
  return axios.get('/api/templates/info');
}

export function downloadTemplate() {
  return axios.get('/api/templates/download', { responseType: 'blob' });
}

export function fetchAdminTemplates() {
  return axios.get('/api/admin/templates');
}

export function uploadTemplate(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return axios.post('/api/admin/templates', formData);
}

export function downloadAdminTemplate(templateId: number) {
  return axios.get(`/api/admin/templates/${templateId}/download`, { responseType: 'blob' });
}

export function deleteTemplate(templateId: number) {
  return axios.delete(`/api/admin/templates/${templateId}`);
}
