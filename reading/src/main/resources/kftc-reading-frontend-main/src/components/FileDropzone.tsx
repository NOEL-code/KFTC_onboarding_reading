import { useRef } from 'react';
import { Box, Typography, IconButton } from '@mui/material';
import FileUploadOutlinedIcon from '@mui/icons-material/FileUploadOutlined';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';

interface FileDropzoneProps {
  accept?: string;
  disabled?: boolean;
  file: File | null;
  onFileChange: (file: File | null) => void;
  mainText?: string;
  subText?: string;
}

function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export default function FileDropzone({
  accept,
  disabled = false,
  file,
  onFileChange,
  mainText = '파일을 여기에 드래그하거나 클릭하여 업로드',
  subText = '.hwp, .hwpx 파일만 업로드 가능합니다',
}: FileDropzoneProps) {
  const inputRef = useRef<HTMLInputElement>(null);

  function handleDrop(e: React.DragEvent<HTMLDivElement>) {
    e.preventDefault();
    if (disabled) return;
    const dropped = e.dataTransfer.files[0];
    if (dropped) onFileChange(dropped);
  }

  function handleDragOver(e: React.DragEvent<HTMLDivElement>) {
    e.preventDefault();
  }

  function handleClick() {
    if (!disabled) inputRef.current?.click();
  }

  function handleInputChange(e: React.ChangeEvent<HTMLInputElement>) {
    const selected = e.target.files?.[0] ?? null;
    onFileChange(selected);
    e.target.value = '';
  }

  function handleDelete(e: React.MouseEvent) {
    e.stopPropagation();
    onFileChange(null);
  }

  return (
    <Box>
      {/* Drop zone */}
      <Box
        onClick={handleClick}
        onDrop={handleDrop}
        onDragOver={handleDragOver}
        sx={{
          border: '2px dashed #0064dd',
          borderRadius: '8px',
          bgcolor: '#f8f9fb',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          py: 4,
          px: 2,
          cursor: disabled ? 'not-allowed' : 'pointer',
          opacity: disabled ? 0.5 : 1,
          transition: 'opacity 0.2s',
        }}
      >
        <Box
          sx={{
            width: 48,
            height: 48,
            borderRadius: '50%',
            bgcolor: '#e8f0fe',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            mb: 1.5,
          }}
        >
          <FileUploadOutlinedIcon sx={{ color: '#0064dd', fontSize: 24 }} />
        </Box>

        <Typography sx={{ fontSize: 14, color: '#666666', mb: 0.5 }}>
          {mainText}
        </Typography>
        <Typography sx={{ fontSize: 12, color: '#999999' }}>
          {subText}
        </Typography>

        <input
          ref={inputRef}
          type="file"
          accept={accept}
          style={{ display: 'none' }}
          onChange={handleInputChange}
          disabled={disabled}
        />
      </Box>

      {/* Attached file info */}
      {file && (
        <Box
          sx={{
            mt: 1.5,
            px: 2,
            py: 1,
            bgcolor: '#f0f7ff',
            borderRadius: '6px',
            display: 'flex',
            alignItems: 'center',
            gap: 1,
          }}
        >
          <AttachFileIcon sx={{ fontSize: 16, color: '#0064dd' }} />
          <Typography noWrap sx={{ fontSize: 13, color: '#222222', flexGrow: 1 }}>
            {file.name}
          </Typography>
          <Typography sx={{ fontSize: 12, color: '#888888', whiteSpace: 'nowrap' }}>
            {formatBytes(file.size)}
          </Typography>
          <IconButton size="small" onClick={handleDelete} sx={{ color: '#cc3333', p: 0.5 }}>
            <DeleteOutlineIcon sx={{ fontSize: 16 }} />
          </IconButton>
        </Box>
      )}
    </Box>
  );
}
