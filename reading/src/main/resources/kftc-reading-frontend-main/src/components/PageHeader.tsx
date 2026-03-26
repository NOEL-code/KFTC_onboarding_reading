import { Box, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface PageHeaderProps {
  title: string;
  children?: ReactNode;
}

export default function PageHeader({ title, children }: PageHeaderProps) {
  return (
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 3 }}>
      <Typography sx={{ fontSize: 22, fontWeight: 700, color: '#222222' }}>
        {title}
      </Typography>
      {children && <Box sx={{ display: 'flex', gap: 1 }}>{children}</Box>}
    </Box>
  );
}
