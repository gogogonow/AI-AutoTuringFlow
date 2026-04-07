// HistoryList Component
class HistoryList {
  constructor() {
    this.container = null;
    this.currentPage = 0;
    this.pageSize = CONFIG.DEFAULT_PAGE_SIZE;
    this.totalPages = 0;
    this.totalElements = 0;
    this.init();
  }

  init() {
    this.container = document.createElement('div');
    this.container.className = 'history-list-container';
    this.render();
  }

  render() {
    this.container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">修改历史（共 <span id="historyTotalCount">0</span> 条）</h2>
        </div>
        <div class="history-table-container">
          <div class="table-container">
            <table class="table history-table">
              <thead>
                <tr>
                  <th>操作时间</th>
                  <th>操作类型</th>
                  <th>序列号</th>
                  <th>型号</th>
                  <th>操作前状态</th>
                  <th>操作后状态</th>
                  <th>操作人</th>
                  <th>备注</th>
                </tr>
              </thead>
              <tbody id="historyTableBody">
                <tr>
                  <td colspan="8" style="text-align: center; padding: 40px;">
                    <div class="loading-spinner" style="margin: 0 auto;"></div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="pagination" id="historyPagination"></div>
      </div>
    `;

    this.loadData();
  }

  async loadData() {
    try {
      const params = {
        page: this.currentPage,
        size: this.pageSize
      };

      const response = await API.getHistories(params);
      this.renderTable(response.content || []);
      this.totalPages = response.totalPages || 0;
      this.totalElements = response.totalElements || 0;
      this.renderPagination();
      
      // Update total count
      const totalCount = this.container.querySelector('#historyTotalCount');
      if (totalCount) {
        totalCount.textContent = this.totalElements;
      }
    } catch (error) {
      const tbody = this.container.querySelector('#historyTableBody');
      tbody.innerHTML = `
        <tr>
          <td colspan="8">
            ${Utils.renderErrorState(
              '加载失败: ' + error.message,
              '<button class="btn btn-secondary" onclick="window.app.currentComponent.loadData()">重试</button>'
            )}
          </td>
        </tr>
      `;
    }
  }

  renderTable(histories) {
    const tbody = this.container.querySelector('#historyTableBody');
    
    if (histories.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="8">
            ${Utils.renderEmptyState('📜', '暂无历史记录', '')}
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = histories.map(item => `
      <tr>
        <td>${Utils.formatDateTime(item.operationTime)}</td>
        <td>${Utils.getOperationTypeText(item.operationType)}</td>
        <td>${Utils.escapeHtml(item.moduleSerialNumber || '-')}</td>
        <td>${Utils.escapeHtml(item.moduleModel || '-')}</td>
        <td>
          ${item.previousStatus ? `<span class="status-badge ${Utils.getStatusClass(item.previousStatus)}">${Utils.getStatusText(item.previousStatus)}</span>` : '-'}
        </td>
        <td>
          ${item.nextStatus ? `<span class="status-badge ${Utils.getStatusClass(item.nextStatus)}">${Utils.getStatusText(item.nextStatus)}</span>` : '-'}
        </td>
        <td>${Utils.escapeHtml(item.operator || '系统')}</td>
        <td>${Utils.escapeHtml(item.remark || '-')}</td>
      </tr>
    `).join('');
  }

  renderPagination() {
    const pagination = this.container.querySelector('#historyPagination');
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

  loadPage(page) {
    this.currentPage = page;
    this.loadData();
  }

  refresh() {
    this.loadData();
  }

  getElement() {
    return this.container;
  }
}

// Make HistoryList globally available
window.HistoryList = HistoryList;
