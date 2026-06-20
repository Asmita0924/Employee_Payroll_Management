// Global State
const state = {
    token: localStorage.getItem('jwt_token') || null,
    username: localStorage.getItem('username') || null,
    role: localStorage.getItem('role') || null,
    currentTab: 'tab-dashboard',
    deptChartInstance: null,
    salaryChartInstance: null
};

// API Base Call wrapper with JWT Injection
async function apiRequest(url, options = {}) {
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers
    };

    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }

    const config = {
        ...options,
        headers
    };

    try {
        const response = await fetch(url, config);
        
        if (response.status === 401 || response.status === 403) {
            // Token expired or invalid
            logout();
            throw new Error('Session expired. Please log in again.');
        }

        // For file downloads (like PDF), return the response directly
        if (config.responseType === 'blob') {
            if (!response.ok) throw new Error('File download failed');
            return response;
        }

        const data = await response.json().catch(() => null);
        
        if (!response.ok) {
            const message = data && (data.message || data.error) ? (data.message || data.error) : 'API request failed';
            throw new Error(message);
        }

        return data;
    } catch (error) {
        console.error(`API Error on ${url}:`, error);
        throw error;
    }
}

// Check Authentication & State Binding
function checkAuth() {
    const authPage = document.getElementById('auth-page');
    const mainLayout = document.getElementById('main-layout');

    if (state.token && state.role) {
        authPage.style.display = 'none';
        mainLayout.style.display = 'flex';
        
        // Populate profile info
        document.getElementById('logged-username').innerText = state.username;
        document.getElementById('logged-role').innerText = state.role.replace('ROLE_', '');
        
        // If HR or Employee, hide restricted tabs/buttons from sidebar
        adjustUIByRole();

        // Load initial dashboard stats
        switchTab(state.currentTab);
    } else {
        authPage.style.display = 'flex';
        mainLayout.style.display = 'none';
    }
}

// Adjust UI based on user role
function adjustUIByRole() {
    const addEmpBtns = document.querySelectorAll('.action-btn.primary, button[onclick="openAddEmpModal()"]');
    const genPayrollBtns = document.querySelectorAll('button[onclick="openGeneratePayrollModal()"]');
    const reqLeaveBtn = document.querySelector('button[onclick="openRequestLeaveModal()"]');
    const logAttBtn = document.querySelector('button[onclick="openLogAttendanceModal()"]');

    if (state.role === 'ROLE_EMPLOYEE') {
        addEmpBtns.forEach(btn => btn.style.display = 'none');
        genPayrollBtns.forEach(btn => btn.style.display = 'none');
        // Employees can request leave and log attendance
        if (reqLeaveBtn) reqLeaveBtn.style.display = 'inline-block';
        if (logAttBtn) logAttBtn.style.display = 'inline-block';
    } else if (state.role === 'ROLE_HR') {
        addEmpBtns.forEach(btn => btn.style.display = 'inline-block');
        genPayrollBtns.forEach(btn => btn.style.display = 'inline-block');
    } else {
        // ADMIN has all access
        addEmpBtns.forEach(btn => btn.style.display = 'inline-block');
        genPayrollBtns.forEach(btn => btn.style.display = 'inline-block');
    }
}

// Login
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const usernameInput = document.getElementById('username').value.trim();
    const passwordInput = document.getElementById('password').value;
    const loginError = document.getElementById('login-error');
    
    loginError.style.display = 'none';
    loginError.innerText = '';

    try {
        const data = await apiRequest('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username: usernameInput, password: passwordInput })
        });

        if (data && data.token) {
            state.token = data.token;
            state.username = data.username;
            state.role = data.role;

            localStorage.setItem('jwt_token', data.token);
            localStorage.setItem('username', data.username);
            localStorage.setItem('role', data.role);

            checkAuth();
        } else {
            throw new Error('Invalid token response from server.');
        }
    } catch (error) {
        loginError.innerText = error.message;
        loginError.style.display = 'block';
    }
});

// Logout
function logout() {
    state.token = null;
    state.username = null;
    state.role = null;

    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');

    checkAuth();
}

