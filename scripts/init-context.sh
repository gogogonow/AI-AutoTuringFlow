#!/usr/bin/env bash
# =============================================================================
# scripts/init-context.sh
# 项目上下文初始化脚本
#
# 用途：首次为项目建立固定项目上下文文档结构。
#       在 docs/ 目录生成基础文档，扫描现有代码结构填充初始内容。
#
# 使用方法：
#   chmod +x scripts/init-context.sh
#   ./scripts/init-context.sh
#
# 前置条件：
#   - 从仓库根目录执行
#   - 无需额外依赖（纯 bash）
# =============================================================================

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DOCS_DIR="${REPO_ROOT}/docs"
SNAPSHOTS_DIR="${DOCS_DIR}/snapshots"
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
DATE_TAG=$(date -u +"%Y.%m.%d")

# 颜色输出
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }

# =============================================================================
# 1. 确保目录结构存在
# =============================================================================
init_directories() {
    log_info "Creating docs directory structure..."
    mkdir -p "${DOCS_DIR}/templates"
    mkdir -p "${SNAPSHOTS_DIR}"
    log_success "Directories ready: docs/, docs/templates/, docs/snapshots/"
}

# =============================================================================
# 2. 扫描前端目录结构
# =============================================================================
scan_frontend() {
    local frontend_dir="${REPO_ROOT}/frontend"
    if [ ! -d "${frontend_dir}" ]; then
        echo "  (frontend/ directory not found)"
        return
    fi

    echo "  Files:"
    find "${frontend_dir}" -type f \
        \( -name "*.html" -o -name "*.js" -o -name "*.css" \) \
        ! -path "*/node_modules/*" \
        | sed "s|${REPO_ROOT}/||" \
        | sort \
        | head -30 \
        | while read -r f; do echo "    - ${f}"; done
}

# =============================================================================
# 3. 扫描后端目录结构
# =============================================================================
scan_backend() {
    local backend_dir="${REPO_ROOT}/backend/src/main/java"
    if [ ! -d "${backend_dir}" ]; then
        echo "  (backend/src/main/java directory not found)"
        return
    fi

    echo "  Java source files:"
    find "${backend_dir}" -name "*.java" \
        | sed "s|${REPO_ROOT}/||" \
        | sort \
        | head -40 \
        | while read -r f; do echo "    - ${f}"; done
}

# =============================================================================
# 4. 提取后端 API 路径（从 Controller 文件中）
# =============================================================================
extract_apis() {
    local backend_dir="${REPO_ROOT}/backend/src/main/java"
    if [ ! -d "${backend_dir}" ]; then
        echo "  (no backend directory found)"
        return
    fi

    echo "  Detected API annotations:"
    grep -r --include="*.java" \
        -E "@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)" \
        "${backend_dir}" 2>/dev/null \
        | grep -oE '"[^"]*"' \
        | sort -u \
        | head -20 \
        | while read -r path; do echo "    - ${path}"; done || echo "  (none found)"
}

# =============================================================================
# 5. 更新 project-context.md 中的刷新时间戳
# =============================================================================
update_context_timestamp() {
    local ctx_file="${DOCS_DIR}/project-context.md"
    if [ ! -f "${ctx_file}" ]; then
        log_warn "project-context.md not found, skipping timestamp update"
        return
    fi

    # Replace the refresh time placeholder or existing timestamp.
    # Use ~ as delimiter to avoid conflict with | in Markdown table syntax.
    if grep -q "<!-- CONTEXT_REFRESH_TIME -->" "${ctx_file}"; then
        sed -i "s|<!-- CONTEXT_REFRESH_TIME -->|${TIMESTAMP}|g" "${ctx_file}"
    else
        sed -i "s~\(| \*\*上下文最后刷新时间\*\* | \)[^|]*~\1${TIMESTAMP} ~" "${ctx_file}" || true
    fi
    log_success "Updated refresh timestamp in project-context.md"
}

# =============================================================================
# 6. 生成初始上下文快照
# =============================================================================
generate_snapshot() {
    local snapshot_file="${SNAPSHOTS_DIR}/context-snapshot-${DATE_TAG}-001.md"

    if [ -f "${snapshot_file}" ]; then
        log_warn "Snapshot for today already exists: ${snapshot_file}"
        log_warn "Skipping snapshot generation. Use refresh-context.sh for subsequent updates."
        return
    fi

    log_info "Generating initial context snapshot..."

    cat > "${snapshot_file}" << SNAPSHOT_EOF
# 项目上下文快照 — 初始化快照

## 快照元数据

| 字段 | 内容 |
|------|------|
| **快照版本** | v${DATE_TAG}.001 |
| **生成时间** | ${TIMESTAMP} |
| **生成方式** | \`scripts/init-context.sh\` 自动生成 |
| **触发原因** | 项目上下文初始化 |
| **关联 PR/Issue** | （初始化，无关联） |
| **生成人/Agent** | $(git config user.name 2>/dev/null || echo "unknown") |

## 1. 初始化时扫描到的文件结构

### 前端文件

$(scan_frontend)

### 后端文件

$(scan_backend)

## 2. 初始化时检测到的 API 路径

$(extract_apis)

## 3. 初始化备注

本快照由 \`scripts/init-context.sh\` 自动生成，记录项目上下文治理机制建立时的初始状态。
后续请使用 \`scripts/refresh-context.sh\` 在每次功能变更后更新上下文。
SNAPSHOT_EOF

    log_success "Initial snapshot created: ${snapshot_file}"
}

# =============================================================================
# 7. 检查上下文文档完整性
# =============================================================================
check_docs_completeness() {
    log_info "Checking docs completeness..."
    local missing=0
    local required_docs=(
        "docs/project-context.md"
        "docs/domain-glossary.md"
        "docs/module-boundaries.md"
        "docs/architecture.md"
        "docs/multi-agent-rules.md"
        "docs/templates/agent-task-template.md"
        "docs/templates/context-snapshot-template.md"
    )

    for doc in "${required_docs[@]}"; do
        if [ -f "${REPO_ROOT}/${doc}" ]; then
            log_success "  Found: ${doc}"
        else
            log_warn "  MISSING: ${doc}"
            missing=$((missing + 1))
        fi
    done

    if [ "${missing}" -gt 0 ]; then
        log_warn "${missing} required document(s) are missing."
        log_warn "Please ensure all docs/ files are committed to the repository."
    else
        log_success "All required context documents are present."
    fi
}

# =============================================================================
# Main
# =============================================================================
main() {
    echo "============================================================"
    echo "  AI-AutoTuringFlow — Project Context Initialization"
    echo "  Time: ${TIMESTAMP}"
    echo "============================================================"
    echo ""

    init_directories
    update_context_timestamp
    generate_snapshot
    check_docs_completeness

    echo ""
    echo "============================================================"
    echo "  Initialization Complete!"
    echo ""
    echo "  Next steps:"
    echo "  1. Review and customize docs/project-context.md"
    echo "  2. Update docs/domain-glossary.md with your domain terms"
    echo "  3. Verify docs/module-boundaries.md matches your structure"
    echo "  4. Commit all docs/ changes to your repository"
    echo "  5. Use scripts/refresh-context.sh after future changes"
    echo "============================================================"
}

main "$@"
