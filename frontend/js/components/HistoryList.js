// 修改历史记录组件
class HistoryList {
    constructor(container, moduleId) {
        this.container = container;
        this.moduleId = moduleId;
        this.history = [];
    }
    
    async render() {
        try {
            Utils.showLoading();
            this.history = await API.modules.getHistory(this.moduleId);
            Utils.hideLoading();
            
            this.container.innerHTML = `
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">修改历史记录</h2>
                        <div class="card-actions">
                            <button class="btn btn-secondary" onclick="window.app.showPage('details', ${this.moduleId})">
                                ← 返回详情
                            </button>
                            <button class="btn btn-primary" onclick="window.app.showPage('list')">
                                返回列表
                            </button>
                        </div>
                    </div>
                    
                    ${this.history.length === 0 ? this.renderEmptyState() : this.renderTimeline()}
                </div>
            `;
        } catch (error) {
            Utils.hideLoading();
            Utils.showToast('加载历史记录失败: ' + error.message, 'error');
            this.container.innerHTML = `
                <div class="card">
                    <div class="empty-state">
                        <div class="empty-state-icon">⚠️</div>
                        <div class="empty-state-text">加载失败</div>
                        <button class="btn btn-secondary" onclick="window.app.showPage('details', ${this.moduleId})">返回详情</button>
                    </div>
                </div>
            `;
        }
    }
    
    renderEmptyState() {
        return `
            <div class="empty-state">
                <div class="empty-state-icon">📜</div>
                <div class="empty-state-text">暂无修改历史记录</div>
            </div>
        `;
    }
    
    renderTimeline() {
        return `
            <div class="timeline">
                ${this.history.map(item => this.renderTimelineItem(item)).join('')}
            </div>
        `;
    }
    
    renderTimelineItem(item) {
        return `
            <div class="timeline-item">
                <div class="timeline-content">
                    <div class="timeline-header">
                        <span class="timeline-type">${this.getOperationTypeText(item.operation_type)}</span>
                        <span class="timeline-time">${Utils.formatDateTime(item.timestamp)}</span>
                    </div>
                    
                    ${item.changes ? this.renderChanges(item.changes) : ''}
                    
                    ${item.user ? `
                        <div style="margin-top: 0.5rem; font-size: 0.85rem; color: #7f8c8d;">
                            操作人: ${item.user}
                        </div>
                    ` : ''}
                </div>
            </div>
        `;
    }
    
    renderChanges(changes) {
        if (!changes || typeof changes !== 'object') {
            return '';
        }
        
        const changeItems = Object.entries(changes).map(([field, change]) => {
            return this.renderChangeItem(field, change);
        }).join('');
        
        return `
            <div class="timeline-changes">
                ${changeItems}
            </div>
        `;
    }
    
    renderChangeItem(field, change) {
        const fieldName = this.getFieldName(field);
        
        // 如果有旧值和新值
        if (change.old_value !== undefined && change.new_value !== undefined) {
            return `
                <div class="change-item">
                    <div class="change-label">${fieldName}</div>
                    <div class="change-values">
                        <span class="old-value">${this.formatValue(change.old_value)}</span>
                        <span>→</span>
                        <span class="new-value">${this.formatValue(change.new_value)}</span>
                    </div>
                </div>
            `;
        }
        
        // 只有新值（创建操作）
        if (change.new_value !== undefined) {
            return `
                <div class="change-item">
                    <div class="change-label">${fieldName}</div>
                    <div class="change-values">
                        <span class="new-value">${this.formatValue(change.new_value)}</span>
                    </div>
                </div>
            `;
        }
        
        return '';
    }
    
    getOperationTypeText(type) {
        const typeMap = {
            'create': '✨ 创建',
            'update': '📝 更新',
            'delete': '🗑️ 删除'
        };
        return typeMap[type] || type;
    }
    
    getFieldName(field) {
        const fieldMap = {
            'module_number': '光模块编号',
            'vendor': '供应商',
            'model': '型号',
            'status': '状态',
            'wavelength': '波长',
            'data_rate': '传输速率',
            'distance': '传输距离',
            'temperature_range': '温度范围',
            'power': '功率',
            'connector_type': '连接器类型',
            'description': '描述信息'
        };
        return fieldMap[field] || field;
    }
    
    formatValue(value) {
        if (value === null || value === undefined || value === '') {
            return '-';
        }
        
        if (typeof value === 'boolean') {
            return value ? '是' : '否';
        }
        
        // 状态值转换
        if (typeof value === 'string') {
            const statusText = Utils.getStatusText(value);
            if (statusText !== value) {
                return statusText;
            }
        }
        
        return String(value);
    }
}