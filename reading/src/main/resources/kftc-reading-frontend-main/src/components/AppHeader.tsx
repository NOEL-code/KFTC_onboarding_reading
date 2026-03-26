import { Box, Button, MenuItem, Select, Typography } from '@mui/material';
import type { SelectChangeEvent } from '@mui/material';
import { useCourse } from '../context/CourseContext.tsx';

type AppHeaderProps =
  | { variant: 'user'; onLogoClick: () => void; onCourseSelect: (id: string) => void; }
  | { variant: 'admin'; adminName?: string; onLogout: () => void; };

function CourseDropdown({ onSelect }: { onSelect: (id: string) => void }) {
  const { courses, selectedCourseId } = useCourse();

  return (
    <Select
      size="small"
      value={selectedCourseId}
      onChange={(e: SelectChangeEvent<string>) => onSelect(e.target.value)}
      displayEmpty
      renderValue={(val) => {
        if (!val) {
          return (
            <Typography component="span" sx={{ fontSize: 14, color: '#999999' }}>
              독서과정 선택
            </Typography>
          );
        }
        const course = courses.find((c) => String(c.id) === val);
        return course?.name ?? val;
      }}
      sx={{ fontSize: 14, minWidth: 160 }}
    >
      {courses.map((course) => (
        <MenuItem key={course.id} value={String(course.id)} sx={{ fontSize: 14 }}>
          {course.name}
        </MenuItem>
      ))}
    </Select>
  );
}

export default function AppHeader(props: AppHeaderProps) {
  const { setSelectedCourseId } = useCourse();

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
      {props.variant === 'user' ? (
        <CourseDropdown onSelect={props.onCourseSelect} />
      ) : (
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          {props.adminName && (
            <Typography sx={{ fontSize: 14, color: '#444444' }}>{props.adminName}</Typography>
          )}
          <CourseDropdown onSelect={setSelectedCourseId} />
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
