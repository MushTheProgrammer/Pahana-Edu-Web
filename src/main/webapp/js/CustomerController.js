// Compute API base dynamically so it works with any context path
(function(){
  const parts = window.location.pathname.split('/').filter(Boolean);
  const ctx = parts.length > 0 ? '/' + parts[0] : '';
  window.API_BASE = ctx; // e.g., "/PahanaEduSystem_war_exploded"
})();

// Fetch and render customer list
// Build query string helper
function qs(params) {
  const entries = Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && String(v).trim() !== '');
  if (!entries.length) return '';
  const usp = new URLSearchParams();
  entries.forEach(([k, v]) => usp.append(k, String(v).trim()));
  return `?${usp.toString()}`;
}

async function fetchCustomers(filters = {}) {
    try {
        const query = qs({
          acc: filters.acc || document.getElementById('searchAcc')?.value || '',
          name: filters.name || document.getElementById('searchName')?.value || '',
          email: filters.email || document.getElementById('searchEmail')?.value || '',
          phone: filters.phone || document.getElementById('searchPhone')?.value || ''
        });
        // Add paging/sorting defaults
        const def = { page: window.CUST_PAGE||1, size: window.CUST_SIZE||10, sort: window.CUST_SORT||'customer_id', dir: window.CUST_DIR||'asc' };
        const qp = qs(Object.assign(def, {
          acc: filters.acc || document.getElementById('searchAcc')?.value || '',
          name: filters.name || document.getElementById('searchName')?.value || '',
          email: filters.email || document.getElementById('searchEmail')?.value || '',
          phone: filters.phone || document.getElementById('searchPhone')?.value || ''
        }));
        const response = await fetch(`${window.API_BASE}/customers${qp}`);
        if (!response.ok) throw new Error("Failed to fetch customers");

        const payload = await response.json();
        const customers = Array.isArray(payload) ? payload : payload.data;
        renderCustomerTable(customers);
        if (!Array.isArray(payload)) renderPagination(payload.page, payload.size, payload.total);
    } catch (err) {
        console.error("Error loading customers:", err);
        showAlert('Failed to load customers', 'danger');
    }
}

function showAlert(message, type = 'success') {
  const wrap = document.getElementById('customerAlerts');
  if (!wrap) return alert(message);
  const id = `alert-${Date.now()}`;
  wrap.innerHTML = `
    <div id="${id}" class="alert alert-${type} alert-dismissible fade show" role="alert">
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>`;
  // Auto-dismiss after 3s on mobile friendliness
  setTimeout(() => {
    const el = document.getElementById(id);
    if (el) bootstrap.Alert.getOrCreateInstance(el).close();
  }, 3000);
}
// Global helper to toggle Bootstrap invalid state
function setInvalid(id, invalid=true) {
  const el = document.getElementById(id);
  if (!el) return;
  if (invalid) el.classList.add('is-invalid');
  else el.classList.remove('is-invalid');
}


// Inline validation: wire events and clear invalids on modal open/close
function clearInvalidAll() {
  ['accountNo','customerName','customerEmail','customerPhone'].forEach(id => setInvalid(id, false));
}

function wireLiveFieldValidation() {
  const nameEl = document.getElementById('customerName');
  const emailEl = document.getElementById('customerEmail');
  const phoneEl = document.getElementById('customerPhone');
  const accEl = document.getElementById('accountNo');
  // Email must include '@' per requirement
  const emailHasAt = (v) => v.includes('@');
  // focus clears invalid
  [accEl, nameEl, emailEl, phoneEl].forEach(el => {
    if (!el) return;
    el.addEventListener('focus', () => setInvalid(el.id, false));
  });
  // live checks
  if (nameEl) nameEl.addEventListener('input', () => setInvalid('customerName', nameEl.value.trim() === ''));
  if (emailEl) emailEl.addEventListener('input', () => {
    const bad = !emailHasAt(emailEl.value.trim());
    setInvalid('customerEmail', bad);
  });
  if (phoneEl) phoneEl.addEventListener('input', () => {
    // keep only digits and cap at 10
    phoneEl.value = (phoneEl.value || '').replace(/[^0-9]/g, '').slice(0, 10);
    const bad = phoneEl.value !== '' && !/^\d{10}$/.test(phoneEl.value);
    setInvalid('customerPhone', bad);
  });
  // Reset invalid states when modal opens/closes
  const modalEl = document.getElementById('customerModal');
  if (modalEl) {
    modalEl.addEventListener('shown.bs.modal', clearInvalidAll);
    modalEl.addEventListener('hidden.bs.modal', clearInvalidAll);
  }
}





