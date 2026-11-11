import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
});

api.interceptors.request.use((configuracao) => {
  const token = localStorage.getItem('token');
  if (token) {
    configuracao.headers.Authorization = `Bearer ${token}`;
  }
  return configuracao;
});

api.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    const url = erro.config?.url || '';
    if (erro.response?.status === 401 && !url.includes('/auth/login')) {
      localStorage.removeItem('token');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(erro);
  },
);

export function mensagemErro(erro) {
  return erro?.response?.data?.mensagem || 'Não foi possível concluir a operação';
}

export function errosCampos(erro) {
  return erro?.response?.data?.campos || {};
}

export default api;
