// API Client
class API {
  static getAuthToken() {
    return localStorage.getItem('authToken');
  }

  static setAuthToken(token) {
    localStorage.setItem('authToken', token);
  }

  static removeAuthToken() {
    localStorage.removeItem('authToken');
  }

  static async request(endpoint, options = {}) {
    const url = CONFIG.API_BASE_URL + endpoint;
    const headers = {
      'Content-Type': 'application/json',
      ...options.headers
    };

    // Add Authorization header if token exists
    const token = this.getAuthToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
      headers,
      ...options
    };

    try {
      const response = await fetch(url, config);
      const contentType = response.headers.get('content-type') || '';
      const contentLength = response.headers.get('content-length');
      const isJson = contentType.includes('application/json');
      const isNoContent = response.status === 204 || response.status === 205 || contentLength === '0';

      // Gracefully handle responses without a body (e.g. DELETE 204)
      if (isNoContent) {
        if (!response.ok) {
          const error = new Error('Request failed');
          error.status = response.status;
          throw error;
        }
        return null;
      }

      // Handle non-JSON responses (like file downloads)
      if (!isJson) {
        if (!response.ok) {
          throw new Error('Request failed');
        }
        return response;
      }

      let data = null;
      try {
        if (typeof response.text === 'function') {
          const raw = await response.text();
          data = raw ? JSON.parse(raw) : null;
        } else if (typeof response.json === 'function') {
          data = await response.json();
        }
      } catch (parseError) {
        console.warn('Failed to parse JSON response', parseError);
      }

      if (!response.ok) {
        const error = new Error(
          (data && (data.message || data.error)) || 'Request failed'
        );
        error.status = response.status;
        if (data && data.validationErrors) {
          error.validationErrors = data.validationErrors;
        }

        // Handle 401 Unauthorized - redirect to login
        if (response.status === 401) {
          this.removeAuthToken();
          if (window.app && window.location.hash !== '#/login') {
            window.app.showPage('login');
          }
        }

        throw error;
      }

      return data;
    } catch (error) {
      console.error('API request failed:', error);
      throw error;
    }
  }

  // Auth APIs
  static async login(username, password) {
    const response = await this.request('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password })
    });
    if (response && response.token) {
      this.setAuthToken(response.token);
    }
    return response;
  }

  static async register(username, password, email, role) {
    return this.request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, password, email, role })
    });
  }

  static async getCurrentUser() {
    return this.request('/auth/me');
  }

  static logout() {
    this.removeAuthToken();
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

  static async uploadPhotodetectorFile(moduleId, vendorId, file) {
    const formData = new FormData();
    formData.append('file', file);

    const url = CONFIG.API_BASE_URL + `/modules/${moduleId}/vendor-infos/${vendorId}/photodetector-file`;
    const headers = {};
    const token = this.getAuthToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: formData
    });

    if (!response.ok) {
      const data = await response.json().catch(() => ({}));
      throw new Error(data.error || '上传失败');
    }

    return response.json();
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
