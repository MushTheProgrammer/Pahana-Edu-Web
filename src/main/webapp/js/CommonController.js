// Toggle sidebar on mobile
document.getElementById('sidebarToggle').addEventListener('click', function() {
    document.getElementById('sidebar').classList.toggle('active');
    document.getElementById('content').classList.toggle('active');
});

// Close sidebar when clicking the close button
document.getElementById('sidebarCollapse').addEventListener('click', function() {
    document.getElementById('sidebar').classList.remove('active');
    document.getElementById('content').classList.remove('active');
});

// Simple navigation between sections
document.querySelectorAll('.sidebar-menu a').forEach(link => {
    link.addEventListener('click', function(e) {
        e.preventDefault();

        // Hide all sections
        document.querySelectorAll('.content-section').forEach(section => {
            section.style.display = 'none';
        });

        // Show the selected section
        const targetId = this.getAttribute('href').substring(1);
        document.getElementById(targetId).style.display = 'block';
        if (targetId === 'customers') {
            // Reset modal title/button for Add flow
            const modalTitle = document.getElementById('customerModalLabel');
            if (modalTitle) modalTitle.innerText = 'Add New Customer';
            const saveBtn = document.getElementById('saveCustomer');
            if (saveBtn) saveBtn.textContent = 'Save Customer';
        }

        // Update active link
        document.querySelectorAll('.sidebar-menu a').forEach(item => {
            item.classList.remove('active');
        });
        this.classList.add('active');

        // Close sidebar on mobile after selection
// Reset modal when opening Add Customer
const addBtn = document.getElementById('addCustomerBtn');
if (addBtn) {
  addBtn.addEventListener('click', () => {
    const form = document.getElementById('customerForm');
    if (form) form.reset();
    const idEl = document.getElementById('customerId');
    if (idEl) idEl.value = '';
    const title = document.getElementById('customerModalLabel');
    if (title) title.textContent = 'Add New Customer';
    const saveBtn = document.getElementById('saveCustomer');
    if (saveBtn) saveBtn.textContent = 'Save Customer';
  });
}

// Also clear on modal hide to avoid stale data
const modalEl = document.getElementById('customerModal');
if (modalEl) {
  modalEl.addEventListener('hidden.bs.modal', () => {
    const form = document.getElementById('customerForm');
    if (form) form.reset();
    const idEl = document.getElementById('customerId');
    if (idEl) idEl.value = '';
    const saveBtn = document.getElementById('saveCustomer');
    if (saveBtn) saveBtn.textContent = 'Save Customer';
  });
}

        if (window.innerWidth < 768) {
            document.getElementById('sidebar').classList.remove('active');
            document.getElementById('content').classList.remove('active');
        }
    });
});

