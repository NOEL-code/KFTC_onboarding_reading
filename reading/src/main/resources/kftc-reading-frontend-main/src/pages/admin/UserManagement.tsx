import { useState, useMemo, useEffect, useRef } from 'react';
import {
  Box,
  Button,
  Checkbox,
  Link,
  TextField,
  Typography,
  Pagination,
} from '@mui/material';
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined';
import PersonAddOutlinedIcon from '@mui/icons-material/PersonAddOutlined';
import {
  fetchUsersByCourse,
  createUsers,
  updateUsers,
  deleteUsers,
  uploadUsersExcel,
} from '../../api/userApi.ts';
import DataTable from '../../components/DataTable.tsx';
import type { Column } from '../../components/DataTable.tsx';
import ConfirmModal from '../../components/ConfirmModal.tsx';
import FileDropzone from '../../components/FileDropzone.tsx';
import FormDialog from '../../components/FormDialog.tsx';
import FormField from '../../components/FormField.tsx';
import PageHeader from '../../components/PageHeader.tsx';
import { useCourse } from '../../context/CourseContext.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

interface User {
  id: number;
  employeeNo: string;
  name: string;
  dept: string;  // UI label: 소속명/부서 / API field: team
}

type UserForm = Omit<User, 'id'>;

const EMPTY_FORM: UserForm = { employeeNo: '', name: '', dept: '' };

const PAGE_SIZE = 10;

// ─── Field config for the user form ──────────────────────────────────────────

const FORM_FIELDS: { key: keyof UserForm; label: string; placeholder: string; type?: string }[] = [
  { key: 'employeeNo', label: '사번', placeholder: '사번을 입력해주세요' },
  { key: 'name',       label: '이름', placeholder: '이름을 입력해주세요' },
  { key: 'dept',       label: '팀', placeholder: '팀명을 입력해주세요' },
];

// ─── User form dialog (add / edit) ───────────────────────────────────────────

interface UserFormDialogProps {
  open: boolean;
  initialValues: UserForm;
  mode: 'add' | 'edit';
  onClose: () => void;
  onSubmit: (values: UserForm) => void;
}

