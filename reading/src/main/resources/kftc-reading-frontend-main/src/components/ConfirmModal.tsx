import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Button,
  Divider,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';

interface ConfirmModalProps {
  open: boolean;
  title: string;
  children: React.ReactNode;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmColor?: 'primary' | 'error' | 'warning' | 'success' | 'info';
  onConfirm: () => void;
  onCancel: () => void;
}

export default function ConfirmModal({
  open,
  title,
  children,
  confirmLabel = '확인',
  cancelLabel = '취소',
  confirmColor = 'primary',
  onConfirm,
  onCancel,
}: ConfirmModalProps) {
  return (
    <Dialog
      open={open}
      onClose={onCancel}
      PaperProps={{
        sx: { borderRadius: '12px', minWidth: 400, maxWidth: 480 },
      }}
    >
      {/* Header */}
      <DialogTitle
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          py: 2,
          px: 3,
        }}
      >
        <Typography sx={{ fontWeight: 700, fontSize: 16, color: '#222222' }}>
          {title}
        </Typography>
        <IconButton size="small" onClick={onCancel} sx={{ color: '#888888' }}>
          <CloseIcon fontSize="small" />
        </IconButton>
      </DialogTitle>

      <Divider />

      {/* Content */}
      <DialogContent sx={{ px: 3, py: 2.5 }}>{children}</DialogContent>

      {/* Footer */}
      <DialogActions sx={{ px: 3, pb: 2.5, pt: 1, gap: 1 }}>
        <Button
          variant="outlined"
          onClick={onCancel}
          sx={{
            height: 40,
            color: '#666666',
            borderColor: '#cccccc',
            '&:hover': { borderColor: '#aaaaaa', bgcolor: 'transparent' },
          }}
        >
          {cancelLabel}
        </Button>
        <Button variant="contained" color={confirmColor} onClick={onConfirm} sx={{ height: 40 }}>
          {confirmLabel}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
