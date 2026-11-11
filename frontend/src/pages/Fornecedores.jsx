import { useEffect, useState } from 'react';
import Aviso from '../components/Aviso';
import Campo from '../components/Campo';
import api, { errosCampos, mensagemErro } from '../services/api';
import { TIPOS_PIX, rotuloTipoPix } from '../utils/constantes';
import { formatarChavePix, formatarCnpj, formatarTelefone } from '../utils/formatadores';
import { chavePixParaFormulario, mascaraCnpj, mascaraTelefone, somenteDigitos } from '../utils/mascaras';
import { cnpjValido, mensagemChavePix, telefoneValido, validarChavePix } from '../utils/validacoes';

const FORM_VAZIO = {
  razaoSocial: '',
  cnpj: '',
  telefone: '',
  nomeContato: '',
  tipoChavePix: '',
  chavePix: '',
};

export default function Fornecedores() {
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
      const { data } = await api.get('/fornecedores');
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
    if (!form.razaoSocial.trim()) {
      novos.razaoSocial = 'Razão social é obrigatória';
    }
    const digitosCnpj = somenteDigitos(form.cnpj);
    if (!digitosCnpj) {
      novos.cnpj = 'CNPJ é obrigatório';
    } else if (!cnpjValido(form.cnpj)) {
      novos.cnpj = 'CNPJ inválido';
    }
    if (form.telefone && !telefoneValido(form.telefone)) {
      novos.telefone = 'Telefone deve conter 10 ou 11 dígitos com DDD válido';
    }
    const erroChavePix = validarChavePix(form.tipoChavePix, form.chavePix);
    if (erroChavePix) {
      novos.chavePix = erroChavePix;
    }
    if (Object.keys(novos).length > 0) {
      setErros(novos);
      setAviso({ tipo: 'erro', texto: 'Verifique os campos destacados' });
      return;
    }

    const corpo = {
      ...form,
      tipoChavePix: form.tipoChavePix || null,
      chavePix: form.chavePix || null,
    };
    setEnviando(true);
    try {
      if (editando) {
        await api.put(`/fornecedores/${editando}`, corpo);
        setAviso({ tipo: 'sucesso', texto: 'Fornecedor atualizado' });
      } else {
        await api.post('/fornecedores', corpo);
        setAviso({ tipo: 'sucesso', texto: 'Fornecedor cadastrado' });
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
      razaoSocial: item.razaoSocial || '',
      cnpj: mascaraCnpj(item.cnpj),
      telefone: item.telefone ? mascaraTelefone(item.telefone) : '',
      nomeContato: item.nomeContato || '',
      tipoChavePix: item.tipoChavePix || '',
      chavePix: chavePixParaFormulario(item.tipoChavePix, item.chavePix),
    });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function excluir(item) {
    if (!window.confirm(`Excluir o fornecedor ${item.razaoSocial}?`)) {
      return;
    }
    try {
      await api.delete(`/fornecedores/${item.id}`);
      setAviso({ tipo: 'sucesso', texto: 'Fornecedor excluído' });
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
        <h1 className="text-xl font-semibold text-slate-800">Fornecedores</h1>
        <p className="mt-1 text-sm text-slate-500">Cadastro dos fornecedores usados nos pedidos de orçamento.</p>
      </div>

      <Aviso aviso={aviso} aoFechar={() => setAviso(null)} />

      <form onSubmit={salvar} className="cartao">
        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
          {editando ? 'Editar fornecedor' : 'Novo fornecedor'}
        </h2>

        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          <Campo rotulo="Razão social" obrigatorio erro={erros.razaoSocial}>
            <input
              className={`entrada ${erros.razaoSocial ? 'entrada-erro' : ''}`}
              value={form.razaoSocial}
              onChange={(evento) => {
                setForm({ ...form, razaoSocial: evento.target.value });
                definirErro('razaoSocial', '');
              }}
              maxLength={150}
            />
          </Campo>

          <Campo rotulo="CNPJ" obrigatorio erro={erros.cnpj}>
            <input
              className={`entrada ${erros.cnpj ? 'entrada-erro' : ''}`}
              value={form.cnpj}
              onChange={(evento) => {
                const valor = mascaraCnpj(evento.target.value);
                setForm({ ...form, cnpj: valor });
                definirErro(
                  'cnpj',
                  somenteDigitos(valor).length === 14 && !cnpjValido(valor) ? 'CNPJ inválido' : '',
                );
              }}
              placeholder="00.000.000/0000-00"
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

          <Campo rotulo="Nome do contato" erro={erros.nomeContato}>
            <input
              className="entrada"
              value={form.nomeContato}
              onChange={(evento) => setForm({ ...form, nomeContato: evento.target.value })}
              maxLength={120}
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
                const valor = chavePixParaFormulario(form.tipoChavePix, evento.target.value);
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
        <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-500">
          Fornecedores cadastrados
        </h2>
        <div className="overflow-x-auto">
          <table className="tabela">
            <thead>
              <tr>
                <th>ID</th>
                <th>Razão social</th>
                <th>CNPJ</th>
                <th>Telefone</th>
                <th>Contato</th>
                <th>Chave PIX</th>
                <th className="text-right">Ações</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td className="font-medium text-slate-800">{item.razaoSocial}</td>
                  <td>{formatarCnpj(item.cnpj)}</td>
                  <td>{formatarTelefone(item.telefone)}</td>
                  <td>{item.nomeContato || '-'}</td>
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
                  <td colSpan={7} className="py-6 text-center text-sm text-slate-500">
                    Nenhum fornecedor cadastrado.
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
