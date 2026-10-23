const [major, minor] = process.versions.node.split('.').map(Number);
if (!((major === 22 && minor >= 12) || major === 24 || major >= 26)) {
  console.error('Node ' + process.versions.node + ' no es compatible. Instala Node.js 24 LTS y vuelve a abrir PowerShell.');
  process.exit(1);
}
