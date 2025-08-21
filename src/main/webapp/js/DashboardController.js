class DashboardController {
    constructor() {
        this.apiBaseUrl = 'api/dashboard'; // Using relative path
        this.elements = {
            totalRevenue: document.getElementById('totalRevenue'),
            totalCustomers: document.getElementById('totalCustomers'),
            totalOrders: document.getElementById('totalOrders'),
            activeItems: document.getElementById('activeItems'),
            loadingSpinner: document.getElementById('dashboardLoading'),
            statsContainer: document.getElementById('dashboardStats')
        };
    }

    async init() {
        try {
            this.showLoading(true);
            await this.loadDashboardStats();
        } catch (error) {
            console.error('Error initializing dashboard:', error);
            this.showError('Failed to load dashboard data');
        } finally {
            this.showLoading(false);
        }
    }

    async loadDashboardStats() {
        try {
            const response = await fetch(this.apiBaseUrl);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            this.updateDashboardUI(data);
        } catch (error) {
            console.error('Error loading dashboard stats:', error);
            throw error;
        }
    }

    updateDashboardUI(stats) {
        if (!stats) return;

        // Format and update each stat
        if (this.elements.totalRevenue && stats.totalRevenue !== undefined) {
            this.elements.totalRevenue.textContent = this.formatCurrency(stats.totalRevenue);
        }
        if (this.elements.totalCustomers && stats.totalCustomers !== undefined) {
            this.elements.totalCustomers.textContent = stats.totalCustomers.toLocaleString();
        }
        if (this.elements.totalOrders && stats.totalOrders !== undefined) {
            this.elements.totalOrders.textContent = stats.totalOrders.toLocaleString();
        }
        if (this.elements.activeItems && stats.activeItems !== undefined) {
            this.elements.activeItems.textContent = stats.activeItems.toLocaleString();
        }
    }

    formatCurrency(amount) {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'LKR',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(amount);
    }

    showLoading(show) {
        if (this.elements.loadingSpinner) {
            this.elements.loadingSpinner.style.display = show ? 'block' : 'none';
        }
        if (this.elements.statsContainer) {
            this.elements.statsContainer.style.opacity = show ? '0.5' : '1';
        }
    }

    showError(message) {
        // You can implement a more sophisticated error display
        console.error(message);
        alert(message);
    }
}

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    const dashboardController = new DashboardController();
    dashboardController.init();
});
