// ItemController.js
// Compute API base context
(function(){
  const parts = window.location.pathname.split('/').filter(Boolean);
  const ctx = parts.length > 0 ? '/' + parts[0] : '';
  window.API_BASE = ctx;
})();

// Utilities
function itemQs(params){
  const e = Object.entries(params).filter(([_,v]) => v !== undefined && v !== null && String(v).trim() !== '');
  if (!e.length) return '';
  const usp = new URLSearchParams();
  e.forEach(([k,v]) => usp.append(k, String(v).trim()));
  return `?${usp.toString()}`;
}

function itemShowAlert(message, type='success'){
  const wrap = document.getElementById('itemAlerts');
  if (!wrap) {
    // Fallback to customer alerts if item alerts not found
    const fallback = document.getElementById('customerAlerts');
    if (fallback) {
      const id = `alert-${Date.now()}`;
      fallback.innerHTML = `
        <div id="${id}" class="alert alert-${type} alert-dismissible fade show" role="alert">
          ${message}
          <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>`;
      setTimeout(()=>{
        const el = document.getElementById(id);
        if (el) bootstrap.Alert.getOrCreateInstance(el).close();
      }, 3000);
      return;
    }
    return alert(message);
  }

  const id = `alert-${Date.now()}`;
  wrap.innerHTML = `
    <div id="${id}" class="alert alert-${type} alert-dismissible fade show" role="alert">
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>`;

  setTimeout(()=>{
    const el = document.getElementById(id);
    if (el) bootstrap.Alert.getOrCreateInstance(el).close();
  }, 3000);
}

// Fetch list with filters/paging
async function fetchItems(){
  const def = { page: window.ITEM_PAGE||1, size: window.ITEM_SIZE||10, sort: window.ITEM_SORT||'item_code', dir: window.ITEM_DIR||'asc' };
  const qp = itemQs(Object.assign(def, {
    code: document.getElementById('searchItemCode')?.value || '',
    name: document.getElementById('searchItemName')?.value || '',
    minPrice: document.getElementById('searchMinPrice')?.value || '',
    maxPrice: document.getElementById('searchMaxPrice')?.value || '',
    minQty: document.getElementById('searchMinQty')?.value || '',
    maxQty: document.getElementById('searchMaxQty')?.value || ''
  }));
  const res = await fetch(`${window.API_BASE}/items${qp}`);
  if (!res.ok) throw new Error('Failed to fetch items');
  const payload = await res.json();
  const data = Array.isArray(payload) ? payload : payload.data;
  renderItemTable(data);
  if (!Array.isArray(payload)) renderItemPagination(payload.page, payload.size, payload.total);
}

