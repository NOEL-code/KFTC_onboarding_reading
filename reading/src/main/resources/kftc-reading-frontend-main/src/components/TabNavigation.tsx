import { Box } from '@mui/material';

interface Tab {
  label: string;
  path: string;
}

interface TabNavigationProps {
  tabs: Tab[];
  activeTab: string;
  onTabClick: (path: string) => void;
}

export default function TabNavigation({ tabs, activeTab, onTabClick }: TabNavigationProps) {
  return (
    <Box
      sx={{
        bgcolor: '#ffffff',
        borderBottom: '1px solid #e0e0e0',
        px: 4,
        display: 'flex',
        alignItems: 'flex-end',
        flexShrink: 0,
      }}
    >
      {tabs.map((tab) => {
        const isActive = activeTab === tab.path;
        return (
          <Box
            key={tab.path}
            onClick={() => onTabClick(tab.path)}
            sx={{
              px: 2,
              py: 1.5,
              cursor: 'pointer',
              fontSize: 14,
              fontWeight: isActive ? 700 : 400,
              color: isActive ? '#0064dd' : '#666666',
              borderBottom: isActive ? '2px solid #0064dd' : '2px solid transparent',
              userSelect: 'none',
              '&:hover': { color: '#0064dd' },
            }}
          >
            {tab.label}
          </Box>
        );
      })}
    </Box>
  );
}
