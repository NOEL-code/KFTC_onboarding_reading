import { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

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
    axios
      .get('/api/courses')
      .then(({ data }) => {
        const list: Course[] = Array.isArray(data)
          ? data
          : (data.courses ?? data.content ?? []);
        if (list.length > 0) {
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
