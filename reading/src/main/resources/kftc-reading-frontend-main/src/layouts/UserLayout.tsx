import { useEffect } from 'react';
import { Box } from '@mui/material';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import AppHeader from '../components/AppHeader.tsx';
import TabNavigation from '../components/TabNavigation.tsx';
import { useCourse } from '../context/CourseContext.tsx';

function getActiveTab(pathname: string, currentCourseId: number | undefined): string {
  if (['/submit', '/view', '/courses'].some((p) => pathname.startsWith(p))) {
    return currentCourseId ? `/courses/${currentCourseId}/participants` : '/';
  }
  return pathname === '/' ? '/' : '';
}

export default function UserLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { courses, setSelectedCourseId } = useCourse();

  const currentCourse = courses.find((c) => c.status === '진행중');

  const TABS = [
    { label: '홈', path: '/' },
    ...(currentCourse
      ? [{ label: '참여자 목록', path: `/courses/${currentCourse.id}/participants` }]
      : []),
  ];

  const activeTab = getActiveTab(location.pathname, currentCourse?.id);

  // Sync URL course ID → context so the header dropdown reflects the current page
  const urlCourseId = location.pathname.match(/\/courses\/(\d+)\//)?.[1] ?? '';
  useEffect(() => {
    setSelectedCourseId(urlCourseId);
  }, [urlCourseId, setSelectedCourseId]);

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: '#f5f6f8' }}>
      <AppHeader
        variant="user"
        onLogoClick={() => navigate('/')}
        onCourseSelect={(id) => navigate(`/courses/${id}/participants`)}
      />

      <TabNavigation tabs={TABS} activeTab={activeTab} onTabClick={(path) => navigate(path)} />

      {/* Content */}
      <Box component="main" sx={{ flexGrow: 1, px: 4, py: 3 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
