import { useEffect, useState } from 'react';
import Aviso from '../components/Aviso';
import Campo from '../components/Campo';
import api, { errosCampos, mensagemErro } from '../api';
import { STATUS_USUARIO } from '../utils/constantes';
import { emailValido } from '../utils/validacoes';

const FORM_VAZIO = { nome: '', login: '', senha: '', status: 'ATIVO', telas: [] };

export default function Usuarios() {
  const [lista, setLista] = useState([]);
  const [telas, setTelas] = useState([]);
  const [form, setForm] = useState(FORM_VAZIO);
  const [editando, setEditando] = useState(null);
  const [erros, setErros] = useState({});
  const [aviso, setAviso] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    carregar();
  }, []);

  async function carregar() {
    try {
      const [usuarios, telasDisponiveis] = await Promise.all([api.get('/usuarios'), api.get('/telas')]);
      setLista(usuarios.data);
      setTelas(telasDisponiveis.data);
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  function limpar() {
    setForm(FORM_VAZIO);
    setEditando(null);
    setErros({});
  }

  function alternarTela(chave) {
    setForm((atual) => ({
      ...atual,
      telas: atual.telas.includes(chave)
        ? atual.telas.filter((tela) => tela !== chave)
        : [...atual.telas, chave],
    }));
  }

  async function salvar(evento) {
    evento.preventDefault();
    setErros({});
    setAviso(null);

    const novos = {};
    if (!form.nome.trim()) {
      novos.nome = 'Nome é obrigatório';
    }
    if (!form.login.trim()) {
      novos.login = 'E-mail é obrigatório';
    } else if (!emailValido(form.login)) {
      novos.login = 'E-mail inválido';
    }
    if (!editando && !form.senha.trim()) {
      novos.senha = 'Senha é obrigatória';
    } else if (form.senha && form.senha.length < 6) {
      novos.senha = 'Senha deve ter ao menos 6 caracteres';
    }
    if (!form.status) {
      novos.status = 'Status é obrigatório';
    }
    if (form.telas.length === 0) {
      novos.telas = 'Selecione ao menos uma tela';
    }
    if (Object.keys(novos).length > 0) {
      setErros(novos);
      setAviso({ tipo: 'erro', texto: 'Verifique os campos destacados' });
      return;
    }

    const corpo = { ...form, senha: form.senha || null };
    setEnviando(true);
    try {
      if (editando) {
        await api.put(`/usuarios/${editando}`, corpo);
        setAviso({ tipo: 'sucesso', texto: 'Usuário atualizado' });
      } else {
        await api.post('/usuarios', corpo);
        setAviso({ tipo: 'sucesso', texto: 'Usuário cadastrado' });
      }
      limpar();
      carregar();
    } catch (falha) {
      setErros(errosCampos(falha));
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    } finally {
      setEnviando(false);
    }
  }

  function editar(item) {
    setEditando(item.id);
    setErros({});
    setForm({ nome: item.nome, login: item.login, senha: '', status: item.status, telas: [...item.telas] });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function excluir(item) {
    if (!window.confirm(`Excluir o usuário ${item.nome}?`)) {
      return;
    }
    try {
      await api.delete(`/usuarios/${item.id}`);
      setAviso({ tipo: 'sucesso', texto: 'Usuário excluído' });
      if (editando === item.id) {
        limpar();
      }
      carregar();
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Usuários</h1>
        <p className="mt-1 text-sm text-slate-500">Cadastro de usuários e definição das telas permitidas.</p>
      </div>

      <Aviso aviso={aviso} aoFechar={() => setAviso(null)} />

      <form onSubmit={salvar} className="cartao">
        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
          {editando ? 'Editar usuário' : 'Novo usuário'}
        </h2>

        <div className="grid gap-4 md:grid-cols-2">
          <Campo rotulo="Nome" obrigatorio erro={erros.nome}>
            <input
              className={`entrada ${erros.nome ? 'entrada-erro' : ''}`}
              value={form.nome}
              onChange={(evento) => setForm({ ...form, nome: evento.target.value })}
              maxLength={120}
            />
          </Campo>

          <Campo rotulo="E-mail" obrigatorio erro={erros.login}>
            <input
              type="email"
              className={`entrada ${erros.login ? 'entrada-erro' : ''}`}
              value={form.login}
              onChange={(evento) => setForm({ ...form, login: evento.target.value })}
              placeholder="nome@empresa.com"
              maxLength={60}
            />
          </Campo>

          <Campo rotulo={editando ? 'Nova senha (opcional)' : 'Senha'} obrigatorio={!editando} erro={erros.senha}>
            <input
              type="password"
              className={`entrada ${erros.senha ? 'entrada-erro' : ''}`}
              value={form.senha}
              onChange={(evento) => setForm({ ...form, senha: evento.target.value })}
              autoComplete="new-password"
            />
          </Campo>

          <Campo rotulo="Status" obrigatorio erro={erros.status}>
            <select
              className="entrada"
              value={form.status}
              onChange={(evento) => setForm({ ...form, status: evento.target.value })}
            >
              {STATUS_USUARIO.map((status) => (
                <option key={status.valor} value={status.valor}>
                  {status.rotulo}
                </option>
              ))}
            </select>
          </Campo>
        </div>

        <Campo rotulo="Telas permitidas" obrigatorio erro={erros.telas} className="mt-4">
          <div className="grid gap-2 rounded-md border border-slate-200 p-3 sm:grid-cols-2 lg:grid-cols-3">
            {telas.map((tela) => (
              <label key={tela.chave} className="flex items-center gap-2 text-sm text-slate-700">
                <input
                  type="checkbox"
                  className="h-4 w-4 rounded border-slate-300"
                  checked={form.telas.includes(tela.chave)}
                  onChange={() => alternarTela(tela.chave)}
                />
                {tela.descricao}
              </label>
            ))}
          </div>
        </Campo>

        <div className="mt-5 flex flex-wrap gap-3">
          <button type="submit" className="botao" disabled={enviando}>
            {editando ? 'Salvar alterações' : 'Cadastrar'}
          </button>
          <button type="button" className="botao-secundario" onClick={limpar}>
            Limpar
          </button>
        </div>
      </form>

      <div className="cartao">
        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
          Usuários cadastrados
        </h2>
        <div className="overflow-x-auto">
          <table className="tabela">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Status</th>
                <th>Telas</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td className="font-medium text-slate-800">{item.nome}</td>
                  <td>{item.login}</td>
                  <td>
                    <span
                      className={`etiqueta ${
                        item.status === 'ATIVO'
                          ? 'bg-emerald-100 text-emerald-700'
                          : 'bg-slate-200 text-slate-600'
                      }`}
                    >
                      {item.status === 'ATIVO' ? 'Ativo' : 'Inativo'}
                    </span>
                  </td>
                  <td>{item.telas.length}</td>
                  <td className="space-x-3 text-right">
                    <button type="button" className="botao-link" onClick={() => editar(item)}>
                      Editar
                    </button>
                    <button
                      type="button"
                      className="text-sm font-medium text-red-600 hover:underline"
                      onClick={() => excluir(item)}
                    >
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
              {lista.length === 0 && (
                <tr>
                  <td colSpan={6} className="py-6 text-center text-sm text-slate-500">
                    Nenhum usuário cadastrado.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
