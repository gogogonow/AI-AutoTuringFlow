// ModuleDetails Component
class ModuleDetails {
  constructor(options = {}) {
    this.container = null;
    this.moduleId = options.id || null;
    this.moduleData = null;
    this.init();
  }

  init() {
    this.container = document.createElement('div');
    this.container.className = 'module-details-container';
    if (this.moduleId) {
      this.loadData();
    } else {
      this.renderError('未指定光模块 ID');
    }
  }

  async loadData() {
    try {
      Utils.showLoading();
      this.moduleData = await API.getModule(this.moduleId);
      const history = await API.getModuleHistory(this.moduleId);
      Utils.hideLoading();
      this.render(history.content || []);
    } catch (error) {
      Utils.hideLoading();
      this.renderError('加载失败: ' + error.message);
    }
  }

  render(historyList) {
    if (!this.moduleData) return;

    const module = this.moduleData;
    this.container.innerHTML = `
      <!-- Basic Info Card -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">光模块详情</h2>
          <div class="card-actions">
            <button class="btn btn-secondary btn-sm" id="editBtn">✏️ 编辑</button>
            <button class="btn btn-danger btn-sm" id="deleteBtn">🗑️ 删除</button>
          </div>
        </div>
        <div class="details-grid">
          <div class="detail-item">
            <div class="detail-label">序列号</div>
            <div class="detail-value" id="detail-serialNumber">${Utils.escapeHtml(module.serialNumber || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">型号</div>
            <div class="detail-value" id="detail-model">${Utils.escapeHtml(module.model || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">供应商</div>
            <div class="detail-value" id="detail-vendor">${Utils.escapeHtml(module.vendor || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">端口速率</div>
            <div class="detail-value" id="detail-speed">${Utils.escapeHtml(module.speed || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">波长</div>
            <div class="detail-value" id="detail-wavelength">${Utils.escapeHtml(module.wavelength || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">传输距离</div>
            <div class="detail-value" id="detail-transmissionDistance">${module.transmissionDistance ? module.transmissionDistance + ' m' : '-'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">接口类型</div>
            <div class="detail-value" id="detail-connectorType">${Utils.escapeHtml(module.connectorType || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">当前状态</div>
            <div class="detail-value">
              <span class="status-badge ${Utils.getStatusClass(module.status)}" id="detail-status">
                ${Utils.getStatusText(module.status)}
              </span>
            </div>
          </div>
          <div class="detail-item">
            <div class="detail-label">入库时间</div>
            <div class="detail-value" id="detail-inboundTime">${Utils.formatDateTime(module.inboundTime)}</div>
          </div>
          <div class="detail-item" style="grid-column: 1 / -1;">
            <div class="detail-label">备注</div>
            <div class="detail-value" id="detail-remark">${Utils.escapeHtml(module.remark || '-')}</div>
          </div>
        </div>
      </div>

      <!-- Status Actions Card -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">状态操作</h2>
        </div>
        <div class="status-actions" style="padding: var(--spacing-lg); display: flex; gap: var(--spacing-sm); flex-wrap: wrap;">
          ${this.renderStatusActions(module.status)}
        </div>
      </div>

      <!-- Operation History Timeline -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">操作历史</h2>
        </div>
        <div class="timeline" id="historyTimeline">
          ${this.renderTimeline(historyList)}
        </div>
      </div>
    `;

    this.bindEvents();
  }

  renderStatusActions(currentStatus) {
    const actions = [];

    // Define available actions based on current status
    if (currentStatus === 'IN_STOCK') {
      actions.push({ id: 'deploy', label: '部署到设备', class: 'btn-primary' });
      actions.push({ id: 'scrap', label: '报废', class: 'btn-danger' });
    } else if (currentStatus === 'DEPLOYED') {
      actions.push({ id: 'retrieve', label: '从设备收回', class: 'btn-secondary' });
      actions.push({ id: 'markFaulty', label: '标记故障', class: 'btn-warning' });
    } else if (currentStatus === 'FAULTY') {
      actions.push({ id: 'sendRepair', label: '送修', class: 'btn-info' });
      actions.push({ id: 'scrap', label: '报废', class: 'btn-danger' });
    } else if (currentStatus === 'UNDER_REPAIR') {
      actions.push({ id: 'returnRepair', label: '维修归还', class: 'btn-success' });
    }

    if (actions.length === 0) {
      return '<div style="color: var(--color-text-secondary);">当前状态无可用操作</div>';
    }

    return actions.map(action => 
      `<button class="btn ${action.class}" data-action="${action.id}">${action.label}</button>`
    ).join('');
  }

  renderTimeline(historyList) {
    if (historyList.length === 0) {
      return Utils.renderEmptyState('📜', '暂无操作历史', '');
    }

    return historyList.map(item => `
      <div class="timeline-item">
        <div class="timeline-dot"></div>
        <div class="timeline-content">
          <div class="timeline-header">
            <div class="timeline-type">${Utils.getOperationTypeText(item.operationType)}</div>
            <div class="timeline-time">${Utils.formatDateTime(item.operationTime)}</div>
          </div>
          <div class="timeline-operator">操作人：${Utils.escapeHtml(item.operator || '系统')}</div>
          ${item.previousStatus || item.nextStatus ? `
            <div class="timeline-states">
              ${item.previousStatus ? `<span class="status-badge ${Utils.getStatusClass(item.previousStatus)}">${Utils.getStatusText(item.previousStatus)}</span>` : ''}
              ${item.previousStatus && item.nextStatus ? '<span>→</span>' : ''}
              ${item.nextStatus ? `<span class="status-badge ${Utils.getStatusClass(item.nextStatus)}">${Utils.getStatusText(item.nextStatus)}</span>` : ''}
            </div>
          ` : ''}
          ${item.remark ? `<div class="timeline-remark">${Utils.escapeHtml(item.remark)}</div>` : ''}
        </div>
      </div>
    `).join('');
  }

  bindEvents() {
    // Edit button
    const editBtn = this.container.querySelector('#editBtn');
    if (editBtn) {
      editBtn.addEventListener('click', () => {
        window.app.showPage('edit', { id: this.moduleId });
      });
    }

    // Delete button
    const deleteBtn = this.container.querySelector('#deleteBtn');
    if (deleteBtn) {
      deleteBtn.addEventListener('click', () => {
        this.handleDelete();
      });
    }

    // Status action buttons
    const actionBtns = this.container.querySelectorAll('.status-actions button[data-action]');
    actionBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        const action = btn.dataset.action;
        this.handleStatusAction(action);
      });
    });
  }

  handleDelete() {
    Utils.confirm('确定删除该光模块？此操作不可恢复。', async () => {
      try {
        Utils.showLoading();
        await API.deleteModule(this.moduleId);
        Utils.hideLoading();
        Utils.showToast('删除成功', 'success');
        window.app.showPage('list');
      } catch (error) {
        Utils.hideLoading();
        Utils.showToast('删除失败: ' + error.message, 'error');
      }
    });
  }

  handleStatusAction(action) {
    // TODO: Implement modal dialogs for each action type
    // For now, just show a placeholder
    Utils.showToast(`${action} 操作功能开发中`, 'info');
  }

  renderError(message) {
    this.container.innerHTML = `
      <div class="card">
        ${Utils.renderErrorState(
          message,
          '<button class="btn btn-secondary" onclick="window.app.showPage(\'list\')">返回列表</button>'
        )}
      </div>
    `;
  }

  refresh() {
    this.loadData();
  }

  getElement() {
    return this.container;
  }
}

// Make ModuleDetails globally available
window.ModuleDetails = ModuleDetails;
