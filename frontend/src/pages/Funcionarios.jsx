import { useEffect, useState } from 'react';
import Aviso from '../components/Aviso';
import Campo from '../components/Campo';
import EntradaMoeda from '../components/EntradaMoeda';
import api, { errosCampos, mensagemErro } from '../api';
import { TIPOS_PIX, rotuloTipoPix } from '../utils/constantes';
import { formatarChavePix, formatarCpf, formatarTelefone, moeda } from '../utils/formatadores';
import {
  chavePixParaFormulario,
  mascaraCpf,
  mascaraTelefone,
  moedaParaNumero,
  numeroParaMoeda,
  somenteDigitos,
} from '../utils/mascaras';
import { cpfValido, mensagemChavePix, telefoneValido, validarChavePix } from '../utils/validacoes';

const FORM_VAZIO = {
  nome: '',
  telefone: '',
  funcao: '',
  valorDiaria: '',
  tipoChavePix: '',
  chavePix: '',
  cpf: '',
};

function chaveMascarada(tipo, valor) {
  return chavePixParaFormulario(tipo, valor);
}

export default function Funcionarios() {
  const [lista, setLista] = useState([]);
  const [form, setForm] = useState(FORM_VAZIO);
  const [editando, setEditando] = useState(null);
  const [filtro, setFiltro] = useState('');
  const [erros, setErros] = useState({});
  const [aviso, setAviso] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    carregar();
  }, [filtro]);

  async function carregar() {
    try {
      const { data } = await api.get('/funcionarios', {
        params: filtro ? { tipoChavePix: filtro } : {},
      });
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

  function definirErro(campo, mensagem) {
    setErros((atual) => {
      const copia = { ...atual };
      if (mensagem) {
        copia[campo] = mensagem;
      } else {
        delete copia[campo];
      }
      return copia;
    });
  }

  async function salvar(evento) {
    evento.preventDefault();
    setErros({});
    setAviso(null);

    const novos = {};
    if (!form.nome.trim()) {
      novos.nome = 'Nome é obrigatório';
    }
    const digitosCpf = somenteDigitos(form.cpf);
    if (!digitosCpf) {
      novos.cpf = 'CPF é obrigatório';
    } else if (!cpfValido(form.cpf)) {
      novos.cpf = 'CPF inválido';
    }
    if (form.telefone && !telefoneValido(form.telefone)) {
      novos.telefone = 'Telefone deve conter 10 ou 11 dígitos com DDD válido';
    }
    const erroChavePix = validarChavePix(form.tipoChavePix, form.chavePix);
    if (erroChavePix) {
      novos.chavePix = erroChavePix;
    }
    const valorDiaria = moedaParaNumero(form.valorDiaria);
    if (!valorDiaria || valorDiaria < 0.01) {
      novos.valorDiaria = 'Valor da diária deve ser maior que zero';
    }
    if (Object.keys(novos).length > 0) {
      setErros(novos);
      setAviso({ tipo: 'erro', texto: 'Verifique os campos destacados' });
      return;
    }

    const corpo = {
      ...form,
      valorDiaria,
      tipoChavePix: form.tipoChavePix || null,
      chavePix: form.chavePix || null,
    };
    setEnviando(true);
    try {
      if (editando) {
        await api.put(`/funcionarios/${editando}`, corpo);
        setAviso({ tipo: 'sucesso', texto: 'Funcionário atualizado' });
      } else {
        await api.post('/funcionarios', corpo);
        setAviso({ tipo: 'sucesso', texto: 'Funcionário cadastrado' });
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
    setForm({
      nome: item.nome || '',
      telefone: item.telefone ? mascaraTelefone(item.telefone) : '',
      funcao: item.funcao || '',
      valorDiaria: numeroParaMoeda(item.valorDiaria),
      tipoChavePix: item.tipoChavePix || '',
      chavePix: chaveMascarada(item.tipoChavePix, item.chavePix),
      cpf: mascaraCpf(item.cpf),
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function excluir(item) {
    if (!window.confirm(`Excluir o funcionário ${item.nome}?`)) {
      return;
    }
    try {
      await api.delete(`/funcionarios/${item.id}`);
      setAviso({ tipo: 'sucesso', texto: 'Funcionário excluído' });
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
        <h1 className="text-xl font-semibold text-slate-800">Funcionários</h1>
        <p className="mt-1 text-sm text-slate-500">Cadastro dos funcionários e valor da diária.</p>
      </div>

      <Aviso aviso={aviso} aoFechar={() => setAviso(null)} />

      <form onSubmit={salvar} className="cartao">
        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
          {editando ? 'Editar funcionário' : 'Novo funcionário'}
        </h2>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <Campo rotulo="Nome" obrigatorio erro={erros.nome}>
            <input
              className={`entrada ${erros.nome ? 'entrada-erro' : ''}`}
              value={form.nome}
              onChange={(evento) => {
                setForm({ ...form, nome: evento.target.value });
                definirErro('nome', '');
              }}
              maxLength={120}
            />
          </Campo>

          <Campo rotulo="CPF" obrigatorio erro={erros.cpf}>
            <input
              className={`entrada ${erros.cpf ? 'entrada-erro' : ''}`}
              value={form.cpf}
              onChange={(evento) => {
                const valor = mascaraCpf(evento.target.value);
                setForm({ ...form, cpf: valor });
                definirErro(
                  'cpf',
                  somenteDigitos(valor).length === 11 && !cpfValido(valor) ? 'CPF inválido' : '',
                );
              }}
              placeholder="000.000.000-00"
            />
          </Campo>

          <Campo rotulo="Telefone" erro={erros.telefone}>
            <input
              className={`entrada ${erros.telefone ? 'entrada-erro' : ''}`}
              value={form.telefone}
              onChange={(evento) => {
                const valor = mascaraTelefone(evento.target.value);
                setForm({ ...form, telefone: valor });
                const digitos = somenteDigitos(valor);
                definirErro(
                  'telefone',
                  (digitos.length === 10 || digitos.length === 11) && !telefoneValido(valor)
                    ? 'Telefone inválido'
                    : '',
                );
              }}
              placeholder="(00) 00000-0000"
            />
          </Campo>

          <Campo rotulo="Função" erro={erros.funcao}>
            <input
              className="entrada"
              value={form.funcao}
              onChange={(evento) => setForm({ ...form, funcao: evento.target.value })}
              placeholder="Pedreiro, servente, eletricista..."
              maxLength={60}
            />
          </Campo>

          <Campo rotulo="Valor da diária" obrigatorio erro={erros.valorDiaria}>
            <EntradaMoeda
              valor={form.valorDiaria}
              aoMudar={(valor) => {
                setForm({ ...form, valorDiaria: valor });
                const numero = moedaParaNumero(valor);
                if (numero && numero >= 0.01) {
                  definirErro('valorDiaria', '');
                }
              }}
              erro={Boolean(erros.valorDiaria)}
            />
          </Campo>

          <Campo rotulo="Tipo de chave PIX" erro={erros.tipoChavePix}>
            <select
              className="entrada"
              value={form.tipoChavePix}
              onChange={(evento) => {
                setForm({ ...form, tipoChavePix: evento.target.value, chavePix: '' });
                definirErro('chavePix', '');
              }}
            >
              <option value="">Sem chave PIX</option>
              {TIPOS_PIX.map((tipo) => (
                <option key={tipo.valor} value={tipo.valor}>
                  {tipo.rotulo}
                </option>
              ))}
            </select>
          </Campo>

          <Campo rotulo="Chave PIX" erro={erros.chavePix}>
            <input
              className={`entrada ${erros.chavePix ? 'entrada-erro' : ''}`}
              value={form.chavePix}
              disabled={!form.tipoChavePix}
              onChange={(evento) => {
                const valor = chaveMascarada(form.tipoChavePix, evento.target.value);
                setForm({ ...form, chavePix: valor });
                definirErro('chavePix', mensagemChavePix(form.tipoChavePix, valor));
              }}
              placeholder={
                form.tipoChavePix === 'EMAIL'
                  ? 'nome@empresa.com'
                  : form.tipoChavePix === 'ALEATORIA'
                    ? '00000000-0000-0000-0000-000000000000'
                    : form.tipoChavePix === 'TELEFONE'
                      ? '+55 (00) 00000-0000'
                      : 'Selecione o tipo primeiro'
              }
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
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-slate-500">
            Funcionários cadastrados
          </h2>
          <div className="flex items-center gap-2">
            <label htmlFor="filtro" className="text-sm text-slate-600">Filtrar por PIX:</label>
            <select
              id="filtro"
              className="entrada w-auto"
              value={filtro}
              onChange={(evento) => setFiltro(evento.target.value)}
            >
              <option value="">Todos</option>
              {TIPOS_PIX.map((tipo) => (
                <option key={tipo.valor} value={tipo.valor}>
                  {tipo.rotulo}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="tabela">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>CPF</th>
                <th>Função</th>
                <th>Telefone</th>
                <th>Diária</th>
                <th>Chave PIX</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td className="font-medium text-slate-800">{item.nome}</td>
                  <td>{formatarCpf(item.cpf)}</td>
                  <td>{item.funcao || '-'}</td>
                  <td>{formatarTelefone(item.telefone)}</td>
                  <td>{moeda(item.valorDiaria)}</td>
                  <td>
                    {item.chavePix
                      ? `${rotuloTipoPix(item.tipoChavePix)} · ${formatarChavePix(item.tipoChavePix, item.chavePix)}`
                      : '-'}
                  </td>
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
                  <td colSpan={8} className="py-6 text-center text-sm text-slate-500">
                    Nenhum funcionário encontrado.
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
