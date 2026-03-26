import { useMemo, useState, useEffect } from 'react';
import {
  Box,
  Chip,
  Link,
  Pagination,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';
import CalendarTodayOutlinedIcon from '@mui/icons-material/CalendarTodayOutlined';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchCourseDetail } from '../../api/courseApi.ts';
import { fetchCourseReports, downloadReport } from '../../api/reportApi.ts';
import StatusBadge from '../../components/StatusBadge.tsx';
import ConfirmModal from '../../components/ConfirmModal.tsx';

// ─── Types ────────────────────────────────────────────────────────────────────

type ParticipantStatus = '승인' | '제출완료' | '미제출' | '보완';

interface Participant {
  id: number;        // reportId
  employeeNo: string;
  name: string;
  dept: string;
  filename?: string;
  status: ParticipantStatus;
}

interface CourseInfo {
  name: string;
  endDate: string;
}

// ─── Action config per status ─────────────────────────────────────────────────

const ACTION_CONFIG: Record<
  ParticipantStatus,
  { label: string; color: string; action: 'submit' | 'view' }
> = {
  미제출:   { label: '제출', color: '#0064dd', action: 'submit' },
  제출완료: { label: '조회', color: '#0064dd', action: 'view'   },
  보완:     { label: '보완', color: '#cc3333', action: 'submit' },
  승인:     { label: '조회', color: '#0064dd', action: 'view'   },
};

// ─── StatusBadge extended for participant statuses ────────────────────────────

const EXTRA_STATUS_STYLES: Partial<Record<ParticipantStatus, { bg: string; color: string }>> = {
  승인:     { bg: '#e8f4fd', color: '#0064dd' },
  제출완료: { bg: '#e8f5e9', color: '#2e7d32' },
};

function ParticipantStatusBadge({ status }: { status: ParticipantStatus }) {
  const extra = EXTRA_STATUS_STYLES[status];
  if (extra) {
    return (
      <Chip
        label={status}
        size="small"
        sx={{
          backgroundColor: extra.bg,
          color: extra.color,
          borderRadius: '4px',
          fontSize: 12,
          fontWeight: 500,
          height: 24,
          minWidth: 68,
          '& .MuiChip-label': { px: 1, width: '100%', textAlign: 'center' },
        }}
      />
    );
  }
  return <StatusBadge status={status as '미제출' | '보완'} />;
}

// ─── Main page ────────────────────────────────────────────────────────────────

const PAGE_SIZE = 10;

