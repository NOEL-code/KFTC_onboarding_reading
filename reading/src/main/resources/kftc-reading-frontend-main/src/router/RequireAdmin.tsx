import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.tsx';

const DEV_BYPASS = false;

/**
 * Wraps admin routes. Redirects to /admin/login if not authenticated,
 * preserving the intended destination so the user is sent there after login.
 */
export default function RequireAdmin() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!DEV_BYPASS && !isAuthenticated) {
    return <Navigate to="/admin/login" state={{ from: location }} replace />;
  }

  return <Outlet />;
}
