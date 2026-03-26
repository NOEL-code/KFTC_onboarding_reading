import { Box } from '@mui/material';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import AppHeader from '../components/AppHeader.tsx';
import TabNavigation from '../components/TabNavigation.tsx';

const TABS = [
  { label: '독후감 관리', path: '/admin/reports' },
  { label: '사용자 관리', path: '/admin/users' },
  { label: '독서 과정 관리', path: '/admin/courses' },
  { label: '양식 관리', path: '/admin/templates' },
];

function getActiveTab(pathname: string): string {
  const match = TABS.slice()
    .reverse()
    .find((t) => pathname === t.path || pathname.startsWith(t.path + '/'));
  return match?.path ?? '/admin/reports';
}

export default function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const activeTab = getActiveTab(location.pathname);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: '#f5f6f8' }}>
      <AppHeader
        variant="admin"
        adminName="관리자"
        onLogout={() => navigate('/admin/login')}
      />

      <TabNavigation tabs={TABS} activeTab={activeTab} onTabClick={(path) => navigate(path)} />

      {/* Content */}
      <Box component="main" sx={{ flexGrow: 1, px: 4, py: 3 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
