import { useEffect, useMemo, useState } from 'react';
import Aviso from '../components/Aviso';
import Campo from '../components/Campo';
import EntradaMoeda from '../components/EntradaMoeda';
import api, { errosCampos, mensagemErro } from '../services/api';
import { baixarArquivo } from '../utils/download';
import { dataBR, formatarCnpj, moeda, numeroPedido, quantidade } from '../utils/formatadores';
import { dataDoApi, dataParaApi, hojeISO, moedaParaNumero, numeroParaMoeda } from '../utils/mascaras';

const FORM_VAZIO = { pedidoId: '', valor: '', dataEmissao: hojeISO(), arquivo: null };
const LIMITE_ANEXO = Number(import.meta.env.VITE_UPLOAD_MAX_BYTES || 0);
const EXTENSOES_ANEXO = ['jpg', 'jpeg', 'png', 'pdf'];

export default function Autorizacoes() {
  const [lista, setLista] = useState([]);
  const [pedidos, setPedidos] = useState([]);
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
      const [autorizacoes, listaPedidos] = await Promise.all([api.get('/autorizacoes'), api.get('/pedidos')]);
      setLista(autorizacoes.data);
      setPedidos(listaPedidos.data);
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  const pedidosDisponiveis = useMemo(() => {
    const usados = new Set(lista.map((autorizacao) => autorizacao.pedidoId));
    return pedidos.filter(
      (pedido) => !usados.has(pedido.id) || (editando && editando.pedidoId === pedido.id),
    );
  }, [pedidos, lista, editando]);

  const pedidoSelecionado = useMemo(
    () => pedidos.find((pedido) => String(pedido.id) === String(form.pedidoId)),
    [pedidos, form.pedidoId],
  );

  function limpar() {
    setForm(FORM_VAZIO);
    setEditando(null);
    setErros({});
  }

  async function salvar(evento) {
    evento.preventDefault();
    setErros({});
    setAviso(null);

    const valor = moedaParaNumero(form.valor);
    const novos = {};
    if (!editando && !form.pedidoId) {
      novos.pedidoId = 'Selecione o pedido de orçamento';
    }
    if (!valor || valor <= 0) {
      novos.valor = 'Informe o valor da autorização';
    }
    if (!form.dataEmissao) {
      novos.dataEmissao = 'Data de emissão é obrigatória';
    }
    if (form.arquivo) {
      const extensao = form.arquivo.name.split('.').pop().toLowerCase();
      if (!EXTENSOES_ANEXO.includes(extensao)) {
        novos.arquivo = 'Envie um arquivo JPG, PNG ou PDF';
      } else if (LIMITE_ANEXO > 0 && form.arquivo.size > LIMITE_ANEXO) {
        novos.arquivo = `O anexo deve ter no máximo ${Math.round(LIMITE_ANEXO / (1024 * 1024))} MB`;
      }
    }
    if (Object.keys(novos).length > 0) {
      setErros(novos);
      setAviso({ tipo: 'erro', texto: 'Verifique os campos destacados' });
      return;
    }

    const dados = new FormData();
    if (!editando) {
      dados.append('pedidoId', form.pedidoId);
    }
    dados.append('valor', String(valor));
    dados.append('dataEmissao', dataParaApi(form.dataEmissao));
    if (form.arquivo) {
      dados.append('arquivo', form.arquivo);
    }

    setEnviando(true);
    try {
      if (editando) {
        await api.put(`/autorizacoes/${editando.id}`, dados);
        setAviso({ tipo: 'sucesso', texto: 'Autorização atualizada' });
      } else {
        await api.post('/autorizacoes', dados);
        setAviso({ tipo: 'sucesso', texto: 'Autorização cadastrada' });
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

  function editar(autorizacao) {
    setEditando(autorizacao);
    setErros({});
    setForm({
      pedidoId: String(autorizacao.pedidoId),
      valor: numeroParaMoeda(autorizacao.valor),
      dataEmissao: dataDoApi(autorizacao.dataEmissao),
      arquivo: null,
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function excluir(autorizacao) {
    if (!window.confirm(`Excluir a autorização ${numeroPedido(autorizacao.numero)}?`)) {
      return;
    }
    try {
      await api.delete(`/autorizacoes/${autorizacao.id}`);
      setAviso({ tipo: 'sucesso', texto: 'Autorização excluída' });
      if (editando?.id === autorizacao.id) {
        limpar();
      }
      carregar();
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  async function gerarPdf(autorizacao) {
    try {
      await baixarArquivo(
        `/autorizacoes/${autorizacao.id}/pdf`,
        `autorizacao-${numeroPedido(autorizacao.numero)}.pdf`,
      );
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  async function abrirAnexo(autorizacao) {
    try {
      await baixarArquivo(`/autorizacoes/${autorizacao.id}/anexo`, autorizacao.arquivoNome || 'anexo');
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Autorização de pagamento</h1>
        <p className="mt-1 text-sm text-slate-500">
          Cada pedido de orçamento gera uma única autorização de pagamento.
        </p>
      </div>

      <Aviso aviso={aviso} aoFechar={() => setAviso(null)} />

      <form onSubmit={salvar} className="cartao space-y-5">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
          {editando ? `Editar autorização ${numeroPedido(editando.numero)}` : 'Nova autorização'}
        </h2>

        <div className="grid gap-4 md:grid-cols-3">
          <Campo rotulo="Pedido de orçamento" obrigatorio={!editando} erro={erros.pedidoId}>
            <select
              className={`entrada ${erros.pedidoId ? 'entrada-erro' : ''}`}
              value={form.pedidoId}
              disabled={Boolean(editando)}
              onChange={(evento) => setForm({ ...form, pedidoId: evento.target.value })}
            >
              <option value="">Selecione</option>
              {pedidosDisponiveis.map((pedido) => (
                <option key={pedido.id} value={pedido.id}>
                  {numeroPedido(pedido.numero)} — {pedido.fornecedor.razaoSocial}
                </option>
              ))}
            </select>
          </Campo>

          <Campo rotulo="Valor" obrigatorio erro={erros.valor}>
            <EntradaMoeda
              valor={form.valor}
              aoMudar={(valor) => setForm({ ...form, valor })}
              erro={Boolean(erros.valor)}
            />
          </Campo>

          <Campo rotulo="Data de emissão" obrigatorio erro={erros.dataEmissao}>
            <input
              type="date"
              className="entrada"
              value={form.dataEmissao}
              onChange={(evento) => setForm({ ...form, dataEmissao: evento.target.value })}
            />
          </Campo>
        </div>

        {pedidoSelecionado && (
          <div className="rounded-md bg-slate-50 p-4 text-sm text-slate-600">
            <p>
              <span className="font-medium text-slate-700">Fornecedor:</span>{' '}
              {pedidoSelecionado.fornecedor.razaoSocial}
            </p>
            <p className="mt-1">
              <span className="font-medium text-slate-700">CNPJ:</span>{' '}
              {formatarCnpj(pedidoSelecionado.fornecedor.cnpj)}
            </p>

            <div className="mt-3 overflow-x-auto">
              <table className="tabela">
                <thead>
                  <tr>
                    <th className="w-12">ID</th>
                    <th className="w-12">Nº</th>
                    <th>Item</th>
                    <th>Quantidade</th>
                    <th>Unidade</th>
                  </tr>
                </thead>
                <tbody>
                  {pedidoSelecionado.itens.map((linha) => (
                    <tr key={linha.numero}>
                      <td>{linha.id}</td>
                      <td>{linha.numero}</td>
                      <td>{linha.descricao}</td>
                      <td>{quantidade(linha.quantidade)}</td>
                      <td>{linha.unidade}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        <div className="grid gap-4 md:grid-cols-3">
          <Campo rotulo="Anexo (JPG, PNG ou PDF)" erro={erros.arquivo} className="md:col-span-2">
            <input
              type="file"
              accept=".jpg,.jpeg,.png,.pdf,image/jpeg,image/png,application/pdf"
              className="entrada file:mr-3 file:rounded file:border-0 file:bg-slate-100 file:px-3 file:py-1 file:text-sm"
              onChange={(evento) => setForm({ ...form, arquivo: evento.target.files?.[0] || null })}
            />
            {editando?.possuiAnexo && (
              <p className="mt-1 text-xs text-slate-500">Anexo atual: {editando.arquivoNome}</p>
            )}
          </Campo>
        </div>

        <div className="flex flex-wrap gap-3">
          <button type="submit" className="botao" disabled={enviando}>
            {editando ? 'Salvar alterações' : 'Salvar autorização'}
          </button>
          <button type="button" className="botao-secundario" onClick={limpar}>
            Limpar
          </button>
        </div>
      </form>

      <div className="cartao">
        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
          Autorizações cadastradas
        </h2>
        <div className="overflow-x-auto">
          <table className="tabela">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nº</th>
                <th>Pedido</th>
                <th>Fornecedor</th>
                <th>Valor</th>
                <th>Data</th>
                <th>Anexo</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((autorizacao) => (
                <tr key={autorizacao.id}>
                  <td>{autorizacao.id}</td>
                  <td className="font-medium text-slate-800">{numeroPedido(autorizacao.numero)}</td>
                  <td>{numeroPedido(autorizacao.pedidoNumero)}</td>
                  <td>{autorizacao.fornecedor}</td>
                  <td>{moeda(autorizacao.valor)}</td>
                  <td>{dataBR(autorizacao.dataEmissao)}</td>
                  <td>
                    {autorizacao.possuiAnexo ? (
                      <button type="button" className="botao-link" onClick={() => abrirAnexo(autorizacao)}>
                        {autorizacao.arquivoNome}
                      </button>
                    ) : (
                      '-'
                    )}
                  </td>
                  <td className="space-x-3 whitespace-nowrap text-right">
                    <button type="button" className="botao-link" onClick={() => gerarPdf(autorizacao)}>
                      Gerar PDF
                    </button>
                    <button type="button" className="botao-link" onClick={() => editar(autorizacao)}>
                      Editar
                    </button>
                    <button
                      type="button"
                      className="text-sm font-medium text-red-600 hover:underline"
                      onClick={() => excluir(autorizacao)}
                    >
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
              {lista.length === 0 && (
                <tr>
                  <td colSpan={8} className="py-6 text-center text-sm text-slate-500">
                    Nenhuma autorização cadastrada.
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
