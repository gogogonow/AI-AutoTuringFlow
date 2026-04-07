// ModuleList Component
class ModuleList {
  constructor() {
    this.container = null;
    this.currentPage = 0;
    this.pageSize = CONFIG.DEFAULT_PAGE_SIZE;
    this.totalPages = 0;
    this.totalElements = 0;
    this.filters = {};
    this.selectedIds = new Set();
    this.init();
  }

  init() {
    this.container = document.createElement('div');
    this.container.className = 'module-list-container';
    this.render();
  }

  render() {
    this.container.innerHTML = `
      <!-- Filters Card -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">筛选条件</h2>
          <button class="btn btn-secondary btn-sm" id="resetFiltersBtn">重置</button>
        </div>
        <div class="filters-form">
          <div class="form-row">
            <div class="form-col">
              <input class="form-control" type="text" placeholder="序列号" id="filterSn">
            </div>
            <div class="form-col">
              <select class="form-control" id="filterStatus">
                <option value="">全部状态</option>
                <option value="IN_STOCK">在库</option>
                <option value="DEPLOYED">已部署</option>
                <option value="FAULTY">故障</option>
                <option value="UNDER_REPAIR">维修中</option>
                <option value="SCRAPPED">已报废</option>
              </select>
            </div>
            <div class="form-col">
              <input class="form-control" type="text" placeholder="型号" id="filterModel">
            </div>
            <div class="form-col">
              <input class="form-control" type="text" placeholder="供应商" id="filterVendor">
            </div>
          </div>
          <div class="form-actions">
            <button class="btn btn-primary" id="searchBtn">🔍 搜索</button>
          </div>
        </div>
      </div>

      <!-- List Card -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">光模块列表（共 <span id="totalCount">0</span> 条）</h2>
          <div class="card-actions">
            <button class="btn btn-success btn-sm" id="exportBtn">📥 导出Excel</button>
            <button class="btn btn-info btn-sm" id="importBtn">📤 导入Excel</button>
            <input type="file" id="importFileInput" accept=".xlsx,.xls" style="display:none;">
            <button class="btn btn-success btn-sm" id="batchInboundBtn">批量入库</button>
          </div>
        </div>
        <div class="table-container">
          <table class="table">
            <thead>
              <tr>
                <th width="50"><input type="checkbox" id="selectAll"></th>
                <th width="150">序列号</th>
                <th width="120">型号</th>
                <th width="100">供应商</th>
                <th width="80">速率</th>
                <th width="80">波长</th>
                <th width="100">状态</th>
                <th width="150">入库时间</th>
                <th width="150">操作</th>
              </tr>
            </thead>
            <tbody id="moduleTableBody">
              <tr>
                <td colspan="9" style="text-align: center; padding: 40px;">
                  <div class="loading-spinner" style="margin: 0 auto;"></div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination" id="pagination"></div>
      </div>
    `;

    this.bindEvents();
    this.loadData();
  }

  bindEvents() {
    // Reset filters
    const resetBtn = this.container.querySelector('#resetFiltersBtn');
    resetBtn.addEventListener('click', () => {
      this.container.querySelector('#filterSn').value = '';
      this.container.querySelector('#filterStatus').value = '';
      this.container.querySelector('#filterModel').value = '';
      this.container.querySelector('#filterVendor').value = '';
      this.filters = {};
      this.currentPage = 0;
      this.loadData();
    });

    // Search
    const searchBtn = this.container.querySelector('#searchBtn');
    searchBtn.addEventListener('click', () => {
      this.filters = this.getFilters();
      this.currentPage = 0;
      this.loadData();
    });

    // Select all
    const selectAll = this.container.querySelector('#selectAll');
    selectAll.addEventListener('change', (e) => {
      const checkboxes = this.container.querySelectorAll('tbody input[type="checkbox"]');
      checkboxes.forEach(cb => {
        cb.checked = e.target.checked;
        const id = cb.dataset.id;
        if (e.target.checked) {
          this.selectedIds.add(id);
        } else {
          this.selectedIds.delete(id);
        }
      });
    });

    // Batch inbound
    const batchBtn = this.container.querySelector('#batchInboundBtn');
    batchBtn.addEventListener('click', () => {
      if (this.selectedIds.size === 0) {
        Utils.showToast('请先选择要操作的光模块', 'warning');
        return;
      }
      // TODO: Implement batch inbound modal
      Utils.showToast('批量入库功能开发中', 'info');
    });

    // Export
    const exportBtn = this.container.querySelector('#exportBtn');
    exportBtn.addEventListener('click', () => {
      this.handleExport();
    });

    // Import
    const importBtn = this.container.querySelector('#importBtn');
    const importFileInput = this.container.querySelector('#importFileInput');
    importBtn.addEventListener('click', () => {
      importFileInput.click();
    });
    importFileInput.addEventListener('change', (e) => {
      const file = e.target.files[0];
      if (file) {
        this.handleImport(file);
        importFileInput.value = '';
      }
    });
  }

  getFilters() {
    const filters = {};
    const sn = this.container.querySelector('#filterSn').value.trim();
    const status = this.container.querySelector('#filterStatus').value;
    const model = this.container.querySelector('#filterModel').value.trim();
    const vendor = this.container.querySelector('#filterVendor').value.trim();

    if (sn) filters.serialNumber = sn;
    if (status) filters.status = status;
    if (model) filters.model = model;
    if (vendor) filters.vendor = vendor;

    return filters;
  }