export default function ParticipantList() {
  const { courseId } = useParams<{ courseId: string }>();
  const navigate = useNavigate();

  const [participants, setParticipants] = useState<Participant[]>([]);
  const [courseInfo, setCourseInfo]     = useState<CourseInfo>({ name: '과정', endDate: '' });
  const [searchQuery, setSearchQuery]   = useState('');
  const [page, setPage]                 = useState(1);
  const [downloadTarget, setDownloadTarget] = useState<Participant | null>(null);

  // ── Fetch course participants ─────────────────────────────────────────────

  useEffect(() => {
    if (!courseId) return;

    // Fetch course details
    fetchCourseDetail(courseId)
      .then(({ data }) => {
        setCourseInfo({
          name:    data.name ?? '과정',
          endDate: data.endDate ?? '',
        });
      })
      .catch(() => {});

    // Fetch participant / report list
    fetchCourseReports(courseId)
      .then(({ data }) => {
        const reports = data.reports ?? data.content ?? (Array.isArray(data) ? data : []);
        const list: Participant[] = reports.map((r: Record<string, unknown>) => {
          const rawStatus = String(r.status ?? '미제출');
          const statusMap: Record<string, ParticipantStatus> = {
            '미제출': '미제출',
            '제출':   '제출완료',
            '승인':   '승인',
            '보완':   '보완',
          };
          return {
            id:         Number(r.reportId ?? r.id ?? 0),
            employeeNo: String(r.employeeNo ?? ''),
            name:       String(r.name ?? ''),
            dept:       String(r.team ?? ''),
            filename:   r.title ? String(r.title) : undefined,
            status:     statusMap[rawStatus] ?? '미제출',
          };
        });
        setParticipants(list);
      })
      .catch(() => {});
  }, [courseId]);

  const filtered = useMemo(() => {
    const q = searchQuery.trim().toLowerCase();
    if (!q) return participants;
    return participants.filter((p) =>
      p.name.toLowerCase().includes(q) || p.dept.toLowerCase().includes(q),
    );
  }, [participants, searchQuery]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated  = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  function handleAction(p: Participant) {
    const cfg = ACTION_CONFIG[p.status];
    const state = { courseName: courseInfo.name, courseId, action: cfg.action };
    if (cfg.action === 'view') {
      navigate(`/view/${p.id}`, { state });
    } else {
      navigate(`/submit/${p.id}`, { state });
    }
  }

  function handleDownloadConfirm() {
    if (!downloadTarget?.filename) return;
    downloadReport(downloadTarget.id, downloadTarget.filename!)
      .then(({ data }) => {
        const url = URL.createObjectURL(data);
        const a = document.createElement('a');
        a.href = url;
        a.download = downloadTarget.filename!;
        a.click();
        URL.revokeObjectURL(url);
      })
      .catch(() => {});
    setDownloadTarget(null);
  }

  return (
    <Box sx={{ maxWidth: 1200, mx: 'auto' }}>
      {/* ── Page header ─────────────────────────────────────────────────── */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'flex-start',
          justifyContent: 'space-between',
          mb: 3,
          gap: 2,
        }}
      >
        <Box>
          <Typography sx={{ fontSize: 22, fontWeight: 700, color: '#222222', mb: 0.75 }}>
            참여자 목록
          </Typography>
          <Typography sx={{ fontSize: 14, color: '#888888' }}>
            본인 이름을 찾아 클릭하면 독후감을 제출할 수 있습니다.
          </Typography>
        </Box>

        {/* Course info card */}
        <Box
          sx={{
            flexShrink: 0,
            border: '1px solid #dde5f4',
            borderRadius: '10px',
            bgcolor: '#f0f5ff',
            px: 2.5,
            py: 1.5,
            display: 'flex',
            flexDirection: 'column',
            gap: 0.5,
            minWidth: 200,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Chip
              label={courseInfo.name}
              size="small"
              sx={{
                bgcolor: '#0064dd',
                color: '#ffffff',
                fontWeight: 700,
                fontSize: 12,
                height: 22,
                '& .MuiChip-label': { px: 1 },
              }}
            />
          </Box>
          {courseInfo.endDate && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.75 }}>
              <CalendarTodayOutlinedIcon sx={{ fontSize: 12, color: '#6b8ccc' }} />
              <Typography sx={{ fontSize: 12, color: '#4a6fa5', fontWeight: 500 }}>
                {courseInfo.endDate.slice(0, 10).replace(/-/g, '.')} 까지
              </Typography>
            </Box>
          )}
        </Box>
      </Box>

      {/* ── Search + count row ───────────────────────────────────────────── */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          mb: 2,
          gap: 2,
        }}
      >
        <Typography sx={{ fontSize: 14, fontWeight: 700, color: '#222222' }}>
          전체 {participants.length}명
          {filtered.length !== participants.length && (
            <Box component="span" sx={{ fontWeight: 400, color: '#888888', ml: 1 }}>
              ({filtered.length}명 표시 중)
            </Box>
          )}
        </Typography>

        <TextField
          size="small"
          placeholder="이름, 소속 검색..."
          value={searchQuery}
          onChange={(e) => { setSearchQuery(e.target.value); setPage(1); }}
          sx={{ width: 220, bgcolor: '#ffffff' }}
        />
      </Box>

      {/* ── Table ────────────────────────────────────────────────────────── */}
      <Box
        sx={{
          bgcolor: '#ffffff',
          borderRadius: '8px',
          border: '1px solid #e8e8e8',
          overflow: 'hidden',
        }}
      >
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ borderTop: '1px solid #e0e0e0', borderBottom: '1px solid #e0e0e0' }}>
                {[
                  { label: '사번',   width: 90,        align: 'center' as const },
                  { label: '이름',   width: 120,       align: 'left'   as const },
                  { label: '소속명', width: 140,       align: 'left'   as const },
                  { label: '파일명', width: undefined, align: 'left'   as const },
                  { label: '작업',   width: 130,       align: 'center' as const },
                  { label: '상태',   width: 110,       align: 'center' as const },
                ].map((col, i) => (
                  <TableCell
                    key={i}
                    align={col.align}
                    width={col.width}
                    sx={{
                      bgcolor: '#f5f6f8',
                      fontWeight: 700,
                      color: '#555555',
                      fontSize: 13,
                      py: 1.5,
                      borderBottom: 'none',
                    }}
                  >
                    {col.label}
                  </TableCell>
                ))}
              </TableRow>
            </TableHead>

            <TableBody>
              {filtered.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={6}
                    align="center"
                    sx={{ py: 6, color: '#aaaaaa', fontSize: 14, border: 'none' }}
                  >
                    {participants.length === 0 ? '데이터를 불러오는 중...' : '검색 결과가 없습니다.'}
                  </TableCell>
                </TableRow>
              ) : (
                paginated.map((p) => {
                  const action = ACTION_CONFIG[p.status];
                  return (
                    <TableRow
                      key={p.id}
                      sx={{
                        bgcolor: '#ffffff',
                        '&:last-child td': { borderBottom: 'none' },
                        '&:hover': { bgcolor: '#fafcff' },
                      }}
                    >
                      {/* 사번 */}
                      <TableCell
                        align="center"
                        sx={{ fontSize: 13, color: '#555555', py: 1.5, borderBottom: '1px solid #eeeeee' }}
                      >
                        {p.employeeNo}
                      </TableCell>

                      {/* 이름 */}
                      <TableCell
                        sx={{ fontSize: 13, fontWeight: 600, color: '#222222', py: 1.5, borderBottom: '1px solid #eeeeee' }}
                      >
                        {p.name}
                      </TableCell>

                      {/* 소속 */}
                      <TableCell
                        sx={{ fontSize: 13, color: '#444444', py: 1.5, borderBottom: '1px solid #eeeeee' }}
                      >
                        {p.dept}
                      </TableCell>

                      {/* 파일명 */}
                      <TableCell
                        sx={{ fontSize: 13, color: '#444444', py: 1.5, borderBottom: '1px solid #eeeeee' }}
                      >
                        {p.filename ? (
                          <Link
                            component="button"
                            underline="hover"
                            onClick={() => setDownloadTarget(p)}
                            sx={{ fontSize: 13, color: '#0064dd', cursor: 'pointer', background: 'none', border: 'none', textAlign: 'left' }}
                          >
                            {p.filename}
                          </Link>
                        ) : (
                          <Typography component="span" sx={{ fontSize: 13, color: '#bbbbbb' }}>—</Typography>
                        )}
                      </TableCell>

                      {/* 작업 */}
                      <TableCell
                        align="center"
                        sx={{ py: 1.5, borderBottom: '1px solid #eeeeee' }}
                      >
                        <Link
                          component="button"
                          underline="hover"
                          onClick={() => handleAction(p)}
                          sx={{
                            fontSize: 13,
                            fontWeight: 600,
                            color: action.color,
                            cursor: 'pointer',
                            background: 'none',
                            border: 'none',
                          }}
                        >
                          {action.label}
                        </Link>
                      </TableCell>

                      {/* 상태 */}
                      <TableCell
                        align="center"
                        sx={{ py: 1.5, borderBottom: '1px solid #eeeeee' }}
                      >
                        <ParticipantStatusBadge status={p.status} />
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>
        </TableContainer>

        {/* Pagination footer */}
        {totalPages > 1 && (
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'center',
              py: 2,
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
        )}
      </Box>

      {/* Download confirm modal */}
      <ConfirmModal
        open={!!downloadTarget}
        title="파일 다운로드"
        confirmLabel="다운로드"
        onConfirm={handleDownloadConfirm}
        onCancel={() => setDownloadTarget(null)}
      >
        <Typography sx={{ fontSize: 14, color: '#444444' }}>
          <strong>{downloadTarget?.filename}</strong> 파일을 다운로드하시겠습니까?
        </Typography>
      </ConfirmModal>
    </Box>
  );
}
