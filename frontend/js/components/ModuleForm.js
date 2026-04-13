// ModuleForm Component
class ModuleForm {
  constructor(options = {}) {
    this.container = null;
    this.isEditMode = false;
    this.moduleId = options.id || null;
    this.init();
  }

  init() {
    this.container = document.createElement('div');
    this.container.className = 'module-form-container';
    this.render();
  }

  async render() {
    this.container.innerHTML = `
      <div class="card">
        <div class="card-header">
          <h2 class="card-title" id="formTitle">入库登记</h2>
        </div>
        <form id="moduleForm" class="module-form" style="padding: var(--spacing-lg);">
          <!-- Basic Information -->
          <div class="form-section">
            <h3 class="form-section-title">基本信息</h3>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label" data-required>编码</label>
                  <input class="form-control" type="text" name="serialNumber" required>
                  <div class="form-error" id="error-serialNumber"></div>
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">端口速率</label>
                  <select class="form-control" name="speed">
                    <option value="">请选择</option>
                    <option value="1G">1G</option>
                    <option value="10G">10G</option>
                    <option value="25G">25G</option>
                    <option value="40G">40G</option>
                    <option value="100G">100G</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <!-- Technical Parameters -->
          <div class="form-section">
            <h3 class="form-section-title">技术参数</h3>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">波长（nm）</label>
                  <input class="form-control" type="text" name="wavelength" placeholder="如 850 / 1310 / 1550">
                  <div class="form-error" id="error-wavelength"></div>
                </div>
              </div>
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">传输距离（m）</label>
                  <input class="form-control" type="number" name="transmissionDistance" min="0">
                  <div class="form-error" id="error-transmissionDistance"></div>
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">接口类型</label>
                  <select class="form-control" name="connectorType">
                    <option value="">请选择</option>
                    <option value="LC">LC</option>
                    <option value="SC">SC</option>
                    <option value="MPO">MPO</option>
                    <option value="RJ45">RJ45</option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <!-- Comprehensive Specifications -->
          <div class="form-section">
            <h3 class="form-section-title">完整规格信息</h3>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">生命周期状态</label>
                  <select class="form-control" name="lifecycleStatus">
                    <option value="">请选择</option>
                    <option value="GA">GA（正式发布）</option>
                    <option value="EOM">EOM（停止销售）</option>
                    <option value="EOP">EOP（停止生产）</option>
                  </select>
                </div>
              </div>
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">封装形式</label>
                  <input class="form-control" type="text" name="packageForm" placeholder="如 SFP, QSFP28">
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">光纤数目</label>
                  <input class="form-control" type="number" name="fiberCount" min="0">
                </div>
              </div>
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">光类型</label>
                  <select class="form-control" name="lightType">
                    <option value="">请选择</option>
                    <option value="GRAY">灰光</option>
                    <option value="COLOR">彩光</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">速率集</label>
                  <input class="form-control" type="text" name="speedSet" placeholder="如 1G,10G,25G">
                </div>
              </div>
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">光纤类型</label>
                  <select class="form-control" name="fiberType">
                    <option value="">请选择</option>
                    <option value="SMF">SMF（单模）</option>
                    <option value="MMF">MMF（多模）</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">最大功耗（W）</label>
                  <input class="form-control" type="number" name="maxPowerConsumption" step="0.01" min="0">
                </div>
              </div>
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">最低工作壳温（°C）</label>
                  <input class="form-control" type="number" name="minCaseTemp">
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">最高工作壳温（°C）</label>
                  <input class="form-control" type="number" name="maxCaseTemp">
                </div>
              </div>
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">规格书模板版本</label>
                  <input class="form-control" type="text" name="specTemplateVersion">
                </div>
              </div>
            </div>
          </div>

          <!-- Shipment Information -->
          <div class="form-section">
            <h3 class="form-section-title">发货信息</h3>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">历史总发货量</label>
                  <input class="form-control" type="number" name="totalShipmentVolume" min="0" value="0">
                </div>
              </div>
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">近五年发货量</label>
                  <input class="form-control" type="number" name="recent5yearShipmentVolume" min="0" value="0">
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">发货地区</label>
                  <input class="form-control" type="text" name="shipmentRegions" placeholder="如 中国, 北美, 欧洲">
                </div>
              </div>
              <div class="form-col">
                <div class="form-group">
                  <label class="form-label">主流发货</label>
                  <select class="form-control" name="isMainstreamShipment">
                    <option value="false">否</option>
                    <option value="true">是</option>
                  </select>
                </div>
              </div>
            </div>
            <div class="form-row">
              <div class="form-col" style="flex: 1;">
                <div class="form-group">
                  <label class="form-label">当前发货厂家</label>
                  <input class="form-control" type="text" name="currentShippingVendors" placeholder="多个厂家用逗号分隔">
                </div>
              </div>
            </div>
          </div>

          <!-- Remarks -->
          <div class="form-section">
            <h3 class="form-section-title">备注信息</h3>
            <div class="form-row">
              <div class="form-col" style="flex: 1;">
                <div class="form-group">
                  <label class="form-label">备注</label>
                  <textarea class="form-control" name="remark" rows="4" placeholder="可选填写"></textarea>
                </div>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="form-actions">
            <button type="button" class="btn btn-secondary" id="cancelBtn">取消</button>
            <button type="submit" class="btn btn-success">保存</button>
          </div>
        </form>
      </div>
    `;

    this.bindEvents();
    
    // If edit mode, load module data
    if (this.moduleId) {
      await this.loadModuleData();
    }
  }

