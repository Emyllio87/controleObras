import { Navigate } from 'react-router-dom';
import { useSessao } from '../context/SessaoContext';

export default function RotaTela({ tela, children }) {
  const { usuario } = useSessao();

  if (!usuario?.telas?.includes(tela)) {
    return <Navigate to="/" replace />;
  }

  return children;
}
