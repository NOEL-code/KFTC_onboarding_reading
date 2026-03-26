import { Box, Typography } from '@mui/material';

interface InfoRowProps {
  label: string;
  value: string;
}

export default function InfoRow({ label, value }: InfoRowProps) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
      <Typography
        sx={{ width: 120, flexShrink: 0, fontSize: 14, fontWeight: 500, color: '#666666' }}
      >
        {label}
      </Typography>
      <Typography sx={{ fontSize: 14, color: '#222222' }}>{value}</Typography>
    </Box>
  );
}
