#!/usr/bin/env python3
"""光模块管理系统 UI 视觉回归测试

测试目标：
1. 验证 CSS 变量系统正确加载
2. 验证关键组件样式一致性
3. 验证响应式布局不破坏功能
4. 验证动画和过渡效果
5. 验证颜色对比度符合 WCAG 2.1 AA 标准
"""

import pytest
import re
from pathlib import Path


class TestCSSVariables:
    """测试 CSS 变量系统"""

    @pytest.fixture
    def variables_css(self):
        """读取 variables.css 文件"""
        path = Path(__file__).parent.parent / 'frontend' / 'styles' / 'variables.css'
        return path.read_text(encoding='utf-8')

    def test_color_system_complete(self, variables_css):
        """测试主色调系统完整性"""
        required_colors = [
            '--color-primary:',
            '--color-primary-hover:',
            '--color-primary-light:',
            '--color-primary-dark:',
            '--color-secondary:',
            '--color-success:',
            '--color-warning:',
            '--color-danger:',
            '--color-info:',
        ]
        for color_var in required_colors:
            assert color_var in variables_css, f"缺少必要的颜色变量: {color_var}"

    def test_status_colors_complete(self, variables_css):
        """测试状态色映射完整性"""
        required_status = [
            '--status-in-stock:',
            '--status-deployed:',
            '--status-faulty:',
            '--status-under-repair:',
            '--status-scrapped:',
        ]
        for status_var in required_status:
            assert status_var in variables_css, f"缺少必要的状态颜色: {status_var}"

    def test_typography_system_complete(self, variables_css):
        """测试字体排版系统完整性"""
        required_typography = [
            '--font-sans:',
            '--font-mono:',
            '--text-xs:',
            '--text-sm:',
            '--text-base:',
            '--text-lg:',
            '--text-xl:',
            '--font-regular:',
            '--font-medium:',
            '--font-semibold:',
            '--font-bold:',
            '--leading-tight:',
            '--leading-normal:',
        ]
        for typo_var in required_typography:
            assert typo_var in variables_css, f"缺少必要的排版变量: {typo_var}"

    def test_spacing_system_complete(self, variables_css):
        """测试间距系统完整性（4px 基准倍数）"""
        required_spacing = [
            '--space-1:',  # 4px
            '--space-2:',  # 8px
            '--space-3:',  # 12px
            '--space-4:',  # 16px
            '--space-6:',  # 24px
            '--space-8:',  # 32px
        ]
        for space_var in required_spacing:
            assert space_var in variables_css, f"缺少必要的间距变量: {space_var}"

    def test_border_radius_system(self, variables_css):
        """测试圆角系统完整性"""
        required_radius = [
            '--radius-sm:',
            '--radius-base:',
            '--radius-lg:',
            '--radius-full:',
        ]
        for radius_var in required_radius:
            assert radius_var in variables_css, f"缺少必要的圆角变量: {radius_var}"

    def test_shadow_system_complete(self, variables_css):
        """测试阴影系统完整性"""
        required_shadows = [
            '--shadow-sm:',
            '--shadow-base:',
            '--shadow-md:',
            '--shadow-lg:',
            '--shadow-xl:',
        ]
        for shadow_var in required_shadows:
            assert shadow_var in variables_css, f"缺少必要的阴影变量: {shadow_var}"

    def test_animation_system(self, variables_css):
        """测试动效系统完整性"""
        required_animation = [
            '--duration-fast:',
            '--duration-base:',
            '--duration-slow:',
            '--ease-in:',
            '--ease-out:',
            '--ease-in-out:',
        ]
        for anim_var in required_animation:
            assert anim_var in variables_css, f"缺少必要的动效变量: {anim_var}"

    def test_keyframes_defined(self, variables_css):
        """测试关键帧动画定义"""
        required_keyframes = [
            '@keyframes spin',
            '@keyframes fadeIn',
            '@keyframes slideInDown',
            '@keyframes bounceIn',
        ]
        for keyframe in required_keyframes:
            assert keyframe in variables_css, f"缺少必要的关键帧动画: {keyframe}"


