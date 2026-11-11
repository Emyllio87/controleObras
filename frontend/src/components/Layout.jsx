import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useSessao } from '../context/SessaoContext';
import { MENU } from '../menu';

export default function Layout() {
  const { usuario, sair } = useSessao();
  const navigate = useNavigate();
  const [menuAberto, setMenuAberto] = useState(false);

  const itens = MENU.filter((item) => usuario.telas.includes(item.tela));

  function encerrar() {
    sair();
    navigate('/login', { replace: true });
  }

  return (
    <div className="min-h-screen bg-slate-100">
      <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 md:hidden">
        <span className="font-semibold text-slate-800">Controle de Obras</span>
        <button type="button" className="botao-secundario px-3 py-1" onClick={() => setMenuAberto((aberto) => !aberto)}>
          Menu
        </button>
      </header>

      <div className="flex">
        <aside
          className={`${menuAberto ? 'block' : 'hidden'} w-full border-b border-slate-200 bg-white md:block md:min-h-screen md:w-64 md:border-b-0 md:border-r`}
        >
          <div className="hidden px-5 py-5 md:block">
            <p className="text-lg font-semibold text-slate-800">Controle de Obras</p>
            <p className="mt-1 text-xs text-slate-500">Gestão de obras e pagamentos</p>
          </div>
          <nav className="flex flex-col gap-1 p-3">
            {itens.map((item) => (
              <NavLink
                key={item.rota}
                to={item.rota}
                onClick={() => setMenuAberto(false)}
                className={({ isActive }) =>
                  `rounded-md px-3 py-2 text-sm ${
                    isActive ? 'bg-sky-50 font-semibold text-sky-800' : 'text-slate-600 hover:bg-slate-50'
                  }`
                }
              >
                {item.rotulo}
              </NavLink>
            ))}
          </nav>
        </aside>

        <div className="flex-1">
          <div className="hidden items-center justify-end gap-4 border-b border-slate-200 bg-white px-6 py-3 md:flex">
            <span className="text-sm text-slate-600">{usuario.nome}</span>
            <button type="button" className="botao-secundario px-3 py-1" onClick={encerrar}>
              Sair
            </button>
          </div>

          <main className="p-4 md:p-8">
            <Outlet />
          </main>

          <div className="border-t border-slate-200 bg-white px-4 py-3 md:hidden">
            <button type="button" className="botao-secundario w-full" onClick={encerrar}>
              Sair
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
