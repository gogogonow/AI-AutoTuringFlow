#!/usr/bin/env python3
"""光模块管理系统 UI 升级验证脚本

无需外部依赖的简单验证器，检查：
1. CSS 文件存在性
2. 关键 CSS 变量定义
3. 组件样式完整性
4. 文件结构一致性
"""

import sys
import re
from pathlib import Path


class Colors:
    """终端颜色"""
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    RESET = '\033[0m'
    BOLD = '\033[1m'


def print_pass(msg):
    print(f"{Colors.GREEN}✓{Colors.RESET} {msg}")


def print_fail(msg):
    print(f"{Colors.RED}✗{Colors.RESET} {msg}")


def print_info(msg):
    print(f"{Colors.BLUE}ℹ{Colors.RESET} {msg}")


def print_section(title):
    print(f"\n{Colors.BOLD}{Colors.BLUE}{'=' * 60}{Colors.RESET}")
    print(f"{Colors.BOLD}{title}{Colors.RESET}")
    print(f"{Colors.BOLD}{Colors.BLUE}{'=' * 60}{Colors.RESET}\n")


class UIUpgradeValidator:
    def __init__(self):
        self.project_root = Path(__file__).parent.parent
        self.frontend_dir = self.project_root / 'frontend'
        self.styles_dir = self.frontend_dir / 'styles'
        self.passed = 0
        self.failed = 0

    def validate_file_structure(self):
        """验证文件结构"""
        print_section("1. 文件结构验证")
        
        required_css_files = [
            'reset.css',
            'variables.css',
            'main.css',
            'components.css',
            'module-list.css',
            'history-list.css',
            'responsive.css',
        ]
        
        for filename in required_css_files:
            file_path = self.styles_dir / filename
            if file_path.exists():
                print_pass(f"{filename} 存在")
                self.passed += 1
            else:
                print_fail(f"{filename} 缺失")
                self.failed += 1

    def validate_variables_css(self):
        """验证 variables.css 中的关键变量"""
        print_section("2. CSS 变量系统验证")
        
        variables_path = self.styles_dir / 'variables.css'
        if not variables_path.exists():
            print_fail("variables.css 不存在，跳过验证")
            self.failed += 1
            return
        
        content = variables_path.read_text(encoding='utf-8')
        
        required_variables = {
            '主色调': [
                '--color-primary:',
                '--color-primary-hover:',
                '--color-secondary:',
            ],
            '功能色': [
                '--color-success:',
                '--color-warning:',
                '--color-danger:',
                '--color-info:',
            ],
            '状态色': [
                '--status-in-stock:',
                '--status-deployed:',
                '--status-faulty:',
                '--status-under-repair:',
                '--status-scrapped:',
            ],
            '字体排版': [
                '--font-sans:',
                '--font-mono:',
                '--text-base:',
                '--font-medium:',
                '--leading-normal:',
            ],
            '间距系统': [
                '--space-1:',
                '--space-2:',
                '--space-4:',
                '--space-6:',
            ],
            '圆角系统': [
                '--radius-base:',
                '--radius-lg:',
                '--radius-full:',
            ],
            '阴影系统': [
                '--shadow-sm:',
                '--shadow-base:',
                '--shadow-lg:',
            ],
            '动效系统': [
                '--duration-fast:',
                '--duration-base:',
                '--ease-out:',
            ],
        }
        
        for category, variables in required_variables.items():
            print_info(f"检查 {category}")
            for var in variables:
                if var in content:
                    print_pass(f"  {var} 已定义")
                    self.passed += 1
                else:
                    print_fail(f"  {var} 未定义")
                    self.failed += 1

    def validate_components_css(self):
        """验证 components.css 中的组件样式"""
        print_section("3. 组件样式验证")
        
        components_path = self.styles_dir / 'components.css'
        if not components_path.exists():
            print_fail("components.css 不存在，跳过验证")
            self.failed += 1
            return
        
        content = components_path.read_text(encoding='utf-8')
        
        required_components = {
            '卡片组件': ['.card {', '.card-header {', '.card-title {'],
            '按钮组件': ['.btn {', '.btn-primary {', '.btn-secondary {', '.btn-success {'],
            '表单组件': ['.form-group {', '.form-label {', '.form-control {'],
            '表格组件': ['.table {', '.table thead {', '.table td {'],
            '状态徽章': ['.status-badge {', '.status-badge.status-in_stock {'],
            '时间线': ['.timeline {', '.timeline-item {'],
            '空状态': ['.empty-state {'],
            '详情网格': ['.details-grid {', '.detail-item {'],
        }
        
        for category, selectors in required_components.items():
            print_info(f"检查 {category}")
            for selector in selectors:
                if selector in content:
                    print_pass(f"  {selector} 已定义")
                    self.passed += 1
                else:
                    print_fail(f"  {selector} 未定义")
                    self.failed += 1

    def validate_main_css(self):
        """验证 main.css 中的布局样式"""
        print_section("4. 主布局样式验证")
        
        main_path = self.styles_dir / 'main.css'
        if not main_path.exists():
            print_fail("main.css 不存在，跳过验证")
            self.failed += 1
            return
        
        content = main_path.read_text(encoding='utf-8')
        
        required_selectors = [
            '#header {',
            '#sidebar {',
            '#main-content {',
            '#loading {',
            '#toast {',
            '.nav-link {',
            '@media (max-width: 768px)',
        ]
        
        for selector in required_selectors:
            if selector in content:
                print_pass(f"{selector} 已定义")
                self.passed += 1
            else:
                print_fail(f"{selector} 未定义")
                self.failed += 1

    def validate_responsive_design(self):
        """验证响应式设计"""
        print_section("5. 响应式设计验证")
        
        main_path = self.styles_dir / 'main.css'
        responsive_path = self.styles_dir / 'responsive.css'
        
        breakpoints_found = False
        
        if main_path.exists():
            content = main_path.read_text(encoding='utf-8')
            breakpoints = [
                '@media (max-width: 1024px)',
                '@media (max-width: 768px)',
                '@media (max-width: 576px)',
            ]
            for bp in breakpoints:
                if bp in content:
                    print_pass(f"{bp} 已定义")
                    self.passed += 1
                    breakpoints_found = True
                else:
                    print_fail(f"{bp} 未定义")
                    self.failed += 1
        
        if responsive_path.exists():
            content = responsive_path.read_text(encoding='utf-8')
            if '@media' in content:
                print_pass("responsive.css 包含媒体查询")
                self.passed += 1
            else:
                print_fail("responsive.css 无媒体查询")
                self.failed += 1

    def validate_css_variable_usage(self):
        """验证 CSS 变量在组件中的使用"""
        print_section("6. CSS 变量使用验证")
        
        css_files = ['components.css', 'main.css']
        
        for css_file in css_files:
            path = self.styles_dir / css_file
            if not path.exists():
                continue
            
            content = path.read_text(encoding='utf-8')
            
            # 检查是否使用了新的变量命名
            new_vars_used = [
                'var(--bg-primary)',
                'var(--text-primary)',
                'var(--color-primary)',
                'var(--space-',
                'var(--radius-',
                'var(--shadow-',
            ]
            
            print_info(f"检查 {css_file}")
            for var_pattern in new_vars_used:
                if var_pattern in content:
                    print_pass(f"  使用了新变量: {var_pattern}")
                    self.passed += 1
                else:
                    print_fail(f"  未使用新变量: {var_pattern}")
                    self.failed += 1

    def print_summary(self):
        """打印验证摘要"""
        print_section("验证摘要")
        
        total = self.passed + self.failed
        pass_rate = (self.passed / total * 100) if total > 0 else 0
        
        print(f"总计测试项: {total}")
        print(f"{Colors.GREEN}通过: {self.passed}{Colors.RESET}")
        print(f"{Colors.RED}失败: {self.failed}{Colors.RESET}")
        print(f"通过率: {pass_rate:.1f}%")
        
        if self.failed == 0:
            print(f"\n{Colors.BOLD}{Colors.GREEN}✓ 所有验证通过！UI 升级成功。{Colors.RESET}")
            return 0
        else:
            print(f"\n{Colors.BOLD}{Colors.RED}✗ 发现 {self.failed} 个问题，请修复后重试。{Colors.RESET}")
            return 1

    def run(self):
        """运行所有验证"""
        print(f"{Colors.BOLD}{Colors.BLUE}光模块管理系统 UI 视觉升级验证{Colors.RESET}")
        print(f"项目根目录: {self.project_root}")
        print(f"前端目录: {self.frontend_dir}")
        
        self.validate_file_structure()
        self.validate_variables_css()
        self.validate_components_css()
        self.validate_main_css()
        self.validate_responsive_design()
        self.validate_css_variable_usage()
        
        return self.print_summary()


if __name__ == '__main__':
    validator = UIUpgradeValidator()
    sys.exit(validator.run())
