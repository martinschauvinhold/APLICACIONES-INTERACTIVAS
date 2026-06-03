/**
 * Btn — botón con variantes.
 * Props:
 *   variant: 'primary' | 'ghost'  (default 'primary')
 *   size:    'sm' | 'md' | 'lg'   (default 'md')
 *   children, ...rest
 */
function Btn({ variant = 'primary', size = 'md', className = '', children, ...rest }) {
  const cls = `btn btn-${variant} btn-${size} ${className}`;
  return (
    <button {...rest} className={cls}>
      {children}
    </button>
  );
}

export default Btn;
