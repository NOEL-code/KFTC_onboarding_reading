import { Box, Button, Typography } from '@mui/material';

type AppHeaderProps =
  | { variant: 'user'; onLogoClick: () => void; }
  | { variant: 'admin'; adminName?: string; onLogout: () => void; };

export default function AppHeader(props: AppHeaderProps) {
  return (
    <Box
      sx={{
        bgcolor: '#ffffff',
        borderBottom: '1px solid #e0e0e0',
        px: 4,
        height: 56,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexShrink: 0,
      }}
    >
      {/* Brand */}
      <Box
        onClick={props.variant === 'user' ? props.onLogoClick : undefined}
        sx={{
          display: 'flex',
          alignItems: 'baseline',
          gap: 1,
          cursor: props.variant === 'user' ? 'pointer' : 'default',
        }}
      >
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

      {/* Right side */}
      {props.variant === 'user' ? null : (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {props.adminName && (
            <Typography sx={{ fontSize: 14, color: '#444444' }}>{props.adminName}</Typography>
          )}
          <Button
            variant="outlined"
            onClick={props.onLogout}
            sx={{
              height: 40,
              fontSize: 12,
              color: '#666666',
              borderColor: '#cccccc',
              '&:hover': { borderColor: '#aaaaaa' },
            }}
          >
            로그아웃
          </Button>
        </Box>
      )}
    </Box>
  );
}
