// BillingController.js - Invoice Generation System
(function(){
  // Set the API base URL to the current origin + context path
  window.API_BASE = window.location.origin + window.location.pathname.split('/').slice(0, -1).join('/');
  console.log('API Base URL set to:', window.API_BASE);
})();

// Global variables
let selectedCustomer = null;
let invoiceItems = [];
let currentInvoiceNumber = null;

// Utility functions
function billingShowAlert(message, type='success'){
  const wrap = document.getElementById('billingAlerts');
  if (!wrap) return alert(message);
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

// Initialize billing form
async function initializeBilling() {
  try {
    // Check for invoice number in URL for editing
    const urlParams = new URLSearchParams(window.location.search);
    const invoiceNumber = urlParams.get('edit');
    
    if (invoiceNumber) {
      // We're in edit mode, load the existing bill
      await loadBillForEditing(invoiceNumber);
    } else {
      // We're creating a new bill
      // Set current date and time
      const now = new Date();
      const localDateTime = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
      const dateTimeField = document.getElementById('invoiceDateTime');
      if (dateTimeField) dateTimeField.value = localDateTime;
      
      // Get next invoice number
      await getNextInvoiceNumber().then(number => {
        currentInvoiceNumber = number;
        const invoiceNumberField = document.getElementById('invoiceNumber');
        if (invoiceNumberField) invoiceNumberField.value = number;
      });
    }
    
    // Clear form
    clearInvoiceForm();
  } catch (error) {
    console.error('Failed to initialize billing:', error);
  }
}

// Load an existing bill for editing
async function loadBillForEditing(invoiceNumber) {
  try {
    console.log(`Loading bill ${invoiceNumber} for editing...`);
    
    const response = await fetch(`${window.API_BASE}/api/bills/invoice/${encodeURIComponent(invoiceNumber)}`, {
      method: 'GET',
      credentials: 'same-origin',
      headers: {
        'Accept': 'application/json',
        'Cache-Control': 'no-cache',
        'Pragma': 'no-cache'
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const bill = await response.json();
    console.log('Loaded bill:', bill);

    // Set the invoice number
    currentInvoiceNumber = bill.invoiceNumber;
    const invoiceNumberField = document.getElementById('invoiceNumber');
    if (invoiceNumberField) invoiceNumberField.value = currentInvoiceNumber;
    
    // Set the invoice date
    const dateTimeField = document.getElementById('invoiceDateTime');
    if (dateTimeField) {
      const billDate = new Date(bill.billDate);
      const localDateTime = new Date(billDate.getTime() - (billDate.getTimezoneOffset() * 60000)).toISOString().slice(0, 16);
      dateTimeField.value = localDateTime;
    }

    // Set the customer
    if (bill.customer) {
      selectCustomer({
        id: bill.customer.id,
        name: bill.customer.name,
        accountNumber: bill.customer.accountNo || bill.customer.accountNumber,
        email: bill.customer.email,
        phone: bill.customer.phone
      });
    }

    // Set the items
    if (bill.items && bill.items.length > 0) {
      invoiceItems = bill.items.map(item => ({
        itemId: item.itemId || item.id,
        itemCode: item.itemCode || item.code || '',
        itemName: item.itemName || item.name || 'Unnamed Item',
        unitPrice: parseFloat(item.unitPrice || item.price || 0),
        quantity: parseInt(item.quantity || 1, 10),
        lineTotal: parseFloat(item.lineTotal || (item.unitPrice * item.quantity) || 0)
      }));
      renderInvoiceItems();
      calculateTotals();
    }

    // Set notes if any
    if (bill.notes) {
      const notesField = document.getElementById('invoiceNotes');
      if (notesField) notesField.value = bill.notes;
    }

    // Update the UI
    const pageTitle = document.getElementById('pageTitle');
    if (pageTitle) {
      pageTitle.textContent = `Edit Invoice #${bill.invoiceNumber}`;
    }
    
    const submitButton = document.getElementById('generateInvoiceBtn');
    if (submitButton) {
      submitButton.textContent = 'Update Invoice';
      submitButton.onclick = () => generateInvoice(true);
    }

    billingShowAlert(`Loaded invoice #${bill.invoiceNumber} for editing`, 'info');
    
  } catch (error) {
    console.error('Error loading bill for editing:', error);
    billingShowAlert(`Failed to load invoice: ${error.message}`, 'danger');
  }
}

// Get next invoice number
async function getNextInvoiceNumber() {
  try {
    const res = await fetch(`${window.API_BASE}/bills?next=true`, { method: 'HEAD' });
    return res.headers.get('X-Next-Invoice-Number');
  } catch (error) {
    console.error('Failed to get next invoice number:', error);
    throw error;
  }
}

// Customer search functionality
async function searchCustomers() {
  const query = document.getElementById('customerSearch').value.trim();
  if (query.length < 1) {
    hideCustomerResults();
    return;
  }

  // If query is very short, show all customers
  if (query.length < 2) {
    await loadAllCustomers();
    return;
  }

  try {
    // Search both account number and name fields
    const url = `${window.API_BASE}/customers?page=1&size=20&acc=${encodeURIComponent(query)}&name=${encodeURIComponent(query)}`;

    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${res.statusText}`);
    }

    const data = await res.json();
    const customers = Array.isArray(data) ? data : data.data || [];

    showCustomerResults(customers);
  } catch (error) {
    console.error('Customer search failed:', error);
    billingShowAlert(`Failed to search customers: ${error.message}`, 'warning');
    hideCustomerResults();
  }
}

async function loadAllCustomers() {
  try {
    const res = await fetch(`${window.API_BASE}/customers?page=1&size=20`);
    if (!res.ok) throw new Error(`HTTP ${res.status}`);

    const data = await res.json();
    const customers = Array.isArray(data) ? data : data.data || [];
    console.log('Loaded all customers:', customers);
    showCustomerResults(customers);
  } catch (error) {
    console.error('Failed to load customers:', error);
    hideCustomerResults();
  }
}

function showCustomerResults(customers) {
  const resultsDiv = document.getElementById('customerSearchResults');
  resultsDiv.innerHTML = '';

  if (customers.length === 0) {
    resultsDiv.innerHTML = '<div class="list-group-item text-muted">No customers found</div>';
  } else {
    customers.forEach(customer => {
      // Handle both possible field name formats
      const accountNo = customer.accountNo || customer.account_no || 'N/A';
      const name = customer.name || 'Unknown';
      const email = customer.email || '';

      const item = document.createElement('div');
      item.className = 'list-group-item list-group-item-action';
      item.innerHTML = `
        <div class="d-flex justify-content-between">
          <div>
            <strong>${name}</strong><br>
            <small class="text-muted">${accountNo}</small>
          </div>
          <small class="text-muted">${email}</small>
        </div>`;
      item.addEventListener('click', () => selectCustomer({
        name: name,
        accountNo: accountNo,
        account_no: accountNo, // Ensure both formats are available
        email: email
      }));
      resultsDiv.appendChild(item);
    });
  }

  resultsDiv.style.display = 'block';
}

function hideCustomerResults() {
  document.getElementById('customerSearchResults').style.display = 'none';
}

function selectCustomer(customer) {
  selectedCustomer = customer;
  document.getElementById('customerSearch').value = '';
  document.getElementById('selectedCustomerName').textContent = customer.name || 'Unknown';
  document.getElementById('selectedCustomerAccount').textContent = customer.accountNo || customer.account_no || 'N/A';
  document.getElementById('selectedCustomer').style.display = 'block';
  hideCustomerResults();
}

function clearSelectedCustomer() {
  selectedCustomer = null;
  document.getElementById('selectedCustomer').style.display = 'none';
  document.getElementById('customerSearch').value = '';
}

// Item search functionality
async function searchItems() {
  const query = document.getElementById('itemSearch').value.trim();
  if (query.length < 2) {
    hideItemResults();
    return;
  }

  try {
    // Search by both item code and name
    const url = `${window.API_BASE}/items?page=1&size=20&code=${encodeURIComponent(query)}&name=${encodeURIComponent(query)}`;

    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${res.statusText}`);
    }

    const data = await res.json();
    const items = Array.isArray(data) ? data : data.data || [];

    showItemResults(items);
  } catch (error) {
    console.error('Item search failed:', error);
    billingShowAlert(`Failed to search items: ${error.message}`, 'warning');
    hideItemResults();
  }
}

function showItemResults(items) {
  const resultsDiv = document.getElementById('itemSearchResults');
  resultsDiv.innerHTML = '';
  
  if (items.length === 0) {
    resultsDiv.innerHTML = '<div class="list-group-item text-muted">No items found</div>';
  } else {
    items.forEach(item => {
      const itemDiv = document.createElement('div');
      itemDiv.className = 'list-group-item list-group-item-action';
      itemDiv.innerHTML = `
        <div class="d-flex justify-content-between">
          <div>
            <strong>${item.name}</strong><br>
            <small class="text-muted">${item.itemCode}</small>
          </div>
          <div class="text-end">
            <strong>LKR ${Number(item.unitPrice).toFixed(2)}</strong><br>
            <small class="text-muted">Qty: ${item.qtyOnHand}</small>
          </div>
        </div>`;
      itemDiv.addEventListener('click', () => addItemToInvoice(item));
      resultsDiv.appendChild(itemDiv);
    });
  }
  
  resultsDiv.style.display = 'block';
}

function hideItemResults() {
  document.getElementById('itemSearchResults').style.display = 'none';
}

function addItemToInvoice(item) {
  // Check if item already exists
  const existingIndex = invoiceItems.findIndex(i => i.itemCode === item.itemCode);
  
  if (existingIndex >= 0) {
    // Increase quantity
    invoiceItems[existingIndex].quantity += 1;
    invoiceItems[existingIndex].lineTotal = invoiceItems[existingIndex].unitPrice * invoiceItems[existingIndex].quantity;
  } else {
    // Add new item
    invoiceItems.push({
      itemCode: item.itemCode,
      itemName: item.name,
      unitPrice: Number(item.unitPrice),
      quantity: 1,
      lineTotal: Number(item.unitPrice)
    });
  }
  
  document.getElementById('itemSearch').value = '';
  hideItemResults();
  renderInvoiceItems();
  calculateTotals();
}

function renderInvoiceItems() {
  const tbody = document.getElementById('invoiceItemsTable');
  const noItemsRow = document.getElementById('noItemsRow');
  
  // Clear existing rows except the "no items" row
  const rows = tbody.querySelectorAll('tr:not(#noItemsRow)');
  rows.forEach(row => row.remove());
  
  if (invoiceItems.length === 0) {
    noItemsRow.style.display = 'table-row';
    return;
  }
  
  noItemsRow.style.display = 'none';
  
  invoiceItems.forEach((item, index) => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${item.itemCode}</td>
      <td>${item.itemName}</td>
      <td>LKR ${item.unitPrice.toFixed(2)}</td>
      <td>
        <input type="number" class="form-control form-control-sm" style="width: 80px;" 
               value="${item.quantity}" min="1" onchange="updateItemQuantity(${index}, this.value)">
      </td>
      <td>LKR ${item.lineTotal.toFixed(2)}</td>
      <td>
        <button class="btn btn-sm btn-outline-danger" onclick="removeItem(${index})">
          <i class="bi bi-trash"></i>
        </button>
      </td>`;
    tbody.appendChild(row);
  });
}

function updateItemQuantity(index, newQuantity) {
  const qty = parseInt(newQuantity, 10);
  if (qty > 0) {
    invoiceItems[index].quantity = qty;
    invoiceItems[index].lineTotal = invoiceItems[index].unitPrice * qty;
    renderInvoiceItems();
    calculateTotals();
  }
}

function removeItem(index) {
  invoiceItems.splice(index, 1);
  renderInvoiceItems();
  calculateTotals();
}

function calculateTotals() {
  const subtotal = invoiceItems.reduce((sum, item) => sum + item.lineTotal, 0);
  const discountRate = parseFloat(document.getElementById('discountRate').value) || 0;
  const discountAmount = subtotal * (discountRate / 100);
  const total = subtotal - discountAmount;

  document.getElementById('invoiceSubtotal').textContent = `LKR ${subtotal.toFixed(2)}`;
  document.getElementById('invoiceDiscount').textContent = `-LKR ${discountAmount.toFixed(2)}`;
  document.getElementById('invoiceTotal').textContent = `LKR ${total.toFixed(2)}`;
}

// Generate invoice
async function generateInvoice() {
  console.log('=== INVOICE GENERATION STARTED ===');

  if (!validateInvoiceForm()) {
    console.log('❌ Validation failed');
    return;
  }

  console.log('✅ Validation passed');
  console.log('Selected customer:', selectedCustomer);
  console.log('Invoice items:', invoiceItems);
  console.log('Current invoice number:', currentInvoiceNumber);

  const billData = {
    invoiceNumber: currentInvoiceNumber,
    customerAccountNo: selectedCustomer.accountNo || selectedCustomer.account_no,
    billDate: document.getElementById('invoiceDateTime').value,
    discountRate: parseFloat(document.getElementById('discountRate').value) || 0,
    notes: document.getElementById('invoiceNotes').value.trim(),
    items: invoiceItems
  };

  console.log('Bill data to send:', billData);
  
  try {
    console.log('📤 Sending POST request to:', `${window.API_BASE}/bills`);
    console.log('📤 Request body:', JSON.stringify(billData, null, 2));

    const res = await fetch(`${window.API_BASE}/bills`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(billData)
    });

    console.log('📥 Fetch completed, response received');

    console.log('📥 Response status:', res.status);
    console.log('📥 Response headers:', res.headers);

    if (!res.ok) {
      const errorText = await res.text();
      console.error('❌ HTTP Error:', res.status, errorText);
      throw new Error(`HTTP ${res.status}: ${errorText}`);
    }

    const result = await res.json();
    console.log('📥 Response data:', result);
    console.log('📥 Response success:', result.success);
    console.log('📥 Response error:', result.error);
    console.log('📥 Response billId:', result.billId);
    console.log('📥 Response invoiceNumber:', result.invoiceNumber);

    if (result.success) {
      billingShowAlert(`Invoice ${result.invoiceNumber} generated successfully!`, 'success');

      // Generate PDF
      console.log('Attempting to generate PDF for invoice:', result.invoiceNumber);
      try {
        generateInvoicePDF(result.invoiceNumber, billData);
        console.log('PDF generation completed');
      } catch (pdfError) {
        console.error('PDF generation failed:', pdfError);
        billingShowAlert('Invoice saved but PDF generation failed', 'warning');
      }

      clearInvoiceForm();
      initializeBilling(); // Get next invoice number
    } else {
      console.log('❌ Invoice generation failed on server');
      console.log('❌ Server error:', result.error);
      billingShowAlert(result.error || 'Failed to generate invoice', 'danger');
    }
  } catch (error) {
    console.error('❌ Invoice generation failed:', error);
    
    let errorMessage = error.message;
    
    // Add more specific error messages for common issues
    if (error.message.includes('Failed to fetch')) {
      errorMessage = 'Could not connect to the server. Please check your internet connection and try again.';
    } else if (error.message.includes('NetworkError')) {
      errorMessage = 'Network error. Please check your connection and try again.';
    } else if (error.message.includes('404')) {
      errorMessage = 'The server endpoint was not found. Please check the API URL configuration.';
    } else if (error.message.includes('500')) {
      errorMessage = 'Server error. Please try again later or contact support.';
    }
    
    billingShowAlert(`Failed to generate invoice: ${errorMessage}`, 'danger');
  }

  console.log('=== INVOICE GENERATION ENDED ===');
}

function validateInvoiceForm() {
  console.log('🔍 Validating invoice form...');
  console.log('Selected customer:', selectedCustomer);
  console.log('Invoice items count:', invoiceItems.length);

  if (!selectedCustomer) {
    console.log('❌ No customer selected');
    billingShowAlert('Please select a customer', 'warning');
    return false;
  }

  if (invoiceItems.length === 0) {
    console.log('❌ No items added');
    billingShowAlert('Please add at least one item to the invoice', 'warning');
    return false;
  }

  const discountRate = parseFloat(document.getElementById('discountRate').value) || 0;
  console.log('Discount rate:', discountRate);

  if (discountRate < 0 || discountRate > 100) {
    console.log('❌ Invalid discount rate');
    billingShowAlert('Discount rate must be between 0 and 100', 'warning');
    return false;
  }

  console.log('✅ Validation passed');
  return true;
}

function clearInvoiceForm() {
  selectedCustomer = null;
  invoiceItems = [];
  document.getElementById('selectedCustomer').style.display = 'none';
  document.getElementById('customerSearch').value = '';
  document.getElementById('itemSearch').value = '';
  document.getElementById('discountRate').value = '0';
  document.getElementById('invoiceNotes').value = '';
  hideCustomerResults();
  hideItemResults();
  renderInvoiceItems();
  calculateTotals();
}

// Event listeners
document.addEventListener('DOMContentLoaded', () => {
  // Initialize billing when page loads
  initializeBilling();

  // Test API connectivity
  testAPIConnectivity();

  // Check if jsPDF is loaded
  if (window.jspdf) {
    console.log('✅ jsPDF library loaded successfully');
    // Test PDF generation capability
    try {
      const { jsPDF } = window.jspdf;
      const testDoc = new jsPDF();
      console.log('✅ PDF document creation test passed');
    } catch (testError) {
      console.error('❌ PDF document creation test failed:', testError);
    }
  } else {
    console.error('❌ jsPDF library not loaded');
  }
  
  // Customer search
  const customerSearch = document.getElementById('customerSearch');
  const searchCustomerBtn = document.getElementById('searchCustomerBtn');
  const clearCustomerBtn = document.getElementById('clearCustomerBtn');
  
  if (customerSearch) {
    customerSearch.addEventListener('input', searchCustomers);
    customerSearch.addEventListener('focus', searchCustomers);
  }
  if (searchCustomerBtn) searchCustomerBtn.addEventListener('click', searchCustomers);
  if (clearCustomerBtn) clearCustomerBtn.addEventListener('click', clearSelectedCustomer);
  
  // Item search
  const itemSearch = document.getElementById('itemSearch');
  const searchItemBtn = document.getElementById('searchItemBtn');
  
  if (itemSearch) {
    itemSearch.addEventListener('input', searchItems);
    itemSearch.addEventListener('focus', searchItems);
  }
  if (searchItemBtn) searchItemBtn.addEventListener('click', searchItems);
  
  // Discount rate change
  const discountRate = document.getElementById('discountRate');
  if (discountRate) discountRate.addEventListener('input', calculateTotals);
  
  // Action buttons
  const generateBtn = document.getElementById('generateInvoiceBtn');
  const clearBtn = document.getElementById('clearInvoiceBtn');
  const testPdfBtn = document.getElementById('testPdfBtn');
  const testPostBtn = document.getElementById('testPostBtn');

  if (generateBtn) generateBtn.addEventListener('click', generateInvoice);
  if (clearBtn) clearBtn.addEventListener('click', clearInvoiceForm);
  if (testPdfBtn) testPdfBtn.addEventListener('click', testPdfGeneration);
  if (testPostBtn) testPostBtn.addEventListener('click', testPostRequest);
  
  // Hide dropdowns when clicking outside
  document.addEventListener('click', (e) => {
    if (!e.target.closest('#customerSearch') && !e.target.closest('#customerSearchResults')) {
      hideCustomerResults();
    }
    if (!e.target.closest('#itemSearch') && !e.target.closest('#itemSearchResults')) {
      hideItemResults();
    }
  });
});

// PDF Generation
function generateInvoicePDF(invoiceNumber, billData) {
  console.log('PDF Generation started for:', invoiceNumber);
  console.log('Bill data:', billData);

  try {
    // Check if jsPDF is available
    if (!window.jspdf) {
      throw new Error('jsPDF library not loaded');
    }

    console.log('jsPDF library found');
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    console.log('PDF document created');

    // Company Header
    doc.setFontSize(20);
    doc.setFont(undefined, 'bold');
    doc.text('Pahana Edu Book Store', 20, 20);

    doc.setFontSize(12);
    doc.setFont(undefined, 'normal');
    doc.text('Educational Supplies & Services', 20, 30);
    doc.text('Colombo 04', 20, 40);
    doc.text('Phone: +94 11 234 5678', 20, 50);
    doc.text('Email: info@pahanaedu.lk', 20, 60);

    // Invoice Title
    doc.setFontSize(16);
    doc.setFont(undefined, 'bold');
    doc.text('INVOICE', 150, 20);

    // Invoice Details
    doc.setFontSize(10);
    doc.setFont(undefined, 'normal');
    doc.text(`Invoice #: ${invoiceNumber}`, 150, 35);
    doc.text(`Date: ${new Date(billData.billDate).toLocaleDateString()}`, 150, 45);
    doc.text(`Status: ${billData.status || 'Paid'}`, 150, 55);

    // Customer Details
    doc.setFontSize(12);
    doc.setFont(undefined, 'bold');
    doc.text('Bill To:', 20, 70);

    doc.setFontSize(10);
    doc.setFont(undefined, 'normal');
    doc.text(`${selectedCustomer.name}`, 20, 80);
    doc.text(`Account: ${selectedCustomer.accountNo || selectedCustomer.account_no}`, 20, 90);
    if (selectedCustomer.email) {
      doc.text(`Email: ${selectedCustomer.email}`, 20, 100);
    }

    // Items Table Header
    let yPos = 120;
    doc.setFontSize(10);
    doc.setFont(undefined, 'bold');
    doc.text('Item Code', 20, yPos);
    doc.text('Description', 60, yPos);
    doc.text('Unit Price', 120, yPos);
    doc.text('Qty', 150, yPos);
    doc.text('Total', 170, yPos);

    // Draw line under header
    doc.line(20, yPos + 2, 190, yPos + 2);

    // Items
    yPos += 10;
    doc.setFont(undefined, 'normal');
    billData.items.forEach(item => {
      doc.text(item.itemCode, 20, yPos);
      doc.text(item.itemName.substring(0, 25), 60, yPos); // Truncate long names
      doc.text(`LKR ${item.unitPrice.toFixed(2)}`, 120, yPos);
      doc.text(item.quantity.toString(), 150, yPos);
      doc.text(`LKR ${item.lineTotal.toFixed(2)}`, 170, yPos);
      yPos += 8;
    });

    // Totals
    yPos += 10;
    doc.line(120, yPos, 190, yPos); // Line above totals
    yPos += 10;

    const subtotal = billData.items.reduce((sum, item) => sum + item.lineTotal, 0);
    const discountAmount = subtotal * (billData.discountRate / 100);
    const total = subtotal - discountAmount;

    doc.text('Subtotal:', 120, yPos);
    doc.text(`LKR ${subtotal.toFixed(2)}`, 170, yPos);
    yPos += 8;

    if (billData.discountRate > 0) {
      doc.text(`Discount (${billData.discountRate}%):`, 120, yPos);
      doc.text(`-LKR ${discountAmount.toFixed(2)}`, 170, yPos);
      yPos += 8;
    }

    doc.setFont(undefined, 'bold');
    doc.text('Total Amount:', 120, yPos);
    doc.text(`LKR ${total.toFixed(2)}`, 170, yPos);

    // Notes
    if (billData.notes && billData.notes.trim()) {
      yPos += 20;
      doc.setFont(undefined, 'bold');
      doc.text('Notes:', 20, yPos);
      doc.setFont(undefined, 'normal');
      yPos += 8;
      const notes = doc.splitTextToSize(billData.notes, 170);
      doc.text(notes, 20, yPos);
    }

    // Footer
    doc.setFontSize(8);
    doc.setFont(undefined, 'italic');
    doc.text('Thank you for your business!', 20, 280);
    doc.text(`Generated on ${new Date().toLocaleString()}`, 20, 285);

    // Save PDF
    doc.save(`Invoice_${invoiceNumber}.pdf`);

  } catch (error) {
    console.error('PDF generation failed:', error);
    billingShowAlert('Failed to generate PDF. Invoice saved successfully.', 'warning');
  }
}

// Test API connectivity
async function testAPIConnectivity() {
  console.log('=== BILLING SYSTEM DEBUG ===');
  console.log('API Base URL:', window.API_BASE);

  // Test customers endpoint
  try {
    const customerRes = await fetch(`${window.API_BASE}/customers?page=1&size=3`);
    console.log('✅ Customers API Status:', customerRes.status);
    if (customerRes.ok) {
      const customerData = await customerRes.json();
      console.log('✅ Customers Data:', customerData);
    }
  } catch (error) {
    console.error('❌ Customers API Error:', error);
  }

  // Test items endpoint
  try {
    const itemRes = await fetch(`${window.API_BASE}/items?page=1&size=3`);
    console.log('✅ Items API Status:', itemRes.status);
    if (itemRes.ok) {
      const itemData = await itemRes.json();
      console.log('✅ Items Data:', itemData);
    }
  } catch (error) {
    console.error('❌ Items API Error:', error);
  }

  // Test bills endpoint
  try {
    const billRes = await fetch(`${window.API_BASE}/bills?next=true`, { method: 'HEAD' });
    console.log('✅ Bills API Status:', billRes.status);
    const nextInvoice = billRes.headers.get('X-Next-Invoice-Number');
    console.log('✅ Next Invoice Number:', nextInvoice);

    // Test BillServlet test endpoint
    const testRes = await fetch(`${window.API_BASE}/bills?test=true`);
    console.log('✅ Bills Test Endpoint Status:', testRes.status);
    if (testRes.ok) {
      const testData = await testRes.json();
      console.log('✅ Bills Test Response:', testData);
    }
  } catch (error) {
    console.error('❌ Bills API Error:', error);
  }

  console.log('=== END DEBUG ===');
}

// Test PDF generation
function testPdfGeneration() {
  console.log('Testing PDF generation...');

  try {
    if (!window.jspdf) {
      throw new Error('jsPDF library not available');
    }

    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();

    // Simple test PDF
    doc.setFontSize(20);
    doc.text('Test PDF Generation', 20, 20);
    doc.setFontSize(12);
    doc.text('This is a test PDF to verify jsPDF is working correctly.', 20, 40);
    doc.text('Generated at: ' + new Date().toLocaleString(), 20, 60);

    // Save the PDF
    doc.save('test-pdf.pdf');

    billingShowAlert('Test PDF generated successfully!', 'success');
    console.log('Test PDF generation completed');

  } catch (error) {
    console.error('Test PDF generation failed:', error);
    billingShowAlert('Test PDF generation failed: ' + error.message, 'danger');
  }
}

// Test POST request to BillServlet
async function testPostRequest() {
  console.log('=== TESTING POST REQUEST ===');

  const testBillData = {
    invoiceNumber: "TEST-2025-08-00001",
    customerAccountNo: "ACC0001",
    billDate: new Date().toISOString().slice(0, 16),
    discountRate: 5.0,
    notes: "Test invoice",
    items: [
      {
        itemCode: "ITM0001",
        itemName: "Test Item",
        unitPrice: 100.00,
        quantity: 2,
        lineTotal: 200.00
      }
    ]
  };

  try {
    console.log('📤 Sending test POST request...');
    console.log('📤 Test data:', testBillData);

    const res = await fetch(`${window.API_BASE}/bills`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(testBillData)
    });

    console.log('📥 Response status:', res.status);

    if (!res.ok) {
      const errorText = await res.text();
      console.error('❌ Error response:', errorText);
      billingShowAlert(`POST test failed: ${res.status} - ${errorText}`, 'danger');
      return;
    }

    const result = await res.json();
    console.log('📥 Response data:', result);

    if (result.success) {
      billingShowAlert(`POST test successful! Bill ID: ${result.billId}`, 'success');
    } else {
      billingShowAlert(`POST test failed: ${result.error}`, 'danger');
    }

  } catch (error) {
    console.error('❌ POST test error:', error);
    billingShowAlert(`POST test error: ${error.message}`, 'danger');
  }

  console.log('=== POST TEST COMPLETED ===');
}

// Export functions for inline event handlers
window.updateItemQuantity = updateItemQuantity;
window.removeItem = removeItem;
