#!/bin/bash

################################################################################
# 光模块管理系统 - 自动化测试执行脚本
# 执行所有测试套件：后端、Python、前端
################################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 显示测试横幅
show_banner() {
    echo -e "${BLUE}"
    echo "═══════════════════════════════════════════════════════════════"
    echo "          光模块管理系统 - 自动化测试套件"
    echo "═══════════════════════════════════════════════════════════════"
    echo -e "${NC}"
}

# 检查依赖
check_dependencies() {
    log_info "检查测试环境依赖..."
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        log_error "未找到 Java，请安装 Java 11+"
        exit 1
    fi
    log_success "Java: $(java -version 2>&1 | head -n 1)"
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        log_error "未找到 Maven，请安装 Maven 3.6+"
        exit 1
    fi
    log_success "Maven: $(mvn -version | head -n 1)"
    
    # 检查 Python
    if ! command -v python3 &> /dev/null && ! command -v python &> /dev/null; then
        log_error "未找到 Python，请安装 Python 3.8+"
        exit 1
    fi
    PYTHON_CMD=$(command -v python3 || command -v python)
    log_success "Python: $($PYTHON_CMD --version)"
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        log_warning "未找到 Node.js，将跳过前端测试"
    else
        log_success "Node.js: $(node -version)"
    fi
    
    echo ""
}

# 运行后端 Java 测试
run_backend_tests() {
    log_info "开始运行后端 Java 测试..."
    echo "───────────────────────────────────────────────────────────────"
    
    cd ../backend
    
    if mvn clean test jacoco:report; then
        log_success "后端测试全部通过！"
        
        # 显示覆盖率摘要
        if [ -f "target/site/jacoco/index.html" ]; then
            log_info "覆盖率报告已生成：backend/target/site/jacoco/index.html"
        fi
    else
        log_error "后端测试失败！"
        return 1
    fi
    
    cd ../tests
    echo ""
}

# 运行 Python 测试
run_python_tests() {
    log_info "开始运行 Python 脚本测试..."
    echo "───────────────────────────────────────────────────────────────"
    
    PYTHON_CMD=$(command -v python3 || command -v python)
    
    # 检查 pytest 是否安装
    if ! $PYTHON_CMD -c "import pytest" &> /dev/null; then
        log_warning "pytest 未安装，正在安装..."
        $PYTHON_CMD -m pip install pytest pytest-cov
    fi
    
    # 运行测试
    if $PYTHON_CMD -m pytest backend/test_data_parser.py -v --cov=../backend/scripts --cov-report=html --cov-report=term; then
        log_success "Python 测试全部通过！"
        
        if [ -d "htmlcov" ]; then
            log_info "覆盖率报告已生成：tests/htmlcov/index.html"
        fi
    else
        log_error "Python 测试失败！"
        return 1
    fi
    
    echo ""
}

# 运行前端 Jest 测试
run_frontend_tests() {
    log_info "开始运行前端 Jest 测试..."
    echo "───────────────────────────────────────────────────────────────"
    
    if ! command -v node &> /dev/null; then
        log_warning "未找到 Node.js，跳过前端测试"
        return 0
    fi
    
    # 检查 npm 依赖是否安装
    if [ ! -d "node_modules" ]; then
        log_info "安装 npm 依赖..."
        npm install
    fi
    
    # 运行测试
    if npm run test:coverage; then
        log_success "前端测试全部通过！"
        
        if [ -d "coverage" ]; then
            log_info "覆盖率报告已生成：tests/coverage/lcov-report/index.html"
        fi
    else
        log_error "前端测试失败！"
        return 1
    fi
    
    echo ""
}

