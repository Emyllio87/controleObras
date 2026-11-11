export function somenteDigitos(valor) {
  return String(valor ?? '').replace(/\D/g, '');
}

export function mascaraCpf(valor) {
  return somenteDigitos(valor)
    .slice(0, 11)
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/(\d{3})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3-$4');
}

export function mascaraCnpj(valor) {
  return somenteDigitos(valor)
    .slice(0, 14)
    .replace(/(\d{2})(\d)/, '$1.$2')
    .replace(/(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/(\d{2})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3/$4')
    .replace(/(\d{2})\.(\d{3})\.(\d{3})\/(\d{4})(\d)/, '$1.$2.$3/$4-$5');
}

export function mascaraTelefone(valor) {
  const digitos = somenteDigitos(valor).slice(0, 11);
  if (digitos.length <= 10) {
    return digitos.replace(/(\d{2})(\d)/, '($1) $2').replace(/(\d{4})(\d)/, '$1-$2');
  }
  return digitos.replace(/(\d{2})(\d)/, '($1) $2').replace(/(\d{5})(\d)/, '$1-$2');
}

export function mascaraMoeda(valor) {
  const digitos = somenteDigitos(valor).slice(0, 13);
  if (!digitos) {
    return '';
  }
  return (Number(digitos) / 100).toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

export function moedaParaNumero(texto) {
  if (texto === '' || texto === null || texto === undefined) {
    return null;
  }
  return Number(String(texto).replace(/\./g, '').replace(',', '.'));
}

export function numeroParaMoeda(valor) {
  if (valor === '' || valor === null || valor === undefined) {
    return '';
  }
  return Number(valor).toLocaleString('pt-BR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

export function numeroParaQuantidade(valor) {
  if (valor === '' || valor === null || valor === undefined) {
    return '';
  }
  return Number(valor).toLocaleString('pt-BR', { maximumFractionDigits: 3 });
}

export function hojeISO() {
  const agora = new Date();
  const mes = String(agora.getMonth() + 1).padStart(2, '0');
  const dia = String(agora.getDate()).padStart(2, '0');
  return `${agora.getFullYear()}-${mes}-${dia}`;
}

export function dataParaApi(iso) {
  if (!iso) {
    return '';
  }
  const [ano, mes, dia] = iso.split('-');
  return `${dia}/${mes}/${ano}`;
}

export function dataDoApi(texto) {
  if (!texto) {
    return '';
  }
  if (/^\d{4}-\d{2}-\d{2}/.test(texto)) {
    return String(texto).slice(0, 10);
  }
  const [dia, mes, ano] = String(texto).split('/');
  return `${ano}-${mes}-${dia}`;
}

export function mascaraPixTelefone(valor) {
  const texto = String(valor ?? '').trim();
  const temFormatacao = /[+()\s-]/.test(texto);
  const semPrefixo = texto.startsWith('+55') ? texto.slice(3) : texto;
  let digitos = somenteDigitos(semPrefixo);
  if (!temFormatacao && digitos.length === 13 && digitos.startsWith('55')) {
    digitos = digitos.slice(2);
  }
  digitos = digitos.slice(0, 11);
  if (!digitos) {
    return '';
  }
  const ddd = digitos.slice(0, 2);
  const numero = digitos.slice(2);
  let saida = `+55 (${ddd}`;
  if (digitos.length >= 2) {
    saida += ')';
  }
  if (numero) {
    saida += ` ${numero.slice(0, 5)}`;
  }
  if (numero.length > 5) {
    saida += `-${numero.slice(5)}`;
  }
  return saida;
}

export function formatarChavePix(tipo, valor) {
  if (!valor) {
    return '';
  }
  switch (tipo) {
    case 'CPF':
      return mascaraCpf(valor);
    case 'CNPJ':
      return mascaraCnpj(valor);
    case 'TELEFONE':
      return mascaraPixTelefone(valor);
    default:
      return valor;
  }
}

export function chavePixParaFormulario(tipo, valor) {
  return formatarChavePix(tipo, valor);
}