  bindEvents() {
    const form = this.container.querySelector('#moduleForm');
    form.addEventListener('submit', (e) => this.handleSubmit(e));

    const cancelBtn = this.container.querySelector('#cancelBtn');
    cancelBtn.addEventListener('click', () => this.handleCancel());
  }

  async loadModuleData() {
    try {
      Utils.showLoading();
      const module = await API.getModule(this.moduleId);
      Utils.hideLoading();

      this.isEditMode = true;
      const formTitle = this.container.querySelector('#formTitle');
      formTitle.textContent = '编辑光模块';

      // Populate form fields
      const form = this.container.querySelector('#moduleForm');
      Object.keys(module).forEach(key => {
        const input = form.elements[key];
        if (input && module[key] != null) {
          input.value = module[key];
        }
      });
    } catch (error) {
      Utils.hideLoading();
      this.container.innerHTML = Utils.renderErrorState(
        '加载失败: ' + error.message,
        '<button class="btn btn-secondary" onclick="window.app.showPage(\'list\')">返回列表</button>'
      );
    }
  }

  validate() {
    const errors = {};
    const form = this.container.querySelector('#moduleForm');
    const formData = new FormData(form);

    // Required fields
    const serialNumber = formData.get('serialNumber')?.trim();

    if (!serialNumber) {
      errors.serialNumber = '编码为必填项';
    } else if (serialNumber.length < 6 || serialNumber.length > 50) {
      errors.serialNumber = '编码长度应在 6-50 字符之间';
    } else if (/\s/.test(serialNumber)) {
      errors.serialNumber = '编码不能包含空格';
    }

    // Transmission distance validation
    const distance = formData.get('transmissionDistance');
    if (distance && parseInt(distance) < 0) {
      errors.transmissionDistance = '传输距离必须为正整数';
    }

    return errors;
  }

  showErrors(errors) {
    // Clear all errors first
    this.container.querySelectorAll('.form-error').forEach(el => el.textContent = '');
    this.container.querySelectorAll('.form-control.error').forEach(el => el.classList.remove('error'));

    // Show new errors
    Object.keys(errors).forEach(field => {
      const errorEl = this.container.querySelector(`#error-${field}`);
      const inputEl = this.container.querySelector(`[name="${field}"]`);
      
      if (errorEl) {
        errorEl.textContent = errors[field];
      }
      if (inputEl) {
        inputEl.classList.add('error');
      }
    });
  }

  async handleSubmit(e) {
    e.preventDefault();

    // Validate
    const errors = this.validate();
    if (Object.keys(errors).length > 0) {
      this.showErrors(errors);
      return;
    }

    // Collect form data
    const form = this.container.querySelector('#moduleForm');
    const formData = new FormData(form);
    const data = {};
    
    formData.forEach((value, key) => {
      if (value !== '') {
        data[key] = value;
      }
    });

    // Submit
    try {
      Utils.showLoading();
      
      if (this.isEditMode) {
        await API.updateModule(this.moduleId, data);
        Utils.hideLoading();
        Utils.showToast('更新成功', 'success');
      } else {
        await API.createModule(data);
        Utils.hideLoading();
        Utils.showToast('入库成功', 'success');
      }
      
      window.app.showPage('list');
    } catch (error) {
      Utils.hideLoading();
      
      // Handle backend validation errors
      if (error.validationErrors) {
        this.showErrors(error.validationErrors);
      } else {
        Utils.showToast('保存失败: ' + error.message, 'error');
      }
    }
  }

  handleCancel() {
    const form = this.container.querySelector('#moduleForm');
    const hasChanges = Array.from(form.elements).some(el => {
      if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
        return el.value.trim() !== '';
      }
      if (el.tagName === 'SELECT') {
        return el.value !== '' && el.value !== 'IN_STOCK';
      }
      return false;
    });

    if (hasChanges) {
      Utils.confirm('确定放弃修改？', () => {
        window.app.showPage('list');
      });
    } else {
      window.app.showPage('list');
    }
  }

  refresh() {
    if (this.moduleId) {
      this.loadModuleData();
    }
  }

  getElement() {
    return this.container;
  }
}

// Make ModuleForm globally available
window.ModuleForm = ModuleForm;
