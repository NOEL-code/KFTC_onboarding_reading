import { Box } from '@mui/material';
import { Outlet, useNavigate } from 'react-router-dom';
import AppHeader from '../components/AppHeader.tsx';

export default function UserLayout() {
  const navigate = useNavigate();

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: '#f5f6f8' }}>
      <AppHeader
        variant="user"
        onLogoClick={() => navigate('/')}
      />

      {/* Content */}
      <Box component="main" sx={{ flexGrow: 1, px: 4, py: 3 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