class TestComponentStyles:
    """测试组件样式一致性"""

    @pytest.fixture
    def components_css(self):
        """读取 components.css 文件"""
        path = Path(__file__).parent.parent / 'frontend' / 'styles' / 'components.css'
        return path.read_text(encoding='utf-8')

    def test_card_component(self, components_css):
        """测试卡片组件样式"""
        assert '.card {' in components_css
        assert '.card-header {' in components_css
        assert '.card-title {' in components_css
        assert '.card-actions {' in components_css
        # 检查是否使用新的 CSS 变量
        assert 'var(--bg-primary)' in components_css
        assert 'var(--radius-lg)' in components_css

    def test_button_variants(self, components_css):
        """测试按钮变体完整性"""
        button_classes = [
            '.btn {',
            '.btn-primary {',
            '.btn-secondary {',
            '.btn-success {',
            '.btn-warning {',
            '.btn-danger {',
            '.btn-sm {',
        ]
        for btn_class in button_classes:
            assert btn_class in components_css, f"缺少按钮样式: {btn_class}"

    def test_form_elements(self, components_css):
        """测试表单元素样式"""
        form_classes = [
            '.form-group {',
            '.form-label {',
            '.form-control {',
            '.form-error {',
            '.form-row {',
            '.form-actions {',
        ]
        for form_class in form_classes:
            assert form_class in components_css, f"缺少表单样式: {form_class}"

    def test_table_styles(self, components_css):
        """测试表格样式"""
        assert '.table-container {' in components_css
        assert '.table {' in components_css
        assert '.table thead {' in components_css
        assert '.table th {' in components_css
        assert '.table td {' in components_css
        assert '.table tbody tr:hover {' in components_css

    def test_status_badge_variants(self, components_css):
        """测试状态徽章所有变体"""
        status_classes = [
            '.status-badge {',
            '.status-badge.status-in_stock {',
            '.status-badge.status-deployed {',
            '.status-badge.status-faulty {',
            '.status-badge.status-under_repair {',
            '.status-badge.status-scrapped {',
        ]
        for status_class in status_classes:
            assert status_class in components_css, f"缺少状态徽章样式: {status_class}"

    def test_timeline_styles(self, components_css):
        """测试时间线样式"""
        assert '.timeline {' in components_css
        assert '.timeline-item {' in components_css
        assert '.timeline-content {' in components_css
        assert '.timeline-header {' in components_css
        assert '.change-item {' in components_css

    def test_empty_state_styles(self, components_css):
        """测试空状态样式"""
        assert '.empty-state {' in components_css
        assert '.empty-state-icon {' in components_css
        assert '.empty-state-text {' in components_css

    def test_details_grid_styles(self, components_css):
        """测试详情网格样式"""
        assert '.details-grid {' in components_css
        assert '.detail-item {' in components_css
        assert '.detail-label {' in components_css
        assert '.detail-value {' in components_css


class TestMainLayout:
    """测试主布局样式"""

    @pytest.fixture
    def main_css(self):
        """读取 main.css 文件"""
        path = Path(__file__).parent.parent / 'frontend' / 'styles' / 'main.css'
        return path.read_text(encoding='utf-8')

    def test_header_styles(self, main_css):
        """测试顶部导航栏样式"""
        assert '#header {' in main_css
        assert '.header-content {' in main_css
        assert '.system-title {' in main_css
        assert 'var(--header-height)' in main_css

    def test_sidebar_styles(self, main_css):
        """测试侧边栏样式"""
        assert '#sidebar {' in main_css
        assert '.sidebar-nav {' in main_css
        assert '.nav-menu {' in main_css
        assert '.nav-link {' in main_css
        assert 'var(--sidebar-width)' in main_css

    def test_main_content_layout(self, main_css):
        """测试主内容区布局"""
        assert '#main-content {' in main_css
        assert '#page-container {' in main_css
        assert 'var(--space-6)' in main_css

    def test_loading_overlay(self, main_css):
        """测试加载遮罩样式"""
        assert '#loading {' in main_css
        assert '.loading-spinner {' in main_css
        assert 'var(--z-loading)' in main_css
        assert 'animation: spin' in main_css

    def test_toast_notification(self, main_css):
        """测试 Toast 提示样式"""
        assert '#toast {' in main_css
        assert '#toast.success {' in main_css
        assert '#toast.error {' in main_css
        assert '#toast.warning {' in main_css
        assert 'var(--z-toast)' in main_css

    def test_responsive_design(self, main_css):
        """测试响应式断点"""
        assert '@media (max-width: 1024px)' in main_css
        assert '@media (max-width: 768px)' in main_css
        assert '@media (max-width: 576px)' in main_css

    def test_scrollbar_customization(self, main_css):
        """测试滚动条美化"""
        assert '::-webkit-scrollbar' in main_css
        assert '::-webkit-scrollbar-thumb' in main_css


