// BillManagementController.js - Bill/Invoice Management System
(function(){
  const parts = window.location.pathname.split('/').filter(Boolean);
  const ctx = parts.length > 0 ? '/' + parts[0] : '';
  window.API_BASE = ctx;
})();

// Global variables for bill management
window.BILL_PAGE = 1;
window.BILL_SIZE = 10;
window.BILL_SORT = 'bill_date';
window.BILL_DIR = 'desc';
window.BILL_TOTAL = 0;

// Utility functions
function billMgmtShowAlert(message, type='success'){
  const wrap = document.getElementById('billAlerts');
  if (!wrap) {
    // Fallback to billing alerts if bill alerts not found
    const fallback = document.getElementById('billingAlerts');
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

function billQs(params){
  const entries = Object.entries(params).filter(([_,v]) => v !== undefined && v !== null && String(v).trim() !== '');
  if (!entries.length) return '';
  const usp = new URLSearchParams();
  entries.forEach(([k,v]) => usp.append(k, String(v).trim()));
  return `?${usp.toString()}`;
}

// Fetch bills with filters and pagination
async function fetchBills(){
  try {
    console.log('Fetching bills...');
    const searchBillNumber = document.getElementById('searchBillNumber')?.value.trim() || '';
    
    // If we have an invoice number, search specifically for it
    if (searchBillNumber) {
      console.log('Searching for invoice number:', searchBillNumber);
      
      // First try with the exact number as entered
      let response = await fetch(`${window.API_BASE}/bills?invoiceNumber=${encodeURIComponent(searchBillNumber)}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        },
        credentials: 'same-origin'
      });
      
      let data = [];
      
      if (response.ok) {
        const result = await response.json();
        console.log('Invoice search result:', result);
        
        // Handle different response formats
        if (result.invoiceNumber) {
          // Single bill response
          data = [result];
        } else if (result.data && Array.isArray(result.data)) {
          // Paginated response
          data = result.data;
        } else if (Array.isArray(result)) {
          // Direct array response
          data = result;
        }
      }
      
      // If no results and the number has leading zeros, try without them
      if (data.length === 0 && /^0+/.test(searchBillNumber)) {
        const trimmedNumber = searchBillNumber.replace(/^0+/, '');
        if (trimmedNumber) {  // Only proceed if there are digits left after removing zeros
          console.log('Trying without leading zeros:', trimmedNumber);
          
          const response2 = await fetch(`${window.API_BASE}/bills?invoiceNumber=${encodeURIComponent(trimmedNumber)}`, {
            method: 'GET',
            headers: {
              'Accept': 'application/json',
              'Content-Type': 'application/json'
            },
            credentials: 'same-origin'
          });
          
          if (response2.ok) {
            const result = await response2.json();
            console.log('Trimmed invoice search result:', result);
            
            if (result.invoiceNumber) {
              data = [result];
            } else if (result.data && Array.isArray(result.data)) {
              data = result.data;
            } else if (Array.isArray(result)) {
              data = result;
            }
          }
        }
      }
      
      if (data.length === 0) {
        console.log('No matching invoices found');
        billMgmtShowAlert('No bills found matching the invoice number', 'info');
      }
      
      renderBillTable(data);
      return;
    }
    
    // Regular search with other filters
    const defaultParams = { 
      page: window.BILL_PAGE || 1, 
      size: window.BILL_SIZE || 10, 
      sort: window.BILL_SORT || 'bill_date', 
      dir: window.BILL_DIR || 'desc' 
    };
    
    const searchParams = {
      customer: document.getElementById('searchBillCustomer')?.value || '',
      status: document.getElementById('searchBillStatus')?.value || '',
      fromDate: document.getElementById('searchBillFromDate')?.value || '',
      toDate: document.getElementById('searchBillToDate')?.value || ''
    };
    
    const allParams = { ...defaultParams, ...searchParams };
    const queryParams = billQs(allParams);
    
    console.log('Fetching from:', `${window.API_BASE}/bills${queryParams}`);
    
    const response = await fetch(`${window.API_BASE}/bills${queryParams}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin'
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error response:', errorText);
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const payload = await response.json();
    console.log('Received data:', payload);
    
    const data = Array.isArray(payload) ? payload : (payload.data || []);
    
    if (data.length === 0) {
      console.log('No bills found');
      billMgmtShowAlert('No bills found matching your criteria', 'info');
    }
    
    renderBillTable(data);
    
    if (!Array.isArray(payload) && payload.page && payload.size && payload.total) {
      renderBillPagination(payload.page, payload.size, payload.total);
    }
  } catch (error) {
    console.error('Failed to fetch bills:', error);
    billMgmtShowAlert('Failed to load bills', 'danger');
  }
}

function renderBillTable(bills){
  const tbody = document.getElementById('billTable');
  if (!tbody) return;
  
  tbody.innerHTML = '';
  
  bills.forEach(bill => {
    const tr = document.createElement('tr');
    const billDate = new Date(bill.billDate);
    const formattedDate = billDate.toLocaleDateString() + ' ' + billDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
    
    tr.innerHTML = `
      <td>${bill.invoiceNumber}</td>
      <td>${bill.customerName || 'Unknown'} (${bill.customerAccountNo})</td>
      <td>${formattedDate}</td>
      <td>${bill.itemCount || 0} items</td>
      <td>$${Number(bill.totalAmount).toFixed(2)}</td>
      <td>${getStatusBadge(bill.status)}</td>
      <td class="text-nowrap">
        <button class="btn btn-sm btn-outline-primary me-1" title="View Invoice" onclick="viewBill('${bill.invoiceNumber}')">
          <i class="bi bi-eye"></i>
        </button>
        <button class="btn btn-sm btn-outline-danger me-1" title="Delete Bill" onclick="deleteBill('${bill.invoiceNumber}')">
          <i class="bi bi-trash"></i>
        </button>
        <button class="btn btn-sm btn-outline-success me-1" title="Update Status" onclick="updateBillStatus(${bill.billId}, '${bill.status}')">
          <i class="bi bi-check-circle"></i>
        </button>
      </td>`;
    tbody.appendChild(tr);
  });
}

function getStatusBadge(status) {
  const badges = {
    'paid': '<span class="badge bg-success">Paid</span>',
    'overdue': '<span class="badge bg-danger">Overdue</span>',
    'cancelled': '<span class="badge bg-dark">Cancelled</span>'
  };
  return badges[status] || '<span class="badge bg-light text-dark">Unknown</span>';
}

function renderBillPagination(page, size, total){
  window.BILL_PAGE = page; 
  window.BILL_SIZE = size; 
  window.BILL_TOTAL = total;
  
  const ul = document.querySelector('#orders .pagination');
  const summary = document.querySelector('#orders .bills-summary');
  
  if (summary) {
    const start = total === 0 ? 0 : (page - 1) * size + 1;
    const end = Math.min(total, page * size);
    summary.textContent = `Showing ${start} to ${end} of ${total} entries`;
  }
  
  if (!ul) return;
  
  ul.innerHTML = '';
  const createPageItem = (label, p, disabled=false, active=false) => {
    const li = document.createElement('li'); 
    li.className = `page-item${disabled?' disabled':''}${active?' active':''}`;
    const a = document.createElement('a'); 
    a.className = 'page-link'; 
    a.href = '#'; 
    a.textContent = label;
    a.addEventListener('click', (e) => { 
      e.preventDefault(); 
      if (disabled||active) return; 
      window.BILL_PAGE = p; 
      fetchBills(); 
    });
    li.appendChild(a); 
    return li;
  };
  
  const pages = Math.max(1, Math.ceil(total / size));
  ul.appendChild(createPageItem('Previous', Math.max(1, page-1), page===1));
  
  const windowSize = 5; 
  const start = Math.max(1, page - Math.floor(windowSize/2)); 
  const end = Math.min(pages, start + windowSize - 1);
  
  for (let i=start; i<=end; i++) {
    ul.appendChild(createPageItem(String(i), i, false, i===page));
  }
  
  ul.appendChild(createPageItem('Next', Math.min(pages, page+1), page===pages));
}

// Bill actions
async function viewBill(invoiceNumber) {
  try {
    console.log('Fetching bill for invoice:', invoiceNumber);
    
    // Show loading state
    billMgmtShowAlert('Loading bill details...', 'info');
    
    // Try to get the bill by invoice number
    const response = await fetch(`${window.API_BASE}/bills?invoiceNumber=${encodeURIComponent(invoiceNumber)}`, {
      method: 'GET',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      credentials: 'same-origin'
    });
    
    if (!response.ok) {
      const errorText = await response.text();
      console.error('Error response:', errorText);
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    
    const result = await response.json();
    console.log('API Response:', result);
    
    // Handle the response format from the backend
    let bill;
    if (result.data && Array.isArray(result.data) && result.data.length > 0) {
      // Response is in the format: { data: [...], page: 1, size: 10, total: 1 }
      bill = result.data[0];
    } else if (result.invoiceNumber) {
      // Direct bill object response
      bill = result;
    } else {
      throw new Error('Unexpected response format from server');
    }
    
    if (!bill) {
      throw new Error('Bill not found');
    }
    
    console.log('Bill data loaded successfully:', bill);
    showBillDetailsModal(bill);
  } catch (error) {
    console.error('Failed to load bill details:', error);
    billMgmtShowAlert(`Failed to load bill details: ${error.message}`, 'danger');
  }
}

// Global variables for pagination and search
let currentPage = 1;
let currentSearch = '';

// Store the current invoice number to be deleted
let currentInvoiceToDelete = null;

async function deleteBill(invoiceNumber) {
  try {
    // Store the invoice number for use in the confirmation
    currentInvoiceToDelete = invoiceNumber;
    
    // Set the invoice number in the modal
    document.getElementById('invoiceNumberToDelete').textContent = invoiceNumber;
    
    // Show the confirmation modal
    const modal = new bootstrap.Modal(document.getElementById('billDeleteConfirmModal'));
    modal.show();
  } catch (error) {
    console.error('Error showing delete confirmation:', error);
    billMgmtShowAlert('Error preparing delete confirmation', 'danger');
  }
}

// Handle the actual deletion when confirmed
async function confirmBillDeletion() {
  if (!currentInvoiceToDelete) {
    console.error('No invoice selected for deletion');
    return false;
  }
  
  const invoiceNumber = currentInvoiceToDelete;
  const modal = bootstrap.Modal.getInstance(document.getElementById('billDeleteConfirmModal'));
  const confirmBtn = document.getElementById('confirmBillDeleteBtn');
  
  if (!confirmBtn) {
    console.error('Confirm button not found');
    return false;
  }
  
  // Disable the confirm button to prevent multiple clicks
  confirmBtn.disabled = true;
  confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Deleting...';
  
  try {
    // Get the current page's base URL
    const baseUrl = window.location.origin;
    const contextPath = window.location.pathname.split('/')[1] || '';
    const apiPath = '/bills';
    
    // Construct the full URL
    const url = `${baseUrl}/${contextPath}${apiPath}?invoiceNumber=${encodeURIComponent(invoiceNumber)}`;
    
    console.log('Base URL:', baseUrl);
    console.log('Context Path:', contextPath);
    console.log('Attempting to delete invoice at URL:', url);
    
    const response = await fetch(url, {
      method: 'DELETE',
      headers: {
        'Accept': 'application/json'
      },
      credentials: 'same-origin' // Include cookies for session management
    });

    if (!response.ok) {
      let errorData;
      try {
        errorData = await response.json();
      } catch (e) {
        errorData = { error: `HTTP error! status: ${response.status}` };
      }
      throw new Error(errorData.error || `HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    
    // Show success message
    billMgmtShowAlert(data.message || `Invoice ${invoiceNumber} has been deleted successfully.`, 'success');
    
    // Close the delete confirmation modal
    if (modal) {
      modal.hide();
    }
    
    // Close the bill details modal if open
    const billDetailsModal = document.getElementById('billDetailsModal');
    if (billDetailsModal) {
      const billModal = bootstrap.Modal.getInstance(billDetailsModal);
      if (billModal) {
        billModal.hide();
      }
    }
    
    // Reset the confirm button state
    confirmBtn.disabled = false;
    confirmBtn.textContent = 'Delete';
    
    // Refresh the bills list
    await fetchBills();
    return true;
    
  } catch (error) {
    console.error('Error deleting bill:', error);
    const errorMessage = error.message.includes('Failed to fetch') 
      ? 'Network error: Could not connect to server' 
      : error.message;
    billMgmtShowAlert(`Failed to delete invoice: ${errorMessage}`, 'danger');
    
    // Reset the confirm button state on error
    if (confirmBtn) {
      confirmBtn.disabled = false;
      confirmBtn.textContent = 'Delete';
    }
    
    return false;
  } finally {
    // Clear the current invoice to delete
    currentInvoiceToDelete = null;
  }
}

function showBillDetailsModal(bill) {
  try {
    console.log('Showing bill details for:', bill);
    
    // Format the date
    const billDate = bill.billDate ? new Date(bill.billDate) : new Date();
    const formattedDate = billDate.toLocaleDateString() + ' ' + billDate.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
    
    // Create items HTML
    let itemsHtml = '';
    if (bill.items && bill.items.length > 0) {
      itemsHtml = bill.items.map(item => `
        <tr>
          <td>${item.itemCode || item.code || 'N/A'}</td>
          <td>${item.itemName || item.name || 'N/A'}</td>
          <td>LKR ${Number(item.unitPrice || item.price || 0).toFixed(2)}</td>
          <td>${item.quantity || 1}</td>
          <td>LKR ${Number(item.lineTotal || ((item.unitPrice || item.price || 0) * (item.quantity || 1))).toFixed(2)}</td>
        </tr>
      `).join('');
    } else {
      itemsHtml = '<tr><td colspan="5" class="text-center">No items found</td></tr>';
    }
  
  // Calculate totals if not provided
  const subtotal = bill.subtotal || (bill.items || []).reduce((sum, item) => 
    sum + (Number(item.unitPrice || item.price || 0) * (item.quantity || 1)), 0);
  
  const discountRate = bill.discountRate || 0;
  const discountAmount = bill.discountAmount || (subtotal * (discountRate / 100));
  const total = bill.totalAmount || (subtotal - discountAmount);
  
  const modalHtml = `
  <div class="modal fade" id="billDetailsModal" tabindex="-1" aria-labelledby="billDetailsModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
      <div class="modal-content">
        <div class="modal-header bg-light">
          <h5 class="modal-title" id="billDetailsModalLabel">Invoice #${bill.invoiceNumber || 'N/A'}</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body">
          <div class="row mb-4">
            <div class="col-md-6">
              <h6>Bill To:</h6>
              <p class="mb-1">${bill.customerName || 'N/A'}</p>
              <p class="mb-1 text-muted small">${bill.customerAccountNo || ''}</p>
              <p class="mb-1 text-muted small">${bill.customerEmail || ''}</p>
              <p class="mb-0 text-muted small">${bill.customerPhone || ''}</p>
            </div>
            <div class="col-md-6 text-md-end">
              <p class="mb-1"><strong>Invoice #:</strong> ${bill.invoiceNumber || 'N/A'}</p>
              <p class="mb-1"><strong>Date:</strong> ${formattedDate}</p>
              <p class="mb-1"><strong>Status:</strong> ${getStatusBadge(bill.status || 'Paid')}</p>
              ${discountRate > 0 ? `<p class="mb-1"><strong>Discount:</strong> ${discountRate}%</p>` : ''}
            </div>
          </div>
          
          <div class="table-responsive">
            <table class="table table-sm table-bordered">
              <thead class="table-light">
                <tr>
                  <th>Code</th>
                  <th>Description</th>
                  <th class="text-end">Unit Price</th>
                  <th class="text-center">Qty</th>
                  <th class="text-end">Total</th>
                </tr>
              </thead>
              <tbody>
                ${itemsHtml}
              </tbody>
              <tfoot>
                <tr>
                  <td colspan="4" class="text-end"><strong>Subtotal:</strong></td>
                  <td class="text-end">LKR ${subtotal.toFixed(2)}</td>
                </tr>
                ${discountAmount > 0 ? `
                <tr>
                  <td colspan="4" class="text-end"><strong>Discount (${discountRate}%):</strong></td>
                  <td class="text-end">-LKR ${discountAmount.toFixed(2)}</td>
                </tr>` : ''}
                <tr>
                  <td colspan="4" class="text-end"><strong>Total:</strong></td>
                  <td class="text-end"><strong>LKR ${total.toFixed(2)}</strong></td>
                </tr>
              </tfoot>
            </table>
          </div>
          
          ${bill.notes ? `
          <div class="mt-3">
            <h6>Notes:</h6>
            <div class="border rounded p-2 bg-light">
              ${bill.notes}
            </div>
          </div>` : ''}
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
            <i class="bi bi-x-circle me-1"></i> Close
          </button>
          <button type="button" class="btn btn-primary" id="printBillBtn" data-invoice-number="${bill.invoiceNumber || ''}">
            <i class="bi bi-printer me-1"></i> Print
          </button>
        </div>
      </div>
    </div>
  </div>`;
  
    // Remove existing modal if any
    const existingModal = document.getElementById('billDetailsModal');
    if (existingModal) {
      const modal = bootstrap.Modal.getInstance(existingModal);
      if (modal) {
        modal.hide();
        modal.dispose();
      }
      existingModal.remove();
    }
    
    // Add modal to body
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    // Initialize and show the modal
    const modalElement = document.getElementById('billDetailsModal');
    if (modalElement) {
      const modal = new bootstrap.Modal(modalElement);
      
      // Show the modal immediately after initialization
      modal.show();
      
      // Add event listeners after the modal is in the DOM
      const printBtn = modalElement.querySelector('#printBillBtn');
      if (printBtn) {
        printBtn.addEventListener('click', function() {
          printBill(bill.invoiceNumber);
        });
      }
      
      // Handle modal close
      modalElement.addEventListener('hidden.bs.modal', function onModalHidden() {
        // Clean up event listeners
        if (printBtn) {
          printBtn.removeEventListener('click', printBill);
        }
        modalElement.removeEventListener('hidden.bs.modal', onModalHidden);
        
        // Remove the modal from DOM after it's hidden
        setTimeout(() => {
          if (document.body.contains(modalElement)) {
            modal.dispose();
            modalElement.remove();
          }
        }, 300);
      });

      // Add event listener for print button
      modalElement.addEventListener('shown.bs.modal', function() {
        const printBtn = document.getElementById('printBillBtn');
        if (printBtn) {
          printBtn.addEventListener('click', async function() {
            try {
              // Show loading state
              const originalBtnText = printBtn.innerHTML;
              printBtn.disabled = true;
              printBtn.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Generating PDF...';
              
              // Initialize jsPDF
              const { jsPDF } = window.jspdf;
              const doc = new jsPDF();
              
              // Add logo (if available)
              // doc.addImage(logo, 'PNG', 10, 10, 50, 20);
              
              // Set document properties
              doc.setProperties({
                title: `Invoice ${bill.invoiceNumber || ''}`,
                subject: 'Invoice',
                author: 'Pahana Edu',
                creator: 'Pahana Edu System'
              });
              
              // Add header
              doc.setFontSize(20);
              doc.setFont('helvetica', 'bold');
              doc.text('INVOICE', 105, 20, { align: 'center' });
              
              // Add invoice number and date
              doc.setFontSize(10);
              doc.setFont('helvetica', 'normal');
              doc.text(`Invoice #: ${bill.invoiceNumber || 'N/A'}`, 15, 35);
              doc.text(`Date: ${new Date(bill.invoiceDate || new Date()).toLocaleDateString()}`, 15, 42);
              
              // Add customer information
              doc.setFont('helvetica', 'bold');
              doc.text('Bill To:', 15, 60);
              doc.setFont('helvetica', 'normal');
              doc.text(bill.customerName || 'N/A', 15, 67);
              if (bill.customerAccountNo) {
                doc.text(`Account: ${bill.customerAccountNo}`, 15, 74);
              }
              if (bill.customerEmail) {
                doc.text(`Email: ${bill.customerEmail}`, 15, 81);
              }
              
              // Add items table header
              doc.setFont('helvetica', 'bold');
              doc.text('Description', 15, 100);
              doc.text('Qty', 130, 100);
              doc.text('Unit Price', 150, 100, { align: 'right' });
              doc.text('Total', 180, 100, { align: 'right' });
              
              // Add items
              doc.setFont('helvetica', 'normal');
              let y = 110;
              const items = bill.items || [];
              
              items.forEach((item, index) => {
                if (y > 250) { // Add new page if running out of space
                  doc.addPage();
                  y = 20;
                }
                
                const itemName = item.itemName || item.name || 'N/A';
                const quantity = item.quantity || 1;
                const unitPrice = parseFloat(item.unitPrice || item.price || 0).toFixed(2);
                const total = (quantity * parseFloat(unitPrice)).toFixed(2);
                
                // Split long item names into multiple lines
                const splitText = doc.splitTextToSize(itemName, 80);
                doc.text(splitText, 15, y);
                doc.text(quantity.toString(), 130, y);
                doc.text(unitPrice, 150, y, { align: 'right' });
                doc.text(total, 180, y, { align: 'right' });
                
                y += Math.max(10, splitText.length * 7); // Adjust y position based on text height
              });
              
              // Add totals
              const subtotal = bill.subtotal || items.reduce((sum, item) => 
                sum + (parseFloat(item.unitPrice || item.price || 0) * (item.quantity || 1)), 0);
              const discountRate = bill.discountRate || 0;
              const discountAmount = bill.discountAmount || (subtotal * (discountRate / 100));
              const total = bill.totalAmount || (subtotal - discountAmount);
              
              y = Math.max(y + 10, 250);
              doc.setFont('helvetica', 'bold');
              doc.text('Subtotal:', 150, y, { align: 'right' });
              doc.text(`LKR ${subtotal.toFixed(2)}`, 180, y, { align: 'right' });
              
              if (discountAmount > 0) {
                y += 7;
                doc.text(`Discount (${discountRate}%):`, 150, y, { align: 'right' });
                doc.text(`-LKR ${discountAmount.toFixed(2)}`, 180, y, { align: 'right' });
              }
              
              y += 7;
              doc.setFontSize(12);
              doc.text('Total:', 150, y, { align: 'right' });
              doc.text(`LKR ${total.toFixed(2)}`, 180, y, { align: 'right' });
              doc.setFontSize(10);
              
              // Add notes if available
              if (bill.notes) {
                y += 20;
                doc.setFont('helvetica', 'bold');
                doc.text('Notes:', 15, y);
                doc.setFont('helvetica', 'normal');
                const notes = doc.splitTextToSize(bill.notes, 180);
                doc.text(notes, 15, y + 7);
              }
              
              // Add footer
              doc.setFontSize(8);
              doc.setTextColor(128);
              doc.text('Thank you for your business!', 105, 285, { align: 'center' });
              
              // Save the PDF
              doc.save(`Invoice_${bill.invoiceNumber || 'N/A'}.pdf`);
              
            } catch (error) {
              console.error('PDF generation error:', error);
              billMgmtShowAlert('Error generating PDF: ' + error.message, 'danger');
            } finally {
              // Restore button state
              if (printBtn) {
                printBtn.disabled = false;
                printBtn.innerHTML = originalBtnText;
              }
            }
          });
        }
      });
      
      // Show the modal
      modal.show();
    }
  } catch (error) {
    console.error('Error showing bill details:', error);
    billMgmtShowAlert('Failed to display invoice details', 'danger');
  }
}

