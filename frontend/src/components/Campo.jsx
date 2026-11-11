export default function Campo({ rotulo, obrigatorio = false, erro, className = '', children }) {
  return (
    <div className={className}>
      <span className="rotulo">
        {rotulo}
        {obrigatorio && <span className="text-red-600"> *</span>}
      </span>
      {children}
      {erro && <p className="mt-1 text-xs text-red-600">{erro}</p>}
    </div>
  );
}
