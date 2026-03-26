---
name: KFTC 독후감 제출 시스템 - 프로젝트 개요
description: 프로젝트 스택, API 연결 현황, 주요 구조에 대한 요약
type: project
---

## 기술 스택

- React 19 + TypeScript, Vite 빌드
- MUI(Material UI) 7, React Router 7, Axios
- 상태 관리: React Context (CourseContext, AuthContext)

## API 기본 정보

- Base URL: `https://{domain}/api`
- 사용자 API: 사번 기반 인증
- 관리자 API: JWT Bearer Token (`POST /api/admin/login`)

## 주요 엔드포인트 (docs/KFTC_API명세서.html 기준)

### 사용자
- `GET /api/courses` — 독서과정 목록 (CourseContext에서 로드)
- `GET /api/courses/{courseId}/reports` — 과정별 참여자+독후감 목록
- `GET /api/courses/{courseId}` — 과정 상세 정보
- `POST /api/reports` — 독후감 제출 (courseId, employeeNo, title, file)
- `PUT /api/reports/{reportId}` — 독후감 수정/재제출
- `GET /api/reports/{reportId}` — 독후감 조회
- `GET /api/reports/{reportId}/download` — 독후감 파일 다운로드
- `GET /api/templates/download` — 독후감 양식 다운로드

### 관리자
- `POST /api/admin/login` — loginId, password
- `GET /api/admin/users?courseId=...` — 사용자 목록 (team 필드 사용)
- `POST /api/admin/users` — `{ users: [{employeeNo, name, team, courseId}] }`
- `PUT /api/admin/users` — `{ courseId, updates: [{userId, name, team}] }`
- `DELETE /api/admin/users` — `{ courseId, userIds: [...] }` (body)
- `POST /api/admin/users/upload` — 엑셀 업로드 (courseId, file)
- `GET /api/admin/reports?courseId=...` — 독후감 목록 (summary 포함)
- `PATCH /api/admin/reports/approve` — `{ reportIds: [...] }`
- `PATCH /api/admin/reports/supplement` — `{ reportIds: [...] }`
- `GET /api/admin/reports/{id}/download` — 독후감 파일 다운로드
- `GET /api/admin/templates` — 현재 양식 정보
- `POST /api/admin/templates` — 양식 업로드
- `DELETE /api/admin/templates/{templateId}` — 양식 삭제
- `GET /api/admin/courses` — 과정 목록 (통계 포함)
- `POST/PUT/DELETE /api/admin/courses/{courseId}` — 과정 CRUD

## API 연결 완료 현황 (2026-03-26)

모든 주요 페이지의 API 연결 완료:
- CourseContext: GET /api/courses 로 동적 로드
- AdminLogin: loginId 필드로 전송
- ReportManagement: courseId 파라미터, PATCH approve/supplement, summary 동적화
- UserManagement: courseId 적용, 올바른 API 포맷, 엑셀 업로드 연결
- CourseManagement: GET /api/admin/courses 로 초기 데이터 로드
- TemplateManagement: GET /api/admin/templates, DELETE /{templateId}
- ParticipantList: GET /api/courses/{courseId}/reports
- Submission: POST /api/reports 또는 PUT /api/reports/{id}
- ReportView: GET /api/reports/{reportId}
- UserMain: GET /api/templates/download

## 공통 컴포넌트 (src/components/)

자동 추출된 공통 컴포넌트:
- `FormDialog` — Dialog 래퍼 (title, confirmLabel, formId/onConfirm)
- `FormField` — label + input 래퍼
- `PageHeader` — 페이지 타이틀 + 우측 액션 버튼
- `InfoRow` — label:value 한 줄 표시
- `TabNavigation` — 탭 네비게이션
- `downloadFile` util — Blob 파일 다운로드 헬퍼 (src/utils/downloadFile.ts)

## 필드 매핑 주의사항

- 프론트엔드 `dept` ↔ API `team` (UserManagement에서 매핑)
- 프론트엔드 `name` ↔ API `employeeName` (ReportManagement에서 매핑)
- 과정 ID: CourseContext의 selectedCourseId (string) — Number()로 변환 필요

**Why:** docs/KFTC_API명세서.html, KFTC_기능명세서.html, KFTC_ERD.html 기준으로 API 연결 작업 완료
**How to apply:** 새 기능 추가 시 위 엔드포인트와 필드명 참고
