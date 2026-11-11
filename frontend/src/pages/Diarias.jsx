import { useEffect, useState } from 'react';
import Aviso from '../components/Aviso';
import Campo from '../components/Campo';
import EntradaMoeda from '../components/EntradaMoeda';
import api, { mensagemErro } from '../services/api';
import { dataBR, moeda } from '../utils/formatadores';
import { dataDoApi, dataParaApi, hojeISO, moedaParaNumero, numeroParaMoeda } from '../utils/mascaras';

export default function Diarias() {
  const [cards, setCards] = useState([]);
  const [aviso, setAviso] = useState(null);

  useEffect(() => {
    carregar();
  }, []);

  async function carregar() {
    try {
      const { data } = await api.get('/diarias');
      setCards(data);
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  function avisar(tipo, texto) {
    setAviso({ tipo, texto });
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Diárias</h1>
        <p className="mt-1 text-sm text-slate-500">
          Lance as diárias de cada funcionário. O fechamento envia o total para a tela de pagamentos.
        </p>
      </div>

      <Aviso aviso={aviso} aoFechar={() => setAviso(null)} />

      {cards.length === 0 && (
        <div className="cartao text-sm text-slate-600">
          Nenhum funcionário cadastrado. Cadastre um funcionário para lançar diárias.
        </div>
      )}

      <div className="grid gap-4 xl:grid-cols-2">
        {cards.map((card) => (
          <CardDiaria key={card.funcionarioId} card={card} aoAtualizar={carregar} avisar={avisar} />
        ))}
      </div>
    </div>
  );
}

function CardDiaria({ card, aoAtualizar, avisar }) {
  const [data, setData] = useState(hojeISO());
  const [valor, setValor] = useState(numeroParaMoeda(card.valorDiaria));
  const [editando, setEditando] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (!editando) {
      setValor(numeroParaMoeda(card.valorDiaria));
    }
  }, [card.valorDiaria, editando]);

  function cancelarEdicao() {
    setEditando(null);
    setData(hojeISO());
    setValor(numeroParaMoeda(card.valorDiaria));
  }

  function editar(diaria) {
    setEditando(diaria.id);
    setData(dataDoApi(diaria.data));
    setValor(numeroParaMoeda(diaria.valor));
  }

  async function lancar(evento) {
    evento.preventDefault();
    if (!data) {
      avisar('erro', 'Selecione a data da diária');
      return;
    }
    const valorNumero = moedaParaNumero(valor);
    if (!valorNumero || valorNumero <= 0) {
      avisar('erro', 'Informe o valor da diária');
      return;
    }
    setEnviando(true);
    try {
      if (editando) {
        await api.put(`/diarias/${editando}`, {
          data: dataParaApi(data),
          valor: valorNumero,
        });
        avisar('sucesso', 'Diária atualizada');
      } else {
        await api.post('/diarias', {
          funcionarioId: card.funcionarioId,
          data: dataParaApi(data),
          valor: valorNumero,
        });
        avisar('sucesso', `Diária lançada para ${card.nome}`);
      }
      cancelarEdicao();
      await aoAtualizar();
    } catch (falha) {
      avisar('erro', mensagemErro(falha));
    } finally {
      setEnviando(false);
    }
  }

  async function remover(diaria) {
    try {
      await api.delete(`/diarias/${diaria.id}`);
      if (editando === diaria.id) {
        cancelarEdicao();
      }
      avisar('sucesso', 'Diária removida');
      await aoAtualizar();
    } catch (falha) {
      avisar('erro', mensagemErro(falha));
    }
  }

  async function fecharDiarias() {
    if (card.diarias.length === 0) {
      avisar('erro', 'Nenhuma diária lançada para este funcionário');
      return;
    }
    if (!window.confirm(`Fechar as diárias de ${card.nome} no total de ${moeda(card.total)}?`)) {
      return;
    }
    try {
      await api.post(`/diarias/fechamentos?funcionarioId=${card.funcionarioId}`);
      cancelarEdicao();
      avisar('sucesso', `Diárias de ${card.nome} fechadas e enviadas para pagamentos`);
      await aoAtualizar();
    } catch (falha) {
      avisar('erro', mensagemErro(falha));
    }
  }

  return (
    <div className="cartao">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div>
          <h2 className="font-semibold text-slate-800">{card.nome}</h2>
          <p className="text-sm text-slate-500">{card.funcao || 'Função não informada'}</p>
        </div>
        <p className="text-sm text-slate-600">
          Diária: <span className="font-medium text-slate-800">{moeda(card.valorDiaria)}</span>
        </p>
      </div>

      {editando && (
        <p className="mt-4 text-xs font-medium text-sky-700">Editando o lançamento selecionado.</p>
      )}

      <form onSubmit={lancar} className="mt-4 grid gap-3 sm:grid-cols-3">
        <Campo rotulo="Data">
          <input
            type="date"
            className="entrada"
            max={hojeISO()}
            value={data}
            onChange={(evento) => setData(evento.target.value)}
          />
        </Campo>

        <Campo rotulo="Valor">
          <EntradaMoeda valor={valor} aoMudar={setValor} />
        </Campo>

        <div className="flex items-end gap-2">
          <button type="submit" className="botao w-full" disabled={enviando}>
            {editando ? 'Salvar alteração' : 'Lançar diária'}
          </button>
          {editando && (
            <button type="button" className="botao-secundario w-full" onClick={cancelarEdicao}>
              Cancelar
            </button>
          )}
        </div>
      </form>

      <div className="mt-4 overflow-x-auto">
        <table className="tabela">
          <thead>
            <tr>
              <th>ID</th>
              <th>Data</th>
              <th>Valor</th>
              <th className="text-right">Ações</th>
            </tr>
          </thead>
          <tbody>
            {card.diarias.map((diaria) => (
              <tr key={diaria.id}>
                <td>{diaria.id}</td>
                <td>{dataBR(diaria.data)}</td>
                <td>{moeda(diaria.valor)}</td>
                <td className="space-x-3 text-right">
                  <button type="button" className="botao-link" onClick={() => editar(diaria)}>
                    Editar
                  </button>
                  <button
                    type="button"
                    className="text-sm font-medium text-red-600 hover:underline"
                    onClick={() => remover(diaria)}
                  >
                    Remover
                  </button>
                </td>
              </tr>
            ))}
            {card.diarias.length === 0 && (
              <tr>
                <td colSpan={4} className="py-4 text-center text-sm text-slate-500">
                  Nenhuma diária lançada.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-slate-200 pt-4">
        <p className="text-sm text-slate-600">
          Total: <span className="text-base font-semibold text-slate-800">{moeda(card.total)}</span>
        </p>
        <button type="button" className="botao" onClick={fecharDiarias} disabled={card.diarias.length === 0}>
          Fechar diárias
        </button>
      </div>
    </div>
  );
}
