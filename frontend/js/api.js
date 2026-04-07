// API Client
class API {
  static async request(endpoint, options = {}) {
    const url = CONFIG.API_BASE_URL + endpoint;
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      },
      ...options
    };

    try {
      const response = await fetch(url, config);
      
      // Handle non-JSON responses (like file downloads)
      const contentType = response.headers.get('content-type');
      if (contentType && !contentType.includes('application/json')) {
        if (!response.ok) {
          throw new Error('Request failed');
        }
        return response;
      }

      const data = await response.json();

      if (!response.ok) {
        const error = new Error(data.message || data.error || 'Request failed');
        error.status = response.status;
        error.validationErrors = data.validationErrors;
        throw error;
      }

      return data;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // Module APIs
  static async getModules(params = {}) {
    const queryString = new URLSearchParams(params).toString();
    return this.request(`/modules${queryString ? '?' + queryString : ''}`);
  }

  static async getModule(id) {
    return this.request(`/modules/${id}`);
  }

  static async createModule(data) {
    return this.request('/modules', {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  static async updateModule(id, data) {
    return this.request(`/modules/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }

  static async deleteModule(id) {
    return this.request(`/modules/${id}`, {
      method: 'DELETE'
    });
  }

  static async changeModuleStatus(id, action, data = {}) {
    return this.request(`/modules/${id}/status`, {
      method: 'POST',
      body: JSON.stringify({ action, ...data })
    });
  }

  static async batchInbound(modules) {
    return this.request('/modules/batch', {
      method: 'POST',
      body: JSON.stringify(modules)
    });
  }

  static async importModules(file) {
    const formData = new FormData();
    formData.append('file', file);
    return this.request('/modules/import', {
      method: 'POST',
      headers: {}, // Let browser set Content-Type for FormData
      body: formData
    });
  }

  static async exportModules(filters = {}) {
    const queryString = new URLSearchParams(filters).toString();
    const response = await this.request(`/modules/export${queryString ? '?' + queryString : ''}`);
    
    // Trigger download
    const blob = response instanceof Response ? await response.blob() : new Blob([response], {type: 'application/octet-stream'});
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'modules_' + new Date().getTime() + '.xlsx';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }

  // Vendor Info APIs
  static async getVendorInfos(moduleId) {
    return this.request(`/modules/${moduleId}/vendor-infos`);
  }

  static async createVendorInfo(moduleId, data) {
    return this.request(`/modules/${moduleId}/vendor-infos`, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }

  static async updateVendorInfo(moduleId, id, data) {
    return this.request(`/modules/${moduleId}/vendor-infos/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }

  static async deleteVendorInfo(moduleId, id) {
    return this.request(`/modules/${moduleId}/vendor-infos/${id}`, {
      method: 'DELETE'
    });
  }

  // History APIs
  static async getHistories(params = {}) {
    const queryString = new URLSearchParams(params).toString();
    return this.request(`/histories${queryString ? '?' + queryString : ''}`);
  }

  static async getModuleHistory(moduleId, params = {}) {
    const queryString = new URLSearchParams(params).toString();
    return this.request(`/histories/module/${moduleId}${queryString ? '?' + queryString : ''}`);
  }
}

// Make API globally available
window.API = API;
