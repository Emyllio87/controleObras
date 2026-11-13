import api from '../api';

function nomeDoCabecalho(cabecalho) {
  if (!cabecalho) {
    return null;
  }
  const utf8 = cabecalho.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8) {
    return decodeURIComponent(utf8[1]);
  }
  const simples = cabecalho.match(/filename="?([^";]+)"?/i);
  return simples ? simples[1] : null;
}

export async function baixarArquivo(url, nomePadrao) {
  const resposta = await api.get(url, { responseType: 'blob' });
  const nome = nomeDoCabecalho(resposta.headers['content-disposition']) || nomePadrao;
  const endereco = URL.createObjectURL(resposta.data);
  const link = document.createElement('a');
  link.href = endereco;
  link.download = nome;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(endereco);
}
