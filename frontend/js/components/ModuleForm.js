// 光模块表单组件（创建和编辑）
class ModuleForm {
    constructor(container, moduleId = null) {
        this.container = container;
        this.moduleId = moduleId;
        this.module = null;
        this.errors = {};
    }
    
    async render() {
        const isEdit = this.moduleId !== null;
        
        if (isEdit) {
            try {
                Utils.showLoading();
                this.module = await API.modules.getById(this.moduleId);
                Utils.hideLoading();
            } catch (error) {
                Utils.hideLoading();
                Utils.showToast('加载光模块信息失败: ' + error.message, 'error');
                this.container.innerHTML = Utils.renderErrorState(
                    '加载失败',
                    `<button class="btn btn-secondary" onclick="window.app.showPage('list')">返回列表</button>`
                );
                return;
            }
        }
        
        this.container.innerHTML = `
            <div class="card">
                <div class="card-header">
                    <h2 class="card-title">${isEdit ? '编辑光模块' : '创建光模块'}</h2>
                    <div class="card-actions">
                        <button class="btn btn-secondary" onclick="window.app.showPage('list')">
                            ← 返回列表
                        </button>
                    </div>
                </div>
                
                <form id="module-form">
                    <div class="form-row">
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="module_number">光模块编号 *</label>
                                <input type="text" 
                                       id="module_number" 
                                       name="module_number" 
                                       class="form-control" 
                                       value="${this.module?.module_number || ''}"
                                       required>
                                <div class="form-error" id="error-module_number"></div>
                            </div>
                        </div>
                        
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="vendor">供应商 *</label>
                                <input type="text" 
                                       id="vendor" 
                                       name="vendor" 
                                       class="form-control" 
                                       value="${this.module?.vendor || ''}"
                                       required>
                                <div class="form-error" id="error-vendor"></div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-row">
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="model">型号 *</label>
                                <input type="text" 
                                       id="model" 
                                       name="model" 
                                       class="form-control" 
                                       value="${this.module?.model || ''}"
                                       required>
                                <div class="form-error" id="error-model"></div>
                            </div>
                        </div>
                        
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="status">状态 *</label>
                                <select id="status" name="status" class="form-control" required>
                                    <option value="active" ${this.module?.status === 'active' ? 'selected' : ''}>活跃</option>
                                    <option value="inactive" ${this.module?.status === 'inactive' ? 'selected' : ''}>停用</option>
                                    <option value="maintenance" ${this.module?.status === 'maintenance' ? 'selected' : ''}>维护中</option>
                                </select>
                                <div class="form-error" id="error-status"></div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-row">
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="wavelength">波长 (nm)</label>
                                <input type="number" 
                                       id="wavelength" 
                                       name="wavelength" 
                                       class="form-control" 
                                       value="${this.module?.wavelength || ''}">
                                <div class="form-error" id="error-wavelength"></div>
                            </div>
                        </div>
                        
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="data_rate">传输速率 (Gbps)</label>
                                <input type="number" 
                                       id="data_rate" 
                                       name="data_rate" 
                                       class="form-control" 
                                       value="${this.module?.data_rate || ''}">
                                <div class="form-error" id="error-data_rate"></div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-row">
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="distance">传输距离 (km)</label>
                                <input type="number" 
                                       id="distance" 
                                       name="distance" 
                                       class="form-control" 
                                       value="${this.module?.distance || ''}">
                                <div class="form-error" id="error-distance"></div>
                            </div>
                        </div>
                        
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="temperature_range">温度范围 (°C)</label>
                                <input type="text" 
                                       id="temperature_range" 
                                       name="temperature_range" 
                                       class="form-control" 
                                       value="${this.module?.temperature_range || ''}" 
                                       placeholder="例如: -40 to 85">
                                <div class="form-error" id="error-temperature_range"></div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-row">
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="power">功率 (mW)</label>
                                <input type="number" 
                                       step="0.01"
                                       id="power" 
                                       name="power" 
                                       class="form-control" 
                                       value="${this.module?.power || ''}">
                                <div class="form-error" id="error-power"></div>
                            </div>
                        </div>
                        
                        <div class="form-col">
                            <div class="form-group">
                                <label class="form-label" for="connector_type">连接器类型</label>
                                <input type="text" 
                                       id="connector_type" 
                                       name="connector_type" 
                                       class="form-control" 
                                       value="${this.module?.connector_type || ''}" 
                                       placeholder="例如: LC, SC">
                                <div class="form-error" id="error-connector_type"></div>
                            </div>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label class="form-label" for="description">描述信息</label>
                        <textarea id="description" 
                                  name="description" 
                                  class="form-control" 
                                  rows="4" 
                                  placeholder="可选的描述信息...">${this.module?.description || ''}</textarea>
                        <div class="form-error" id="error-description"></div>
                    </div>
                    
                    <div class="form-actions">
                        <button type="button" class="btn btn-secondary" onclick="window.app.showPage('list')">
                            取消
                        </button>
                        <button type="submit" class="btn btn-success">
                            ${isEdit ? '保存' : '创建'}
                        </button>
                    </div>
                </form>
            </div>
        `;
        
        this.attachEventListeners();
    }
    
