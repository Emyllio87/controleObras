import { useEffect, useState } from 'react';
import Aviso from '../components/Aviso';
import api, { mensagemErro } from '../api';
import { STATUS_PAGAMENTO } from '../utils/constantes';
import { dataBR, formatarChavePix, moeda } from '../utils/formatadores';

export default function Pagamentos() {
  const [lista, setLista] = useState([]);
  const [filtro, setFiltro] = useState('');
  const [aviso, setAviso] = useState(null);

  useEffect(() => {
    carregar();
  }, [filtro]);

  async function carregar() {
    try {
      const { data } = await api.get('/pagamentos', {
        params: filtro ? { status: filtro } : {},
      });
      setLista(data);
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  async function alterarStatus(pagamento) {
    const novoStatus = pagamento.status === 'PAGO' ? 'PENDENTE' : 'PAGO';
    try {
      await api.patch(`/pagamentos/${pagamento.id}/status`, { status: novoStatus });
      setAviso({
        tipo: 'sucesso',
        texto: `Pagamento de ${pagamento.funcionario} marcado como ${novoStatus === 'PAGO' ? 'pago' : 'pendente'}`,
      });
      carregar();
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold text-slate-800">Pagamentos</h1>
          <p className="mt-1 text-sm text-slate-500">
            Pagamentos gerados pelo fechamento de diárias.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <label htmlFor="filtro" className="text-sm text-slate-600">Status:</label>
          <select
            id="filtro"
            className="entrada w-auto"
            value={filtro}
            onChange={(evento) => setFiltro(evento.target.value)}
          >
            <option value="">Todos</option>
            {STATUS_PAGAMENTO.map((status) => (
              <option key={status.valor} value={status.valor}>
                {status.rotulo}
              </option>
            ))}
          </select>
        </div>
      </div>

      <Aviso aviso={aviso} aoFechar={() => setAviso(null)} />

      <div className="cartao">
        <div className="overflow-x-auto">
          <table className="tabela">
            <thead>
              <tr>
                <th>ID</th>
                <th>Funcionário</th>
                <th>Período</th>
                <th>Valor</th>
                <th>Chave PIX</th>
                <th>Status</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((pagamento) => (
                <tr key={pagamento.id}>
                  <td>{pagamento.id}</td>
                  <td className="font-medium text-slate-800">{pagamento.funcionario}</td>
                  <td>
                    {dataBR(pagamento.periodoInicio)} a {dataBR(pagamento.periodoFim)}
                  </td>
                  <td>{moeda(pagamento.valorTotal)}</td>
                  <td>{formatarChavePix(pagamento.tipoChavePix, pagamento.chavePix)}</td>
                  <td>
                    <span
                      className={`etiqueta ${
                        pagamento.status === 'PAGO'
                          ? 'bg-emerald-100 text-emerald-700'
                          : 'bg-amber-100 text-amber-700'
                      }`}
                    >
                      {pagamento.status === 'PAGO' ? 'Pago' : 'Pendente'}
                    </span>
                  </td>
                  <td className="text-right">
                    <button type="button" className="botao-link" onClick={() => alterarStatus(pagamento)}>
                      {pagamento.status === 'PAGO' ? 'Marcar como pendente' : 'Marcar como pago'}
                    </button>
                  </td>
                </tr>
              ))}
              {lista.length === 0 && (
                <tr>
                  <td colSpan={7} className="py-6 text-center text-sm text-slate-500">
                    Nenhum pagamento encontrado.
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
