import { useEffect, useState } from 'react';
import {
  Box,
  Divider,
  Link,
  Typography,
} from '@mui/material';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { fetchReportDetail, downloadReport } from '../../api/reportApi.ts';
import InfoRow from '../../components/InfoRow.tsx';
import { downloadFile } from '../../utils/downloadFile.ts';

// ─── Types ────────────────────────────────────────────────────────────────────

interface ReportData {
  courseName: string;
  name: string;
  dept: string;
  filename: string;
  fileSize: string;
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function ReportView() {
  const { userId: reportId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const stateCourseName = (location.state as { courseName?: string } | null)?.courseName;

  const [report, setReport] = useState<ReportData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!reportId) return;
    let cancelled = false;
    setLoading(true);
    fetchReportDetail(reportId)
      .then(({ data }) => {
        if (!cancelled) {
          setReport({
            courseName: data.courseName ?? stateCourseName ?? '',
            name:       String(data.name ?? ''),
            dept:       String(data.team ?? ''),
            filename:   String(data.fileName ?? data.title ?? ''),
            fileSize:   data.fileSize
              ? `${Math.round(Number(data.fileSize) / 1024)} KB`
              : '',
          });
        }
      })
      .catch(() => {})
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [reportId]);  // eslint-disable-line react-hooks/exhaustive-deps

  function handleFileDownload() {
    if (!reportId || !report) return;
    downloadReport(Number(reportId), report.filename)
      .then(({ data }) => downloadFile(data, report.filename))
      .catch(() => {});
  }

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 10 }}>
        <Typography sx={{ fontSize: 14, color: '#888888' }}>불러오는 중...</Typography>
      </Box>
    );
  }

  if (!report) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', py: 10 }}>
        <Typography sx={{ fontSize: 14, color: '#cc3333' }}>독후감 정보를 불러올 수 없습니다.</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', px: 2 }}>
      <Box
        sx={{
          width: '100%',
          maxWidth: 720,
          bgcolor: '#ffffff',
          borderRadius: '8px',
          boxShadow: '0 4px 20px rgba(0,0,0,0.08)',
          p: '40px',
          display: 'flex',
          flexDirection: 'column',
          gap: '20px',
        }}
      >
        {/* 1. Title */}
        <Typography sx={{ fontSize: 22, fontWeight: 700, color: '#222222', textAlign: 'center' }}>
          독후감 조회
        </Typography>

        {/* 2. Divider */}
        <Divider />

        {/* 3. User info box */}
        <Box
          sx={{
            bgcolor: '#f8f9fb',
            borderRadius: '6px',
            px: '20px',
            py: '16px',
            display: 'flex',
            flexDirection: 'column',
            gap: 1.5,
          }}
        >
          <InfoRow label="독서과정명" value={report.courseName} />
          <InfoRow label="이름"       value={report.name}       />
          <InfoRow label="팀"         value={report.dept}       />
        </Box>

        {/* 4. Attached file (read-only, clickable to download) */}
        <Box
          onClick={handleFileDownload}
          sx={{
            display: 'flex',
            alignItems: 'center',
            gap: 1.5,
            bgcolor: '#f0f7ff',
            borderRadius: '6px',
            px: 2,
            py: 1.5,
            cursor: 'pointer',
            '&:hover': { bgcolor: '#e4f0ff' },
            transition: 'background-color 0.15s',
          }}
        >
          <AttachFileIcon sx={{ color: '#0064dd', fontSize: 18, flexShrink: 0 }} />
          <Typography
            sx={{
              fontSize: 14,
              color: '#0064dd',
              fontWeight: 500,
              textDecoration: 'underline',
              flexGrow: 1,
            }}
          >
            {report.filename}
          </Typography>
          {report.fileSize && (
            <Typography sx={{ fontSize: 12, color: '#888888', whiteSpace: 'nowrap' }}>
              ({report.fileSize})
            </Typography>
          )}
        </Box>

        {/* 5. Back link */}
        <Box sx={{ textAlign: 'center' }}>
          <Link
            component="button"
            underline="hover"
            onClick={() => navigate(-1)}
            sx={{ fontSize: 13, color: '#0064dd', cursor: 'pointer', background: 'none', border: 'none' }}
          >
            ← 사용자 목록으로 돌아가기
          </Link>
        </Box>
      </Box>
    </Box>
  );
}
