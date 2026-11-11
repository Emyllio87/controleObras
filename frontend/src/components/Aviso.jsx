export default function Aviso({ aviso, aoFechar }) {
  if (!aviso) {
    return null;
  }

  const cores =
    aviso.tipo === 'sucesso'
      ? 'border-emerald-200 bg-emerald-50 text-emerald-800'
      : 'border-red-200 bg-red-50 text-red-800';

  return (
    <div className={`mb-4 flex items-start justify-between gap-4 rounded-md border px-4 py-3 text-sm ${cores}`}>
      <span>{aviso.texto}</span>
      {aoFechar && (
        <button type="button" onClick={aoFechar} className="text-xs font-semibold uppercase">
          Fechar
        </button>
      )}
    </div>
  );
}