async function updateBillStatus(billId, currentStatus) {
  const statuses = ['paid', 'overdue', 'cancelled'];
  const currentIndex = statuses.indexOf(currentStatus);
  const nextStatus = statuses[(currentIndex + 1) % statuses.length];
  
  try {
    const res = await fetch(`${window.API_BASE}/bills`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ billId: billId, status: nextStatus })
    });
    
    const result = await res.json();
    if (result.success) {
      billMgmtShowAlert(`Bill status updated to ${nextStatus}`, 'success');
      fetchBills(); // Refresh the table
    } else {
      billMgmtShowAlert('Failed to update bill status', 'danger');
    }
  } catch (error) {
    console.error('Failed to update bill status:', error);
    billMgmtShowAlert('Failed to update bill status', 'danger');
  }
}

async function printBill(invoiceNumber) {
  try {
    // First update the bill status to 'paid'
    const response = await fetch(`${API_BASE}/api/bills/invoice/${encodeURIComponent(invoiceNumber)}/status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ status: 'paid' })
    });
    
    if (!response.ok) {
      throw new Error('Failed to update bill status');
    }
    
    // Then open the print dialog
    const printWindow = window.open(`${API_BASE}/bills/print/${encodeURIComponent(invoiceNumber)}`, '_blank');
    printWindow.onload = function() {
      printWindow.print();
    };
    
    // Refresh the bills list to show updated status
    fetchBills();
  } catch (error) {
    console.error('Error printing bill:', error);
    billMgmtShowAlert('Failed to update bill status before printing', 'danger');
  }
}

function emailBill(invoiceNumber) {
  billMgmtShowAlert(`Email functionality for ${invoiceNumber} - To be implemented`, 'info');
}

function exportBills() {
  billMgmtShowAlert('Export functionality - To be implemented', 'info');
}

// Search functionality
function wireBillSearch(){
  const searchBtn = document.getElementById('searchBillsBtn');
  const clearBtn = document.getElementById('clearBillsBtn');
  const searchInputs = ['searchBillNumber', 'searchBillCustomer', 'searchBillStatus', 'searchBillFromDate', 'searchBillToDate'];
  const inputs = searchInputs.map(id => document.getElementById(id)).filter(Boolean);
  
  if (searchBtn) searchBtn.addEventListener('click', () => { window.BILL_PAGE = 1; fetchBills(); });
  if (clearBtn) clearBtn.addEventListener('click', () => { 
    inputs.forEach(i => i.value = ''); 
    window.BILL_PAGE = 1; 
    fetchBills(); 
  });
  
  inputs.forEach(input => {
    input.addEventListener('input', () => { 
      window.BILL_PAGE = 1; 
      fetchBills(); 
    });
  });
}

// Initialize bill management
document.addEventListener('DOMContentLoaded', () => {
  // Add click event for bill delete confirmation
  const confirmBillDeleteBtn = document.getElementById('confirmBillDeleteBtn');
  if (confirmBillDeleteBtn) {
    confirmBillDeleteBtn.addEventListener('click', confirmBillDeletion);
  }
  
  // Export bills button
  const exportBtn = document.getElementById('exportBillsBtn');
  if (exportBtn) exportBtn.addEventListener('click', exportBills);
  
  // Wire search functionality
  wireBillSearch();
  
  // Load bills when orders section is active
  const ordersSection = document.getElementById('orders');
  if (ordersSection) {
    // Check if orders section is visible, if so load bills
    const observer = new MutationObserver((mutations) => {
      mutations.forEach((mutation) => {
        if (mutation.type === 'attributes' && mutation.attributeName === 'style') {
          const isVisible = ordersSection.style.display !== 'none';
          if (isVisible && !ordersSection.dataset.loaded) {
            fetchBills();
            ordersSection.dataset.loaded = 'true';
          }
        }
      });
    });
    
    observer.observe(ordersSection, { attributes: true });
    
    // Initial load if section is already visible
    if (ordersSection.style.display !== 'none') {
      fetchBills();
      ordersSection.dataset.loaded = 'true';
    }
  }
});

// Export functions for inline event handlers
window.viewBill = viewBill;
window.printBill = printBill;
window.emailBill = emailBill;
window.updateBillStatus = updateBillStatus;
