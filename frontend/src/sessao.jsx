import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import api from './api';

const SessaoContext = createContext(null);

export function SessaoProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setCarregando(false);
      return;
    }
    api
      .get('/auth/sessao')
      .then(({ data }) => setUsuario(data))
      .catch(() => localStorage.removeItem('token'))
      .finally(() => setCarregando(false));
  }, []);

  const entrar = useCallback((dados) => {
    localStorage.setItem('token', dados.token);
    setUsuario({ nome: dados.nome, login: dados.login, telas: dados.telas });
  }, []);

  const sair = useCallback(() => {
    localStorage.removeItem('token');
    setUsuario(null);
  }, []);

  const valor = useMemo(() => ({ usuario, carregando, entrar, sair }), [usuario, carregando, entrar, sair]);

  return <SessaoContext.Provider value={valor}>{children}</SessaoContext.Provider>;
}

export function useSessao() {
  return useContext(SessaoContext);
}
