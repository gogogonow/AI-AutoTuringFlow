// Login Component
class Login {
  constructor() {
    this.element = null;
  }

  getElement() {
    if (!this.element) {
      this.element = this.render();
    }
    return this.element;
  }

  updateText() {
    if (!this.element) return;

    // Update title
    const title = this.element.querySelector('.login-title');
    if (title) {
      title.textContent = '光模块管理系统';
    }

    // Update username label
    const usernameLabel = this.element.querySelector('label[for="username"]');
    if (usernameLabel) {
      usernameLabel.textContent = '用户名';
    }

    // Update password label
    const passwordLabel = this.element.querySelector('label[for="password"]');
    if (passwordLabel) {
      passwordLabel.textContent = '密码';
    }

    // Update login button
    const loginBtn = this.element.querySelector('button[type="submit"]');
    if (loginBtn) {
      loginBtn.textContent = '登录';
    }

    // Update footer text
    const footerParagraphs = this.element.querySelectorAll('.login-footer p');
    if (footerParagraphs.length >= 2) {
      footerParagraphs[0].innerHTML = '<strong>默认管理员账号:</strong> admin / admin123';
      footerParagraphs[1].innerHTML = '<strong>默认只读账号:</strong> reader / reader123';
    }
  }

  render() {
    const container = document.createElement('div');
    container.className = 'login-container';
    container.innerHTML = `
      <div class="login-card">
        <h2 class="login-title">光模块管理系统</h2>
        <form id="loginForm" class="login-form">
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              type="text"
              id="username"
              name="username"
              class="form-control"
              required
              autocomplete="username"
            />
          </div>
          <div class="form-group">
            <label for="password">密码</label>
            <input
              type="password"
              id="password"
              name="password"
              class="form-control"
              required
              autocomplete="current-password"
            />
          </div>
          <div class="form-actions">
            <button type="submit" class="btn btn-primary btn-block">登录</button>
          </div>
        </form>
        <div class="login-footer">
          <p><strong>默认管理员账号:</strong> admin / admin123</p>
          <p><strong>默认只读账号:</strong> reader / reader123</p>
        </div>
      </div>
    `;

    // Add event listener
    const form = container.querySelector('#loginForm');
    form.addEventListener('submit', (e) => this.handleLogin(e));

    return container;
  }

  async handleLogin(e) {
    e.preventDefault();

    const username = e.target.username.value.trim();
    const password = e.target.password.value;

    if (!username || !password) {
      Utils.showToast('请输入用户名和密码', 'error');
      return;
    }

    Utils.showLoading();
    try {
      const response = await API.login(username, password);

      // Store user info
      localStorage.setItem('currentUser', JSON.stringify({
        username: response.username,
        role: response.role,
        email: response.email
      }));

      Utils.showToast('登录成功', 'success');

      // Redirect to main page
      window.app.showPage('list');

      // Reload header to show user info
      if (window.app.header) {
        window.app.header.updateUserInfo();
      }
    } catch (error) {
      Utils.showToast(error.message || '登录失败', 'error');
    } finally {
      Utils.hideLoading();
    }
  }
}

// Make Login globally available
window.Login = Login;
