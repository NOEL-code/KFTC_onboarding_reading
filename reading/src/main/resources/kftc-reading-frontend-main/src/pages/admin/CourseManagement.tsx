import { useState, useEffect } from 'react';
import {
  Box,
  Button,
  Link,
  TextField,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PeopleOutlineIcon from '@mui/icons-material/PeopleOutline';
import CheckCircleOutlineIcon from '@mui/icons-material/CheckCircleOutline';
import axios from 'axios';
import StatusBadge from '../../components/StatusBadge.tsx';
import ConfirmModal from '../../components/ConfirmModal.tsx';
import FormDialog from '../../components/FormDialog.tsx';
import FormField from '../../components/FormField.tsx';
import PageHeader from '../../components/PageHeader.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

type CourseStatus = '진행중' | '완료' | '종료';

interface Course {
  id: number;
  name: string;
  status: CourseStatus;
  startDate: string;
  endDate: string;
  totalUsers: number;
  submittedUsers: number;
}

interface CourseForm {
  name: string;
  startDate: string;
  endDate: string;
}

const EMPTY_FORM: CourseForm = { name: '', startDate: '', endDate: '' };


// ─── Course Form Dialog ───────────────────────────────────────────────────────

interface CourseFormDialogProps {
  open: boolean;
  mode: 'create' | 'edit';
  initialValues: CourseForm;
  error: string;
  onClose: () => void;
  onSubmit: (values: CourseForm) => void;
}

function CourseFormDialog({
  open,
  mode,
  initialValues,
  error,
  onClose,
  onSubmit,
}: CourseFormDialogProps) {
  const [form, setForm] = useState<CourseForm>(initialValues);

  function set(key: keyof CourseForm, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  return (
    <FormDialog
      open={open}
      title={mode === 'create' ? '독서 과정 생성' : '독서 과정 수정'}
      confirmLabel={mode === 'create' ? '생성하기' : '저장'}
      width={520}
      onClose={onClose}
      onConfirm={() => onSubmit(form)}
      onEnter={() => setForm(initialValues)}
    >
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
        <FormField label="과정명" required htmlFor="course-name">
          <TextField
            id="course-name"
            size="small"
            fullWidth
            placeholder="예: 26년 상반기"
            value={form.name}
            onChange={(e) => set('name', e.target.value)}
          />
        </FormField>

        <FormField label="기간" required>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
            <TextField
              size="small"
              fullWidth
              placeholder="YYYY-MM-DD"
              value={form.startDate}
              onChange={(e) => set('startDate', e.target.value)}
              inputProps={{ maxLength: 10 }}
            />
            <Typography sx={{ color: '#888888', flexShrink: 0 }}>~</Typography>
            <TextField
              size="small"
              fullWidth
              placeholder="YYYY-MM-DD"
              value={form.endDate}
              onChange={(e) => set('endDate', e.target.value)}
              inputProps={{ maxLength: 10 }}
            />
          </Box>
        </FormField>

        {error && (
          <Typography sx={{ fontSize: 13, color: '#cc3333' }}>{error}</Typography>
        )}
      </Box>
    </FormDialog>
  );
}

// ─── Course Card ──────────────────────────────────────────────────────────────

interface CourseCardProps {
  course: Course;
  onEdit: (course: Course) => void;
  onDelete: (course: Course) => void;
}

function CourseCard({ course, onEdit, onDelete }: CourseCardProps) {
  return (
    <Box
      sx={{
        bgcolor: '#ffffff',
        border: '1px solid #e0e0e0',
        borderRadius: '8px',
        p: '20px',
        minHeight: 180,
        display: 'flex',
        flexDirection: 'column',
        gap: 1.5,
      }}
    >
      {/* Title + status */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <Typography sx={{ fontSize: 16, fontWeight: 700, color: '#222222' }}>
          {course.name}
        </Typography>
        <StatusBadge status={course.status} />
      </Box>

      {/* Period */}
      <Typography sx={{ fontSize: 13, color: '#888888' }}>
        {course.startDate} ~ {course.endDate}
      </Typography>

      {/* Stats */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.75, flexGrow: 1 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <PeopleOutlineIcon sx={{ fontSize: 15, color: '#aaaaaa' }} />
          <Typography sx={{ fontSize: 13, color: '#555555' }}>
            신청 인원:{' '}
            <Box component="span" sx={{ fontWeight: 700 }}>
              {course.totalUsers}명
            </Box>
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <CheckCircleOutlineIcon sx={{ fontSize: 15, color: '#aaaaaa' }} />
          <Typography sx={{ fontSize: 13, color: '#555555' }}>
            제출 완료:{' '}
            <Box component="span" sx={{ fontWeight: 700, color: '#0064dd' }}>
              {course.submittedUsers}명
            </Box>
          </Typography>
        </Box>
      </Box>

      {/* Actions */}
      <Box sx={{ display: 'flex', gap: 2, pt: 0.5, borderTop: '1px solid #f0f0f0', mt: 0.5 }}>
        <Link
          component="button"
          underline="hover"
          onClick={() => onEdit(course)}
          sx={{ fontSize: 13, color: '#0064dd', background: 'none', border: 'none', cursor: 'pointer', p: 0 }}
        >
          수정
        </Link>
        <Link
          component="button"
          underline="hover"
          onClick={() => onDelete(course)}
          sx={{ fontSize: 13, color: '#cc3333', background: 'none', border: 'none', cursor: 'pointer', p: 0 }}
        >
          삭제
        </Link>
      </Box>
    </Box>
  );
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function CourseManagement() {
  const [courses, setCourses]       = useState<Course[]>([]);
  const [createOpen, setCreateOpen] = useState(false);

  useEffect(() => {
    axios
      .get('/api/courses')
      .then(({ data }) => {
        const list: Course[] = (data.courses ?? data.content ?? (Array.isArray(data) ? data : [])).map(
          (c: Record<string, unknown>) => ({
            id:             Number(c.id ?? c.courseId),
            name:           String(c.name),
            status:         (c.status ?? '진행중') as CourseStatus,
            startDate:      String(c.startDate ?? '').slice(0, 7),
            endDate:        String(c.endDate   ?? '').slice(0, 7),
            totalUsers:     Number(c.totalUsers     ?? 0),
            submittedUsers: Number(c.submittedUsers ?? 0),
          })
        );
        if (list.length > 0) setCourses(list);
      })
      .catch(() => {});
  }, []);
  const [editCourse, setEditCourse] = useState<Course | null>(null);
  const [deleteCourse, setDeleteCourse] = useState<Course | null>(null);
  const [formError, setFormError]   = useState('');

  // ── Validation ─────────────────────────────────────────────────────────────

  function validate(form: CourseForm): string {
    if (!form.name.trim())      return '과정명을 입력해주세요.';
    if (!form.startDate.trim()) return '시작일을 입력해주세요.';
    if (!form.endDate.trim())   return '종료일을 입력해주세요.';
    return '';
  }

  // ── CRUD ───────────────────────────────────────────────────────────────────

  function handleCreate(form: CourseForm) {
    const err = validate(form);
    if (err) { setFormError(err); return; }

    const newCourse: Course = {
      id: Date.now(),
      name: form.name.trim(),
      status: '진행중',
      startDate: form.startDate.trim(),
      endDate: form.endDate.trim(),
      totalUsers: 0,
      submittedUsers: 0,
    };
    setCourses((prev) => [newCourse, ...prev]);
    setCreateOpen(false);
    setFormError('');
    axios.post('/api/admin/courses', form).catch(() => {});
  }

  function handleEdit(form: CourseForm) {
    const err = validate(form);
    if (err) { setFormError(err); return; }
    if (!editCourse) return;

    setCourses((prev) =>
      prev.map((c) =>
        c.id === editCourse.id
          ? { ...c, name: form.name.trim(), startDate: form.startDate.trim(), endDate: form.endDate.trim() }
          : c,
      ),
    );
    setEditCourse(null);
    setFormError('');
    axios.put(`/api/admin/courses/${editCourse.id}`, form).catch(() => {});
  }

  function handleDeleteConfirm() {
    if (!deleteCourse) return;
    setCourses((prev) => prev.filter((c) => c.id !== deleteCourse.id));
    setDeleteCourse(null);
    axios.delete(`/api/admin/courses/${deleteCourse.id}`).catch(() => {});
  }

  function openCreate() {
    setFormError('');
    setCreateOpen(true);
  }

  function openEdit(course: Course) {
    setFormError('');
    setEditCourse(course);
  }

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <Box>
      <PageHeader title="독서 과정 관리">
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={openCreate}
          sx={{ height: 40, bgcolor: '#0064dd', '&:hover': { bgcolor: '#004ca8' } }}
        >
          과정 생성
        </Button>
      </PageHeader>

      {/* Cards grid — 4 columns, responsive; sorted newest startDate first */}
      <Box
        sx={{
          display: 'grid',
          gridTemplateColumns: {
            xs: '1fr',
            sm: 'repeat(2, 1fr)',
            md: 'repeat(3, 1fr)',
            lg: 'repeat(4, 1fr)',
          },
          gap: 2,
        }}
      >
        {[...courses].sort((a, b) => b.startDate.localeCompare(a.startDate)).map((course) => (
          <CourseCard
            key={course.id}
            course={course}
            onEdit={openEdit}
            onDelete={setDeleteCourse}
          />
        ))}
      </Box>

      {/* Create dialog */}
      <CourseFormDialog
        open={createOpen}
        mode="create"
        initialValues={EMPTY_FORM}
        error={formError}
        onClose={() => { setCreateOpen(false); setFormError(''); }}
        onSubmit={handleCreate}
      />

      {/* Edit dialog */}
      <CourseFormDialog
        open={!!editCourse}
        mode="edit"
        initialValues={
          editCourse
            ? { name: editCourse.name, startDate: editCourse.startDate, endDate: editCourse.endDate }
            : EMPTY_FORM
        }
        error={formError}
        onClose={() => { setEditCourse(null); setFormError(''); }}
        onSubmit={handleEdit}
      />

      {/* Delete confirm */}
      <ConfirmModal
        open={!!deleteCourse}
        title="과정 삭제"
        confirmLabel="삭제"
        confirmColor="error"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteCourse(null)}
      >
        <Typography sx={{ fontSize: 14, color: '#444444' }}>
          <strong>{deleteCourse?.name}</strong> 과정을 삭제하시겠습니까?
          <br />
          <Typography component="span" sx={{ fontSize: 13, color: '#cc3333', mt: 0.5, display: 'block' }}>
            삭제 시 관련 독후감 데이터도 함께 삭제됩니다.
          </Typography>
        </Typography>
      </ConfirmModal>
    </Box>
  );
}
