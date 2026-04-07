// 光模块列表组件
class ModuleList {
    constructor(container) {
        this.container = container;
        this.modules = [];
    }
    
    async render() {
        try {
            Utils.showLoading();
            this.modules = await API.modules.getAll();
            Utils.hideLoading();
            
            this.container.innerHTML = `
                <div class="card">
                    <div class="card-header">
                        <h2 class="card-title">光模块列表</h2>
                        <div class="card-actions">
                            <button class="btn btn-primary" id="create-module-btn">
                                ➕ 创建光模块
                            </button>
                        </div>
                    </div>
                    
                    ${this.modules.length === 0 ? this.renderEmptyState() : this.renderTable()}
                </div>
            `;
            
            this.attachEventListeners();
        } catch (error) {
            Utils.hideLoading();
            Utils.showToast('加载光模块列表失败: ' + error.message, 'error');
            this.container.innerHTML = Utils.renderErrorState(
                '加载失败',
                `<button class="btn btn-primary" onclick="window.app.showPage('list')">重试</button>`
            );
        }
    }
    
    renderEmptyState() {
        return Utils.renderEmptyState(
            '📋',
            '暂无光模块数据',
            `<button class="btn btn-primary" onclick="window.app.showPage('create')">创建第一个光模块</button>`
        );
    }
    
    renderTable() {
        return `
            <div class="table-container">
                <table class="table">
                    <thead>
                        <tr>
                            <th>编号</th>
                            <th>供应商</th>
                            <th>型号</th>
                            <th>状态</th>
                            <th>创建时间</th>
                            <th>操作</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${this.modules.map(module => this.renderTableRow(module)).join('')}
                    </tbody>
                </table>
            </div>
        `;
    }
    
    renderTableRow(module) {
        return `
            <tr>
                <td>${module.module_number || '-'}</td>
                <td>${module.vendor || '-'}</td>
                <td>${module.model || '-'}</td>
                <td>
                    <span class="status-badge ${Utils.getStatusClass(module.status)}">
                        ${Utils.getStatusText(module.status)}
                    </span>
                </td>
                <td>${Utils.formatDateTime(module.created_at)}</td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-primary btn-sm" onclick="window.app.showPage('details', ${module.id})">
                            查看
                        </button>
                        <button class="btn btn-warning btn-sm" onclick="window.app.showPage('edit', ${module.id})">
                            编辑
                        </button>
                        <button class="btn btn-danger btn-sm" data-delete-id="${module.id}">
                            删除
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }
    
    attachEventListeners() {
        // 创建按钮
        const createBtn = document.getElementById('create-module-btn');
        if (createBtn) {
            createBtn.addEventListener('click', () => {
                window.app.showPage('create');
            });
        }
        
        // 删除按钮
        const deleteButtons = document.querySelectorAll('[data-delete-id]');
        deleteButtons.forEach(btn => {
            btn.addEventListener('click', async (e) => {
                const moduleId = parseInt(e.target.getAttribute('data-delete-id'));
                await this.deleteModule(moduleId);
            });
        });
    }
    
    async deleteModule(moduleId) {
        Utils.confirm('确定要删除这个光模块吗？此操作不可恢复。', async () => {
            try {
                Utils.showLoading();
                await API.modules.delete(moduleId);
                Utils.hideLoading();
                Utils.showToast('光模块删除成功', 'success');
                await this.render();
            } catch (error) {
                Utils.hideLoading();
                Utils.showToast('删除失败: ' + error.message, 'error');
            }
        });
    }
}