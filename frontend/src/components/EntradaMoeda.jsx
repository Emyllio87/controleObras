import { mascaraMoeda } from '../utils/mascaras';

export default function EntradaMoeda({ valor, aoMudar, erro = false, ...resto }) {
  return (
    <div
      className={`flex items-center rounded-md border bg-white focus-within:ring-1 ${
        erro
          ? 'border-red-400 focus-within:border-red-500 focus-within:ring-red-500'
          : 'border-slate-300 focus-within:border-sky-600 focus-within:ring-sky-600'
      }`}
    >
      <span className="pl-3 text-sm text-slate-500">R$</span>
      <input
        className="w-full border-0 bg-transparent px-2 py-2 text-sm text-slate-800 outline-none"
        inputMode="numeric"
        value={valor}
        onChange={(evento) => aoMudar(mascaraMoeda(evento.target.value))}
        {...resto}
      />
    </div>
  );
}
