import {
  Box,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Pagination,
} from '@mui/material';

export interface Column<T> {
  id: keyof T | string;
  label: React.ReactNode;
  width?: number | string;
  align?: 'left' | 'center' | 'right';
  render?: (value: unknown, row: T) => React.ReactNode;
}

interface DataTableProps<T extends object> {
  columns: Column<T>[];
  rows: T[];
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function DataTable<T extends object>({
  columns,
  rows,
  page,
  totalPages,
  onPageChange,
}: DataTableProps<T>) {
  return (
    <Box>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow
              sx={{
                borderTop: '1px solid #e0e0e0',
                borderBottom: '1px solid #e0e0e0',
              }}
            >
              {columns.map((col) => (
                <TableCell
                  key={String(col.id)}
                  align={col.align ?? 'left'}
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
            {rows.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  align="center"
                  sx={{ py: 6, color: '#aaaaaa', fontSize: 14, border: 'none' }}
                >
                  데이터가 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              rows.map((row, rowIndex) => (
                <TableRow
                  key={rowIndex}
                  sx={{
                    bgcolor: rowIndex % 2 === 0 ? '#ffffff' : '#fafbfc',
                    '&:last-child td': { borderBottom: 'none' },
                  }}
                >
                  {columns.map((col) => {
                    const value = (row as Record<string, unknown>)[String(col.id)];
                    return (
                      <TableCell
                        key={String(col.id)}
                        align={col.align ?? 'left'}
                        sx={{
                          fontSize: 13,
                          color: '#222222',
                          py: 1.5,
                          borderBottom: '1px solid #eeeeee',
                        }}
                      >
                        {col.render ? col.render(value, row) : (value as React.ReactNode)}
                      </TableCell>
                    );
                  })}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', pt: 3 }}>
          <Pagination
            count={totalPages}
            page={page}
            onChange={(_, p) => onPageChange(p)}
            color="primary"
            shape="rounded"
            size="small"
          />
        </Box>
      )}
    </Box>
  );
}
