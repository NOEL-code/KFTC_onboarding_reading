import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';

import theme from './theme.ts';
import { CourseProvider } from './context/CourseContext.tsx';

import AdminLayout from './layouts/AdminLayout.tsx';
import UserLayout from './layouts/UserLayout.tsx';

// Admin pages
import ReportManagement from './pages/admin/ReportManagement.tsx';
import UserManagement from './pages/admin/UserManagement.tsx';
import CourseManagement from './pages/admin/CourseManagement.tsx';
import TemplateManagement from './pages/admin/TemplateManagement.tsx';

// User pages
import UserMain from './pages/user/UserMain.tsx';
import ParticipantList from './pages/user/ParticipantList.tsx';
import Submission from './pages/user/Submission.tsx';
import ReportView from './pages/user/ReportView.tsx';

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <CourseProvider>
      <BrowserRouter>
        <Routes>
          {/* ── Admin routes (auth disabled) ──────────────────────── */}
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<Navigate to="/admin/reports" replace />} />
            <Route path="reports"   element={<ReportManagement />} />
            <Route path="users"     element={<UserManagement />} />
            <Route path="courses"   element={<CourseManagement />} />
            <Route path="templates" element={<TemplateManagement />} />
          </Route>

          {/* ── User routes ───────────────────────────────────────── */}
          <Route path="/" element={<UserLayout />}>
            <Route index element={<UserMain />} />
            <Route path="courses/:courseId/participants" element={<ParticipantList />} />
            <Route path="submit/:userId" element={<Submission />} />
            <Route path="view/:userId"   element={<ReportView />} />
          </Route>

          {/* ── Catch-all ─────────────────────────────────────────── */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
      </CourseProvider>
    </ThemeProvider>
  );
}