function UserFormDialog({ open, initialValues, mode, onClose, onSubmit }: UserFormDialogProps) {
  const [form, setForm] = useState<UserForm>(initialValues);

  function handleChange(key: keyof UserForm, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    onSubmit(form);
  }

  return (
    <FormDialog
      open={open}
      title={mode === 'add' ? '사용자 추가' : '사용자 수정'}
      confirmLabel={mode === 'add' ? '추가' : '저장'}
      formId="user-form"
      onClose={onClose}
      onEnter={() => setForm(initialValues)}
    >
      <Box
        component="form"
        id="user-form"
        onSubmit={handleSubmit}
        sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}
      >
        {FORM_FIELDS.map(({ key, label, placeholder, type }) => (
          <FormField key={key} label={label} htmlFor={`user-field-${key}`}>
            <TextField
              id={`user-field-${key}`}
              size="small"
              fullWidth
              type={type ?? 'text'}
              placeholder={placeholder}
              value={form[key]}
              onChange={(e) => handleChange(key, e.target.value)}
              required
            />
          </FormField>
        ))}
      </Box>
    </FormDialog>
  );
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function UserManagement() {
  const { selectedCourseId } = useCourse();
  const [rows, setRows]               = useState<User[]>([]);
  const [selected, setSelected]       = useState<Set<number>>(new Set());
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage]               = useState(1);
  const [excelFile, setExcelFile]     = useState<File | null>(null);
  const excelUploadingRef             = useRef(false);

  const [addOpen, setAddOpen]               = useState(false);
  const [editUser, setEditUser]             = useState<User | null>(null);
  const [deleteUser, setDeleteUser]         = useState<User | null>(null);
  const [deleteBulkOpen, setDeleteBulkOpen] = useState(false);

  // ── Load users from API ─────────────────────────────────────────────────────

  useEffect(() => {
    if (!selectedCourseId) return;
    fetchUsersByCourse(selectedCourseId)
      .then(({ data }) => {
        const raw = data.users ?? data.content ?? (Array.isArray(data) ? data : []);
        const list: User[] = raw.map(
          (u: Record<string, unknown>) => ({
            id:         Number(u.userId ?? u.id),
            employeeNo: String(u.employeeNo ?? ''),
            name:       String(u.name ?? ''),
            dept:       String(u.team ?? ''),
          })
        );
        setRows(list);
        setSelected(new Set());
      })
      .catch(() => {});
  }, [selectedCourseId]);

  // ── Derived state ──────────────────────────────────────────────────────────

  const filtered = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return rows;
    return rows.filter(
      (r) => r.name.toLowerCase().includes(q) || r.dept.toLowerCase().includes(q),
    );
  }, [rows, searchQuery]);

  const totalPages  = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const pageRows    = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  const allSelected  = pageRows.length > 0 && pageRows.every((r) => selected.has(r.id));
  const someSelected = pageRows.some((r) => selected.has(r.id)) && !allSelected;

  // ── Selection ──────────────────────────────────────────────────────────────

  function toggleAll() {
    setSelected((prev) => {
      const next = new Set(prev);
      allSelected
        ? pageRows.forEach((r) => next.delete(r.id))
        : pageRows.forEach((r) => next.add(r.id));
      return next;
    });
  }

  function toggleRow(id: number) {
    setSelected((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }

  // ── CRUD ───────────────────────────────────────────────────────────────────

  function handleAdd(form: UserForm) {
    const newUser: User = { id: Date.now(), ...form };
    setRows((prev) => [newUser, ...prev]);
    setAddOpen(false);
    createUsers([{ courseId: Number(selectedCourseId), employeeNo: form.employeeNo, name: form.name, team: form.dept }])
      .catch(() => {});
  }

  function handleEdit(form: UserForm) {
    if (!editUser) return;
    setRows((prev) => prev.map((r) => (r.id === editUser.id ? { ...r, ...form } : r)));
    setEditUser(null);
    updateUsers(Number(selectedCourseId), [{ userId: editUser.id, name: form.name, team: form.dept }])
      .catch(() => {});
  }

  function handleDeleteConfirm() {
    if (!deleteUser) return;
    setRows((prev) => prev.filter((r) => r.id !== deleteUser.id));
    setSelected((prev) => { const n = new Set(prev); n.delete(deleteUser.id); return n; });
    setDeleteUser(null);
    deleteUsers(Number(selectedCourseId), [deleteUser.id])
      .catch(() => {});
  }

  function handleBulkDeleteConfirm() {
    const idsToDelete = [...selected];
    setRows((prev) => prev.filter((r) => !selected.has(r.id)));
    setSelected(new Set());
    setDeleteBulkOpen(false);
    deleteUsers(Number(selectedCourseId), idsToDelete)
      .catch(() => {});
  }

  // ── Excel upload ────────────────────────────────────────────────────────────

  useEffect(() => {
    if (!excelFile || excelUploadingRef.current || !selectedCourseId) return;
    excelUploadingRef.current = true;
    uploadUsersExcel(selectedCourseId, excelFile)
      .then(() => fetchUsersByCourse(selectedCourseId))
      .then(({ data }) => {
        const raw = data.users ?? data.content ?? (Array.isArray(data) ? data : []);
        const list: User[] = raw.map(
          (u: Record<string, unknown>) => ({
            id:         Number(u.userId ?? u.id),
            employeeNo: String(u.employeeNo ?? ''),
            name:       String(u.name ?? ''),
            dept:       String(u.team ?? ''),
          })
        );
        setRows(list);
      })
      .catch(() => {})
      .finally(() => {
        excelUploadingRef.current = false;
        setExcelFile(null);
      });
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [excelFile]);

  // ── Columns ────────────────────────────────────────────────────────────────

  const columns: Column<User>[] = useMemo(() => [
    {
      id: '_checkbox',
      label: (
        <Checkbox
          size="small"
          checked={allSelected}
          indeterminate={someSelected}
          onChange={toggleAll}
          sx={{ p: 0 }}
        />
      ),
      width: 48,
      align: 'center',
      render: (_val, row) => (
        <Checkbox
          size="small"
          checked={selected.has(row.id)}
          onChange={() => toggleRow(row.id)}
          sx={{ p: 0 }}
        />
      ),
    },
    { id: 'employeeNo', label: '사번',   width: 100 },
    { id: 'name',       label: '이름',   width: 90  },
    { id: 'dept',       label: '소속명'             },
    {
      id: '_actions',
      label: '작업',
      width: 100,
      align: 'center',
      render: (_val, row) => (
        <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1.5 }}>
          <Link
            component="button"
            underline="hover"
            onClick={() => setEditUser(row)}
            sx={{ fontSize: 13, color: '#0064dd', cursor: 'pointer', background: 'none', border: 'none' }}
          >
            수정
          </Link>
          <Link
            component="button"
            underline="hover"
            onClick={() => setDeleteUser(row)}
            sx={{ fontSize: 13, color: '#cc3333', cursor: 'pointer', background: 'none', border: 'none' }}
          >
            삭제
          </Link>
        </Box>
      ),
    },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [pageRows, selected, allSelected, someSelected, page]);

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <Box>
      <PageHeader title="사용자 관리">
        <Button
          variant="contained"
          startIcon={<PersonAddOutlinedIcon />}
          onClick={() => setAddOpen(true)}
          sx={{ height: 40, bgcolor: '#0064dd', '&:hover': { bgcolor: '#004ca8' } }}
        >
          + 사용자 추가
        </Button>
      </PageHeader>

      {/* Excel upload zone */}
      <Box sx={{ mb: 3 }}>
        <Typography sx={{ fontSize: 13, color: '#555555', mb: 1 }}>
          <DownloadOutlinedIcon sx={{ fontSize: 14, verticalAlign: 'middle', mr: 0.5 }} />
          엑셀 파일(.xlsx, .xls)로 최대 500명까지 한 번에 등록할 수 있습니다.
        </Typography>
        <FileDropzone
          accept=".xlsx,.xls"
          file={excelFile}
          onFileChange={setExcelFile}
          mainText="엑셀 파일을 드래그하거나 클릭하여 업로드"
          subText=".xlsx, .xls | 최대 500명 | 필수 열: 사번, 이름, 팀"
        />
      </Box>

      {/* User count + actions */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
        <Typography sx={{ fontSize: 14, fontWeight: 700, color: '#222222' }}>
          전체 {rows.length}명
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Button
            variant="outlined"
            disabled={selected.size === 0}
            onClick={() => setDeleteBulkOpen(true)}
            sx={{
              height: 40,
              fontSize: 13,
              color: '#c4317b',
              borderColor: '#e8a0c0',
              '&:hover': { borderColor: '#c4317b', bgcolor: 'transparent' },
              '&:disabled': { color: '#cccccc', borderColor: '#e0e0e0' },
            }}
          >
            선택 삭제
          </Button>
          <TextField
            size="small"
            placeholder="이름, 팀 검색..."
            value={searchQuery}
            onChange={(e) => { setSearchQuery(e.target.value); setPage(1); }}
            sx={{ width: 220, bgcolor: '#ffffff', fontSize: 13 }}
          />
        </Box>
      </Box>

      {/* Table */}
      <Box
        sx={{
          bgcolor: '#ffffff',
          borderRadius: '8px',
          border: '1px solid #e8e8e8',
          overflow: 'hidden',
        }}
      >
        <DataTable<User>
          columns={columns}
          rows={pageRows}
          page={page}
          totalPages={1}
          onPageChange={setPage}
        />

        {/* Footer row */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'flex-end',
            px: 2,
            py: 1.5,
            borderTop: '1px solid #eeeeee',
          }}
        >
          <Pagination
            count={totalPages}
            page={page}
            onChange={(_, p) => setPage(p)}
            color="primary"
            shape="rounded"
            size="small"
          />
        </Box>
      </Box>

      {/* Add user dialog */}
      <UserFormDialog
        open={addOpen}
        mode="add"
        initialValues={EMPTY_FORM}
        onClose={() => setAddOpen(false)}
        onSubmit={handleAdd}
      />

      {/* Edit user dialog */}
      <UserFormDialog
        open={!!editUser}
        mode="edit"
        initialValues={
          editUser
            ? { employeeNo: editUser.employeeNo, name: editUser.name, dept: editUser.dept }
            : EMPTY_FORM
        }
        onClose={() => setEditUser(null)}
        onSubmit={handleEdit}
      />

      {/* Single-row delete confirm */}
      <ConfirmModal
        open={!!deleteUser}
        title="사용자 삭제"
        confirmLabel="삭제"
        confirmColor="error"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteUser(null)}
      >
        <Typography sx={{ fontSize: 14, color: '#444444' }}>
          <strong>{deleteUser?.name}</strong> ({deleteUser?.dept}) 사용자를 삭제하시겠습니까?
          <br />
          <Typography component="span" sx={{ fontSize: 13, color: '#cc3333', mt: 0.5, display: 'block' }}>
            이 작업은 되돌릴 수 없습니다.
          </Typography>
        </Typography>
      </ConfirmModal>

      {/* Bulk delete confirm */}
      <ConfirmModal
        open={deleteBulkOpen}
        title="선택 삭제"
        confirmLabel="삭제"
        confirmColor="error"
        onConfirm={handleBulkDeleteConfirm}
        onCancel={() => setDeleteBulkOpen(false)}
      >
        <Typography sx={{ fontSize: 14, color: '#444444' }}>
          선택한 <strong>{selected.size}명</strong>의 사용자를 삭제하시겠습니까?
          <br />
          <Typography component="span" sx={{ fontSize: 13, color: '#cc3333', mt: 0.5, display: 'block' }}>
            이 작업은 되돌릴 수 없습니다.
          </Typography>
        </Typography>
      </ConfirmModal>
    </Box>
  );
}
