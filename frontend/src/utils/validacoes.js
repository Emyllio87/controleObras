import { somenteDigitos } from './mascaras';

const EMAIL = /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/;
const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function cpfValido(valor) {
  const cpf = somenteDigitos(valor);
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
    return false;
  }
  for (let posicao = 9; posicao < 11; posicao += 1) {
    let soma = 0;
    for (let i = 0; i < posicao; i += 1) {
      soma += Number(cpf[i]) * (posicao + 1 - i);
    }
    let digito = 11 - (soma % 11);
    if (digito >= 10) {
      digito = 0;
    }
    if (digito !== Number(cpf[posicao])) {
      return false;
    }
  }
  return true;
}

export function cnpjValido(valor) {
  const cnpj = somenteDigitos(valor);
  if (cnpj.length !== 14 || /^(\d)\1{13}$/.test(cnpj)) {
    return false;
  }
  const pesosPrimeiro = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  const pesosSegundo = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  const digito = (pesos) => {
    let soma = 0;
    for (let i = 0; i < pesos.length; i += 1) {
      soma += Number(cnpj[i]) * pesos[i];
    }
    const resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  };
  return digito(pesosPrimeiro) === Number(cnpj[12]) && digito(pesosSegundo) === Number(cnpj[13]);
}

export function telefoneValido(valor) {
  const digitos = somenteDigitos(valor);
  if (digitos.length !== 10 && digitos.length !== 11) {
    return false;
  }
  const ddd = Number(digitos.slice(0, 2));
  return ddd >= 11 && ddd % 10 !== 0;
}

export function emailValido(valor) {
  return EMAIL.test(String(valor || '').trim().toLowerCase());
}

export function uuidValido(valor) {
  return UUID.test(String(valor || '').trim());
}

function digitosPixTelefone(valor) {
  const texto = String(valor || '').trim();
  const temFormatacao = /[+()\s-]/.test(texto);
  const semPrefixo = texto.startsWith('+55') ? texto.slice(3) : texto;
  let digitos = somenteDigitos(semPrefixo);
  if (!temFormatacao && digitos.length === 13 && digitos.startsWith('55')) {
    digitos = digitos.slice(2);
  }
  return digitos;
}

export function validarChavePix(tipo, valor) {
  if (!tipo) {
    return valor ? 'Informe o tipo da chave PIX' : '';
  }
  if (!valor || !String(valor).trim()) {
    return 'Chave PIX é obrigatória';
  }
  switch (tipo) {
    case 'CPF':
      return cpfValido(valor) ? '' : 'CPF da chave PIX inválido';
    case 'CNPJ':
      return cnpjValido(valor) ? '' : 'CNPJ da chave PIX inválido';
    case 'TELEFONE':
      return digitosPixTelefone(valor).length === 11
        ? ''
        : 'Chave PIX de telefone deve ter 11 dígitos após o código do país';
    case 'EMAIL':
      return emailValido(valor) ? '' : 'E-mail inválido';
    case 'ALEATORIA':
      return uuidValido(valor) ? '' : 'Chave aleatória deve estar no formato UUID';
    default:
      return '';
  }
}

/**
 * Validação em tempo real: só aponta erro quando o valor já tem tamanho
 * suficiente para ser considerado completo, evitando mensagens enquanto o
 * usuário ainda está digitando.
 */
export function mensagemChavePix(tipo, valor) {
  if (!tipo || !valor) {
    return '';
  }
  switch (tipo) {
    case 'CPF':
      return somenteDigitos(valor).length === 11 ? validarChavePix(tipo, valor) : '';
    case 'CNPJ':
      return somenteDigitos(valor).length === 14 ? validarChavePix(tipo, valor) : '';
    case 'TELEFONE':
      return digitosPixTelefone(valor).length === 11 ? validarChavePix(tipo, valor) : '';
    case 'EMAIL':
      return String(valor).includes('@') ? validarChavePix(tipo, valor) : '';
    case 'ALEATORIA':
      return String(valor).length === 36 ? validarChavePix(tipo, valor) : '';
    default:
      return '';
  }
}
