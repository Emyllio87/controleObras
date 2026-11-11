import { useEffect, useState } from 'react';
import Aviso from '../components/Aviso';
import Campo from '../components/Campo';
import api, { errosCampos, mensagemErro } from '../services/api';
import { baixarArquivo } from '../utils/download';
import { quantidade } from '../utils/formatadores';

const FORM_VAZIO = { nome: '', quantidade: '', unidade: 'UN' };

export default function Almoxarifado() {
  const [lista, setLista] = useState([]);
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
      const { data } = await api.get('/ferramentas');
      setLista(data);
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  function limpar() {
    setForm(FORM_VAZIO);
    setEditando(null);
    setErros({});
  }

  async function salvar(evento) {
    evento.preventDefault();
    setErros({});
    setAviso(null);

    const valorQuantidade = Number(String(form.quantidade).replace(',', '.'));
    const novos = {};
    if (!form.nome.trim()) {
      novos.nome = 'Nome é obrigatório';
    }
    if (!valorQuantidade || valorQuantidade <= 0) {
      novos.quantidade = 'Informe uma quantidade maior que zero';
    }
    if (!form.unidade.trim()) {
      novos.unidade = 'Unidade é obrigatória';
    }
    if (Object.keys(novos).length > 0) {
      setErros(novos);
      setAviso({ tipo: 'erro', texto: 'Verifique os campos destacados' });
      return;
    }

    const corpo = { ...form, quantidade: valorQuantidade };
    setEnviando(true);
    try {
      if (editando) {
        await api.put(`/ferramentas/${editando}`, corpo);
        setAviso({ tipo: 'sucesso', texto: 'Ferramenta atualizada' });
      } else {
        await api.post('/ferramentas', corpo);
        setAviso({ tipo: 'sucesso', texto: 'Ferramenta cadastrada' });
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

  function editar(ferramenta) {
    setEditando(ferramenta.id);
    setErros({});
    setForm({
      nome: ferramenta.nome,
      quantidade: String(ferramenta.quantidade),
      unidade: ferramenta.unidade,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function excluir(ferramenta) {
    if (!window.confirm(`Excluir a ferramenta ${ferramenta.nome}?`)) {
      return;
    }
    try {
      await api.delete(`/ferramentas/${ferramenta.id}`);
      setAviso({ tipo: 'sucesso', texto: 'Ferramenta excluída' });
      if (editando === ferramenta.id) {
        limpar();
      }
      carregar();
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  async function gerarPdf() {
    try {
      await baixarArquivo('/ferramentas/pdf', 'almoxarifado.pdf');
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold text-slate-800">Almoxarifado</h1>
          <p className="mt-1 text-sm text-slate-500">Cadastro das ferramentas disponíveis na obra.</p>
        </div>
        <button type="button" className="botao-secundario" onClick={gerarPdf}>
          Gerar PDF
        </button>
      </div>

      <Aviso aviso={aviso} aoFechar={() => setAviso(null)} />

      <form onSubmit={salvar} className="cartao">
        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
          {editando ? 'Editar ferramenta' : 'Nova ferramenta'}
        </h2>

        <div className="grid gap-4 md:grid-cols-4">
          <Campo rotulo="Nome / descrição" obrigatorio erro={erros.nome} className="md:col-span-2">
            <input
              className={`entrada ${erros.nome ? 'entrada-erro' : ''}`}
              value={form.nome}
              onChange={(evento) => setForm({ ...form, nome: evento.target.value })}
              maxLength={150}
            />
          </Campo>

          <Campo rotulo="Quantidade" obrigatorio erro={erros.quantidade}>
            <input
              type="number"
              min="0.001"
              step="0.001"
              className={`entrada ${erros.quantidade ? 'entrada-erro' : ''}`}
              value={form.quantidade}
              onChange={(evento) => setForm({ ...form, quantidade: evento.target.value })}
            />
          </Campo>

          <Campo rotulo="Unidade" obrigatorio erro={erros.unidade}>
            <input
              className={`entrada ${erros.unidade ? 'entrada-erro' : ''}`}
              value={form.unidade}
              onChange={(evento) => setForm({ ...form, unidade: evento.target.value })}
              maxLength={20}
            />
          </Campo>
        </div>

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
          Ferramentas cadastradas
        </h2>
        <div className="overflow-x-auto">
          <table className="tabela">
            <thead>
              <tr>
                <th>ID</th>
                <th>Ferramenta</th>
                <th>Quantidade</th>
                <th>Unidade</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((ferramenta) => (
                <tr key={ferramenta.id}>
                  <td>{ferramenta.id}</td>
                  <td className="font-medium text-slate-800">{ferramenta.nome}</td>
                  <td>{quantidade(ferramenta.quantidade)}</td>
                  <td>{ferramenta.unidade}</td>
                  <td className="space-x-3 text-right">
                    <button type="button" className="botao-link" onClick={() => editar(ferramenta)}>
                      Editar
                    </button>
                    <button
                      type="button"
                      className="text-sm font-medium text-red-600 hover:underline"
                      onClick={() => excluir(ferramenta)}
                    >
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
              {lista.length === 0 && (
                <tr>
                  <td colSpan={5} className="py-6 text-center text-sm text-slate-500">
                    Nenhuma ferramenta cadastrada.
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