class TestColorContrast:
    """测试颜色对比度（WCAG 2.1 AA 标准）"""

    def hex_to_rgb(self, hex_color):
        """将十六进制颜色转换为 RGB"""
        hex_color = hex_color.lstrip('#')
        return tuple(int(hex_color[i:i+2], 16) for i in (0, 2, 4))

    def relative_luminance(self, rgb):
        """计算相对亮度"""
        def adjust(c):
            c = c / 255.0
            return c / 12.92 if c <= 0.03928 else ((c + 0.055) / 1.055) ** 2.4
        r, g, b = [adjust(c) for c in rgb]
        return 0.2126 * r + 0.7152 * g + 0.0722 * b

    def contrast_ratio(self, color1, color2):
        """计算对比度"""
        l1 = self.relative_luminance(self.hex_to_rgb(color1))
        l2 = self.relative_luminance(self.hex_to_rgb(color2))
        lighter = max(l1, l2)
        darker = min(l1, l2)
        return (lighter + 0.05) / (darker + 0.05)

    def test_primary_button_contrast(self):
        """测试主按钮文字对比度（蓝底白字）"""
        bg_color = '#2563eb'  # color-primary
        text_color = '#ffffff'  # text-inverse
        ratio = self.contrast_ratio(bg_color, text_color)
        assert ratio >= 4.5, f"主按钮对比度 {ratio:.2f} 不符合 WCAG AA 标准（需>=4.5）"

    def test_success_badge_contrast(self):
        """测试成功徽章文字对比度（绿底绿字）"""
        bg_color = '#d1fae5'  # status-in-stock-bg
        text_color = '#10b981'  # status-in-stock
        ratio = self.contrast_ratio(bg_color, text_color)
        assert ratio >= 4.5, f"成功徽章对比度 {ratio:.2f} 不符合 WCAG AA 标准（需>=4.5）"

    def test_danger_badge_contrast(self):
        """测试危险徽章文字对比度（红底红字）"""
        bg_color = '#fee2e2'  # status-faulty-bg
        text_color = '#ef4444'  # status-faulty
        ratio = self.contrast_ratio(bg_color, text_color)
        assert ratio >= 4.5, f"危险徽章对比度 {ratio:.2f} 不符合 WCAG AA 标准（需>=4.5）"


class TestFileStructure:
    """测试文件结构完整性"""

    def test_css_files_exist(self):
        """测试所有 CSS 文件存在"""
        base_path = Path(__file__).parent.parent / 'frontend' / 'styles'
        required_files = [
            'reset.css',
            'variables.css',
            'main.css',
            'components.css',
            'module-list.css',
            'history-list.css',
            'responsive.css',
        ]
        for filename in required_files:
            file_path = base_path / filename
            assert file_path.exists(), f"缺少必要的 CSS 文件: {filename}"

    def test_html_loads_css_in_order(self):
        """测试 HTML 按正确顺序加载 CSS"""
        index_path = Path(__file__).parent.parent / 'frontend' / 'index.html'
        if index_path.exists():
            content = index_path.read_text(encoding='utf-8')
            css_order = ['reset.css', 'variables.css', 'main.css', 'components.css']
            last_pos = -1
            for css_file in css_order:
                pos = content.find(css_file)
                assert pos > last_pos, f"CSS 加载顺序错误: {css_file} 应在之前的文件之后"
                last_pos = pos


if __name__ == '__main__':
    pytest.main([__file__, '-v', '--tb=short'])
