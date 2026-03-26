import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  IconButton,
  Typography,
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import type { ReactNode } from 'react';

interface FormDialogProps {
  open: boolean;
  title: string;
  confirmLabel: string;
  onClose: () => void;
  /** Use with a <form id={formId}> inside children for submit-based dialogs */
  formId?: string;
  /** Use for click-based confirmation */
  onConfirm?: () => void;
  /** Dialog paper width (default: 480) */
  width?: number;
  /** Called when the dialog's enter transition begins — useful to reset form state */
  onEnter?: () => void;
  children: ReactNode;
}

export default function FormDialog({
  open,
  title,
  confirmLabel,
  onClose,
  formId,
  onConfirm,
  width = 480,
  onEnter,
  children,
}: FormDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={onClose}
      TransitionProps={{ onEnter }}
      PaperProps={{ sx: { borderRadius: '12px', width } }}
    >
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
        <IconButton size="small" onClick={onClose} sx={{ color: '#888888' }}>
          <CloseIcon fontSize="small" />
        </IconButton>
      </DialogTitle>

      <Divider />

      <DialogContent sx={{ px: 3, py: 2.5 }}>
        {children}
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2.5, pt: 1, gap: 1 }}>
        <Button
          variant="outlined"
          onClick={onClose}
          sx={{ height: 40, color: '#666666', borderColor: '#cccccc', '&:hover': { borderColor: '#aaaaaa' } }}
        >
          취소
        </Button>
        {formId ? (
          <Button type="submit" form={formId} variant="contained" color="primary" sx={{ height: 40 }}>
            {confirmLabel}
          </Button>
        ) : (
          <Button
            variant="contained"
            onClick={onConfirm}
            sx={{ height: 40, bgcolor: '#0064dd', '&:hover': { bgcolor: '#004ca8' } }}
          >
            {confirmLabel}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
