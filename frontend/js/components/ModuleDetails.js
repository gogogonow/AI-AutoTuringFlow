// ModuleDetails Component
class ModuleDetails {
  constructor(options = {}) {
    this.container = null;
    this.moduleId = (options.id !== undefined && options.id !== null && options.id !== '') ? options.id : null;
    this.moduleData = null;
    this.vendorInfos = [];
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
      const historyData = await API.getModuleHistory(this.moduleId);
      this.vendorInfos = await API.getVendorInfos(this.moduleId);
      Utils.hideLoading();
      // getModuleHistory returns a plain array, not a paged response
      const historyList = Array.isArray(historyData) ? historyData : (historyData.content || []);
      this.render(historyList);
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

      <!-- Vendor Info Card -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">多厂家信息</h2>
          <div class="card-actions">
            <button class="btn btn-primary btn-sm" id="addVendorInfoBtn">➕ 新增厂家</button>
          </div>
        </div>
        <div id="vendorInfoSection">
          ${this.renderVendorInfoTable(this.vendorInfos)}
        </div>
      </div>

      <!-- Vendor Info Modal (hidden) -->
      <div id="vendorInfoModal" style="display:none; position:fixed; top:0; left:0; width:100%; height:100%;
        background:rgba(0,0,0,0.5); z-index:1000; overflow:auto;">
        <div style="background:#fff; margin:40px auto; padding:24px; border-radius:8px; max-width:700px; width:90%;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
            <h3 id="vendorModalTitle" style="margin:0;">新增厂家信息</h3>
            <button class="btn btn-secondary btn-sm" id="closeVendorModal">✕ 关闭</button>
          </div>
          <div id="vendorModalForm">
            ${this.renderVendorForm()}
          </div>
          <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:16px;">
            <button class="btn btn-secondary" id="cancelVendorBtn">取消</button>
            <button class="btn btn-primary" id="saveVendorBtn">保存</button>
          </div>
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

  renderVendorInfoTable(vendorInfos) {
    if (!vendorInfos || vendorInfos.length === 0) {
      return Utils.renderEmptyState('🏭', '暂无厂家信息', '');
    }
    return `
      <div style="overflow-x:auto;">
        <table class="table" style="min-width:1200px;">
          <thead>
            <tr>
              <th>厂家</th>
              <th>流程状态</th>
              <th>进入时间</th>
              <th>退出时间</th>
              <th>LD</th>
              <th>PD</th>
              <th>LA+LDO</th>
              <th>TIA</th>
              <th>MCU</th>
              <th>PCN变更点</th>
              <th>高速重点测试</th>
              <th>获取性</th>
              <th>电眼数据</th>
              <th>已覆盖单板</th>
              <th>测试报告</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            ${vendorInfos.map(vi => `
              <tr>
                <td>${Utils.escapeHtml(vi.vendor || '-')}</td>
                <td>${Utils.escapeHtml(vi.processStatus || '-')}</td>
                <td>${Utils.formatDateTime(vi.entryTime)}</td>
                <td>${Utils.formatDateTime(vi.exitTime)}</td>
                <td>${Utils.escapeHtml(vi.ld || '-')}</td>
                <td>${Utils.escapeHtml(vi.pd || '-')}</td>
                <td>${Utils.escapeHtml(vi.laLdo || '-')}</td>
                <td>${Utils.escapeHtml(vi.tia || '-')}</td>
                <td>${Utils.escapeHtml(vi.mcu || '-')}</td>
                <td style="max-width:120px; word-break:break-word;">${Utils.escapeHtml(vi.pcnChanges || '-')}</td>
                <td>${vi.highSpeedTestRecommended === true ? '✅ 是' : vi.highSpeedTestRecommended === false ? '❌ 否' : '-'}</td>
                <td>${Utils.escapeHtml(vi.availability || '-')}</td>
                <td style="max-width:120px; word-break:break-word;">${Utils.escapeHtml(vi.photodetectorData || '-')}</td>
                <td style="max-width:120px; word-break:break-word;">${Utils.escapeHtml(vi.coveredBoards || '-')}</td>
                <td>${vi.testReportLink ? `<a href="${Utils.escapeHtml(vi.testReportLink)}" target="_blank" rel="noopener noreferrer">查看报告</a>` : '-'}</td>
                <td>${Utils.escapeHtml(vi.remark || '-')}</td>
                <td>
                  <div class="action-buttons">
                    <button class="btn btn-secondary btn-sm" data-edit-vendor="${vi.id}">编辑</button>
                    <button class="btn btn-danger btn-sm" data-delete-vendor="${vi.id}">删除</button>
                  </div>
                </td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  }

  renderVendorForm(vi = {}) {
    return `
      <div class="form-row">
        <div class="form-col">
          <label class="form-label">厂家 <span style="color:red">*</span></label>
          <input class="form-control" id="vi_vendor" type="text" value="${Utils.escapeHtml(vi.vendor || '')}" placeholder="厂家名称">
        </div>
        <div class="form-col">
          <label class="form-label">流程状态</label>
          <input class="form-control" id="vi_processStatus" type="text" value="${Utils.escapeHtml(vi.processStatus || '')}" placeholder="如：引入中、已引入、已退出">
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <label class="form-label">进入时间</label>
          <input class="form-control" id="vi_entryTime" type="datetime-local" value="${this._toDatetimeLocal(vi.entryTime)}">
        </div>
        <div class="form-col">
          <label class="form-label">退出时间</label>
          <input class="form-control" id="vi_exitTime" type="datetime-local" value="${this._toDatetimeLocal(vi.exitTime)}">
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <label class="form-label">LD</label>
          <input class="form-control" id="vi_ld" type="text" value="${Utils.escapeHtml(vi.ld || '')}">
        </div>
        <div class="form-col">
          <label class="form-label">PD</label>
          <input class="form-control" id="vi_pd" type="text" value="${Utils.escapeHtml(vi.pd || '')}">
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <label class="form-label">LA+LDO</label>
          <input class="form-control" id="vi_laLdo" type="text" value="${Utils.escapeHtml(vi.laLdo || '')}">
        </div>
        <div class="form-col">
          <label class="form-label">TIA</label>
          <input class="form-control" id="vi_tia" type="text" value="${Utils.escapeHtml(vi.tia || '')}">
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <label class="form-label">MCU</label>
          <input class="form-control" id="vi_mcu" type="text" value="${Utils.escapeHtml(vi.mcu || '')}">
        </div>
        <div class="form-col">
          <label class="form-label">是否建议高速重点测试</label>
          <select class="form-control" id="vi_highSpeedTestRecommended">
            <option value="" ${vi.highSpeedTestRecommended === null || vi.highSpeedTestRecommended === undefined ? 'selected' : ''}>未设置</option>
            <option value="true" ${vi.highSpeedTestRecommended === true ? 'selected' : ''}>是</option>
            <option value="false" ${vi.highSpeedTestRecommended === false ? 'selected' : ''}>否</option>
          </select>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <label class="form-label">获取性</label>
          <input class="form-control" id="vi_availability" type="text" value="${Utils.escapeHtml(vi.availability || '')}">
        </div>
        <div class="form-col">
          <label class="form-label">导入测试报告（链接）</label>
          <input class="form-control" id="vi_testReportLink" type="url" value="${Utils.escapeHtml(vi.testReportLink || '')}" placeholder="https://...">
        </div>
      </div>
      <div class="form-row">
        <div class="form-col" style="grid-column: 1 / -1;">
          <label class="form-label">PCN变更点</label>
          <textarea class="form-control" id="vi_pcnChanges" rows="2">${Utils.escapeHtml(vi.pcnChanges || '')}</textarea>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col" style="grid-column: 1 / -1;">
          <label class="form-label">电眼数据</label>
          <textarea class="form-control" id="vi_photodetectorData" rows="2">${Utils.escapeHtml(vi.photodetectorData || '')}</textarea>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col" style="grid-column: 1 / -1;">
          <label class="form-label">目前已知已覆盖过的单板</label>
          <textarea class="form-control" id="vi_coveredBoards" rows="2">${Utils.escapeHtml(vi.coveredBoards || '')}</textarea>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col" style="grid-column: 1 / -1;">
          <label class="form-label">备注</label>
          <textarea class="form-control" id="vi_remark" rows="2">${Utils.escapeHtml(vi.remark || '')}</textarea>
        </div>
      </div>
    `;
  }

  _toDatetimeLocal(dtStr) {
    if (!dtStr) return '';
    // Backend returns ISO format: 2024-01-01T12:00:00
    try {
      return dtStr.slice(0, 16); // yyyy-MM-ddTHH:mm
    } catch (e) {
      return '';
    }
  }

  _readVendorForm() {
    const get = id => {
      const el = this.container.querySelector('#' + id);
      return el ? el.value.trim() : '';
    };
    const vendor = get('vi_vendor');
    if (!vendor) throw new Error('厂家名称不能为空');

    const hsVal = get('vi_highSpeedTestRecommended');
    const highSpeedTestRecommended = hsVal === 'true' ? true : hsVal === 'false' ? false : null;

    return {
      vendor,
      processStatus: get('vi_processStatus') || null,
      entryTime: get('vi_entryTime') ? get('vi_entryTime') + ':00' : null,
      exitTime: get('vi_exitTime') ? get('vi_exitTime') + ':00' : null,
      ld: get('vi_ld') || null,
      pd: get('vi_pd') || null,
      laLdo: get('vi_laLdo') || null,
      tia: get('vi_tia') || null,
      mcu: get('vi_mcu') || null,
      pcnChanges: get('vi_pcnChanges') || null,
      highSpeedTestRecommended,
      availability: get('vi_availability') || null,
      photodetectorData: get('vi_photodetectorData') || null,
      coveredBoards: get('vi_coveredBoards') || null,
      testReportLink: get('vi_testReportLink') || null,
      remark: get('vi_remark') || null
    };
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
          ${item.changeDetails ? `<div class="timeline-details" style="margin-top: 8px; padding: 8px; background-color: var(--color-bg-secondary, #f5f5f5); border-radius: 4px; font-size: 0.9em; color: var(--color-text-secondary, #666); white-space: pre-wrap; word-break: break-word;">${Utils.escapeHtml(item.changeDetails)}</div>` : ''}
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

    // Add vendor info button
    const addVendorInfoBtn = this.container.querySelector('#addVendorInfoBtn');
    if (addVendorInfoBtn) {
      addVendorInfoBtn.addEventListener('click', () => {
        this._openVendorModal(null);
      });
    }

    // Close/cancel vendor modal
    const closeVendorModal = this.container.querySelector('#closeVendorModal');
    const cancelVendorBtn = this.container.querySelector('#cancelVendorBtn');
    if (closeVendorModal) closeVendorModal.addEventListener('click', () => this._closeVendorModal());
    if (cancelVendorBtn) cancelVendorBtn.addEventListener('click', () => this._closeVendorModal());

    // Save vendor info
    const saveVendorBtn = this.container.querySelector('#saveVendorBtn');
    if (saveVendorBtn) {
      saveVendorBtn.addEventListener('click', () => this._saveVendorInfo());
    }

    // Edit/Delete vendor info rows
    this.container.addEventListener('click', (e) => {
      const editTarget = e.target.closest('[data-edit-vendor]');
      if (editTarget) {
        const id = editTarget.dataset.editVendor;
        const vi = this.vendorInfos.find(v => String(v.id) === String(id));
        if (vi) this._openVendorModal(vi);
        return;
      }
      const deleteTarget = e.target.closest('[data-delete-vendor]');
      if (deleteTarget) {
        const id = deleteTarget.dataset.deleteVendor;
        this._deleteVendorInfo(id);
      }
    });
  }

  _openVendorModal(vi) {
    this._editingVendorId = vi ? vi.id : null;
    const modal = this.container.querySelector('#vendorInfoModal');
    const title = this.container.querySelector('#vendorModalTitle');
    const form = this.container.querySelector('#vendorModalForm');
    if (title) title.textContent = vi ? '编辑厂家信息' : '新增厂家信息';
    if (form) form.innerHTML = this.renderVendorForm(vi || {});
    if (modal) modal.style.display = 'block';
  }

  _closeVendorModal() {
    const modal = this.container.querySelector('#vendorInfoModal');
    if (modal) modal.style.display = 'none';
    this._editingVendorId = null;
  }

  async _saveVendorInfo() {
    try {
      const data = this._readVendorForm();
      Utils.showLoading();
      if (this._editingVendorId) {
        await API.updateVendorInfo(this.moduleId, this._editingVendorId, data);
        Utils.showToast('更新成功', 'success');
      } else {
        await API.createVendorInfo(this.moduleId, data);
        Utils.showToast('新增成功', 'success');
      }
      this._closeVendorModal();
      // Reload vendor infos
      this.vendorInfos = await API.getVendorInfos(this.moduleId);
      Utils.hideLoading();
      const section = this.container.querySelector('#vendorInfoSection');
      if (section) section.innerHTML = this.renderVendorInfoTable(this.vendorInfos);
    } catch (error) {
      Utils.hideLoading();
      Utils.showToast('保存失败: ' + error.message, 'error');
    }
  }

  _deleteVendorInfo(id) {
    Utils.confirm('确定删除该厂家信息？', async () => {
      try {
        Utils.showLoading();
        await API.deleteVendorInfo(this.moduleId, id);
        this.vendorInfos = await API.getVendorInfos(this.moduleId);
        Utils.hideLoading();
        Utils.showToast('删除成功', 'success');
        const section = this.container.querySelector('#vendorInfoSection');
        if (section) section.innerHTML = this.renderVendorInfoTable(this.vendorInfos);
      } catch (error) {
        Utils.hideLoading();
        Utils.showToast('删除失败: ' + error.message, 'error');
      }
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