  async loadData() {
    try {
      const params = {
        ...this.filters,
        page: this.currentPage,
        size: this.pageSize
      };

      const response = await API.getModules(params);
      this.renderTable(response.content || []);
      this.totalPages = response.totalPages || 0;
      this.totalElements = response.totalElements || 0;
      this.renderPagination();
      
      // Update total count
      const totalCount = this.container.querySelector('#totalCount');
      if (totalCount) {
        totalCount.textContent = this.totalElements;
      }
    } catch (error) {
      const tbody = this.container.querySelector('#moduleTableBody');
      tbody.innerHTML = `
        <tr>
          <td colspan="9">
            ${Utils.renderErrorState(
              '加载失败: ' + error.message,
              '<button class="btn btn-secondary" onclick="window.app.currentComponent.loadData()">重试</button>'
            )}
          </td>
        </tr>
      `;
    }
  }

  renderTable(modules) {
    const tbody = this.container.querySelector('#moduleTableBody');
    
    if (modules.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="9">
            ${Utils.renderEmptyState(
              '📦',
              '暂无数据',
              '<button class="btn btn-primary" onclick="window.app.showPage(\'create\')">立即入库</button>'
            )}
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = modules.map(module => `
      <tr>
        <td>
          <input type="checkbox" data-id="${module.id}" 
            ${this.selectedIds.has(String(module.id)) ? 'checked' : ''}
            onchange="window.app.currentComponent.handleCheckbox(this)">
        </td>
        <td>${Utils.escapeHtml(module.serialNumber || '-')}</td>
        <td>${Utils.escapeHtml(module.model || '-')}</td>
        <td>${Utils.escapeHtml(module.vendor || '-')}</td>
        <td>${Utils.escapeHtml(module.speed || '-')}</td>
        <td>${Utils.escapeHtml(module.wavelength || '-')}</td>
        <td>
          <span class="status-badge ${Utils.getStatusClass(module.status)}">
            ${Utils.getStatusText(module.status)}
          </span>
        </td>
        <td>${Utils.formatDateTime(module.inboundTime)}</td>
        <td>
          <div class="action-buttons">
            <button class="btn btn-primary btn-sm" 
              onclick="window.app.showPage('details', {id: ${module.id}})">查看</button>
            <button class="btn btn-secondary btn-sm" 
              onclick="window.app.showPage('edit', {id: ${module.id}})">编辑</button>
            <button class="btn btn-danger btn-sm" 
              onclick="window.app.currentComponent.handleDelete(${module.id})">删除</button>
          </div>
        </td>
      </tr>
    `).join('');
  }

  renderPagination() {
    const pagination = this.container.querySelector('#pagination');
    if (this.totalPages <= 1) {
      pagination.innerHTML = '';
      return;
    }

    let html = `
      <button class="pagination-btn" ${this.currentPage === 0 ? 'disabled' : ''}
        onclick="window.app.currentComponent.loadPage(${this.currentPage - 1})">&lt;</button>
    `;

    for (let i = 0; i < this.totalPages; i++) {
      if (i < 2 || i >= this.totalPages - 2 || Math.abs(i - this.currentPage) <= 1) {
        html += `
          <button class="pagination-btn ${i === this.currentPage ? 'active' : ''}"
            onclick="window.app.currentComponent.loadPage(${i})">${i + 1}</button>
        `;
      } else if (i === 2 || i === this.totalPages - 3) {
        html += '<span style="padding: 0 8px;">...</span>';
      }
    }

    html += `
      <button class="pagination-btn" ${this.currentPage >= this.totalPages - 1 ? 'disabled' : ''}
        onclick="window.app.currentComponent.loadPage(${this.currentPage + 1})">&gt;</button>
    `;

    pagination.innerHTML = html;
  }

  handleCheckbox(checkbox) {
    const id = checkbox.dataset.id;
    if (checkbox.checked) {
      this.selectedIds.add(id);
    } else {
      this.selectedIds.delete(id);
    }
  }

  handleDelete(id) {
    Utils.confirm('确定删除该光模块？此操作不可恢复。', async () => {
      try {
        Utils.showLoading();
        await API.deleteModule(id);
        Utils.hideLoading();
        Utils.showToast('删除成功', 'success');
        this.loadData();
      } catch (error) {
        Utils.hideLoading();
        Utils.showToast('删除失败: ' + error.message, 'error');
      }
    });
  }

  loadPage(page) {
    this.currentPage = page;
    this.loadData();
  }

  async handleExport() {
    try {
      Utils.showLoading();
      await API.exportModules(this.filters);
      Utils.hideLoading();
      Utils.showToast('导出成功', 'success');
    } catch (error) {
      Utils.hideLoading();
      Utils.showToast('导出失败: ' + error.message, 'error');
    }
  }

  async handleImport(file) {
    try {
      Utils.showLoading();
      const result = await API.importModules(file);
      Utils.hideLoading();
      let msg = `导入完成：成功 ${result.successCount} 条`;
      if (result.failCount > 0) {
        msg += `，失败 ${result.failCount} 条`;
      }
      if (result.errors && result.errors.length > 0) {
        msg += '\n' + result.errors.slice(0, 5).join('\n');
      }
      Utils.showToast(msg, result.failCount > 0 ? 'warning' : 'success');
      this.loadData();
    } catch (error) {
      Utils.hideLoading();
      Utils.showToast('导入失败: ' + error.message, 'error');
    }
  }

  refresh() {
    this.loadData();
  }

  getElement() {
    return this.container;
  }
}

// Make ModuleList globally available
window.ModuleList = ModuleList;
