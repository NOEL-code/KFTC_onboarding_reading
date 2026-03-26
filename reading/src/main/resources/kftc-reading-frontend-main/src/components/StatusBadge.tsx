import { Chip } from '@mui/material';

type Status = '승인' | '제출' | '보완' | '미제출' | '진행중' | '완료' | '종료';

const STATUS_STYLES: Record<Status, { bg: string; color: string }> = {
  승인: { bg: '#e8f4fd', color: '#0064dd' },
  제출: { bg: '#fff8e1', color: '#b8860b' },
  보완: { bg: '#fce4ec', color: '#c4317b' },
  미제출: { bg: '#f5f5f5', color: '#999999' },
  진행중: { bg: '#e8f4fd', color: '#0064dd' },
  완료: { bg: '#f0f0f0', color: '#888888' },
  종료: { bg: '#fce4ec', color: '#c4317b' },
};

interface StatusBadgeProps {
  status: Status;
}

export default function StatusBadge({ status }: StatusBadgeProps) {
  const { bg, color } = STATUS_STYLES[status] ?? { bg: '#f5f5f5', color: '#999999' };

  return (
    <Chip
      label={status}
      size="small"
      sx={{
        backgroundColor: bg,
        color,
        borderRadius: '4px',
        fontSize: 12,
        fontWeight: 500,
        height: 24,
        minWidth: 68,
        '& .MuiChip-label': { px: 1, width: '100%', textAlign: 'center' },
      }}
    />
  );
}