// Modal open/close helpers
function openModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

// Open Specific Modals
function openAddEmpModal() { openModal('modal-add-emp'); }
function openLogAttendanceModal() { openModal('modal-log-att'); }
function openRequestLeaveModal() { openModal('modal-req-leave'); }
function openGeneratePayrollModal() { openModal('modal-gen-payroll'); }

// Dynamic Navigation
function switchTab(tabId) {
    state.currentTab = tabId;

    // Switch sidebar navigation highlight
    const navItems = document.querySelectorAll('.nav-menu .nav-item');
    navItems.forEach(item => {
        item.classList.remove('active');
        const clickAttr = item.getAttribute('onclick');
        if (clickAttr && clickAttr.includes(tabId)) {
            item.classList.add('active');
        }
    });

    // Switch visible tab panel
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(tab => {
        tab.classList.remove('active');
    });

    const activeTab = document.getElementById(tabId);
    if (activeTab) {
        activeTab.classList.add('active');
    }

    // Fetch tab-specific data
    if (tabId === 'tab-dashboard') {
        loadDashboardStats();
    } else if (tabId === 'tab-employees') {
        loadEmployees();
    } else if (tabId === 'tab-departments') {
        loadDepartments();
    } else if (tabId === 'tab-attendance') {
        loadAttendanceHistory();
    } else if (tabId === 'tab-leaves') {
        loadLeaves();
    } else if (tabId === 'tab-analytics') {
        loadAnalytics();
    }
}

// 1. Dashboard Stats
async function loadDashboardStats() {
    try {
        // Get total employees list to count stats
        const empData = await apiRequest('/api/employees?size=1000');
        const deptData = await apiRequest('/api/departments');

        if (empData && empData.content) {
            const employees = empData.content;
            
            // KPIs
            const activeEmployees = employees.filter(e => e.status === 'ACTIVE').length;
            const itEmployees = employees.filter(e => e.departmentName === 'IT' || e.departmentId === 1).length;

            document.getElementById('kpi-headcount').innerText = activeEmployees;
            document.getElementById('kpi-it-headcount').innerText = itEmployees;
        }

        if (deptData) {
            document.getElementById('kpi-depts').innerText = deptData.length;
        }

    } catch (err) {
        console.error('Error fetching dashboard counts:', err);
    }
}

