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
import axios from 'axios';
import FileDropzone from '../../components/FileDropzone.tsx';
import FormField from '../../components/FormField.tsx';
import InfoRow from '../../components/InfoRow.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

interface LocationState {
  courseName?: string;
  courseId?: string;
  action?: 'submit' | 'view';
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function Submission() {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const state = (location.state as LocationState | null) ?? {};

  const courseName = state.courseName ?? '';
  const courseId   = state.courseId   ?? '';
  // 'submit' action with an existing reportId means resubmission (PUT)
  const isUpdate   = state.action === 'submit' && !!userId;

  const [employeeNo, setEmployeeNo]   = useState('');
  const [empName, setEmpName]         = useState('');
  const [empDept, setEmpDept]         = useState('');
  const [empLoading, setEmpLoading]   = useState(false);
  const [empError, setEmpError]       = useState('');
  const [file, setFile]               = useState<File | null>(null);
  const [submitting, setSubmitting]   = useState(false);
  const [submitError, setSubmitError] = useState('');

  // ── Fetch employee name/dept when employee number changes ──────────────────

  useEffect(() => {
    const trimmed = employeeNo.trim();
    if (!trimmed) {
      setEmpName('');
      setEmpDept('');
      setEmpError('');
      return;
    }
    if (trimmed.length !== 7) {
      setEmpName('');
      setEmpDept('');
      setEmpError('사번은 7자리입니다.');
      return;
    }
    const timer = setTimeout(() => {
      setEmpLoading(true);
      setEmpError('');
      axios
        .get(`/api/employees/${trimmed}`)
        .then(({ data }) => {
          setEmpName(data.name ?? data.employeeName ?? '');
          setEmpDept(data.dept ?? data.team ?? '');
        })
        .catch(() => {
          setEmpName('');
          setEmpDept('');
          setEmpError('등록되지 않은 사번입니다.');
        })
        .finally(() => setEmpLoading(false));
    }, 500);
    return () => clearTimeout(timer);
  }, [employeeNo]);

  // ── Derived state ──────────────────────────────────────────────────────────

  const empNoFilled = employeeNo.trim().length === 7;
  const canSubmit   = empNoFilled && empName !== '' && file !== null;

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
        await axios.put(`/api/reports/${userId}`, formData);
      } else {
        await axios.post('/api/reports', formData);
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
          <InfoRow label="이름"       value={empName || (empLoading ? '조회 중...' : '-')} />
          <InfoRow label="팀"         value={empDept || (empLoading ? '조회 중...' : '-')} />
        </Box>

        {/* 4. Employee number input */}
        <FormField label="사번" required htmlFor="employee-no" fontSize={14}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <TextField
              id="employee-no"
              size="small"
              fullWidth
              placeholder="사번 7자리를 입력해주세요"
              value={employeeNo}
              onChange={(e) => {
                const digits = e.target.value.replace(/\D/g, '');
                setEmployeeNo(digits);
              }}
              inputProps={{ maxLength: 7, inputMode: 'numeric' }}
            />
            {empLoading && <CircularProgress size={18} sx={{ flexShrink: 0 }} />}
          </Box>
          {empError && (
            <Typography sx={{ fontSize: 12, color: '#cc3333' }}>{empError}</Typography>
          )}
          {!empError && !empName && (
            <Typography sx={{ fontSize: 12, color: '#888888' }}>
              7자리 사번을 입력하면 이름과 팀이 자동으로 조회됩니다.
            </Typography>
          )}
          {!empError && empName && (
            <Typography sx={{ fontSize: 12, color: '#1a7f3c' }}>
              사용자 정보가 확인되었습니다.
            </Typography>
          )}
        </FormField>

        {/* 5. File upload */}
        <FormField label="독후감 파일 업로드 (.hwp)" fontSize={14}>
          <FileDropzone
            accept=".hwp"
            disabled={!empNoFilled || !empName}
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
