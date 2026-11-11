import { mascaraCnpj, mascaraCpf, mascaraTelefone, somenteDigitos } from './mascaras';

const MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const QUANTIDADE = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 3 });

export function moeda(valor) {
  return MOEDA.format(Number(valor ?? 0));
}

export function quantidade(valor) {
  return QUANTIDADE.format(Number(valor ?? 0));
}

export function dataBR(valor) {
  if (!valor) {
    return '-';
  }
  if (/^\d{2}\/\d{2}\/\d{4}/.test(valor)) {
    return String(valor).slice(0, 10);
  }
  const [ano, mes, dia] = String(valor).slice(0, 10).split('-');
  return `${dia}/${mes}/${ano}`;
}

export function formatarCpf(valor) {
  if (!valor) {
    return '-';
  }
  return valor.length === 11 ? mascaraCpf(valor) : valor;
}

export function formatarCnpj(valor) {
  if (!valor) {
    return '-';
  }
  return valor.length === 14 ? mascaraCnpj(valor) : valor;
}

export function formatarTelefone(valor) {
  if (!valor) {
    return '-';
  }
  const digitos = somenteDigitos(valor);
  if (digitos.length === 13) {
    return `+55 ${mascaraTelefone(digitos.slice(2))}`;
  }
  return mascaraTelefone(digitos);
}

export function formatarChavePix(tipo, chave) {
  if (!chave) {
    return '-';
  }
  switch (tipo) {
    case 'CPF':
      return formatarCpf(chave);
    case 'CNPJ':
      return formatarCnpj(chave);
    case 'TELEFONE': {
      const digitos = somenteDigitos(chave);
      return formatarTelefone(digitos.length === 11 ? `55${digitos}` : digitos);
    }
    default:
      return chave;
  }
}

export function numeroPedido(numero) {
  return String(numero ?? '').padStart(6, '0');
}