function renderItemTable(items){
  const tbody = document.getElementById('itemTable');
  if (!tbody) return;
  tbody.innerHTML = '';
  items.forEach(it => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${it.itemCode}</td>
      <td>${it.name}</td>
      <td>${Number(it.unitPrice).toFixed(2)}</td>
      <td>${it.qtyOnHand}</td>
      <td class="text-nowrap">
        <button class="btn btn-sm btn-outline-secondary me-1" data-code="${it.itemCode}" onclick="editItem(event)"><i class="bi bi-pencil"></i></button>
        <button class="btn btn-sm btn-outline-danger" data-code="${it.itemCode}" onclick="deleteItem(event)"><i class="bi bi-trash"></i></button>
      </td>`;
    tbody.appendChild(tr);
  });
}

function renderItemPagination(page, size, total){
  window.ITEM_PAGE = page; window.ITEM_SIZE = size; window.ITEM_TOTAL = total;
  const ul = document.querySelector('#items .pagination');
  const summary = document.querySelector('#items .items-summary');
  if (summary) {
    const start = total === 0 ? 0 : (page - 1) * size + 1;
    const end = Math.min(total, page * size);
    summary.textContent = `Showing ${start} to ${end} of ${total} entries`;
  }
  if (!ul) return;
  ul.innerHTML = '';
  const mk = (label, p, disabled=false, active=false) => {
    const li = document.createElement('li'); li.className = `page-item${disabled?' disabled':''}${active?' active':''}`;
    const a = document.createElement('a'); a.className = 'page-link'; a.href = '#'; a.textContent = label;
    a.addEventListener('click', (e) => { e.preventDefault(); if (disabled||active) return; window.ITEM_PAGE = p; fetchItems(); });
    li.appendChild(a); return li;
  };
  const pages = Math.max(1, Math.ceil(total / size));
  ul.appendChild(mk('Previous', Math.max(1, page-1), page===1));
  const win = 5; const s = Math.max(1, page - Math.floor(win/2)); const e = Math.min(pages, s + win - 1);
  for (let i=s;i<=e;i++) ul.appendChild(mk(String(i), i, false, i===page));
  ul.appendChild(mk('Next', Math.min(pages, page+1), page===pages));
}

// Wire search
function wireItemSearch(){
  const btn = document.getElementById('searchItemsBtn');
  const clr = document.getElementById('clearItemsBtn');
  const ids = ['searchItemCode','searchItemName','searchMinPrice','searchMaxPrice','searchMinQty','searchMaxQty'];
  const inputs = ids.map(id => document.getElementById(id)).filter(Boolean);
  if (btn) btn.addEventListener('click', fetchItems);
  if (clr) clr.addEventListener('click', ()=>{ inputs.forEach(i=> i.value=''); fetchItems(); });
  inputs.forEach(i => i.addEventListener('input', ()=>{ window.ITEM_PAGE=1; fetchItems(); }));
}

// CRUD
async function openAddItem(){
  const title = document.getElementById('itemModalLabel'); if (title) title.textContent = 'Add Item';
  const saveBtn = document.getElementById('saveItem'); if (saveBtn) saveBtn.textContent = 'Save Item';
  const form = document.getElementById('itemForm'); if (form) form.reset();
  try {
    const res = await fetch(`${window.API_BASE}/items?next=true`, { method: 'HEAD' });
    const next = res.headers.get('X-Next-Item-Code');
    const codeEl = document.getElementById('itemCode'); if (codeEl) { codeEl.value = next || ''; codeEl.readOnly = true; }
  } catch(e){}
  new bootstrap.Modal(document.getElementById('itemModal')).show();
}

async function editItem(e){
  const code = e.target.closest('button').getAttribute('data-code');
  const res = await fetch(`${window.API_BASE}/items?code=${encodeURIComponent(code)}&single=true`);
  if (!res.ok) return itemShowAlert('Item not found', 'warning');
  const it = await res.json();
  document.getElementById('itemModalLabel').textContent = 'Edit Item';
  const saveBtn = document.getElementById('saveItem'); if (saveBtn) saveBtn.textContent = 'Update Item';
  document.getElementById('itemCode').value = it.itemCode;
  document.getElementById('itemName').value = it.name;
  document.getElementById('itemUnitPrice').value = Number(it.unitPrice).toFixed(2);
  document.getElementById('itemQty').value = it.qtyOnHand;
  new bootstrap.Modal(document.getElementById('itemModal')).show();
}

function validateItemForm(){
  const codeEl = document.getElementById('itemCode');
  const nameEl = document.getElementById('itemName');
  const priceEl = document.getElementById('itemUnitPrice');
  const qtyEl = document.getElementById('itemQty');
  const setInvalid = (el, bad)=> bad?el.classList.add('is-invalid'):el.classList.remove('is-invalid');
  const price = parseFloat(priceEl.value);
  const qty = parseInt(qtyEl.value,10);
  let bad = false;
  if (!nameEl.value.trim()) { setInvalid(nameEl,true); bad=true; } else setInvalid(nameEl,false);
  if (isNaN(price) || price < 0) { setInvalid(priceEl,true); bad=true; } else setInvalid(priceEl,false);
  if (!Number.isInteger(qty) || qty < 0) { setInvalid(qtyEl,true); bad=true; } else setInvalid(qtyEl,false);
  return !bad;
}

async function saveItem(){
  try {
    if (!validateItemForm()) { itemShowAlert('Please correct the highlighted fields', 'warning'); return; }
    const isEdit = document.getElementById('itemModalLabel').textContent.includes('Edit');
    const payload = {
      itemCode: document.getElementById('itemCode').value,
      name: document.getElementById('itemName').value.trim(),
      unitPrice: parseFloat(document.getElementById('itemUnitPrice').value),
      qtyOnHand: parseInt(document.getElementById('itemQty').value,10)
    };
    console.log('Saving item:', payload, 'Method:', isEdit ? 'PUT' : 'POST');
    const res = await fetch(`${window.API_BASE}/items`, {
      method: isEdit ? 'PUT' : 'POST',
      headers: { 'Content-Type':'application/json' },
      body: JSON.stringify(payload)
    });
    console.log('Response status:', res.status);
    const out = await res.json().catch(()=>({success:false}));
    console.log('Response body:', out);
    if (!res.ok || out.success === false) {
      const msg = (out && out.error) ? out.error : (isEdit ? 'Failed to update item' : 'Failed to add item');
      itemShowAlert(msg, 'warning');
      return;
    }
    bootstrap.Modal.getInstance(document.getElementById('itemModal')).hide();
    itemShowAlert(isEdit ? 'Item updated' : 'Item added', 'success');
    fetchItems();
  } catch (error) {
    console.error('Save item error:', error);
    itemShowAlert('An error occurred while saving the item', 'danger');
  }
}

// Global variable to store the item code to delete
let deleteTargetItemCode = null;

async function deleteItem(e){
  deleteTargetItemCode = e.target.closest('button').getAttribute('data-code');
  const modalEl = document.getElementById('deleteItemConfirmModal');
  if (modalEl) {
    new bootstrap.Modal(modalEl).show();
  } else {
    // Fallback to browser confirm if modal not found
    if (!confirm('Are you sure you want to delete this item?')) return;
    await performItemDelete();
  }
}

async function performItemDelete(){
  if (!deleteTargetItemCode) return;
  const res = await fetch(`${window.API_BASE}/items?code=${encodeURIComponent(deleteTargetItemCode)}`, { method:'DELETE' });
  const out = await res.json().catch(()=>({success:false}));
  if (out.success) { itemShowAlert('Item deleted','success'); fetchItems(); }
  else { itemShowAlert('Failed to delete item','warning'); }
  deleteTargetItemCode = null;
}

// Wire
document.addEventListener('DOMContentLoaded', ()=>{
  // Buttons
  const addBtn = document.getElementById('addItemBtn'); if (addBtn) addBtn.addEventListener('click', openAddItem);
  const saveBtn = document.getElementById('saveItem'); if (saveBtn) saveBtn.addEventListener('click', saveItem);
  const confirmDeleteBtn = document.getElementById('confirmDeleteItemBtn');
  if (confirmDeleteBtn) confirmDeleteBtn.addEventListener('click', performItemDelete);
  wireItemSearch();
  fetchItems();
});

// Export to global (onclick handlers use these)
window.editItem = editItem;
window.deleteItem = deleteItem;

