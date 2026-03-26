import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Divider,
  Typography,
} from '@mui/material';
import ArticleOutlinedIcon from '@mui/icons-material/ArticleOutlined';
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import StatusBadge from '../../components/StatusBadge.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

interface Course {
  id: number;
  name: string;
  status: '진행중' | '완료' | '종료';
  period: string;
}

// ─── Sample fallback data ─────────────────────────────────────────────────────

const SAMPLE_COURSES: Course[] = [
  { id: 1, name: '26년 상반기', status: '진행중', period: '2026.01 ~ 2026.06' },
  { id: 2, name: '25년 하반기', status: '완료',   period: '2025.07 ~ 2025.12' },
  { id: 3, name: '25년 상반기', status: '완료',   period: '2025.01 ~ 2025.06' },
];

// ─── Section card wrapper ─────────────────────────────────────────────────────

function SectionCard({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <Box
      sx={{
        bgcolor: '#ffffff',
        borderRadius: '8px',
        boxShadow: '0 1px 6px rgba(0,0,0,0.06)',
        overflow: 'hidden',
      }}
    >
      <Box sx={{ px: 3, pt: 3, pb: 2 }}>
        <Typography sx={{ fontSize: 18, fontWeight: 700, color: '#222222' }}>
          {title}
        </Typography>
      </Box>
      <Divider />
      <Box sx={{ px: 3, py: 2.5 }}>
        {children}
      </Box>
    </Box>
  );
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function UserMain() {
  const navigate = useNavigate();
  const [courses, setCourses]           = useState<Course[]>(SAMPLE_COURSES);
  const [downloadError, setDownloadError] = useState('');

  // ── Fetch recent courses (3 most recent) ───────────────────────────────────

  useEffect(() => {
    axios
      .get('/api/courses')
      .then(({ data }) => {
        const rawList = Array.isArray(data)
          ? data
          : (data.courses ?? data.content ?? []);
        if (rawList.length === 0) return;
        const list: Course[] = rawList.map((c: Record<string, unknown>) => {
          const start = String(c.startDate ?? '').slice(0, 7).replace('-', '.');
          const end   = String(c.endDate   ?? '').slice(0, 7).replace('-', '.');
          return {
            id:     Number(c.id),
            name:   String(c.name),
            status: (c.status ?? '완료') as '진행중' | '완료' | '종료',
            period: start && end ? `${start} ~ ${end}` : '',
          };
        });
        const sorted = [...list].sort((a, b) => b.id - a.id).slice(0, 3);
        setCourses(sorted);
      })
      .catch(() => { /* keep sample data */ });
  }, []);

  // ── Download handler ───────────────────────────────────────────────────────

  function handleDownload() {
    setDownloadError('');
    axios
      .get('/api/templates/download', { responseType: 'blob' })
      .then(({ data }) => {
        const url = URL.createObjectURL(data);
        const a = document.createElement('a');
        a.href = url;
        a.download = '독후감_제출양식_v2.1.hwp';
        a.click();
        URL.revokeObjectURL(url);
      })
      .catch(() => {
        setDownloadError('파일 다운로드에 실패했습니다. 관리자에게 문의해주세요.');
      });
  }

  return (
    <Box
      sx={{
        maxWidth: 1200,
        mx: 'auto',
        display: 'flex',
        flexDirection: 'column',
        gap: 4,
        px: 4,
        py: 4,
      }}
    >
      {/* ── Section 1: 양식 다운로드 ────────────────────────────────── */}
      <SectionCard title="양식 다운로드">
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            bgcolor: '#f8f9fb',
            borderRadius: '8px',
            px: 2.5,
            py: 2,
            gap: 2,
          }}
        >
          {/* File info */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: '50%',
                bgcolor: '#e8f0fe',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
              }}
            >
              <ArticleOutlinedIcon sx={{ color: '#0064dd', fontSize: 20 }} />
            </Box>
            <Box>
              <Typography sx={{ fontSize: 14, fontWeight: 500, color: '#222222' }}>
                독후감_제출양식_v2.1.hwp
              </Typography>
              <Typography sx={{ fontSize: 12, color: '#888888', mt: 0.25 }}>
                최종 수정: 2026.02.28 | 32 KB
              </Typography>
            </Box>
          </Box>

          {/* Download button */}
          <Button
            variant="contained"
            startIcon={<DownloadOutlinedIcon />}
            onClick={handleDownload}
            sx={{
              height: 40,
              bgcolor: '#0064dd',
              '&:hover': { bgcolor: '#004ca8' },
              flexShrink: 0,
              whiteSpace: 'nowrap',
            }}
          >
            다운로드
          </Button>
        </Box>

        {/* Download error message */}
        {downloadError && (
          <Typography sx={{ fontSize: 13, color: '#cc3333', mt: 1.5 }}>
            {downloadError}
          </Typography>
        )}
      </SectionCard>

      {/* ── Section 2: 독서 과정 선택 ───────────────────────────────── */}
      <SectionCard title="독서 과정 선택">
        <Box
          sx={{
            display: 'grid',
            gridTemplateColumns: 'repeat(3, 1fr)',
            gap: '20px',
          }}
        >
          {courses.map((course) => (
            <Box
              key={course.id}
              onClick={() => navigate(`/courses/${course.id}/participants`)}
              sx={{
                border: '1px solid #e0e0e0',
                borderRadius: '8px',
                p: '20px',
                cursor: 'pointer',
                display: 'flex',
                flexDirection: 'column',
                gap: 1.5,
                transition: 'border-color 0.15s',
                '&:hover': {
                  borderColor: '#0064dd',
                  boxShadow: '0 2px 8px rgba(0, 100, 221, 0.08)',
                },
              }}
            >
              {/* Name + status */}
              <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <Typography sx={{ fontSize: 16, fontWeight: 700, color: '#222222' }}>
                  {course.name}
                </Typography>
                <StatusBadge status={course.status} />
              </Box>

              {/* Period */}
              <Typography sx={{ fontSize: 13, color: '#888888' }}>
                {course.period}
              </Typography>
            </Box>
          ))}
        </Box>
      </SectionCard>
    </Box>
  );
}
