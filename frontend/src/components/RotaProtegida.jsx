import { Navigate, Outlet } from 'react-router-dom';
import { useSessao } from '../sessao';

export default function RotaProtegida() {
  const { usuario, carregando } = useSessao();

  if (carregando) {
    return <div className="flex min-h-screen items-center justify-center text-sm text-slate-500">Carregando...</div>;
  }

  if (!usuario) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}
