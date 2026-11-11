import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom';
import { useSessao } from '../context/SessaoContext';
import api, { mensagemErro } from '../services/api';

export default function Login() {
  const { usuario, entrar } = useSessao();
  const navigate = useNavigate();
  const [form, setForm] = useState({ login: '', senha: '' });
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);

  if (usuario) {
    return <Navigate to="/" replace />;
  }

  async function submeter(evento) {
    evento.preventDefault();
    setErro('');
    setEnviando(true);
    try {
      const { data } = await api.post('/auth/login', form);
      entrar(data);
      navigate('/', { replace: true });
    } catch (falha) {
      setErro(mensagemErro(falha));
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="w-full max-w-sm">
        <div className="mb-6 text-center">
          <h1 className="text-2xl font-semibold text-slate-800">Controle de Obras</h1>
          <p className="mt-1 text-sm text-slate-500">Acesse com seu usuário e senha</p>
        </div>

        <form onSubmit={submeter} className="cartao space-y-4">
          {erro && <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{erro}</div>}

          <div>
            <label htmlFor="login" className="rotulo">Login</label>
            <input
              id="login"
              className="entrada"
              autoComplete="username"
              value={form.login}
              onChange={(evento) => setForm({ ...form, login: evento.target.value })}
              required
            />
          </div>

          <div>
            <label htmlFor="senha" className="rotulo">Senha</label>
            <input
              id="senha"
              type="password"
              className="entrada"
              autoComplete="current-password"
              value={form.senha}
              onChange={(evento) => setForm({ ...form, senha: evento.target.value })}
              required
            />
          </div>

          <button type="submit" className="botao w-full" disabled={enviando}>
            {enviando ? 'Entrando...' : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  );
}
