import { Navigate } from 'react-router-dom';
import { useSessao } from '../context/SessaoContext';
import { MENU } from '../menu';

export default function Inicio() {
  const { usuario } = useSessao();
  const destino = MENU.find((item) => usuario.telas.includes(item.tela));

  if (!destino) {
    return (
      <div className="cartao">
        <h1 className="text-lg font-semibold text-slate-800">Nenhuma tela liberada</h1>
        <p className="mt-2 text-sm text-slate-600">
          Seu usuário não possui permissão para nenhuma tela. Procure o administrador do sistema.
        </p>
      </div>
    );
  }

  return <Navigate to={destino.rota} replace />;
}
