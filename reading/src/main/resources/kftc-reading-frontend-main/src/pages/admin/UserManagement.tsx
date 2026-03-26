import { useState, useMemo, useEffect, useRef, useCallback } from 'react';
import {
  Box,
  Button,
  Checkbox,
  CircularProgress,
  Link,
  MenuItem,
  Select,
  TextField,
  Typography,
  Pagination,
} from '@mui/material';
import type { SelectChangeEvent } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined';
import SaveOutlinedIcon from '@mui/icons-material/SaveOutlined';
import * as XLSX from 'xlsx';
import {
  fetchUsersByCourse,
  createUsers,
  updateUsers,
  deleteUsers,
} from '../../api/userApi.ts';
import DataTable from '../../components/DataTable.tsx';
import type { Column } from '../../components/DataTable.tsx';
import ConfirmModal from '../../components/ConfirmModal.tsx';
import FileDropzone from '../../components/FileDropzone.tsx';
import PageHeader from '../../components/PageHeader.tsx';
import { useCourse } from '../../context/CourseContext.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

interface User {
  id: number;
  employeeNo: string;
  name: string;
  dept: string;
}

type UserForm = Omit<User, 'id'>;

const EMPTY_FORM: UserForm = { employeeNo: '', name: '', dept: '' };

const PAGE_SIZE = 10;

// Locally-added users get negative IDs to distinguish from server IDs
let localIdCounter = -1;
function nextLocalId() {
  return localIdCounter--;
}

// ─── Excel parsing helper ────────────────────────────────────────────────────

const HEADER_MAP: Record<string, keyof UserForm> = {
  '사번': 'employeeNo',
  'employeeno': 'employeeNo',
  '이름': 'name',
  'name': 'name',
  '팀': 'dept',
  '부서': 'dept',
  '소속': 'dept',
  'team': 'dept',
  'dept': 'dept',
};

