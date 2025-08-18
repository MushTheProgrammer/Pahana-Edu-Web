// customer.js
// Compute the webapp context dynamically so it works regardless of the deployed name
(function(){
  const parts = window.location.pathname.split('/').filter(Boolean);
  const ctx = parts.length > 0 ? '/' + parts[0] : '';
  window.API_BASE = ctx; // e.g., "/PahanaEduSystem_war_exploded" or "/PahanaEduSystem"
})();

// =================== API FUNCTIONS ===================
async function fetchCustomersAPI() {
    const res = await fetch(`${window.API_BASE}/customers`);
    if (!res.ok) throw new Error("Failed to fetch customers");
    return await res.json();
}

async function fetchCustomerByIdAPI(id) {
    const res = await fetch(`${window.API_BASE}/customers?id=${id}`);
    if (!res.ok) throw new Error("Customer not found");
    return await res.json();
}

async function saveCustomerAPI(customer, method) {
    const res = await fetch(`${window.API_BASE}/customers`, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(customer)
    });
    return await res.json();
}

async function deleteCustomerAPI(id) {
    const res = await fetch(`${window.API_BASE}/customers?id=${id}`, { method: "DELETE" });
    return await res.json();
}

// =================== UI FUNCTIONS ===================
async function fetchCustomers() {
    try {
        const customers = await fetchCustomersAPI();
        renderCustomerTable(customers);
    } catch (err) {
        console.error(err);
        alert("Error loading customers");
    }
}

function renderCustomerTable(customers) {
    let table = document.getElementById("customerTable");
    table.innerHTML = "";
    customers.forEach(c => {
        table.innerHTML += `
            <tr>
                <td>${c.customerId}</td>
                <td>${c.accountNo}</td>
                <td>${c.name}</td>
                <td>${c.address || ""}</td>
                <td>${c.phone || ""}</td>
                <td class="text-nowrap">
                    <button class="btn btn-sm btn-warning me-1" onclick="editCustomer(${c.customerId})">Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteCustomer(${c.customerId})">Delete</button>
                </td>
            </tr>
        `;
    });
}

function showCustomerForm() {
    document.getElementById("modalTitle").innerText = "Add Customer";
    document.getElementById("customerId").value = "";
    document.getElementById("accountNo").value = "";
    document.getElementById("customerName").value = "";
    document.getElementById("address").value = "";
    document.getElementById("phone").value = "";
    new bootstrap.Modal(document.getElementById("customerModal")).show();
}

async function editCustomer(id) {
    try {
        const c = await fetchCustomerByIdAPI(id);
        document.getElementById("modalTitle").innerText = "Edit Customer";
        document.getElementById("customerId").value = c.customerId;
        document.getElementById("accountNo").value = c.accountNo;
        document.getElementById("customerName").value = c.name;
        document.getElementById("address").value = c.address;
        document.getElementById("phone").value = c.phone;
        new bootstrap.Modal(document.getElementById("customerModal")).show();
    } catch (err) {
        console.error(err);
        alert("Error fetching customer details");
    }
}

async function saveCustomer() {
    const id = document.getElementById("customerId").value;
    const customer = {
        customerId: id ? parseInt(id) : null,
        accountNo: document.getElementById("accountNo").value,
        name: document.getElementById("customerName").value,
        address: document.getElementById("address").value,
        phone: document.getElementById("phone").value
    };

    const method = id ? "PUT" : "POST";
    try {
        const result = await saveCustomerAPI(customer, method);
        if (result.success) {
            bootstrap.Modal.getInstance(document.getElementById("customerModal")).hide();
            fetchCustomers();
        } else {
            alert("Failed to save customer");
        }
    } catch (err) {
        console.error(err);
        alert("Error saving customer");
    }
}

async function deleteCustomer(id) {
    if (!confirm("Are you sure you want to delete this customer?")) return;
    try {
        const result = await deleteCustomerAPI(id);
        if (result.success) {
            fetchCustomers();
        } else {
            alert("Failed to delete customer");
        }
    } catch (err) {
        console.error(err);
        alert("Error deleting customer");
    }
}

// Make functions available to SPA
window.showCustomerForm = showCustomerForm;
window.fetchCustomers = fetchCustomers;
window.editCustomer = editCustomer;
window.saveCustomer = saveCustomer;
window.deleteCustomer = deleteCustomer;