    attachEventListeners() {
        const form = document.getElementById('module-form');
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.handleSubmit();
            });
        }
    }
    
    async handleSubmit() {
        this.clearErrors();
        
        const formData = new FormData(document.getElementById('module-form'));
        const data = {};
        
        for (const [key, value] of formData.entries()) {
            if (value.trim() !== '') {
                // 转换数字类型
                if (['wavelength', 'data_rate', 'distance'].includes(key)) {
                    data[key] = parseInt(value);
                } else if (key === 'power') {
                    data[key] = parseFloat(value);
                } else {
                    data[key] = value;
                }
            }
        }
        
        // 验证必填字段
        if (!this.validateForm(data)) {
            return;
        }
        
        try {
            Utils.showLoading();
            
            if (this.moduleId) {
                await API.modules.update(this.moduleId, data);
                Utils.showToast('光模块更新成功', 'success');
            } else {
                await API.modules.create(data);
                Utils.showToast('光模块创建成功', 'success');
            }
            
            Utils.hideLoading();
            window.app.showPage('list');
        } catch (error) {
            Utils.hideLoading();
            Utils.showToast('操作失败: ' + error.message, 'error');
        }
    }
    
    validateForm(data) {
        let isValid = true;
        
        // 验证必填字段
        const requiredFields = ['module_number', 'vendor', 'model', 'status'];
        for (const field of requiredFields) {
            if (!data[field]) {
                this.showError(field, '此字段为必填项');
                isValid = false;
            }
        }
        
        // 验证数字字段
        if (data.wavelength && data.wavelength < 0) {
            this.showError('wavelength', '波长必须大于0');
            isValid = false;
        }
        
        if (data.data_rate && data.data_rate < 0) {
            this.showError('data_rate', '传输速率必须大于0');
            isValid = false;
        }
        
        if (data.distance && data.distance < 0) {
            this.showError('distance', '传输距离必须大于0');
            isValid = false;
        }
        
        if (data.power && data.power < 0) {
            this.showError('power', '功率必须大于0');
            isValid = false;
        }
        
        return isValid;
    }
    
    showError(field, message) {
        const errorElement = document.getElementById(`error-${field}`);
        const inputElement = document.getElementById(field);
        
        if (errorElement) {
            errorElement.textContent = message;
        }
        
        if (inputElement) {
            inputElement.classList.add('error');
        }
        
        this.errors[field] = message;
    }
    
    clearErrors() {
        this.errors = {};
        
        document.querySelectorAll('.form-error').forEach(el => {
            el.textContent = '';
        });
        
        document.querySelectorAll('.form-control.error').forEach(el => {
            el.classList.remove('error');
        });
    }
}