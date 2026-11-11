import { useEffect, useMemo, useState } from 'react';
import Aviso from '../components/Aviso';
import Campo from '../components/Campo';
import api, { errosCampos, mensagemErro } from '../services/api';
import { baixarArquivo } from '../utils/download';
import { dataBR, formatarChavePix, formatarCnpj, formatarTelefone, numeroPedido, quantidade } from '../utils/formatadores';
import { dataDoApi, dataParaApi, hojeISO } from '../utils/mascaras';

const FORM_VAZIO = { fornecedorId: '', dataEmissao: hojeISO() };
const ITEM_VAZIO = { descricao: '', quantidade: '', unidade: 'UN' };

export default function Pedidos() {
  const [lista, setLista] = useState([]);
  const [fornecedores, setFornecedores] = useState([]);
  const [form, setForm] = useState(FORM_VAZIO);
  const [itens, setItens] = useState([]);
  const [item, setItem] = useState(ITEM_VAZIO);
  const [editando, setEditando] = useState(null);
  const [erros, setErros] = useState({});
  const [aviso, setAviso] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    carregar();
  }, []);

  async function carregar() {
    try {
      const [pedidos, listaFornecedores] = await Promise.all([api.get('/pedidos'), api.get('/fornecedores')]);
      setLista(pedidos.data);
      setFornecedores(listaFornecedores.data);
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  const fornecedorSelecionado = useMemo(
    () => fornecedores.find((fornecedor) => String(fornecedor.id) === String(form.fornecedorId)),
    [fornecedores, form.fornecedorId],
  );

  function limpar() {
    setForm(FORM_VAZIO);
    setItens([]);
    setItem(ITEM_VAZIO);
    setEditando(null);
    setErros({});
  }

  function adicionarItem() {
    const descricao = item.descricao.trim();
    const valorQuantidade = Number(String(item.quantidade).replace(',', '.'));
    if (!descricao) {
      setAviso({ tipo: 'erro', texto: 'Informe a descrição do item' });
      return;
    }
    if (!valorQuantidade || valorQuantidade <= 0) {
      setAviso({ tipo: 'erro', texto: 'Informe uma quantidade maior que zero' });
      return;
    }
    if (!item.unidade.trim()) {
      setAviso({ tipo: 'erro', texto: 'Informe a unidade do item' });
      return;
    }
    setItens([...itens, { descricao, quantidade: valorQuantidade, unidade: item.unidade.trim().toUpperCase() }]);
    setItem({ ...ITEM_VAZIO, unidade: item.unidade });
    setAviso(null);
  }

  function removerItem(indice) {
    setItens(itens.filter((_, posicao) => posicao !== indice));
  }

  async function salvar(evento) {
    evento.preventDefault();
    setErros({});
    setAviso(null);

    const novos = {};
    if (!form.fornecedorId) {
      novos.fornecedorId = 'Selecione o fornecedor';
    }
    if (!form.dataEmissao) {
      novos.dataEmissao = 'Data de emissão é obrigatória';
    }
    if (itens.length === 0) {
      novos.itens = 'Inclua ao menos um item no pedido';
    }
    if (Object.keys(novos).length > 0) {
      setErros(novos);
      setAviso({ tipo: 'erro', texto: 'Verifique os campos destacados' });
      return;
    }

    const corpo = {
      fornecedorId: Number(form.fornecedorId),
      dataEmissao: dataParaApi(form.dataEmissao),
      itens: itens.map(({ descricao, quantidade, unidade }) => ({ descricao, quantidade, unidade })),
    };
    setEnviando(true);
    try {
      if (editando) {
        await api.put(`/pedidos/${editando}`, corpo);
        setAviso({ tipo: 'sucesso', texto: 'Pedido atualizado' });
      } else {
        await api.post('/pedidos', corpo);
        setAviso({ tipo: 'sucesso', texto: 'Pedido cadastrado' });
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

  function editar(pedido) {
    setEditando(pedido.id);
    setErros({});
    setForm({
      fornecedorId: String(pedido.fornecedor.id),
      dataEmissao: dataDoApi(pedido.dataEmissao),
    });
    setItens(
      pedido.itens.map((linha) => ({
        id: linha.id,
        descricao: linha.descricao,
        quantidade: Number(linha.quantidade),
        unidade: linha.unidade,
      })),
    );
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function excluir(pedido) {
    if (!window.confirm(`Excluir o pedido ${numeroPedido(pedido.numero)}?`)) {
      return;
    }
    try {
      await api.delete(`/pedidos/${pedido.id}`);
      setAviso({ tipo: 'sucesso', texto: 'Pedido excluído' });
      if (editando === pedido.id) {
        limpar();
      }
      carregar();
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  async function gerarPdf(pedido) {
    try {
      await baixarArquivo(`/pedidos/${pedido.id}/pdf`, `pedido-${numeroPedido(pedido.numero)}.pdf`);
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Pedidos de orçamento</h1>
        <p className="mt-1 text-sm text-slate-500">
          Selecione o fornecedor, inclua os itens e salve o pedido. A numeração é gerada automaticamente.
        </p>
      </div>

      <Aviso aviso={aviso} aoFechar={() => setAviso(null)} />

      <form onSubmit={salvar} className="cartao space-y-5">
        <div className="flex items-center justify-between">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            {editando ? 'Editar pedido' : 'Novo pedido'}
          </h2>
          {editando && <span className="text-sm text-slate-500">Pedido em edição</span>}
        </div>

        <div className="grid gap-4 md:grid-cols-2">
          <Campo rotulo="Fornecedor" obrigatorio erro={erros.fornecedorId}>
            <select
              className={`entrada ${erros.fornecedorId ? 'entrada-erro' : ''}`}
              value={form.fornecedorId}
              onChange={(evento) => setForm({ ...form, fornecedorId: evento.target.value })}
            >
              <option value="">Selecione</option>
              {fornecedores.map((fornecedor) => (
                <option key={fornecedor.id} value={fornecedor.id}>
                  {fornecedor.razaoSocial}
                </option>
              ))}
            </select>
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

        {fornecedorSelecionado && (
          <div className="grid gap-2 rounded-md bg-slate-50 p-4 text-sm text-slate-600 md:grid-cols-2 lg:grid-cols-3">
            <p><span className="font-medium text-slate-700">CNPJ:</span> {formatarCnpj(fornecedorSelecionado.cnpj)}</p>
            <p><span className="font-medium text-slate-700">Telefone:</span> {formatarTelefone(fornecedorSelecionado.telefone)}</p>
            <p><span className="font-medium text-slate-700">Contato:</span> {fornecedorSelecionado.nomeContato || '-'}</p>
            <p className="md:col-span-2 lg:col-span-3">
              <span className="font-medium text-slate-700">Chave PIX:</span>{' '}
              {formatarChavePix(fornecedorSelecionado.tipoChavePix, fornecedorSelecionado.chavePix) || '-'}
            </p>
          </div>
        )}

        <div className="rounded-md border border-slate-200 p-4">
          <h3 className="mb-3 text-sm font-semibold text-slate-700">Itens do pedido</h3>

          <div className="grid gap-3 md:grid-cols-12">
            <Campo rotulo="Descrição do item" className="md:col-span-6">
              <input
                className="entrada"
                value={item.descricao}
                onChange={(evento) => setItem({ ...item, descricao: evento.target.value })}
                maxLength={300}
              />
            </Campo>

            <Campo rotulo="Quantidade" className="md:col-span-2">
              <input
                type="number"
                min="0.001"
                step="0.001"
                className="entrada"
                value={item.quantidade}
                onChange={(evento) => setItem({ ...item, quantidade: evento.target.value })}
              />
            </Campo>

            <Campo rotulo="Unidade" className="md:col-span-2">
              <input
                className="entrada"
                value={item.unidade}
                onChange={(evento) => setItem({ ...item, unidade: evento.target.value })}
                maxLength={20}
              />
            </Campo>

            <div className="flex items-end md:col-span-2">
              <button type="button" className="botao-secundario w-full" onClick={adicionarItem}>
                Adicionar item
              </button>
            </div>
          </div>

          <div className="mt-4 overflow-x-auto">
            <table className="tabela">
              <thead>
                <tr>
                  <th className="w-12">ID</th>
                  <th className="w-12">Nº</th>
                  <th>Descrição</th>
                  <th>Quantidade</th>
                  <th>Unidade</th>
                  <th className="text-right">Ações</th>
                </tr>
              </thead>
              <tbody>
                {itens.map((linha, indice) => (
                  <tr key={`${linha.descricao}-${indice}`}>
                    <td>{linha.id ?? '-'}</td>
                    <td>{indice + 1}</td>
                    <td>{linha.descricao}</td>
                    <td>{quantidade(linha.quantidade)}</td>
                    <td>{linha.unidade}</td>
                    <td className="text-right">
                      <button
                        type="button"
                        className="text-sm font-medium text-red-600 hover:underline"
                        onClick={() => removerItem(indice)}
                      >
                        Remover
                      </button>
                    </td>
                  </tr>
                ))}
                {itens.length === 0 && (
                  <tr>
                    <td colSpan={6} className="py-4 text-center text-sm text-slate-500">
                      Nenhum item adicionado.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
          {erros.itens && <p className="mt-2 text-xs text-red-600">{erros.itens}</p>}
        </div>

        <div className="flex flex-wrap gap-3">
          <button type="submit" className="botao" disabled={enviando}>
            {editando ? 'Salvar alterações' : 'Salvar pedido'}
          </button>
          <button type="button" className="botao-secundario" onClick={limpar}>
            Limpar
          </button>
        </div>
      </form>

      <div className="cartao">
        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">Pedidos cadastrados</h2>
        <div className="overflow-x-auto">
          <table className="tabela">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nº</th>
                <th>Data</th>
                <th>Fornecedor</th>
                <th>Itens</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((pedido) => (
                <tr key={pedido.id}>
                  <td>{pedido.id}</td>
                  <td className="font-medium text-slate-800">{numeroPedido(pedido.numero)}</td>
                  <td>{dataBR(pedido.dataEmissao)}</td>
                  <td>{pedido.fornecedor.razaoSocial}</td>
                  <td>{pedido.itens.length}</td>
                  <td className="space-x-3 whitespace-nowrap text-right">
                    <button type="button" className="botao-link" onClick={() => gerarPdf(pedido)}>
                      Gerar PDF
                    </button>
                    <button type="button" className="botao-link" onClick={() => editar(pedido)}>
                      Editar
                    </button>
                    <button
                      type="button"
                      className="text-sm font-medium text-red-600 hover:underline"
                      onClick={() => excluir(pedido)}
                    >
                      Excluir
                    </button>
                  </td>
                </tr>
              ))}
              {lista.length === 0 && (
                <tr>
                  <td colSpan={6} className="py-6 text-center text-sm text-slate-500">
                    Nenhum pedido cadastrado.
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
