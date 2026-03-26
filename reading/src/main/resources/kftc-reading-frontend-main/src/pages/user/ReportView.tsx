import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Divider,
  Link,
  TextField,
  Typography,
} from '@mui/material';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { fetchReportDetail, downloadReport, deleteReport } from '../../api/reportApi.ts';
import InfoRow from '../../components/InfoRow.tsx';
import StatusBadge from '../../components/StatusBadge.tsx';
import ConfirmModal from '../../components/ConfirmModal.tsx';
import { downloadFile } from '../../utils/downloadFile.ts';

// ─── Types ────────────────────────────────────────────────────────────────────

interface LocationState {
  courseName?: string;
  courseId?: string;
  employeeNo?: string;
  empName?: string;
  empDept?: string;
}

interface ReportData {
  courseName: string;
  employeeNo: string;
  name: string;
  dept: string;
  filename: string;
  fileSize: string;
  status: string;
  supplementReason: string;
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function ReportView() {
  const { userId: reportId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state as LocationState | null) ?? {};

  const [report, setReport] = useState<ReportData | null>(null);
  const [loading, setLoading] = useState(true);

  // 삭제 관련
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [deleteEmpNo, setDeleteEmpNo] = useState('');
  const [deleteError, setDeleteError] = useState('');
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    if (!reportId) return;
    let cancelled = false;
    setLoading(true);
    fetchReportDetail(reportId)
      .then(({ data }) => {
        if (!cancelled) {
          setReport({
            courseName:       data.courseName ?? state.courseName ?? '',
            employeeNo:       String(data.employeeNo ?? state.employeeNo ?? ''),
            name:             String(data.name ?? ''),
            dept:             String(data.team ?? ''),
            filename:         String(data.fileName ?? data.title ?? ''),
            fileSize:         data.fileSize
              ? `${Math.round(Number(data.fileSize) / 1024)} KB`
              : '',
            status:           String(data.status ?? ''),
            supplementReason: String(data.supplementReason ?? ''),
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

  function handleResubmit() {
    if (!reportId || !report) return;
    navigate(`/submit/${reportId}`, {
      state: {
        courseName: report.courseName,
        courseId: state.courseId,
        action: 'submit',
        employeeNo: report.employeeNo,
        empName: report.name,
        empDept: report.dept,
        reportId: Number(reportId),
      },
    });
  }

  async function handleDeleteConfirm() {
    if (!reportId || !report) return;
    if (deleteEmpNo.trim() !== report.employeeNo) {
      setDeleteError('사번이 일치하지 않습니다.');
      return;
    }
    setDeleteError('');
    setDeleting(true);
    try {
      await deleteReport(Number(reportId), deleteEmpNo.trim());
      navigate(-1);
    } catch {
      setDeleteError('삭제 중 오류가 발생했습니다.');
    } finally {
      setDeleting(false);
    }
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

  const isSupplement = report.status === '보완';

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
        {/* 1. Title + Status */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 1.5 }}>
          <Typography sx={{ fontSize: 22, fontWeight: 700, color: '#222222' }}>
            독후감 조회
          </Typography>
          <StatusBadge status={report.status as '승인' | '제출' | '보완' | '미제출'} />
        </Box>

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

        {/* 4. Supplement reason */}
        {isSupplement && report.supplementReason && (
          <Box
            sx={{
              bgcolor: '#fff5f5',
              border: '1px solid #fed7d7',
              borderRadius: '6px',
              px: 2.5,
              py: 2,
              display: 'flex',
              gap: 1.5,
              alignItems: 'flex-start',
            }}
          >
            <WarningAmberIcon sx={{ color: '#cc3333', fontSize: 20, mt: 0.25, flexShrink: 0 }} />
            <Box>
              <Typography sx={{ fontSize: 13, fontWeight: 700, color: '#cc3333', mb: 0.5 }}>
                보완 사유
              </Typography>
              <Typography sx={{ fontSize: 13, color: '#444444', lineHeight: 1.6, whiteSpace: 'pre-wrap' }}>
                {report.supplementReason}
              </Typography>
            </Box>
          </Box>
        )}

        {/* 5. Attached file */}
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

        {/* 6. Action buttons */}
        <Box sx={{ display: 'flex', justifyContent: 'center', gap: '12px', flexWrap: 'wrap' }}>
          {isSupplement && (
            <Button
              variant="contained"
              onClick={handleResubmit}
              sx={{ bgcolor: '#0064dd', '&:hover': { bgcolor: '#004ca8' }, minWidth: 100 }}
            >
              재제출
            </Button>
          )}
          <Button
            variant="outlined"
            onClick={() => { setDeleteOpen(true); setDeleteEmpNo(''); setDeleteError(''); }}
            sx={{
              color: '#cc3333',
              borderColor: '#cc3333',
              '&:hover': { borderColor: '#aa0000', bgcolor: '#fff5f5' },
              minWidth: 100,
            }}
          >
            삭제
          </Button>
        </Box>

        {/* 7. Divider */}
        <Divider />

        {/* 8. Back link */}
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

      {/* Delete confirm modal */}
      <ConfirmModal
        open={deleteOpen}
        title="독후감 삭제"
        confirmLabel="삭제"
        confirmColor="error"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteOpen(false)}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Typography sx={{ fontSize: 14, color: '#444444' }}>
            본인 확인을 위해 사번을 입력해주세요.
          </Typography>
          <TextField
            size="small"
            fullWidth
            placeholder="사번을 입력해주세요"
            value={deleteEmpNo}
            onChange={(e) => setDeleteEmpNo(e.target.value.trim())}
            inputProps={{ maxLength: 20 }}
          />
          {deleteError && (
            <Typography sx={{ fontSize: 12, color: '#cc3333' }}>{deleteError}</Typography>
          )}
          {deleting && (
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <CircularProgress size={20} />
            </Box>
          )}
        </Box>
      </ConfirmModal>
    </Box>
  );
}
