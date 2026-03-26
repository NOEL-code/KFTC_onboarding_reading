import { useState, useEffect, useMemo } from 'react';
import {
  Box,
  Button,
  Checkbox,
  TextField,
  Typography,
  Link,
  Pagination,
} from '@mui/material';
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined';
import {
  fetchAdminReports,
  approveReports,
  supplementReports,
  downloadAdminReport,
} from '../../api/reportApi.ts';
import DataTable from '../../components/DataTable.tsx';
import type { Column } from '../../components/DataTable.tsx';
import StatusBadge from '../../components/StatusBadge.tsx';
import ConfirmModal from '../../components/ConfirmModal.tsx';
import PageHeader from '../../components/PageHeader.tsx';
import { downloadFile } from '../../utils/downloadFile.ts';
import { useCourse } from '../../context/CourseContext.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

type ReportStatus = '승인' | '제출' | '보완' | '미제출';

interface Report {
  id: number;
  employeeNo: string;
  name: string;
  dept: string;
  title: string;
  submittedAt: string;
  status: ReportStatus;
}

interface SummaryCounts {
  total: number;
  '제출': number;
  '승인': number;
  '보완': number;
  '미제출': number;
}

const INITIAL_SUMMARY: SummaryCounts = {
  total: 0, '제출': 0, '승인': 0, '보완': 0, '미제출': 0,
};

const PAGE_SIZE = 10;

const SUMMARY_CARDS: { label: string; key: keyof SummaryCounts; color: string; filterOpt: StatusFilterOption }[] = [
  { label: '전체',   key: 'total',   color: '#222222', filterOpt: '모두'     },
  { label: '제출',   key: '제출',    color: '#0064dd', filterOpt: '제출'     },
  { label: '승인',   key: '승인',    color: '#2e7d32', filterOpt: '승인'     },
  { label: '보완',   key: '보완',    color: '#c4317b', filterOpt: '보완 필요' },
  { label: '미제출', key: '미제출',  color: '#999999', filterOpt: '미제출'   },
];

type StatusFilterOption = '모두' | '승인' | '제출' | '보완 필요' | '미제출';

function filterOptionToStatus(opt: StatusFilterOption): ReportStatus | null {
  if (opt === '모두') return null;
  if (opt === '보완 필요') return '보완';
  return opt as ReportStatus;
}

// ─── Component ───────────────────────────────────────────────────────────────

