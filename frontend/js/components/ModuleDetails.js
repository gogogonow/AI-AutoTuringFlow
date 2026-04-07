// 光模块详情组件
class ModuleDetails {
    constructor(container, moduleId) {
        this.container = container;
        this.moduleId = moduleId;
        this.module = null;
    }
    
    async render() {
        try {
            Utils.showLoading();
            this.module = await API.modules.getById(this.moduleId);
            Utils.hideLoading();
            
            this.container.innerHTML = `
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">光模块详情</h2>
                        <div class="card-actions">
                            <button class="btn btn-secondary" onclick="window.app.showPage('list')">
                                ← 返回列表
                            </button>
                            <button class="btn btn-warning" onclick="window.app.showPage('edit', ${this.moduleId})">
                                ✏️ 编辑
                            </button>
                            <button class="btn btn-primary" id="view-history-btn">
                                📜 查看历史
                            </button>
                        </div>
                    </div>
                    
                    ${this.renderDetails()}
                </div>
            `;
            
            this.attachEventListeners();
        } catch (error) {
            Utils.hideLoading();
            Utils.showToast('加载光模块详情失败: ' + error.message, 'error');
            this.container.innerHTML = Utils.renderErrorState(
                '加载失败',
                `<button class="btn btn-secondary" onclick="window.app.showPage('list')">返回列表</button>`
            );
        }
    }
    
    renderDetails() {
        return `
            <div class="details-grid">
                <div class="detail-item">
                    <div class="detail-label">光模块编号</div>
                    <div class="detail-value">${this.module.module_number || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">供应商</div>
                    <div class="detail-value">${this.module.vendor || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">型号</div>
                    <div class="detail-value">${this.module.model || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">状态</div>
                    <div class="detail-value">
                        <span class="status-badge ${Utils.getStatusClass(this.module.status)}">
                            ${Utils.getStatusText(this.module.status)}
                        </span>
                    </div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">波长 (nm)</div>
                    <div class="detail-value">${this.module.wavelength || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">传输速率 (Gbps)</div>
                    <div class="detail-value">${this.module.data_rate || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">传输距离 (km)</div>
                    <div class="detail-value">${this.module.distance || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">温度范围 (°C)</div>
                    <div class="detail-value">${this.module.temperature_range || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">功率 (mW)</div>
                    <div class="detail-value">${this.module.power || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">连接器类型</div>
                    <div class="detail-value">${this.module.connector_type || '-'}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">创建时间</div>
                    <div class="detail-value">${Utils.formatDateTime(this.module.created_at)}</div>
                </div>
                
                <div class="detail-item">
                    <div class="detail-label">更新时间</div>
                    <div class="detail-value">${Utils.formatDateTime(this.module.updated_at)}</div>
                </div>
            </div>
            
            ${this.module.description ? `
                <div style="margin-top: 1.5rem;">
                    <div class="detail-label">描述信息</div>
                    <div class="detail-value" style="margin-top: 0.5rem; padding: 1rem; background-color: #f8f9fa; border-radius: 4px;">
                        ${this.module.description}
                    </div>
                </div>
            ` : ''}
        `;
    }
    
    attachEventListeners() {
        const historyBtn = document.getElementById('view-history-btn');
        if (historyBtn) {
            historyBtn.addEventListener('click', () => {
                window.app.showPage('history', this.moduleId);
            });
        }
    }
}