import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import RotaProtegida from './routes/RotaProtegida';
import RotaTela from './routes/RotaTela';
import Inicio from './pages/Inicio';
import Login from './pages/Login';
import Usuarios from './pages/Usuarios';
import Funcionarios from './pages/Funcionarios';
import Fornecedores from './pages/Fornecedores';
import Pedidos from './pages/Pedidos';
import Autorizacoes from './pages/Autorizacoes';
import Almoxarifado from './pages/Almoxarifado';
import Diarias from './pages/Diarias';
import HistoricoDiarias from './pages/HistoricoDiarias';
import Pagamentos from './pages/Pagamentos';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />

      <Route element={<RotaProtegida />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Inicio />} />
          <Route path="/usuarios" element={<RotaTela tela="ADMINISTRADOR"><Usuarios /></RotaTela>} />
          <Route path="/funcionarios" element={<RotaTela tela="FUNCIONARIOS"><Funcionarios /></RotaTela>} />
          <Route path="/fornecedores" element={<RotaTela tela="FORNECEDORES"><Fornecedores /></RotaTela>} />
          <Route path="/pedidos" element={<RotaTela tela="PEDIDOS_ORCAMENTO"><Pedidos /></RotaTela>} />
          <Route path="/autorizacoes" element={<RotaTela tela="AUTORIZACAO_PAGAMENTO"><Autorizacoes /></RotaTela>} />
          <Route path="/almoxarifado" element={<RotaTela tela="ALMOXARIFADO"><Almoxarifado /></RotaTela>} />
          <Route path="/diarias" element={<RotaTela tela="DIARIAS"><Diarias /></RotaTela>} />
          <Route path="/historico-diarias" element={<RotaTela tela="HISTORICO_DIARIAS"><HistoricoDiarias /></RotaTela>} />
          <Route path="/pagamentos" element={<RotaTela tela="PAGAMENTOS"><Pagamentos /></RotaTela>} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