function parseExcelToUsers(buffer: ArrayBuffer): UserForm[] {
  const wb = XLSX.read(buffer, { type: 'array' });
  const ws = wb.Sheets[wb.SheetNames[0]];
  const json: Record<string, unknown>[] = XLSX.utils.sheet_to_json(ws, { defval: '' });

  if (json.length === 0) return [];

  // Map header names to our field keys
  const rawHeaders = Object.keys(json[0]);
  const headerMapping: Record<string, keyof UserForm> = {};
  for (const h of rawHeaders) {
    const mapped = HEADER_MAP[h.trim().toLowerCase()];
    if (mapped) headerMapping[h] = mapped;
  }

  return json.map((row) => {
    const user: UserForm = { employeeNo: '', name: '', dept: '' };
    for (const [raw, key] of Object.entries(headerMapping)) {
      user[key] = String(row[raw] ?? '').trim();
    }
    return user;
  }).filter((u) => u.employeeNo !== '');
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function UserManagement() {
  const { courses, selectedCourseId, setSelectedCourseId } = useCourse();

  // savedRows: the last-known server state (snapshot after load / save)
  const savedRowsRef = useRef<User[]>([]);
  const [rows, setRows]               = useState<User[]>([]);
  const [selected, setSelected]       = useState<Set<number>>(new Set());
  const [searchQuery, setSearchQuery] = useState('');
  const [page, setPage]               = useState(1);
  const [excelFile, setExcelFile]     = useState<File | null>(null);
  const [saving, setSaving]           = useState(false);
  const [excelError, setExcelError]   = useState('');

  const [addingRow, setAddingRow]           = useState<UserForm | null>(null);
  const [editUser, setEditUser]             = useState<User | null>(null);
  const [editForm, setEditForm]             = useState<UserForm>(EMPTY_FORM);
  const [deleteUser, setDeleteUser]         = useState<User | null>(null);
  const [deleteBulkOpen, setDeleteBulkOpen] = useState(false);

  // ── Course change handler ──────────────────────────────────────────────────

  function handleCourseChange(e: SelectChangeEvent<string>) {
    setSelectedCourseId(e.target.value);
    savedRowsRef.current = [];
    setRows([]);
    setSelected(new Set());
    setSearchQuery('');
    setPage(1);
    setExcelFile(null);
    setExcelError('');
  }

  // ── Load users from API ─────────────────────────────────────────────────────

  const loadUsers = useCallback(() => {
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
        savedRowsRef.current = list;
        setRows(list);
        setSelected(new Set());
      })
      .catch(() => {});
  }, [selectedCourseId]);

  useEffect(() => { loadUsers(); }, [loadUsers]);

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

  // ── Change detection ──────────────────────────────────────────────────────

  const hasChanges = useMemo(() => {
    const saved = savedRowsRef.current;
    if (rows.length !== saved.length) return true;
    const savedMap = new Map(saved.map((u) => [u.id, u]));
    for (const r of rows) {
      if (r.id < 0) return true; // new user
      const s = savedMap.get(r.id);
      if (!s) return true; // deleted & re-added? shouldn't happen, but counts
      if (s.name !== r.name || s.dept !== r.dept || s.employeeNo !== r.employeeNo) return true;
    }
    // Check if any saved user was removed
    const currentIds = new Set(rows.map((r) => r.id));
    for (const s of saved) {
      if (!currentIds.has(s.id)) return true;
    }
    return false;
  }, [rows]);

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

  // ── Local CRUD (no API calls) ─────────────────────────────────────────────

  function handleAddConfirm() {
    if (!addingRow || !addingRow.employeeNo.trim()) return;
    const newUser: User = { id: nextLocalId(), ...addingRow };
    setRows((prev) => [...prev, newUser]);
    setAddingRow(null);
  }

  function startEdit(row: User) {
    setEditUser(row);
    setEditForm({ employeeNo: row.employeeNo, name: row.name, dept: row.dept });
  }

  function handleEditConfirm() {
    if (!editUser) return;
    setRows((prev) => prev.map((r) => (r.id === editUser.id ? { ...r, ...editForm } : r)));
    setEditUser(null);
  }

  function handleDeleteConfirm() {
    if (!deleteUser) return;
    setRows((prev) => prev.filter((r) => r.id !== deleteUser.id));
    setSelected((prev) => { const n = new Set(prev); n.delete(deleteUser.id); return n; });
    setDeleteUser(null);
  }

  function handleBulkDeleteConfirm() {
    setRows((prev) => prev.filter((r) => !selected.has(r.id)));
    setSelected(new Set());
    setDeleteBulkOpen(false);
  }

  // ── Excel parsing (local only) ────────────────────────────────────────────

  useEffect(() => {
    if (!excelFile) return;
    setExcelError('');

    const reader = new FileReader();
    reader.onload = (e) => {
      try {
        const buffer = e.target?.result as ArrayBuffer;
        const parsed = parseExcelToUsers(buffer);
        if (parsed.length === 0) {
          setExcelError('엑셀 파일에서 사용자 데이터를 찾을 수 없습니다. 필수 열: 사번, 이름, 팀');
          return;
        }
        // Deduplicate by employeeNo against existing rows
        const existingNos = new Set(rows.map((r) => r.employeeNo));
        const newUsers: User[] = parsed
          .filter((u) => !existingNos.has(u.employeeNo))
          .map((u) => ({ id: nextLocalId(), ...u }));

        if (newUsers.length === 0) {
          setExcelError('엑셀의 모든 사용자가 이미 목록에 존재합니다.');
          return;
        }

        setRows((prev) => [...prev, ...newUsers]);
      } catch {
        setExcelError('엑셀 파일을 읽는 중 오류가 발생했습니다.');
      }
    };
    reader.readAsArrayBuffer(excelFile);
    setExcelFile(null);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [excelFile]);

  // ── Save all changes to backend ───────────────────────────────────────────

  async function handleSaveAll() {
    if (!selectedCourseId || !hasChanges) return;
    setSaving(true);

    const saved = savedRowsRef.current;
    const savedMap = new Map(saved.map((u) => [u.id, u]));
    const currentIds = new Set(rows.map((r) => r.id));
    const courseId = Number(selectedCourseId);

    // 1. New users (negative IDs)
    const newUsers = rows.filter((r) => r.id < 0);
    // 2. Modified users (positive IDs with changed data)
    const modifiedUsers = rows.filter((r) => {
      if (r.id < 0) return false;
      const s = savedMap.get(r.id);
      if (!s) return false;
      return s.name !== r.name || s.dept !== r.dept;
    });
    // 3. Deleted users (saved IDs not in current rows)
    const deletedIds = saved.filter((s) => !currentIds.has(s.id)).map((s) => s.id);

    try {
      const promises: Promise<unknown>[] = [];

      if (newUsers.length > 0) {
        promises.push(
          createUsers(newUsers.map((u) => ({
            courseId,
            employeeNo: u.employeeNo,
            name: u.name,
            team: u.dept,
          })))
        );
      }
      if (modifiedUsers.length > 0) {
        promises.push(
          updateUsers(courseId, modifiedUsers.map((u) => ({
            userId: u.id,
            name: u.name,
            team: u.dept,
          })))
        );
      }
      if (deletedIds.length > 0) {
        promises.push(deleteUsers(courseId, deletedIds));
      }

      await Promise.all(promises);
      // Reload from server to get real IDs
      loadUsers();
    } catch {
      // Error handling — keep current state
    } finally {
      setSaving(false);
    }
  }

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
    {
      id: 'employeeNo', label: '사번', width: 120,
      render: (_val, row) =>
        editUser?.id === row.id ? (
          <TextField size="small" value={editForm.employeeNo} onChange={(e) => setEditForm((f) => ({ ...f, employeeNo: e.target.value }))} sx={{ '& input': { fontSize: 13, py: 0.5 } }} />
        ) : (String(_val ?? '')),
    },
    {
      id: 'name', label: '이름', width: 100,
      render: (_val, row) =>
        editUser?.id === row.id ? (
          <TextField size="small" value={editForm.name} onChange={(e) => setEditForm((f) => ({ ...f, name: e.target.value }))} sx={{ '& input': { fontSize: 13, py: 0.5 } }} />
        ) : (String(_val ?? '')),
    },
    {
      id: 'dept', label: '팀명', width: 120,
      render: (_val, row) =>
        editUser?.id === row.id ? (
          <TextField size="small" value={editForm.dept} onChange={(e) => setEditForm((f) => ({ ...f, dept: e.target.value }))} sx={{ '& input': { fontSize: 13, py: 0.5 } }} />
        ) : (String(_val ?? '')),
    },
    {
      id: '_actions',
      label: '작업',
      width: 140,
      align: 'center',
      render: (_val, row) =>
        editUser?.id === row.id ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1.5 }}>
            <Link component="button" underline="hover" onClick={handleEditConfirm} sx={{ fontSize: 13, color: '#2e7d32', cursor: 'pointer', background: 'none', border: 'none' }}>저장</Link>
            <Link component="button" underline="hover" onClick={() => setEditUser(null)} sx={{ fontSize: 13, color: '#888888', cursor: 'pointer', background: 'none', border: 'none' }}>취소</Link>
          </Box>
        ) : (
          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 1.5 }}>
            <Link component="button" underline="hover" onClick={() => startEdit(row)} sx={{ fontSize: 13, color: '#0064dd', cursor: 'pointer', background: 'none', border: 'none' }}>수정</Link>
            <Link component="button" underline="hover" onClick={() => setDeleteUser(row)} sx={{ fontSize: 13, color: '#cc3333', cursor: 'pointer', background: 'none', border: 'none' }}>삭제</Link>
          </Box>
        ),
    },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [pageRows, selected, allSelected, someSelected, page, editUser, editForm]);

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <Box sx={{ maxWidth: 760, mx: 'auto' }}>
      <PageHeader title="사용자 관리">
        <Box sx={{ display: 'flex', gap: 1.5, alignItems: 'center' }}>
          <Select
            size="small"
            value={selectedCourseId}
            onChange={handleCourseChange}
            displayEmpty
            renderValue={(val) => {
              if (!val) return <Typography component="span" sx={{ fontSize: 14, color: '#999999' }}>과정을 선택해주세요</Typography>;
              const c = courses.find((course) => String(course.id) === val);
              return c?.name ?? val;
            }}
            sx={{ minWidth: 200, bgcolor: '#ffffff', fontSize: 14 }}
          >
            {courses.map((c) => (
              <MenuItem key={c.id} value={String(c.id)} sx={{ fontSize: 14 }}>
                {c.name}
              </MenuItem>
            ))}
          </Select>
          <Button
            variant="contained"
            startIcon={saving ? <CircularProgress size={16} sx={{ color: '#fff' }} /> : <SaveOutlinedIcon />}
            disabled={!hasChanges || saving}
            onClick={handleSaveAll}
            sx={{
              height: 40,
              bgcolor: '#2e7d32',
              '&:hover': { bgcolor: '#1b5e20' },
              '&:disabled': { bgcolor: '#a5d6a7' },
            }}
          >
            등록
          </Button>
        </Box>
      </PageHeader>

      {/* Empty state — no course selected */}
      {!selectedCourseId ? (
        <Box
          sx={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            py: 10,
            bgcolor: '#ffffff',
            borderRadius: '8px',
            border: '1px solid #e8e8e8',
          }}
        >
          <Typography sx={{ fontSize: 16, fontWeight: 600, color: '#888888', mb: 1 }}>
            과정을 선택해주세요
          </Typography>
          <Typography sx={{ fontSize: 13, color: '#aaaaaa' }}>
            사용자를 관리할 독서 과정을 위 드롭다운에서 선택하세요.
          </Typography>
        </Box>
      ) : (
      <>

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
        {excelError && (
          <Typography sx={{ fontSize: 13, color: '#cc3333', mt: 1 }}>
            {excelError}
          </Typography>
        )}
      </Box>

      {/* User count + actions */}
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1.5 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Typography sx={{ fontSize: 14, fontWeight: 700, color: '#222222' }}>
            전체 {rows.length}명
          </Typography>
          {hasChanges && (
            <Typography sx={{ fontSize: 12, color: '#c4317b', fontWeight: 500 }}>
              미저장 변경사항 있음
            </Typography>
          )}
        </Box>
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

        {/* Inline add row */}
        {addingRow && (
          <Box sx={{ display: 'flex', alignItems: 'center', px: 2, py: 1, gap: 1.5, borderTop: '1px solid #eeeeee', bgcolor: '#f9fbff' }}>
            <Box sx={{ width: 48 }} />
            <TextField size="small" placeholder="사번" value={addingRow.employeeNo} onChange={(e) => setAddingRow((f) => f && ({ ...f, employeeNo: e.target.value }))} sx={{ width: 120, '& input': { fontSize: 13, py: 0.5 } }} />
            <TextField size="small" placeholder="이름" value={addingRow.name} onChange={(e) => setAddingRow((f) => f && ({ ...f, name: e.target.value }))} sx={{ width: 100, '& input': { fontSize: 13, py: 0.5 } }} />
            <TextField size="small" placeholder="팀명" value={addingRow.dept} onChange={(e) => setAddingRow((f) => f && ({ ...f, dept: e.target.value }))} sx={{ width: 120, '& input': { fontSize: 13, py: 0.5 } }} />
            <Link component="button" underline="hover" onClick={handleAddConfirm} sx={{ fontSize: 13, color: '#2e7d32', cursor: 'pointer', background: 'none', border: 'none', flexShrink: 0 }}>확인</Link>
            <Link component="button" underline="hover" onClick={() => setAddingRow(null)} sx={{ fontSize: 13, color: '#888888', cursor: 'pointer', background: 'none', border: 'none', flexShrink: 0 }}>취소</Link>
          </Box>
        )}

        {/* Add button + footer */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            px: 2,
            py: 1.5,
            borderTop: '1px solid #eeeeee',
          }}
        >
          <Button
            size="small"
            startIcon={<AddIcon />}
            disabled={!!addingRow}
            onClick={() => setAddingRow({ ...EMPTY_FORM })}
            sx={{ fontSize: 13, color: '#0064dd', textTransform: 'none' }}
          >
            추가
          </Button>
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

      </>
      )}

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
          <strong>{deleteUser?.name}</strong> ({deleteUser?.dept}) 사용자를 목록에서 삭제하시겠습니까?
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
          선택한 <strong>{selected.size}명</strong>의 사용자를 목록에서 삭제하시겠습니까?
        </Typography>
      </ConfirmModal>
    </Box>
  );
}
