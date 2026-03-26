import { useState } from 'react';
import {
  Box,
  Button,
  TextField,
  Typography,
  CircularProgress,
} from '@mui/material';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext.tsx';

export default function AdminLogin() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Redirect to the page the user originally tried to visit, or /admin/reports
  const from: string = (location.state as { from?: { pathname: string } } | null)
    ?.from?.pathname ?? '/admin/reports';

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const { data } = await axios.post('/api/admin/login', { loginId: username, password });
      login(data?.token ?? 'authenticated');
      navigate(from, { replace: true });
    } catch {
      setError('아이디 또는 비밀번호가 올바르지 않습니다.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh', bgcolor: '#f5f6f8' }}>
      {/* Header */}
      <Box
        sx={{
          bgcolor: '#ffffff',
          borderBottom: '1px solid #e0e0e0',
          px: 4,
          height: 56,
          display: 'flex',
          alignItems: 'center',
          flexShrink: 0,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 1 }}>
          <Typography
            variant="h6"
            component="span"
            sx={{ fontWeight: 700, color: '#093f81', lineHeight: 1 }}
          >
            KFTC
          </Typography>
          <Typography component="span" sx={{ fontSize: 14, color: '#666666' }}>
            독후감 제출 시스템
          </Typography>
        </Box>
      </Box>

      {/* Centered content */}
      <Box
        sx={{
          flexGrow: 1,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          px: 2,
        }}
      >
        <Box
          component="form"
          onSubmit={handleSubmit}
          sx={{
            width: 440,
            bgcolor: '#ffffff',
            borderRadius: '12px',
            boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
            p: '48px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '28px',
          }}
        >
          {/* Lock icon */}
          <Box
            sx={{
              width: 64,
              height: 64,
              borderRadius: '50%',
              bgcolor: '#e8f0fe',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <LockOutlinedIcon sx={{ color: '#0064dd', fontSize: 28 }} />
          </Box>

          {/* Title + subtitle */}
          <Box sx={{ textAlign: 'center', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <Typography sx={{ fontSize: 24, fontWeight: 700, color: '#222222' }}>
              관리자 로그인
            </Typography>
            <Typography sx={{ fontSize: 14, color: '#888888' }}>
              독후감 제출 시스템 관리자 페이지입니다.
            </Typography>
          </Box>

          {/* Form fields */}
          <Box sx={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {/* ID field */}
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <Typography
                component="label"
                htmlFor="admin-username"
                sx={{ fontSize: 14, fontWeight: 500, color: '#333333' }}
              >
                아이디
              </Typography>
              <TextField
                id="admin-username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="아이디를 입력해주세요"
                size="small"
                fullWidth
                autoComplete="username"
                required
              />
            </Box>

            {/* Password field */}
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
              <Typography
                component="label"
                htmlFor="admin-password"
                sx={{ fontSize: 14, fontWeight: 500, color: '#333333' }}
              >
                비밀번호
              </Typography>
              <TextField
                id="admin-password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="비밀번호를 입력해주세요"
                size="small"
                fullWidth
                autoComplete="current-password"
                required
              />
            </Box>

            {/* Inline error */}
            {error && (
              <Typography sx={{ fontSize: 13, color: '#cc3333', mt: -1 }}>
                {error}
              </Typography>
            )}
          </Box>

          {/* Login button */}
          <Button
            type="submit"
            variant="contained"
            fullWidth
            disabled={loading}
            sx={{
              height: 40,
              bgcolor: '#093f81',
              '&:hover': { bgcolor: '#072f61' },
              '&:disabled': { bgcolor: '#093f81', opacity: 0.6 },
              fontSize: 15,
              fontWeight: 700,
              color: '#ffffff',
              borderRadius: '6px',
            }}
          >
            {loading ? <CircularProgress size={20} sx={{ color: '#ffffff' }} /> : '로그인'}
          </Button>

          {/* Footer */}
          <Typography sx={{ fontSize: 12, color: '#aaaaaa', textAlign: 'center' }}>
            © 2026 금융결제원 (KFTC). All rights reserved.
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}