// 2. Load Employees
async function loadEmployees() {
    const tableBody = document.getElementById('employees-table-body');
    tableBody.innerHTML = '<tr><td colspan="9" style="text-align: center;">Loading corporate roster...</td></tr>';

    try {
        const data = await apiRequest('/api/employees?size=100');
        tableBody.innerHTML = '';

        if (data && data.content && data.content.length > 0) {
            data.content.forEach(emp => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${emp.id}</td>
                    <td><strong>${escapeHtml(emp.name)}</strong></td>
                    <td>${escapeHtml(emp.email)}</td>
                    <td>${escapeHtml(emp.phone)}</td>
                    <td>${escapeHtml(emp.designation)}</td>
                    <td><span class="badge" style="background: rgba(0, 168, 204, 0.2); color: #00a8cc;">${escapeHtml(emp.departmentName || 'N/A')}</span></td>
                    <td><strong>$${emp.baseSalary.toLocaleString()}</strong></td>
                    <td><span class="badge ${emp.status.toLowerCase() === 'active' ? 'active' : 'rejected'}">${emp.status}</span></td>
                    <td class="action-cell">
                        ${state.role !== 'ROLE_EMPLOYEE' ? `
                            <button class="action-btn small info" onclick="promptUpdateSalary(${emp.id}, ${emp.baseSalary})">Update Salary</button>
                            <button class="action-btn small danger" onclick="confirmDeleteEmployee(${emp.id})">Delete</button>
                        ` : ''}
                        <button class="action-btn small success" onclick="promptIndividualPayroll(${emp.id})">Pay Slip</button>
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = '<tr><td colspan="9" style="text-align: center;">No employees found.</td></tr>';
        }
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="9" style="text-align: center; color: var(--danger);">Failed to load roster: ${err.message}</td></tr>`;
    }
}

// Prompt Update Salary
async function promptUpdateSalary(empId, currentSalary) {
    const newSalaryStr = prompt(`Enter new base salary for Employee ID ${empId}:`, currentSalary);
    if (newSalaryStr === null) return;
    
    const newSalary = parseFloat(newSalaryStr);
    if (isNaN(newSalary) || newSalary < 0) {
        alert('Invalid salary amount.');
        return;
    }

    try {
        const res = await apiRequest(`/api/employees/update-salary?employeeId=${empId}&newSalary=${newSalary}`, {
            method: 'POST'
        });
        alert(res.message || 'Base salary updated successfully.');
        loadEmployees();
    } catch (err) {
        alert('Error updating salary: ' + err.message);
    }
}

// Confirm Delete Employee
async function confirmDeleteEmployee(empId) {
    if (!confirm(`Are you absolutely sure you want to delete Employee ID ${empId}?`)) return;

    try {
        const res = await apiRequest(`/api/employees/${empId}`, {
            method: 'DELETE'
        });
        alert(res.message || 'Employee deleted successfully.');
        loadEmployees();
    } catch (err) {
        alert('Error deleting employee: ' + err.message);
    }
}

// Individual Payroll Download or Generate Slip
async function promptIndividualPayroll(empId) {
    const month = prompt('Enter billing month (YYYY-MM):', '2026-06');
    if (!month) return;

    try {
        // Attempt to fetch existing payroll for this month first
        const payroll = await apiRequest(`/api/payroll/employee/${empId}/month?month=${month}`).catch(() => null);
        
        if (payroll && payroll.id) {
            // Already generated, download it
            await downloadSalarySlipPdf(payroll.id);
        } else {
            // Does not exist, offer to generate it via Stored Procedure
            if (confirm(`No payroll record found for Employee ID ${empId} in ${month}. Generate now?`)) {
                const genRes = await apiRequest(`/api/payroll/generate?employeeId=${empId}&month=${month}`, {
                    method: 'POST'
                });
                alert(genRes.message || 'Payroll generated successfully!');
                
                // Now fetch and download
                const newPayroll = await apiRequest(`/api/payroll/employee/${empId}/month?month=${month}`);
                if (newPayroll && newPayroll.id) {
                    await downloadSalarySlipPdf(newPayroll.id);
                }
            }
        }
    } catch (err) {
        alert('Action failed: ' + err.message);
    }
}

// Fetch and trigger browser PDF download with JWT Header inclusion
async function downloadSalarySlipPdf(payrollId) {
    try {
        const response = await apiRequest(`/api/payroll/${payrollId}/pdf`, {
            method: 'GET',
            responseType: 'blob'
        });

        const blob = await response.blob();
        const blobUrl = window.URL.createObjectURL(blob);
        
        const link = document.createElement('a');
        link.href = blobUrl;
        link.download = `SalarySlip_${payrollId}.pdf`;
        document.body.appendChild(link);
        link.click();
        
        document.body.removeChild(link);
        window.URL.revokeObjectURL(blobUrl);
    } catch (err) {
        alert('Failed to download PDF: ' + err.message);
    }
}

// 3. Load Departments
async function loadDepartments() {
    const tableBody = document.getElementById('departments-table-body');
    tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">Loading departments...</td></tr>';

    try {
        const data = await apiRequest('/api/departments');
        tableBody.innerHTML = '';

        if (data && data.length > 0) {
            data.forEach(dept => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${dept.id}</td>
                    <td><strong>${escapeHtml(dept.name)}</strong></td>
                    <td>${escapeHtml(dept.managerName || 'None')}</td>
                    <td>${dept.managerId || 'N/A'}</td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No departments found.</td></tr>';
        }
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--danger);">Failed to load departments: ${err.message}</td></tr>`;
    }
}

// 4. Load Attendance
async function loadAttendanceHistory() {
    const empId = document.getElementById('att-emp-id').value;
    const tableBody = document.getElementById('attendance-table-body');

    if (!empId) {
        tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center; color: var(--yellow);">Please specify an Employee ID to search.</td></tr>';
        return;
    }

    tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center;">Querying attendance logs...</td></tr>';

    try {
        const data = await apiRequest(`/api/attendance/employee/${empId}?size=50`);
        tableBody.innerHTML = '';

        if (data && data.content && data.content.length > 0) {
            data.content.forEach(att => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${att.id}</td>
                    <td>${att.date}</td>
                    <td><strong>${att.checkIn}</strong></td>
                    <td><strong>${att.checkOut}</strong></td>
                    <td><span class="badge" style="background: rgba(33, 230, 193, 0.15); color: #21e6c1;">${att.workingHours} hrs</span></td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No attendance logs found for this employee.</td></tr>';
        }
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: var(--danger);">Failed to load logs: ${err.message}</td></tr>`;
    }
}

// 5. Load Leaves
async function loadLeaves() {
    const tableBody = document.getElementById('leaves-table-body');
    tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center;">Loading leave request workflows...</td></tr>';

    try {
        const data = await apiRequest('/api/leave');
        tableBody.innerHTML = '';

        if (data && data.length > 0) {
            data.forEach(lv => {
                let badgeClass = 'rejected';
                if (lv.status === 'APPROVED') badgeClass = 'approved';
                if (lv.status === 'PENDING') badgeClass = 'pending';

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${lv.id}</td>
                    <td><strong>${escapeHtml(lv.employeeName || 'ID: ' + lv.employeeId)}</strong></td>
                    <td>${escapeHtml(lv.leaveType)}</td>
                    <td>${lv.startDate}</td>
                    <td>${lv.endDate}</td>
                    <td><span class="badge ${badgeClass}">${lv.status}</span></td>
                    <td class="action-cell">
                        ${lv.status === 'PENDING' && state.role !== 'ROLE_EMPLOYEE' ? `
                            <button class="action-btn small success" onclick="processLeaveApproval(${lv.id}, 'APPROVED')">Approve</button>
                            <button class="action-btn small danger" onclick="processLeaveApproval(${lv.id}, 'REJECTED')">Reject</button>
                        ` : '<span style="color: var(--text-secondary); font-size: 12px;">Processed</span>'}
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        } else {
            tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center;">No leave requests found.</td></tr>';
        }
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align: center; color: var(--danger);">Failed to load leave logs: ${err.message}</td></tr>`;
    }
}

// Process Leave Approval via Stored Procedure
async function processLeaveApproval(leaveId, status) {
    try {
        const res = await apiRequest(`/api/leave/approve/${leaveId}?status=${status}`, {
            method: 'PUT'
        });
        alert(res.message || `Leave request ${status} successfully.`);
        loadLeaves();
    } catch (err) {
        alert('Action failed: ' + err.message);
    }
}

// 6. Advanced SQL Analytics
async function loadAnalytics() {
    const hierarchyBody = document.getElementById('analytics-hierarchy-body');
    const rankBody = document.getElementById('analytics-rank-body');
    const summaryBody = document.getElementById('analytics-summary-body');

    hierarchyBody.innerHTML = '<tr><td colspan="3" style="text-align: center;">Resolving CTE Org Map...</td></tr>';
    rankBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">Calculating salary distributions...</td></tr>';
    summaryBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">Compiling department summaries...</td></tr>';

    let rankData = [];
    let summaryData = [];

    // 6.1 CTE Org Map
    try {
        const data = await apiRequest('/api/payroll/analytics/hierarchy');
        hierarchyBody.innerHTML = '';
        if (data && data.length > 0) {
            data.forEach(row => {
                const tr = document.createElement('tr');
                const indent = row.level > 0 ? '&nbsp;'.repeat(row.level * 4) : '';
                tr.innerHTML = `
                    <td>${indent}├─ <strong>${escapeHtml(row.employee_name)}</strong></td>
                    <td>${escapeHtml(row.designation)}</td>
                    <td><span class="badge" style="background: rgba(13, 115, 119, 0.25); color: #0d7377;">Level ${row.level}</span></td>
                `;
                hierarchyBody.appendChild(tr);
            });
        } else {
            hierarchyBody.innerHTML = '<tr><td colspan="3" style="text-align: center;">No organization chart records.</td></tr>';
        }
    } catch (err) {
        hierarchyBody.innerHTML = `<tr><td colspan="3" style="text-align: center; color: var(--danger);">${err.message}</td></tr>`;
    }

    // 6.2 Rank base salaries
    try {
        const data = await apiRequest('/api/payroll/analytics/rank');
        rankData = data || [];
        rankBody.innerHTML = '';
        if (data && data.length > 0) {
            data.forEach(row => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${escapeHtml(row.employee_name)}</strong></td>
                    <td>$${row.base_salary.toLocaleString()}</td>
                    <td><span class="badge font-mono" style="background: #206a5d; color: #fff;">#${row.sal_rank}</span></td>
                    <td><span class="badge font-mono" style="background: #8fd9a8; color: #1f4068;">#${row.sal_dense_rank}</span></td>
                `;
                rankBody.appendChild(tr);
            });
        } else {
            rankBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No salary entries ranking.</td></tr>';
        }
    } catch (err) {
        rankBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--danger);">${err.message}</td></tr>`;
    }

    // 6.3 Department payout summaries
    try {
        const data = await apiRequest('/api/payroll/analytics/summary');
        summaryData = data || [];
        summaryBody.innerHTML = '';
        if (data && data.length > 0) {
            data.forEach(row => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td><strong>${escapeHtml(row.department_name)}</strong></td>
                    <td><span class="badge badge-warning">${row.total_processed} slips</span></td>
                    <td><strong>$${row.total_payout.toLocaleString()}</strong></td>
                    <td>$${parseFloat(row.avg_payout).toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}</td>
                `;
                summaryBody.appendChild(tr);
            });
        } else {
            summaryBody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No slips processed in billing cycle yet.</td></tr>';
        }
    } catch (err) {
        summaryBody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: var(--danger);">${err.message}</td></tr>`;
    }

    // Render interactive graphs
    renderCharts(rankData, summaryData);
}

// Helper to render Chart.js graphs
function renderCharts(rankData, summaryData) {
    if (typeof Chart === 'undefined') {
        console.warn('Chart.js library is not loaded.');
        return;
    }

    // 1. Department Payout Chart (Bar Chart)
    const deptCtx = document.getElementById('dept-payout-chart');
    if (deptCtx) {
        if (state.deptChartInstance) {
            state.deptChartInstance.destroy();
        }

        const labels = summaryData.map(row => row.department_name);
        const payoutData = summaryData.map(row => row.total_payout);

        state.deptChartInstance = new Chart(deptCtx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Total Monthly Payout ($)',
                    data: payoutData,
                    backgroundColor: 'rgba(56, 189, 248, 0.4)',
                    borderColor: '#38bdf8',
                    borderWidth: 2,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: {
                            color: '#f8fafc'
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { color: '#94a3b8' }
                    },
                    y: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { color: '#94a3b8' },
                        beginAtZero: true
                    }
                }
            }
        });
    }

    // 2. Base Salaries Rank Chart (Horizontal Bar Chart)
    const salaryCtx = document.getElementById('salary-rank-chart');
    if (salaryCtx) {
        if (state.salaryChartInstance) {
            state.salaryChartInstance.destroy();
        }

        const limitedRankData = rankData.slice(0, 8);
        const labels = limitedRankData.map(row => row.employee_name);
        const salaryData = limitedRankData.map(row => row.base_salary);

        state.salaryChartInstance = new Chart(salaryCtx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Base Monthly Salary ($)',
                    data: salaryData,
                    backgroundColor: 'rgba(74, 222, 128, 0.4)',
                    borderColor: '#4ade80',
                    borderWidth: 2,
                    borderRadius: 6
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        labels: {
                            color: '#f8fafc'
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { color: '#94a3b8' },
                        beginAtZero: true
                    },
                    y: {
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { color: '#94a3b8' }
                    }
                }
            }
        });
    }
}

// Form Submission handlers

// Add Employee
document.getElementById('add-emp-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        name: document.getElementById('emp-name').value.trim(),
        email: document.getElementById('emp-email').value.trim(),
        phone: document.getElementById('emp-phone').value.trim(),
        departmentId: parseInt(document.getElementById('emp-dept-id').value),
        designation: document.getElementById('emp-designation').value.trim(),
        joiningDate: document.getElementById('emp-joining-date').value,
        baseSalary: parseFloat(document.getElementById('emp-base-salary').value),
        status: 'ACTIVE'
    };

    try {
        await apiRequest('/api/employees', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        alert('Employee record successfully created.');
        closeModal('modal-add-emp');
        document.getElementById('add-emp-form').reset();
        
        // Refresh appropriate view
        if (state.currentTab === 'tab-employees') {
            loadEmployees();
        } else {
            switchTab('tab-employees');
        }
    } catch (err) {
        alert('Error adding employee: ' + err.message);
    }
});

