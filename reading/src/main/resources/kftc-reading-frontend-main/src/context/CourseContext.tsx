import { createContext, useContext, useState, useEffect } from 'react';
import { fetchCourses } from '../api/courseApi.ts';

export interface Course {
  id: number;
  name: string;
  status: '진행중' | '완료' | '종료';
  startDate?: string;
  endDate?: string;
}

interface CourseContextValue {
  courses: Course[];
  selectedCourseId: string;
  setSelectedCourseId: (id: string) => void;
}

const CourseContext = createContext<CourseContextValue | null>(null);

export function CourseProvider({ children }: { children: React.ReactNode }) {
  const [courses, setCourses] = useState<Course[]>([]);
  const [selectedCourseId, setSelectedCourseId] = useState('');

  useEffect(() => {
    fetchCourses()
      .then(({ data }) => {
        const raw = data.courses ?? data.content ?? (Array.isArray(data) ? data : []);
        const list: Course[] = raw.map((c: Record<string, unknown>) => ({
          id:        Number(c.courseId ?? c.id),
          name:      String(c.name ?? ''),
          status:    (c.status ?? '진행중') as Course['status'],
          startDate: c.startDate ? String(c.startDate) : undefined,
          endDate:   c.endDate ? String(c.endDate) : undefined,
        }));
        if (list.length > 0) {
          list.sort((a, b) => {
            const aEnded = a.status === '종료' ? 1 : 0;
            const bEnded = b.status === '종료' ? 1 : 0;
            if (aEnded !== bEnded) return aEnded - bEnded;
            return (b.startDate ?? '').localeCompare(a.startDate ?? '');
          });
          setCourses(list);
        }
      })
      .catch(() => {});
  }, []);

  return (
    <CourseContext.Provider value={{ courses, selectedCourseId, setSelectedCourseId }}>
      {children}
    </CourseContext.Provider>
  );
}

export function useCourse(): CourseContextValue {
  const ctx = useContext(CourseContext);
  if (!ctx) throw new Error('useCourse must be used within CourseProvider');
  return ctx;
}