// Render helper shared by loaders
function renderCustomerTable(customers) {
  const tableBody = document.getElementById('customerTable') || document.querySelector('#customers table tbody');
  if (!tableBody) return;
  tableBody.innerHTML = '';
  customers.forEach(customer => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${customer.accountNo}</td>
      <td>
        <div class="d-flex align-items-center">
          <span>${customer.name}</span>
        </div>
      </td>
      <td>${customer.email || 'N/A'}</td>
      <td>${customer.phone || 'N/A'}</td>
      <td>0</td>
      <td><span class="badge bg-success">Active</span></td>
      <td class="text-nowrap">
        <button class="btn btn-sm btn-outline-primary me-1 view-customer" data-acc="${customer.accountNo}">
          <i class="bi bi-eye"></i>
        </button>
        <button class="btn btn-sm btn-outline-secondary me-1 edit-customer" data-acc="${customer.accountNo}">
          <i class="bi bi-pencil"></i>
        </button>
        <button class="btn btn-sm btn-outline-danger delete-customer" data-acc="${customer.accountNo}">
          <i class="bi bi-trash"></i>
        </button>
      </td>`;
    tableBody.appendChild(row);
  });
  // wire buttons
  document.querySelectorAll('.view-customer').forEach(btn => btn.addEventListener('click', viewCustomer));
  document.querySelectorAll('.delete-customer').forEach(btn => btn.addEventListener('click', deleteCustomer));
  document.querySelectorAll('.edit-customer').forEach(btn => btn.addEventListener('click', editCustomer));
}

// Pagination renderer
function renderPagination(page, size, total) {
  window.CUST_PAGE = page; window.CUST_SIZE = size; window.CUST_TOTAL = total;
  const totalPages = Math.max(1, Math.ceil(total / size));
  const pagUl = document.querySelector('#customers .pagination');
  const summary = document.querySelector('#customers .text-muted');
  if (summary) {
    const start = total === 0 ? 0 : (page - 1) * size + 1;
    const end = Math.min(total, page * size);
    summary.textContent = `Showing ${start} to ${end} of ${total} entries`;
  }
  if (!pagUl) return;
  pagUl.innerHTML = '';
  const mkItem = (label, p, disabled=false, active=false) => {
    const li = document.createElement('li'); li.className = `page-item${disabled?' disabled':''}${active?' active':''}`;
    const a = document.createElement('a'); a.className = 'page-link'; a.href = '#'; a.textContent = label;
    a.addEventListener('click', (e) => { e.preventDefault(); if (disabled||active) return; window.CUST_PAGE = p; fetchCustomers(); });
    li.appendChild(a); return li;
  };
  pagUl.appendChild(mkItem('Previous', Math.max(1, page-1), page===1, false));
  const windowSize = 5; const start = Math.max(1, page - Math.floor(windowSize/2));
  const end = Math.min(totalPages, start + windowSize - 1);
  for (let i = start; i <= end; i++) pagUl.appendChild(mkItem(String(i), i, false, i===page));
  pagUl.appendChild(mkItem('Next', Math.min(totalPages, page+1), page===totalPages, false));
}


// Apply search filters to current table rows
function applyCustomerSearch() {
  const acc = (document.getElementById('searchAcc')?.value || '').toLowerCase();
  const name = (document.getElementById('searchName')?.value || '').toLowerCase();
  const email = (document.getElementById('searchEmail')?.value || '').toLowerCase();
  const phone = (document.getElementById('searchPhone')?.value || '').toLowerCase();
  const tbody = document.getElementById('customerTable');
  if (!tbody) return;
  const rows = Array.from(tbody.querySelectorAll('tr'));
  rows.forEach(tr => {
    const tds = tr.querySelectorAll('td');
    const vAcc = (tds[0]?.innerText || '').toLowerCase();
    const vName = (tds[1]?.innerText || '').toLowerCase();
    const vEmail = (tds[2]?.innerText || '').toLowerCase();
    const vPhone = (tds[3]?.innerText || '').toLowerCase();
    const match = (!acc || vAcc.includes(acc)) &&
                  (!name || vName.includes(name)) &&
                  (!email || vEmail.includes(email)) &&
                  (!phone || vPhone.includes(phone));
    tr.style.display = match ? '' : 'none';
  });
}

// Wire search inputs
function wireCustomerSearch() {
  const btn = document.getElementById('searchCustomersBtn');
  const clr = document.getElementById('clearCustomersBtn');
  const inputs = ['searchAcc','searchName','searchEmail','searchPhone']
    .map(id => document.getElementById(id))
    .filter(Boolean);
  if (btn) btn.addEventListener('click', () => fetchCustomers());
  if (clr) clr.addEventListener('click', () => {
    inputs.forEach(i => i.value = '');
    fetchCustomers();
  });
  // Live filtering as user types (server-side search)
  inputs.forEach(i => i.addEventListener('input', () => fetchCustomers()));
}

// Wire search on load
document.addEventListener('DOMContentLoaded', wireCustomerSearch);

// ========== CUSTOMER MANAGEMENT ========== //

// Load customers when section is shown
// Auto-fill next account number on opening the Add modal
const addBtn = document.getElementById('addCustomerBtn');
if (addBtn) {
  addBtn.addEventListener('click', async () => {
    // Reset modal for new customer
    document.getElementById('customerModalLabel').innerText = 'Add New Customer';
    const saveBtn = document.getElementById('saveCustomer');
    if (saveBtn) saveBtn.textContent = 'Save Customer';
    document.getElementById('customerForm').reset();

    try {
      const res = await fetch(`${window.API_BASE}/customers?next=true`, { method: 'HEAD' });
      const nextAcc = res.headers.get('X-Next-Account-No');
      if (nextAcc) {
        const accEl = document.getElementById('accountNo');
        if (accEl) {
          accEl.value = nextAcc;
          accEl.setAttribute('readonly', 'true');
        }
      }
    } catch (e) { console.warn('Failed to fetch next account no', e); }
  });
}

document.addEventListener('DOMContentLoaded', fetchCustomers);
const customersLink = document.querySelector('a[href="#customers"]');
if (customersLink) customersLink.addEventListener('click', fetchCustomers);

// (Removed deprecated loadCustomers function to avoid duplicate logic)


// Save new customer
document.getElementById('saveCustomer').addEventListener('click', function(e) {
    e.preventDefault();
    e.stopPropagation();
    const customerData = {
        accountNo: document.getElementById('accountNo').value,
        name: document.getElementById('customerName').value,
        email: document.getElementById('customerEmail').value,
        phone: document.getElementById('customerPhone').value,
        address: document.getElementById('customerAddress').value
    };

    // Validate required fields
    if (!customerData.accountNo || !customerData.name || !customerData.email) {
        showAlert('Please fill in all required fields (Account No, Name, Email)', 'warning');
        return;
    }
    // Strong client validation
    const phoneRe = /^[0-9]{10}$/;
    setInvalid('accountNo', !customerData.accountNo);
    setInvalid('customerName', !customerData.name);
    setInvalid('customerEmail', !(customerData.email || '').includes('@'));
    setInvalid('customerPhone', !!customerData.phone && !phoneRe.test(customerData.phone));

    if (!customerData.accountNo || !customerData.name || !(customerData.email||'').includes('@') || (customerData.phone && !phoneRe.test(customerData.phone))) {
        showAlert('Please correct the highlighted fields and try again', 'warning');
        return;
    }


    // Determine if this is an edit based on modal state
    const modalTitle = document.getElementById('customerModalLabel').innerText;
    const isEdit = modalTitle.includes('Edit');
    const method = isEdit ? 'PUT' : 'POST';

    fetch(`${window.API_BASE}/customers`, {
        method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(customerData)
    })
        .then(async (response) => {
            let data = {};
            try { data = await response.json(); } catch (e) {}
            if (!response.ok || data.success === false) {
                const msg = (data && data.error) ? data.error : (method === 'PUT' ? 'Failed to update customer' : 'Failed to add customer');
                showAlert(msg, 'warning');
                throw new Error(msg);
            }
            // Success
            // Refresh customer list
            fetchCustomers();
            // Close modal
            bootstrap.Modal.getInstance(document.getElementById('customerModal')).hide();
            // Reset form
            document.getElementById('customerForm').reset();
            // Show success message
            showAlert(method === 'PUT' ? 'Customer updated successfully!' : 'Customer added successfully!', 'success');
        })
        .catch(error => {
            console.error('Error:', error);
            if (error && error.message) return; // already alerted
            showAlert('Error saving customer', 'danger');
        });
});

// View customer details in a preview modal
function viewCustomer(e) {
  const accNo = e.target.closest('button').getAttribute('data-acc');
  fetch(`${window.API_BASE}/customers?accNo=${encodeURIComponent(accNo)}`)
    .then(response => response.json())
    .then(c => {
      const setText = (id, val) => { const el = document.getElementById(id); if (el) el.textContent = val || ''; };
      setText('viewCustomerName', c.name);
      setText('viewCustomerAccount', c.accountNo);
      setText('viewCustomerEmail', c.email);
      setText('viewCustomerPhone', c.phone);
      setText('viewCustomerAddress', c.address);
      new bootstrap.Modal(document.getElementById('customerViewModal')).show();
    })
    .catch(err => {
  console.error('Error loading customer', err);
  showAlert('Error loading customer', 'danger');
});
}

// Edit customer: fetch details, populate modal, show it
function editCustomer(e) {
    const acc = e.target.closest('button').getAttribute('data-acc');
    fetch(`${window.API_BASE}/customers?accNo=${encodeURIComponent(acc)}`)
        .then(res => res.json())
        .then(c => {
            document.getElementById('customerModalLabel').innerText = 'Edit Customer';
      const saveBtn = document.getElementById('saveCustomer');
      if (saveBtn) saveBtn.textContent = 'Update Customer';
            // No customerId needed - using accountNo as identifier
            document.getElementById('accountNo').value = c.accountNo || '';
            document.getElementById('customerName').value = c.name || '';
            document.getElementById('customerEmail').value = c.email || '';
            document.getElementById('customerPhone').value = c.phone || '';
            document.getElementById('customerAddress').value = c.address || '';
            new bootstrap.Modal(document.getElementById('customerModal')).show();
        })
        .catch(err => {
          console.error('Error loading customer', err);
          alert('Error loading customer');
        });
}






// Global Bootstrap delete confirmation handler
let deleteTargetId = null;
function deleteCustomer(e) {
  deleteTargetId = e.target.closest('button').getAttribute('data-acc');
  const modalEl = document.getElementById('deleteConfirmModal');
  const modal = new bootstrap.Modal(modalEl);
  modal.show();
  const confirmBtn = document.getElementById('confirmDeleteBtn');
  // Reset previous listeners
  confirmBtn.replaceWith(confirmBtn.cloneNode(true));
  const newConfirm = document.getElementById('confirmDeleteBtn');
  newConfirm.addEventListener('click', () => {
    if (!deleteTargetId) return;
    fetch(`${window.API_BASE}/customers?accNo=${encodeURIComponent(deleteTargetId)}`, { method: 'DELETE' })
      .then(r => r.json())
      .then(out => {
        if (out.success) {
          showAlert('Customer deleted', 'success');
          fetchCustomers();
        } else {
          showAlert('Failed to delete customer', 'warning');
        }
        bootstrap.Modal.getInstance(modalEl).hide();
        deleteTargetId = null;
      })
      .catch(err => {
        console.error('Delete failed', err);
        showAlert('Error deleting customer', 'danger');
        bootstrap.Modal.getInstance(modalEl).hide();
        deleteTargetId = null;
      });
  });
}
