import { useRef, useState, useEffect } from 'react';
import {
  Box,
  Button,
  Chip,
  Typography,
} from '@mui/material';
import InfoOutlinedIcon from '@mui/icons-material/InfoOutlined';
import DownloadOutlinedIcon from '@mui/icons-material/DownloadOutlined';
import FileUploadOutlinedIcon from '@mui/icons-material/FileUploadOutlined';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import axios from 'axios';
import ConfirmModal from '../../components/ConfirmModal.tsx';
import PageHeader from '../../components/PageHeader.tsx';
import { downloadFile } from '../../utils/downloadFile.ts';

// ─── Types ────────────────────────────────────────────────────────────────────

interface TemplateInfo {
  templateId: number;
  fileName: string;
  fileSize: number;
  uploadedAt: string;
  status: string;
}

// ─── HWP icon ─────────────────────────────────────────────────────────────────

function HwpIcon() {
  return (
    <Box
      sx={{
        width: 40,
        height: 40,
        borderRadius: '50%',
        bgcolor: '#f0f0f0',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
      }}
    >
      <Typography sx={{ fontSize: 11, fontWeight: 700, color: '#555555', letterSpacing: '-0.5px' }}>
        HWP
      </Typography>
    </Box>
  );
}

function formatFileSize(bytes: number): string {
  if (!bytes) return '—';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

// ─── Main page ────────────────────────────────────────────────────────────────

export default function TemplateManagement() {
  const [template, setTemplate] = useState<TemplateInfo | null>(null);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const uploadInputRef = useRef<HTMLInputElement>(null);

  // ── Load current template ───────────────────────────────────────────────────

  useEffect(() => {
    axios
      .get('/api/admin/templates')
      .then(({ data }) => {
        const info = Array.isArray(data)
          ? (data.find((t: TemplateInfo) => t.status === '사용중') ?? data[0])
          : data;
        if (info) setTemplate(info);
      })
      .catch(() => {});
  }, []);

  // ── Handlers ───────────────────────────────────────────────────────────────

  function handleUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    axios
      .post('/api/admin/templates', formData)
      .then(({ data }) => setTemplate(data))
      .catch(() => {});
    e.target.value = '';
  }

  function handleDownload() {
    if (!template) return;
    axios
      .get(`/api/admin/templates/${template.templateId}/download`, { responseType: 'blob' })
      .then(({ data }) => downloadFile(data, template.fileName))
      .catch(() => {});
  }

  function handleDeleteConfirm() {
    if (!template) return;
    axios
      .delete(`/api/admin/templates/${template.templateId}`)
      .then(() => setTemplate(null))
      .catch(() => {});
    setDeleteOpen(false);
  }

  // ── Render ─────────────────────────────────────────────────────────────────

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column' }}>
      <PageHeader title="독후감 양식 관리" />

      {/* Info banner */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: 1.5,
          bgcolor: '#e8f0fe',
          borderLeft: '4px solid #0064dd',
          borderRadius: '4px',
          px: 2,
          py: 1.5,
          mb: 3,
        }}
      >
        <InfoOutlinedIcon sx={{ color: '#0064dd', fontSize: 20, mt: '1px', flexShrink: 0 }} />
        <Typography sx={{ fontSize: 14, color: '#333333', lineHeight: 1.6 }}>
          독후감 양식은 모든 독서 과정에 공통 적용됩니다. 양식 변경 시 이후 제출분에 반영됩니다.
        </Typography>
      </Box>

      {/* Current template card */}
      <Box
        sx={{
          bgcolor: '#ffffff',
          borderRadius: '8px',
          boxShadow: '0 1px 6px rgba(0,0,0,0.06)',
          p: 3,
          display: 'flex',
          flexDirection: 'column',
          gap: 2.5,
        }}
      >
        {/* Card header */}
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Typography sx={{ fontSize: 16, fontWeight: 700, color: '#222222' }}>
            현재 등록된 양식
          </Typography>
          {template && (
            <Chip
              label={template.status ?? '사용중'}
              size="small"
              sx={{
                bgcolor: '#e8f4fd',
                color: '#0064dd',
                borderRadius: '4px',
                fontSize: 12,
                fontWeight: 500,
                height: 24,
                '& .MuiChip-label': { px: 1 },
              }}
            />
          )}
        </Box>

        {/* File info row */}
        {template ? (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <HwpIcon />
            <Box>
              <Typography sx={{ fontSize: 14, fontWeight: 700, color: '#222222' }}>
                {template.fileName}
              </Typography>
              <Typography sx={{ fontSize: 12, color: '#888888', mt: 0.25 }}>
                {formatFileSize(template.fileSize)} · {template.uploadedAt ? String(template.uploadedAt).slice(0, 10).replace(/-/g, '.') : ''} 업로드
              </Typography>
            </Box>
          </Box>
        ) : (
          <Typography sx={{ fontSize: 14, color: '#aaaaaa' }}>
            등록된 양식이 없습니다.
          </Typography>
        )}

        {/* Action buttons */}
        <Box sx={{ display: 'flex', gap: 1, pt: 0.5 }}>
          <input
            ref={uploadInputRef}
            type="file"
            accept=".hwp"
            style={{ display: 'none' }}
            onChange={handleUpload}
          />
          <Button
            variant="contained"
            startIcon={<FileUploadOutlinedIcon />}
            onClick={() => uploadInputRef.current?.click()}
            sx={{ height: 40, bgcolor: '#0064dd', '&:hover': { bgcolor: '#004ca8' } }}
          >
            업로드
          </Button>
          {template && (
            <>
              <Button
                variant="outlined"
                startIcon={<DownloadOutlinedIcon />}
                onClick={handleDownload}
                sx={{ height: 40, color: '#555555', borderColor: '#cccccc', '&:hover': { borderColor: '#aaaaaa' } }}
              >
                다운로드
              </Button>
              <Button
                variant="outlined"
                startIcon={<DeleteOutlineIcon />}
                onClick={() => setDeleteOpen(true)}
                sx={{
                  height: 40,
                  color: '#cc3333',
                  borderColor: '#e8a0a0',
                  '&:hover': { borderColor: '#cc3333', bgcolor: 'transparent' },
                }}
              >
                삭제
              </Button>
            </>
          )}
        </Box>
      </Box>

      {/* Delete confirm modal */}
      <ConfirmModal
        open={deleteOpen}
        title="양식 삭제"
        confirmLabel="삭제"
        confirmColor="error"
        onConfirm={handleDeleteConfirm}
        onCancel={() => setDeleteOpen(false)}
      >
        <Typography sx={{ fontSize: 14, color: '#444444' }}>
          현재 등록된 양식(<strong>{template?.fileName}</strong>)을 삭제하시겠습니까?
          <Typography component="span" sx={{ display: 'block', fontSize: 13, color: '#cc3333', mt: 1 }}>
            삭제 후 사용자가 양식을 다운로드할 수 없게 됩니다.
          </Typography>
        </Typography>
      </ConfirmModal>
    </Box>
  );
}
