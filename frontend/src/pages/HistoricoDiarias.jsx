import { Fragment, useEffect, useState } from 'react';
import Aviso from '../components/Aviso';
import api, { mensagemErro } from '../api';
import { baixarArquivo } from '../utils/download';
import { dataBR, moeda } from '../utils/formatadores';

export default function HistoricoDiarias() {
  const [lista, setLista] = useState([]);
  const [aberto, setAberto] = useState(null);
  const [aviso, setAviso] = useState(null);

  useEffect(() => {
    carregar();
  }, []);

  async function carregar() {
    try {
      const { data } = await api.get('/diarias/historico');
      setLista(data);
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  async function gerarPdf(fechamento) {
    try {
      await baixarArquivo(
        `/diarias/historico/${fechamento.id}/pdf`,
        `fechamento-diarias-${fechamento.id}.pdf`,
      );
    } catch (falha) {
      setAviso({ tipo: 'erro', texto: mensagemErro(falha) });
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Histórico de diárias</h1>
        <p className="mt-1 text-sm text-slate-500">Fechamentos já realizados. Somente consulta.</p>
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
                <th>Fechamento</th>
                <th>Total</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((fechamento) => (
                <Fragment key={fechamento.id}>
                  <tr>
                    <td>{fechamento.id}</td>
                    <td className="font-medium text-slate-800">{fechamento.funcionario}</td>
                    <td>
                      {dataBR(fechamento.periodoInicio)} a {dataBR(fechamento.periodoFim)}
                    </td>
                    <td>{dataBR(fechamento.dataFechamento)}</td>
                    <td>{moeda(fechamento.valorTotal)}</td>
                    <td className="space-x-3 whitespace-nowrap text-right">
                      <button
                        type="button"
                        className="botao-link"
                        onClick={() => setAberto(aberto === fechamento.id ? null : fechamento.id)}
                      >
                        {aberto === fechamento.id ? 'Ocultar diárias' : 'Ver diárias'}
                      </button>
                      <button type="button" className="botao-link" onClick={() => gerarPdf(fechamento)}>
                        Gerar PDF
                      </button>
                    </td>
                  </tr>
                  {aberto === fechamento.id && (
                    <tr>
                      <td colSpan={6} className="bg-slate-50">
                        <table className="tabela">
                          <thead>
                            <tr>
                              <th>ID</th>
                              <th>Data</th>
                              <th>Valor</th>
                            </tr>
                          </thead>
                          <tbody>
                            {fechamento.diarias.map((diaria) => (
                              <tr key={diaria.id}>
                                <td>{diaria.id}</td>
                                <td>{dataBR(diaria.data)}</td>
                                <td>{moeda(diaria.valor)}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))}
              {lista.length === 0 && (
                <tr>
                  <td colSpan={6} className="py-6 text-center text-sm text-slate-500">
                    Nenhum fechamento realizado.
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