// Log Attendance
document.getElementById('log-att-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        employeeId: parseInt(document.getElementById('log-att-emp-id').value),
        date: document.getElementById('log-att-date').value,
        checkIn: document.getElementById('log-att-in').value + ':00',
        checkOut: document.getElementById('log-att-out').value + ':00'
    };

    try {
        await apiRequest('/api/attendance', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        alert('Attendance logged successfully.');
        closeModal('modal-log-att');
        
        // Refresh appropriate view
        document.getElementById('att-emp-id').value = payload.employeeId;
        if (state.currentTab === 'tab-attendance') {
            loadAttendanceHistory();
        } else {
            switchTab('tab-attendance');
        }
    } catch (err) {
        alert('Error logging attendance: ' + err.message);
    }
});

// Request Leave
document.getElementById('req-leave-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        employeeId: parseInt(document.getElementById('leave-emp-id').value),
        leaveType: document.getElementById('leave-type').value,
        startDate: document.getElementById('leave-start').value,
        endDate: document.getElementById('leave-end').value,
        status: 'PENDING'
    };

    try {
        await apiRequest('/api/leave', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        alert('Leave request filed successfully.');
        closeModal('modal-req-leave');
        
        if (state.currentTab === 'tab-leaves') {
            loadLeaves();
        } else {
            switchTab('tab-leaves');
        }
    } catch (err) {
        alert('Error requesting leave: ' + err.message);
    }
});

