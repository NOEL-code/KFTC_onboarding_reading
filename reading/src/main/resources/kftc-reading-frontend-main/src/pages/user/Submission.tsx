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
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { fetchEmployee } from '../../api/userApi.ts';
import { submitReport, updateReport } from '../../api/reportApi.ts';
import FileDropzone from '../../components/FileDropzone.tsx';
import FormField from '../../components/FormField.tsx';
import InfoRow from '../../components/InfoRow.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

interface LocationState {
  courseName?: string;
  courseId?: string;
  action?: 'submit' | 'view';
  employeeNo?: string;
  empName?: string;
  empDept?: string;
  reportId?: number;
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function Submission() {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state as LocationState | null) ?? {};

  const courseName = state.courseName ?? '';
  const courseId   = state.courseId   ?? '';
  // reportId가 있으면 재제출(PUT), 없거나 'new'이면 신규 제출(POST)
  const reportId   = state.reportId ?? (userId && userId !== 'new' ? Number(userId) : 0);
  const isUpdate   = reportId > 0;

  // 참여자 목록에서 넘어온 경우: 사번 검증 모드 (직접 입력해서 일치해야 함)
  const expectedEmpNo = state.employeeNo ?? '';
  const hasExpected   = !!(expectedEmpNo && state.empName);

  const [employeeNo, setEmployeeNo]   = useState('');
  const [empName, setEmpName]         = useState('');
  const [empDept, setEmpDept]         = useState('');
  const [empLoading, setEmpLoading]   = useState(false);
  const [empError, setEmpError]       = useState('');
  const [file, setFile]               = useState<File | null>(null);
  const [submitting, setSubmitting]   = useState(false);
  const [submitError, setSubmitError] = useState('');

  // ── 사번 입력 시 검증 ────────────────────────────────────────────────────────

  const empNoMatched = hasExpected && employeeNo.trim() === expectedEmpNo;

  useEffect(() => {
    const trimmed = employeeNo.trim();

    // 참여자 목록에서 넘어온 경우: 사번 일치 여부로 이름/팀 표시
    if (hasExpected) {
      if (trimmed === expectedEmpNo) {
        setEmpName(state.empName ?? '');
        setEmpDept(state.empDept ?? '');
        setEmpError('');
      } else {
        setEmpName('');
        setEmpDept('');
        setEmpError(trimmed.length >= 5 ? '사번이 일치하지 않습니다.' : '');
      }
      return;
    }

    // 직접 접근한 경우: API로 사번 조회
    if (!trimmed) {
      setEmpName('');
      setEmpDept('');
      setEmpError('');
      return;
    }
    if (trimmed.length < 5) {
      setEmpName('');
      setEmpDept('');
      return;
    }
    const timer = setTimeout(() => {
      setEmpLoading(true);
      setEmpError('');
      fetchEmployee(trimmed)
        .then(({ data }) => {
          setEmpName(String(data.name ?? ''));
          setEmpDept(String(data.team ?? ''));
        })
        .catch(() => {
          setEmpName('');
          setEmpDept('');
          setEmpError('등록되지 않은 사번입니다.');
        })
        .finally(() => setEmpLoading(false));
    }, 500);
    return () => clearTimeout(timer);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [employeeNo]);

  // ── Derived state ──────────────────────────────────────────────────────────

  const empVerified = hasExpected ? empNoMatched : (employeeNo.trim().length >= 5 && empName !== '');
  const canSubmit   = empVerified && file !== null;

  // ── Submit handler ─────────────────────────────────────────────────────────

  async function handleSubmit() {
    if (!canSubmit) return;
    setSubmitError('');
    setSubmitting(true);
    try {
      const formData = new FormData();
      formData.append('employeeNo', employeeNo.trim());
      formData.append('title', file!.name);
      formData.append('file', file!);
      formData.append('courseId', courseId);

      if (isUpdate) {
        await updateReport(String(reportId), formData);
      } else {
        await submitReport(formData);
      }
      navigate(-1);
    } catch {
      setSubmitError('제출 중 오류가 발생했습니다. 다시 시도해주세요.');
    } finally {
      setSubmitting(false);
    }
  }

  // ── Render ─────────────────────────────────────────────────────────────────

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
          독후감 제출
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
          <InfoRow label="독서과정명" value={courseName} />
          <InfoRow label="이름"       value={(hasExpected ? state.empName : empName) || (empLoading ? '조회 중...' : '-')} />
          <InfoRow label="팀"         value={(hasExpected ? state.empDept : empDept) || (empLoading ? '조회 중...' : '-')} />
        </Box>

        {/* 4. Employee number input — 항상 직접 입력 */}
        <FormField label="사번" required htmlFor="employee-no" fontSize={14}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TextField
              id="employee-no"
              size="small"
              fullWidth
              placeholder="본인의 사번을 입력해주세요"
              value={employeeNo}
              onChange={(e) => setEmployeeNo(e.target.value.trim())}
              inputProps={{ maxLength: 20 }}
            />
            {empLoading && <CircularProgress size={18} sx={{ flexShrink: 0 }} />}
          </Box>
          {empError && (
            <Typography sx={{ fontSize: 12, color: '#cc3333' }}>{empError}</Typography>
          )}
          {!empError && !empName && !empLoading && (
            <Typography sx={{ fontSize: 12, color: '#888888' }}>
              {hasExpected ? '본인 확인을 위해 사번을 입력해주세요.' : '사번을 입력하면 이름과 팀이 자동으로 조회됩니다.'}
            </Typography>
          )}
          {!empError && empName && (
            <Typography sx={{ fontSize: 12, color: '#1a7f3c' }}>
              사용자 정보가 확인되었습니다.
            </Typography>
          )}
        </FormField>

        {/* 5. File upload */}
        <FormField label="독후감 파일 업로드 (.hwp, .hwpx)" fontSize={14}>
          <FileDropzone
            accept=".hwp,.hwpx"
            disabled={!empVerified}
            file={file}
            onFileChange={setFile}
          />
        </FormField>

        {/* 6. Action buttons */}
        <Box sx={{ display: 'flex', justifyContent: 'center', gap: '12px', flexWrap: 'wrap' }}>
          <Button
            variant="contained"
            disabled={!canSubmit || submitting}
            onClick={handleSubmit}
            sx={{ bgcolor: '#0064dd', '&:hover': { bgcolor: '#004ca8' }, minWidth: 100 }}
          >
            {submitting ? <CircularProgress size={18} sx={{ color: '#ffffff' }} /> : '제출'}
          </Button>
        </Box>

        {/* Submit error */}
        {submitError && (
          <Typography sx={{ fontSize: 13, color: '#cc3333', textAlign: 'center', mt: -1 }}>
            {submitError}
          </Typography>
        )}

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
    </Box>
  );
}
