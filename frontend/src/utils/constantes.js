export const TIPOS_PIX = [
  { valor: 'CPF', rotulo: 'CPF' },
  { valor: 'CNPJ', rotulo: 'CNPJ' },
  { valor: 'TELEFONE', rotulo: 'Telefone' },
  { valor: 'EMAIL', rotulo: 'E-mail' },
  { valor: 'ALEATORIA', rotulo: 'Chave aleatória' },
];

export const STATUS_USUARIO = [
  { valor: 'ATIVO', rotulo: 'Ativo' },
  { valor: 'INATIVO', rotulo: 'Inativo' },
];

export const STATUS_PAGAMENTO = [
  { valor: 'PENDENTE', rotulo: 'Pendente' },
  { valor: 'PAGO', rotulo: 'Pago' },
];

export function rotuloTipoPix(tipo) {
  return TIPOS_PIX.find((item) => item.valor === tipo)?.rotulo || '-';
}
