import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    primary: {
      main: '#0064dd',
      dark: '#004ca8',
      light: '#0f7cff',
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#093f81',
      contrastText: '#ffffff',
    },
    error: {
      main: '#cc3333',
    },
    background: {
      default: '#f5f6f8',
      paper: '#ffffff',
    },
    text: {
      primary: '#222222',
      secondary: '#666666',
      disabled: '#aaaaaa',
    },
    divider: '#e0e0e0',
  },
  typography: {
    fontFamily: [
      'Inter',
      '-apple-system',
      'BlinkMacSystemFont',
      '"Segoe UI"',
      'Roboto',
      '"Helvetica Neue"',
      'Arial',
      'sans-serif',
    ].join(','),
  },
  shape: {
    borderRadius: 6,
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          textTransform: 'none',
          fontWeight: 700,
          padding: '10px 24px',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          boxShadow: '0 1px 6px rgba(0,0,0,0.06)',
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        rounded: {
          borderRadius: 8,
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-head': {
            backgroundColor: '#f5f6f8',
            fontWeight: 700,
            color: '#555555',
          },
        },
      },
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:nth-of-type(odd)': {
            backgroundColor: '#ffffff',
          },
          '&:nth-of-type(even)': {
            backgroundColor: '#fafbfc',
          },
        },
      },
    },
  },
});

export default theme;
