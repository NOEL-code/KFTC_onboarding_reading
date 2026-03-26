import { Box, Typography } from '@mui/material';
import type { ReactNode } from 'react';

interface FormFieldProps {
  label: string;
  required?: boolean;
  htmlFor?: string;
  fontSize?: number;
  children: ReactNode;
}

export default function FormField({ label, required, htmlFor, fontSize = 13, children }: FormFieldProps) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      <Typography
        component={htmlFor ? 'label' : 'span'}
        htmlFor={htmlFor}
        sx={{ fontSize, fontWeight: 500, color: '#333333' }}
      >
        {label}
        {required && (
          <Box component="span" sx={{ color: '#cc3333' }}> *</Box>
        )}
      </Typography>
      {children}
    </Box>
  );
}
