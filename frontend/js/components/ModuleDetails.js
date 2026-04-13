// ModuleDetails Component
class ModuleDetails {
  constructor(options = {}) {
    this.container = null;
    this.moduleId = (options.id !== undefined && options.id !== null && options.id !== '') ? options.id : null;
    this.moduleData = null;
    this.vendorInfos = [];
    this.init();
  }

  isOwner() {
    const user = window.app ? window.app.getCurrentUser() : null;
    return user && user.role === 'OWNER';
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
    const isOwner = this.isOwner();

    this.container.innerHTML = `
      <!-- Basic Info Card -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">光模块详情</h2>
          <div class="card-actions">
            ${isOwner ? `
              <button class="btn btn-secondary btn-sm" id="editBtn">✏️ 编辑</button>
              <button class="btn btn-danger btn-sm" id="deleteBtn">🗑️ 删除</button>
            ` : ''}
          </div>
        </div>
        <div class="details-grid">
          <div class="detail-item">
            <div class="detail-label">编码</div>
            <div class="detail-value" id="detail-serialNumber">${Utils.escapeHtml(module.serialNumber || '-')}</div>
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
            <div class="detail-label">入库时间</div>
            <div class="detail-value" id="detail-inboundTime">${Utils.formatDateTime(module.inboundTime)}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">生命周期状态</div>
            <div class="detail-value" id="detail-lifecycleStatus">${Utils.escapeHtml(module.lifecycleStatus || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">封装形式</div>
            <div class="detail-value" id="detail-packageForm">${Utils.escapeHtml(module.packageForm || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">光纤数目</div>
            <div class="detail-value" id="detail-fiberCount">${module.fiberCount || '-'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">光类型</div>
            <div class="detail-value" id="detail-lightType">${Utils.escapeHtml(module.lightType || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">速率集</div>
            <div class="detail-value" id="detail-speedSet">${Utils.escapeHtml(module.speedSet || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">光纤类型</div>
            <div class="detail-value" id="detail-fiberType">${Utils.escapeHtml(module.fiberType || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">最大功耗</div>
            <div class="detail-value" id="detail-maxPowerConsumption">${module.maxPowerConsumption ? module.maxPowerConsumption + ' W' : '-'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">最低工作壳温</div>
            <div class="detail-value" id="detail-minCaseTemp">${module.minCaseTemp !== null && module.minCaseTemp !== undefined ? module.minCaseTemp + ' °C' : '-'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">最高工作壳温</div>
            <div class="detail-value" id="detail-maxCaseTemp">${module.maxCaseTemp !== null && module.maxCaseTemp !== undefined ? module.maxCaseTemp + ' °C' : '-'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">最后发货时间</div>
            <div class="detail-value" id="detail-lastShipmentTime">${Utils.formatDateTime(module.lastShipmentTime) || '-'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">历史总发货量</div>
            <div class="detail-value" id="detail-totalShipmentVolume">${module.totalShipmentVolume || '0'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">近五年发货量</div>
            <div class="detail-value" id="detail-recent5yearShipmentVolume">${module.recent5yearShipmentVolume || '0'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">发货地区</div>
            <div class="detail-value" id="detail-shipmentRegions">${Utils.escapeHtml(module.shipmentRegions || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">主流发货</div>
            <div class="detail-value" id="detail-isMainstreamShipment">${module.isMainstreamShipment ? '是' : '否'}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">规格书模板版本</div>
            <div class="detail-value" id="detail-specTemplateVersion">${Utils.escapeHtml(module.specTemplateVersion || '-')}</div>
          </div>
          <div class="detail-item">
            <div class="detail-label">当前发货厂家</div>
            <div class="detail-value" id="detail-currentShippingVendors">${Utils.escapeHtml(module.currentShippingVendors || '-')}</div>
          </div>
          <div class="detail-item" style="grid-column: 1 / -1;">
            <div class="detail-label">备注</div>
            <div class="detail-value" id="detail-remark">${Utils.escapeHtml(module.remark || '-')}</div>
          </div>
        </div>
      </div>

      <!-- Vendor Info Card -->
      <div class="card">
        <div class="card-header">
          <h2 class="card-title">多厂家信息</h2>
          <div class="card-actions">
            ${isOwner ? '<button class="btn btn-primary btn-sm" id="addVendorInfoBtn">➕ 新增厂家</button>' : ''}
          </div>
        </div>
        <div style="padding: 8px 16px; background: #f0f7ff; border-bottom: 1px solid #e0e0e0; font-size: 0.9em; color: #555;">
          💡 <strong>提示：</strong>支持为每个光模块配置多个供应商厂家；同一厂家可添加多条记录，使用"版本/批次"字段区分不同供货时期或版本
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
    const isOwner = this.isOwner();

    if (!vendorInfos || vendorInfos.length === 0) {
      return Utils.renderEmptyState('🏭', '暂无厂家信息', '');
    }
    return `
      <div style="overflow-x:auto;">
        <table class="table" style="min-width:1200px;">
          <thead>
            <tr>
              <th>厂家</th>
              <th>版本/批次</th>
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
              ${isOwner ? '<th>操作</th>' : ''}
            </tr>
          </thead>
          <tbody>
            ${vendorInfos.map(vi => {
              // Split covered boards and test reports into arrays for multi-row display
              const boards = (vi.coveredBoards || '').split(/[,，;；\n]/).map(s => s.trim()).filter(Boolean);
              const reports = (vi.testReportLink || '').split(/[,，;；\n]/).map(s => s.trim()).filter(Boolean);
              const maxRows = Math.max(boards.length, reports.length, 1);

              let rows = '';
              for (let i = 0; i < maxRows; i++) {
                rows += '<tr>';
                if (i === 0) {
                  // First row: all vendor fields with rowspan
                  const rs = maxRows > 1 ? ` rowspan="${maxRows}"` : '';
                  rows += `<td${rs}>${Utils.escapeHtml(vi.vendor || '-')}</td>`;
                  rows += `<td${rs}>${Utils.escapeHtml(vi.versionBatch || '-')}</td>`;
                  rows += `<td${rs}>${Utils.escapeHtml(vi.processStatus || '-')}</td>`;
                  rows += `<td${rs}>${Utils.formatDateTime(vi.entryTime)}</td>`;
                  rows += `<td${rs}>${Utils.formatDateTime(vi.exitTime)}</td>`;
                  rows += `<td${rs}>${Utils.escapeHtml(vi.ld || '-')}</td>`;
                  rows += `<td${rs}>${Utils.escapeHtml(vi.pd || '-')}</td>`;
                  rows += `<td${rs}>${Utils.escapeHtml(vi.laLdo || '-')}</td>`;
                  rows += `<td${rs}>${Utils.escapeHtml(vi.tia || '-')}</td>`;
                  rows += `<td${rs}>${Utils.escapeHtml(vi.mcu || '-')}</td>`;
                  rows += `<td${rs} style="max-width:120px; word-break:break-word;">${Utils.escapeHtml(vi.pcnChanges || '-')}</td>`;
                  rows += `<td${rs}>${vi.highSpeedTestRecommended === true ? '✅ 是' : vi.highSpeedTestRecommended === false ? '❌ 否' : '-'}</td>`;
                  rows += `<td${rs}>${Utils.escapeHtml(vi.availability || '-')}</td>`;
                  // Photodetector data - file upload only
                  let photoCell = '';
                  if (vi.photodetectorDataFile) {
                    const safeFilename = encodeURIComponent(vi.photodetectorDataFile);
                    photoCell = `<a href="${CONFIG.API_BASE_URL}/uploads/photodetector/${safeFilename}" target="_blank" rel="noopener noreferrer">📎 查看文件</a>`;
                  } else {
                    photoCell = '-';
                  }
                  if (isOwner) {
                    photoCell += `<br><label class="btn btn-secondary btn-sm" style="margin-top:4px; cursor:pointer; font-size:0.8em;">
                      📤 上传文件
                      <input type="file" style="display:none;" data-upload-vendor="${parseInt(vi.id, 10)}" data-module-id="${parseInt(this.moduleId, 10)}">
                    </label>`;
                  }
                  rows += `<td${rs} style="max-width:150px; word-break:break-word;">${photoCell}</td>`;
                }
                // Covered board for this row
                rows += `<td style="max-width:120px; word-break:break-word;">${boards[i] ? Utils.escapeHtml(boards[i]) : '-'}</td>`;
                // Test report for this row
                const reportUrl = reports[i] || '';
                const isValidUrl = reportUrl.startsWith('http://') || reportUrl.startsWith('https://');
                rows += `<td>${isValidUrl ? `<a href="${Utils.escapeHtml(reportUrl)}" target="_blank" rel="noopener noreferrer">查看报告</a>` : (reportUrl ? Utils.escapeHtml(reportUrl) : '-')}</td>`;

                if (i === 0) {
                  const rs = maxRows > 1 ? ` rowspan="${maxRows}"` : '';
                  rows += `<td${rs}>${Utils.escapeHtml(vi.remark || '-')}</td>`;
                  if (isOwner) {
                    rows += `<td${rs}>
                      <div class="action-buttons">
                        <button class="btn btn-secondary btn-sm" data-edit-vendor="${parseInt(vi.id, 10)}">编辑</button>
                        <button class="btn btn-danger btn-sm" data-delete-vendor="${parseInt(vi.id, 10)}">删除</button>
                      </div>
                    </td>`;
                  }
                }
                rows += '</tr>';
              }
              return rows;
            }).join('')}
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
          <label class="form-label">版本/批次</label>
          <input class="form-control" id="vi_versionBatch" type="text" value="${Utils.escapeHtml(vi.versionBatch || '')}" placeholder="如：V1.0、批次2024Q1">
          <small style="color:#666; font-size:0.85em;">用于区分同一厂家的不同供货时期或版本</small>
        </div>
      </div>
      <div class="form-row">
        <div class="form-col">
          <label class="form-label">流程状态</label>
          <input class="form-control" id="vi_processStatus" type="text" value="${Utils.escapeHtml(vi.processStatus || '')}" placeholder="如：引入中、已引入、已退出">
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
          <label class="form-label">获取性</label>
          <input class="form-control" id="vi_availability" type="text" value="${Utils.escapeHtml(vi.availability || '')}">
        </div>
      </div>
      <div class="form-row">
        <div class="form-col" style="grid-column: 1 / -1;">
          <label class="form-label">导入测试报告（链接）</label>
          <textarea class="form-control" id="vi_testReportLink" rows="2" placeholder="多个链接可用逗号、分号或换行分隔">${Utils.escapeHtml(vi.testReportLink || '')}</textarea>
          <small style="color:#666; font-size:0.85em;">支持多个链接，用逗号、分号或换行分隔</small>
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
          <div style="display:flex; gap:8px; align-items:center;">
            ${vi.photodetectorDataFile ? `<span style="color:#28a745; font-size:0.85em;">📎 已上传文件</span>` : '<span style="color:#999; font-size:0.85em;">暂无文件</span>'}
          </div>
          <small style="color:#666; font-size:0.85em;">电眼数据仅支持上传文件，请在厂家信息表中通过"上传文件"按钮上传</small>
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
      versionBatch: get('vi_versionBatch') || null,
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
      coveredBoards: get('vi_coveredBoards') || null,
      testReportLink: get('vi_testReportLink') || null,
      remark: get('vi_remark') || null
    };
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

    // File upload for photodetector data
    this.container.addEventListener('change', (e) => {
      const uploadInput = e.target.closest('[data-upload-vendor]');
      if (uploadInput && uploadInput.files.length > 0) {
        const vendorId = uploadInput.dataset.uploadVendor;
        const moduleId = uploadInput.dataset.moduleId;
        this._uploadPhotodetectorFile(moduleId, vendorId, uploadInput.files[0]);
        uploadInput.value = '';
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

  async _uploadPhotodetectorFile(moduleId, vendorId, file) {
    try {
      Utils.showLoading();
      await API.uploadPhotodetectorFile(moduleId, vendorId, file);
      this.vendorInfos = await API.getVendorInfos(this.moduleId);
      Utils.hideLoading();
      Utils.showToast('文件上传成功', 'success');
      const section = this.container.querySelector('#vendorInfoSection');
      if (section) section.innerHTML = this.renderVendorInfoTable(this.vendorInfos);
    } catch (error) {
      Utils.hideLoading();
      Utils.showToast('文件上传失败: ' + error.message, 'error');
    }
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

