document.addEventListener('DOMContentLoaded', function () {
  const table = $('#productsTable').DataTable();
  fetch('/api/products/report')
    .then(r => r.json())
    .then(data => {
      const items = data.items || [];
      items.forEach(i => {
        table.row.add([i.id, i.name, i.category, i.stock, i.price]).draw(false);
      });
      // preparar gráfico simple: stock por categoría
      const byCat = {};
      items.forEach(i => { byCat[i.category] = (byCat[i.category]||0) + i.stock; });
      const labels = Object.keys(byCat);
      const values = labels.map(l => byCat[l]);
      const ctx = document.getElementById('stockChart').getContext('2d');
      new Chart(ctx, {
        type: 'pie',
        data: { labels, datasets: [{ data: values, backgroundColor: ['#36a2eb','#ff6384','#ffcd56'] }] }
      });
    });

  document.getElementById('exportBtn').addEventListener('click', () => {
    window.location.href = '/api/products/export';
  });
});

function showTab(tab) {
  document.getElementById('tab-site').style.display = (tab === 'site') ? 'block' : 'none';
  document.getElementById('tab-report').style.display = (tab === 'report') ? 'block' : 'none';
}