// Generate Batch Payroll
document.getElementById('gen-payroll-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const month = document.getElementById('payroll-month').value.trim();
    const idsString = document.getElementById('payroll-ids').value.trim();
    
    let ids;
    try {
        ids = JSON.parse(idsString);
        if (!Array.isArray(ids)) throw new Error();
    } catch (err) {
        alert('Employee IDs must be a valid JSON array, e.g., [1, 2, 3]');
        return;
    }

    try {
        const res = await apiRequest(`/api/payroll/generate-batch?month=${encodeURIComponent(month)}`, {
            method: 'POST',
            body: JSON.stringify(ids)
        });
        alert(res.message || 'Batch transaction processed successfully.');
        closeModal('modal-gen-payroll');
        
        // Refresh dashboard overview
        loadDashboardStats();
    } catch (err) {
        alert('Error generating batch payroll: ' + err.message);
    }
});

// HTML escaping helper to prevent XSS injection
function escapeHtml(str) {
    if (!str) return '';
    return str.toString()
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Initial application entry point
window.addEventListener('DOMContentLoaded', () => {
    // Bind global functions to window scope for HTML onclick events
    window.switchTab = switchTab;
    window.logout = logout;
    
    window.openAddEmpModal = openAddEmpModal;
    window.openLogAttendanceModal = openLogAttendanceModal;
    window.openRequestLeaveModal = openRequestLeaveModal;
    window.openGeneratePayrollModal = openGeneratePayrollModal;
    window.closeModal = closeModal;
    
    window.promptUpdateSalary = promptUpdateSalary;
    window.confirmDeleteEmployee = confirmDeleteEmployee;
    window.promptIndividualPayroll = promptIndividualPayroll;
    window.processLeaveApproval = processLeaveApproval;
    window.loadAttendanceHistory = loadAttendanceHistory;

    // Run auth checker
    checkAuth();
});