# 生成测试报告摘要
generate_summary() {
    log_info "生成测试报告摘要..."
    echo "───────────────────────────────────────────────────────────────"
    
    SUMMARY_FILE="test_summary_$(date +%Y%m%d_%H%M%S).txt"
    
    {
        echo "光模块管理系统 - 测试报告摘要"
        echo "生成时间: $(date '+%Y-%m-%d %H:%M:%S')"
        echo "═══════════════════════════════════════════════════════════════"
        echo ""
        
        echo "【后端 Java 测试】"
        if [ -f "../backend/target/surefire-reports/TEST-*.xml" ]; then
            TOTAL_TESTS=$(grep -oP 'tests="\K[0-9]+' ../backend/target/surefire-reports/TEST-*.xml | awk '{s+=$1} END {print s}')
            FAILED_TESTS=$(grep -oP 'failures="\K[0-9]+' ../backend/target/surefire-reports/TEST-*.xml | awk '{s+=$1} END {print s}')
            echo "  总测试数: $TOTAL_TESTS"
            echo "  失败数: $FAILED_TESTS"
            echo "  通过率: $(echo "scale=2; ($TOTAL_TESTS - $FAILED_TESTS) * 100 / $TOTAL_TESTS" | bc)%"
        else
            echo "  未找到测试报告"
        fi
        echo ""
        
        echo "【Python 测试】"
        if [ -f ".coverage" ]; then
            echo "  覆盖率报告已生成"
        else
            echo "  未找到覆盖率数据"
        fi
        echo ""
        
        echo "【前端 Jest 测试】"
        if [ -f "coverage/coverage-summary.json" ]; then
            echo "  覆盖率报告已生成"
        else
            echo "  未找到覆盖率数据"
        fi
        echo ""
        
        echo "═══════════════════════════════════════════════════════════════"
        echo "详细报告文件："
        echo "  - 后端: backend/target/site/jacoco/index.html"
        echo "  - Python: tests/htmlcov/index.html"
        echo "  - 前端: tests/coverage/lcov-report/index.html"
        
    } > "$SUMMARY_FILE"
    
    cat "$SUMMARY_FILE"
    log_success "测试摘要已保存到: $SUMMARY_FILE"
    echo ""
}

# 打开覆盖率报告（可选）
open_coverage_reports() {
    read -p "是否打开覆盖率报告？(y/n) " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # macOS
        if command -v open &> /dev/null; then
            [ -f "../backend/target/site/jacoco/index.html" ] && open ../backend/target/site/jacoco/index.html
            [ -f "htmlcov/index.html" ] && open htmlcov/index.html
            [ -f "coverage/lcov-report/index.html" ] && open coverage/lcov-report/index.html
        # Linux
        elif command -v xdg-open &> /dev/null; then
            [ -f "../backend/target/site/jacoco/index.html" ] && xdg-open ../backend/target/site/jacoco/index.html
            [ -f "htmlcov/index.html" ] && xdg-open htmlcov/index.html
            [ -f "coverage/lcov-report/index.html" ] && xdg-open coverage/lcov-report/index.html
        # Windows (Git Bash)
        elif command -v start &> /dev/null; then
            [ -f "../backend/target/site/jacoco/index.html" ] && start ../backend/target/site/jacoco/index.html
            [ -f "htmlcov/index.html" ] && start htmlcov/index.html
            [ -f "coverage/lcov-report/index.html" ] && start coverage/lcov-report/index.html
        else
            log_warning "无法自动打开浏览器，请手动打开报告文件"
        fi
    fi
}

# 主函数
main() {
    show_banner
    
    # 记录开始时间
    START_TIME=$(date +%s)
    
    # 检查依赖
    check_dependencies
    
    # 初始化状态
    BACKEND_STATUS=0
    PYTHON_STATUS=0
    FRONTEND_STATUS=0
    
    # 运行各类测试
    run_backend_tests || BACKEND_STATUS=$?
    run_python_tests || PYTHON_STATUS=$?
    run_frontend_tests || FRONTEND_STATUS=$?
    
    # 记录结束时间
    END_TIME=$(date +%s)
    DURATION=$((END_TIME - START_TIME))
    
    # 生成摘要
    generate_summary
    
    # 显示总体结果
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}                      测试执行完成${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════${NC}"
    echo ""
    
    echo "执行时间: ${DURATION} 秒"
    echo ""
    
    # 检查是否有失败的测试
    if [ $BACKEND_STATUS -ne 0 ] || [ $PYTHON_STATUS -ne 0 ] || [ $FRONTEND_STATUS -ne 0 ]; then
        log_error "部分测试失败，请查看上述日志"
        echo ""
        echo "失败的测试套件:"
        [ $BACKEND_STATUS -ne 0 ] && echo "  - 后端 Java 测试"
        [ $PYTHON_STATUS -ne 0 ] && echo "  - Python 脚本测试"
        [ $FRONTEND_STATUS -ne 0 ] && echo "  - 前端 Jest 测试"
        echo ""
        exit 1
    else
        log_success "所有测试全部通过！🎉"
        echo ""
    fi
    
    # 打开覆盖率报告
    open_coverage_reports
}

# 执行主函数
main "$@"