export default function ReportManagement() {
  const { selectedCourseId } = useCourse();

  const [rows, setRows]               = useState<Report[]>([]);
  const [page, setPage]               = useState(1);
  const [selected, setSelected]       = useState<Set<number>>(new Set());
  const [summaryCounts, setSummaryCounts] = useState<SummaryCounts>(INITIAL_SUMMARY);

  const [statusFilter, setStatusFilter] = useState<StatusFilterOption>('모두');
  const [searchQuery, setSearchQuery]   = useState('');

  const [approveOpen, setApproveOpen] = useState(false);

  // Per-row revision modal
  const [revisionRow, setRevisionRow]       = useState<Report | null>(null);
  const [revisionReason, setRevisionReason] = useState('');

  // ── Status filter helpers ──────────────────────────────────────────────────

  function selectStatusFilter(opt: StatusFilterOption) {
    setStatusFilter(opt);
    setPage(1);
    setSelected(new Set());
  }

  // ── Fetch ──────────────────────────────────────────────────────────────────

  useEffect(() => {
    if (!selectedCourseId) return;
    let cancelled = false;

    async function fetchReports() {
      const statusParam = statusFilter === '모두' ? undefined : filterOptionToStatus(statusFilter);

      try {
        const { data } = await fetchAdminReports({
          courseId: selectedCourseId,
          page,
          status: statusParam ?? undefined,
          name:   searchQuery || undefined,
        });
        if (!cancelled) {
          const items: Report[] = (data.reports ?? []).map(
            (r: Record<string, unknown>) => ({
              id:          Number(r.reportId ?? r.id ?? 0),
              employeeNo:  String(r.employeeNo ?? ''),
              name:        String(r.name ?? ''),
              dept:        String(r.team ?? ''),
              title:       String(r.title ?? ''),
              submittedAt: r.submittedAt ? String(r.submittedAt).slice(0, 10) : '—',
              status:      (r.status ?? '미제출') as ReportStatus,
            })
          );
          setRows(items);

          const summary = data.summary;
          if (summary) {
            setSummaryCounts({
              total:   summary.total        ?? 0,
              '제출':  summary.submitted    ?? 0,
              '승인':  summary.approved     ?? 0,
              '보완':  summary.supplement   ?? 0,
              '미제출': summary.notSubmitted ?? 0,
            });
          }
        }
      } catch {
        // API error
      }
    }

    fetchReports();
    return () => { cancelled = true; };
  }, [selectedCourseId, page, statusFilter, searchQuery]);

  // ── Client-side filtering ──────────────────────────────────────────────────

  const filtered = useMemo(() => {
    let result = rows;

    if (statusFilter !== '모두') {
      const status = filterOptionToStatus(statusFilter);
      if (status) result = result.filter((r) => r.status === status);
    }

    const q = searchQuery.trim().toLowerCase();
    if (q) result = result.filter((r) =>
      r.name.toLowerCase().includes(q) || r.dept.toLowerCase().includes(q),
    );

    return result;
  }, [rows, statusFilter, searchQuery]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const pageRows   = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  // ── Selection helpers ──────────────────────────────────────────────────────

  const allSelected   = pageRows.length > 0 && pageRows.every((r) => selected.has(r.id));
  const someSelected  = pageRows.some((r) => selected.has(r.id)) && !allSelected;

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

  // ── Bulk approve ───────────────────────────────────────────────────────────

  async function handleApproveConfirm() {
    const idsToApprove = [...selected].filter((id) => {
      const row = rows.find((r) => r.id === id);
      return row && row.status !== '승인';
    });
    try {
      if (idsToApprove.length > 0) {
        await approveReports(idsToApprove);
        setRows((prev) =>
          prev.map((r) => idsToApprove.includes(r.id) ? { ...r, status: '승인' as ReportStatus } : r)
        );
      }
    } catch { /* noop */ }
    setApproveOpen(false);
    setSelected(new Set());
  }

  // ── Bulk download ──────────────────────────────────────────────────────────

  async function handleBulkDownload() {
    const ids = [...selected];
    for (const id of ids) {
      const row = rows.find((r) => r.id === id);
      if (!row || row.status === '미제출') continue;
      try {
        const { data } = await downloadAdminReport(id);
        downloadFile(data, row.title);
      } catch { /* noop */ }
    }
  }

  // ── Per-row revision (보완 요청) ────────────────────────────────────────────

  async function handleRevisionConfirm() {
    if (!revisionRow) return;
    try {
      await supplementReports([revisionRow.id], revisionReason);
      setRows((prev) =>
        prev.map((r) => r.id === revisionRow.id ? { ...r, status: '보완' as ReportStatus } : r)
      );
    } catch { /* noop */ }
    setRevisionRow(null);
    setRevisionReason('');
  }

  // ── File download ──────────────────────────────────────────────────────────

  function handleFileDownload(reportId: number, filename: string) {
    downloadAdminReport(reportId)
      .then(({ data }) => downloadFile(data, filename))
      .catch(() => {});
  }

  // ── Columns ────────────────────────────────────────────────────────────────

  const columns: Column<Report>[] = useMemo(() => [
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
    { id: 'employeeNo', label: '사번', width: 90 },
    { id: 'name',       label: '이름', width: 90 },
    { id: 'dept', label: '소속명', width: 110 },
    {
      id: 'title',
      label: '파일명',
      render: (val, row) =>
        !val || row.status === '미제출' ? (
          <Typography sx={{ fontSize: 13, color: '#aaaaaa' }}>—</Typography>
        ) : (
          <Link
            component="button"
            underline="hover"
            onClick={() => handleFileDownload(row.id, val as string)}
            sx={{ fontSize: 13, color: '#0064dd', cursor: 'pointer', background: 'none', border: 'none', textAlign: 'left' }}
          >
            {val as string}
          </Link>
        ),
    },
    { id: 'submittedAt', label: '제출일', width: 110, align: 'center' },
    {
      id: 'status',
      label: '상태',
      width: 90,
      align: 'center',
      render: (val) => <StatusBadge status={val as ReportStatus} />,
    },
    {
      id: '_actions',
      label: '작업',
      width: 100,
      align: 'center',
      render: (_val, row) =>
        row.status === '제출' || row.status === '보완' ? (
          <Link
            component="button"
            underline="hover"
            onClick={() => setRevisionRow(row)}
            sx={{ fontSize: 13, color: '#c4317b', cursor: 'pointer', background: 'none', border: 'none' }}
          >
            보완 요청
          </Link>
        ) : (
          <Typography sx={{ fontSize: 13, color: '#cccccc' }}>—</Typography>
        ),
    },
  // eslint-disable-next-line react-hooks/exhaustive-deps
  ], [pageRows, selected, allSelected, someSelected, page]);

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <Box>
      <PageHeader title="독후감 관리" />

      {/* Summary cards — act as filter tabs */}
      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        {SUMMARY_CARDS.map((card) => {
          const active = statusFilter === card.filterOpt;
          const count  = summaryCounts[card.key];
          return (
            <Box
              key={card.key}
              onClick={() => selectStatusFilter(card.filterOpt)}
              sx={{
                flex: 1,
                bgcolor: active ? card.color : '#ffffff',
                border: '2px solid',
                borderColor: active ? card.color : '#e8e8e8',
                borderRadius: '8px',
                p: 2,
                cursor: 'pointer',
                transition: 'all 0.15s',
                '&:hover': { borderColor: card.color },
              }}
            >
              <Typography sx={{ fontSize: 13, color: active ? 'rgba(255,255,255,0.85)' : '#888888', mb: 0.5 }}>
                {card.label}
              </Typography>
              <Typography sx={{ fontSize: 28, fontWeight: 700, color: active ? '#ffffff' : card.color, lineHeight: 1 }}>
                {count}
              </Typography>
            </Box>
          );
        })}
      </Box>

      {/* Search + actions row */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: 1, mb: 2 }}>
        <TextField
          size="small"
          placeholder="이름, 팀 검색..."
          value={searchQuery}
          onChange={(e) => { setSearchQuery(e.target.value); setPage(1); }}
          sx={{ width: 200, bgcolor: '#ffffff', fontSize: 13 }}
        />
        <Button
          variant="outlined"
          startIcon={<DownloadOutlinedIcon />}
          disabled={selected.size === 0}
          onClick={handleBulkDownload}
          sx={{ height: 40, color: '#555555', borderColor: '#cccccc', '&:hover': { borderColor: '#aaaaaa' }, whiteSpace: 'nowrap' }}
        >
          선택 다운로드
        </Button>
        <Button
          variant="contained"
          disabled={selected.size === 0}
          onClick={() => setApproveOpen(true)}
          sx={{ height: 40, bgcolor: '#0064dd', '&:hover': { bgcolor: '#004ca8' }, whiteSpace: 'nowrap' }}
        >
          승인
        </Button>
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
        <DataTable<Report>
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
            justifyContent: 'space-between',
            px: 2,
            py: 1.5,
            borderTop: '1px solid #eeeeee',
          }}
        >
          <Typography sx={{ fontSize: 13, color: '#666666' }}>
            {selected.size > 0 && `${selected.size}건 선택됨 | `}
            총 {filtered.length}건
            {filtered.length !== rows.length && (
              <Box component="span" sx={{ fontWeight: 400, color: '#888888', ml: 1 }}>
                ({rows.length}건 중)
              </Box>
            )}
          </Typography>
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

      {/* Approve modal */}
      <ConfirmModal
        open={approveOpen}
        title="승인"
        confirmLabel="승인"
        confirmColor="primary"
        onConfirm={handleApproveConfirm}
        onCancel={() => setApproveOpen(false)}
      >
        <Typography sx={{ fontSize: 14, color: '#444444' }}>
          선택한 <strong>{selected.size}건</strong>의 독후감을 승인하시겠습니까?
        </Typography>
      </ConfirmModal>

      {/* Per-row revision modal */}
      <ConfirmModal
        open={!!revisionRow}
        title="보완 요청"
        confirmLabel="보완 요청"
        confirmColor="error"
        onConfirm={handleRevisionConfirm}
        onCancel={() => { setRevisionRow(null); setRevisionReason(''); }}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Typography sx={{ fontSize: 14, color: '#444444' }}>
            <strong>{revisionRow?.name}</strong> ({revisionRow?.dept})의 독후감에 보완을 요청합니다.
            <br />사유를 입력해주세요.
          </Typography>
          <TextField
            multiline
            minRows={4}
            fullWidth
            placeholder="보완 요청 사유를 입력해주세요..."
            value={revisionReason}
            onChange={(e) => setRevisionReason(e.target.value)}
            sx={{ fontSize: 13 }}
          />
        </Box>
      </ConfirmModal>
    </Box>
  );
}
