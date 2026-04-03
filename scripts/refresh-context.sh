#!/usr/bin/env bash
# =============================================================================
# scripts/refresh-context.sh
# 项目上下文增量刷新脚本
#
# 用途：在项目代码或功能发生变更后，刷新以下内容：
#       1. docs/project-context.md 中的刷新时间戳
#       2. 生成新的上下文快照（归档到 docs/snapshots/）
#       3. 扫描代码变化，输出需要手动更新的文档摘要
#       4. （可选）触发 README 更新提示
#
# 使用方法：
#   chmod +x scripts/refresh-context.sh
#   ./scripts/refresh-context.sh [--reason "变更原因描述"] [--pr 123]
#
# 参数：
#   --reason  本次刷新的原因（可选，默认：manual refresh）
#   --pr      关联的 PR 编号（可选）
#   --since   扫描此 git ref 之后的变更（可选，默认：HEAD~1）
#
# 前置条件：
#   - 从仓库根目录执行
#   - git 已安装并在 PATH 中
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
RED='\033[0;31m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_success() { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $*"; }

# =============================================================================
# 解析参数
# =============================================================================
REFRESH_REASON="manual refresh"
PR_NUMBER=""
SINCE_REF="HEAD~1"

while [[ $# -gt 0 ]]; do
    case $1 in
        --reason)
            REFRESH_REASON="$2"
            shift 2
            ;;
        --pr)
            PR_NUMBER="$2"
            shift 2
            ;;
        --since)
            SINCE_REF="$2"
            shift 2
            ;;
        *)
            log_warn "Unknown argument: $1"
            shift
            ;;
    esac
done

# =============================================================================
# 1. 确保 docs/snapshots 目录存在
# =============================================================================
ensure_dirs() {
    mkdir -p "${SNAPSHOTS_DIR}"
}

# =============================================================================
# 2. 获取最近的 git 变更摘要
# =============================================================================
get_git_changes() {
    if ! git -C "${REPO_ROOT}" rev-parse HEAD &>/dev/null; then
        echo "  (not a git repository or no commits yet)"
        return
    fi

    local since="${SINCE_REF}"
    if ! git -C "${REPO_ROOT}" rev-parse "${since}" &>/dev/null; then
        since="HEAD"
    fi

    echo "  Changed files (since ${since}):"
    git -C "${REPO_ROOT}" diff --name-only "${since}" HEAD 2>/dev/null \
        | head -30 \
        | while read -r f; do echo "    - ${f}"; done \
        || echo "    (no changes detected)"
}

# =============================================================================
# 3. 检测影响上下文的关键变更
# =============================================================================
detect_context_sensitive_changes() {
    local changed_items=()

    if ! git -C "${REPO_ROOT}" rev-parse HEAD &>/dev/null; then
        return
    fi

    local since="${SINCE_REF}"
    if ! git -C "${REPO_ROOT}" rev-parse "${since}" &>/dev/null; then
        since="HEAD"
    fi

    local changed_files
    changed_files=$(git -C "${REPO_ROOT}" diff --name-only "${since}" HEAD 2>/dev/null || true)

    # 检测实体/模型变更
    if echo "${changed_files}" | grep -qE "backend/.*/(model|entity)/.*\.java$"; then
        changed_items+=("⚠️  Backend entity/model files changed → Update docs/domain-glossary.md")
    fi

    # 检测 Controller/API 变更
    if echo "${changed_files}" | grep -qE "backend/.*/controller/.*\.java$"; then
        changed_items+=("⚠️  Backend controller files changed → Verify API list in context snapshot")
    fi

    # 检测前端页面/JS 变更
    if echo "${changed_files}" | grep -qE "frontend/.*\.(html|js)$"; then
        changed_items+=("⚠️  Frontend pages/scripts changed → Update frontend page list in snapshot")
    fi

    # 检测 pom.xml / requirements.txt 变更（依赖版本）
    if echo "${changed_files}" | grep -qE "(pom\.xml|requirements\.txt|package\.json)$"; then
        changed_items+=("⚠️  Dependency files changed → Update tech stack versions in snapshot")
    fi

    # 检测数据库迁移脚本变更
    if echo "${changed_files}" | grep -qE "db/migration/.*\.sql$"; then
        changed_items+=("⚠️  Database migration scripts changed → Update entity fields in snapshot")
    fi

    # 检测 README 变更
    if echo "${changed_files}" | grep -qE "README\.md$"; then
        changed_items+=("ℹ️  README.md changed → Context may already be updated")
    fi

    if [ ${#changed_items[@]} -gt 0 ]; then
        echo "  Context-sensitive changes detected:"
        for item in "${changed_items[@]}"; do
            echo "    ${item}"
        done
    else
        echo "  No context-sensitive changes detected in this diff."
    fi
}

# =============================================================================
# 4. 生成快照序号（同一天多次刷新时递增）
# =============================================================================
get_snapshot_seq() {
    local count
    count=$(find "${SNAPSHOTS_DIR}" -name "context-snapshot-${DATE_TAG}-*.md" 2>/dev/null | wc -l)
    printf "%03d" $((count + 1))
}

# =============================================================================
# 5. 扫描当前 API 清单
# =============================================================================
scan_current_apis() {
    local backend_dir="${REPO_ROOT}/backend/src/main/java"
    if [ ! -d "${backend_dir}" ]; then
        echo "  (backend source directory not found)"
        return
    fi

    grep -r --include="*.java" \
        -E "@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)\(\"[^\"]+\"\)" \
        "${backend_dir}" 2>/dev/null \
        | grep -oE '"[^"]+"' \
        | sort -u \
        | head -30 \
        | while read -r path; do echo "  - ${path}"; done \
        || echo "  (no mapping annotations found)"
}

# =============================================================================
# 6. 更新 project-context.md 的刷新时间
# =============================================================================
update_context_timestamp() {
    local ctx_file="${DOCS_DIR}/project-context.md"
    if [ ! -f "${ctx_file}" ]; then
        log_warn "docs/project-context.md not found. Run init-context.sh first."
        return 1
    fi

    # Replace the HTML placeholder comment, or update the existing timestamp value in the
    # Markdown table row: "| **上下文最后刷新时间** | <value> |"
    # Use ~ as delimiter to avoid conflict with | in the Markdown table syntax.
    sed -i \
        -e "s|<!-- CONTEXT_REFRESH_TIME -->|${TIMESTAMP}|g" \
        -e "s~\(| \*\*上下文最后刷新时间\*\* | \)[^|]*~\1${TIMESTAMP} ~" \
        "${ctx_file}" 2>/dev/null || true

    log_success "Updated refresh timestamp in project-context.md → ${TIMESTAMP}"
}

# =============================================================================
# 7. 生成增量快照
# =============================================================================
generate_refresh_snapshot() {
    local seq
    seq=$(get_snapshot_seq)
    local snapshot_file="${SNAPSHOTS_DIR}/context-snapshot-${DATE_TAG}-${seq}.md"

    log_info "Generating context snapshot: ${snapshot_file}"

    local pr_ref="N/A"
    if [ -n "${PR_NUMBER}" ]; then
        pr_ref="#${PR_NUMBER}"
    fi

    local git_log_summary=""
    if git -C "${REPO_ROOT}" rev-parse HEAD &>/dev/null; then
        git_log_summary=$(git -C "${REPO_ROOT}" log --oneline -5 2>/dev/null || echo "N/A")
    fi

    cat > "${snapshot_file}" << SNAPSHOT_EOF
# 项目上下文快照 — ${DATE_TAG} (${seq})

## 快照元数据

| 字段 | 内容 |
|------|------|
| **快照版本** | v${DATE_TAG}.${seq} |
| **生成时间** | ${TIMESTAMP} |
| **生成方式** | \`scripts/refresh-context.sh\` 自动生成 |
| **触发原因** | ${REFRESH_REASON} |
| **关联 PR/Issue** | ${pr_ref} |
| **生成人/Agent** | $(git config user.name 2>/dev/null || echo "unknown") |

## 1. 本次变更的 Git 摘要

最近 5 个 commit：
\`\`\`
${git_log_summary}
\`\`\`

### 本次刷新涉及的文件变更

$(get_git_changes)

### 需要关注的上下文影响

$(detect_context_sensitive_changes)

## 2. 当前 API 清单扫描

$(scan_current_apis)

## 3. 需要手动更新的文档

根据上面的变更分析，以下文档可能需要手动更新：

- [ ] \`docs/project-context.md\`（如项目基本信息有变化）
- [ ] \`docs/domain-glossary.md\`（如新增/修改了业务实体或字段）
- [ ] \`docs/module-boundaries.md\`（如目录结构或职责边界有变化）
- [ ] \`docs/architecture.md\`（如架构决策或约束有变化）
- [ ] \`README.md\`（如面向用户的功能说明有变化）

## 4. 变更摘要

> 请填写本次刷新的主要功能或修复摘要（供后续 Agent 参考）：

（请手动填写）

SNAPSHOT_EOF

    log_success "Snapshot created: ${snapshot_file}"
    echo "${snapshot_file}"
}

# =============================================================================
# 8. 检查 README 是否需要更新
# =============================================================================
check_readme_sync() {
    local readme="${REPO_ROOT}/README.md"
    if [ ! -f "${readme}" ]; then
        log_warn "README.md not found"
        return
    fi

    local readme_mtime
    readme_mtime=$(stat -c %Y "${readme}" 2>/dev/null || stat -f %m "${readme}" 2>/dev/null || echo 0)
    local ctx_mtime
    ctx_mtime=$(stat -c %Y "${DOCS_DIR}/project-context.md" 2>/dev/null || stat -f %m "${DOCS_DIR}/project-context.md" 2>/dev/null || echo 0)

    if [ "${ctx_mtime}" -gt "${readme_mtime}" ]; then
        log_warn "project-context.md is newer than README.md"
        log_warn "Consider updating README.md to reflect the latest project state."
    else
        log_success "README.md appears to be in sync with project-context.md"
    fi
}

# =============================================================================
# Main
# =============================================================================
main() {
    echo "============================================================"
    echo "  AI-AutoTuringFlow — Project Context Refresh"
    echo "  Time: ${TIMESTAMP}"
    echo "  Reason: ${REFRESH_REASON}"
    if [ -n "${PR_NUMBER}" ]; then
        echo "  PR: #${PR_NUMBER}"
    fi
    echo "============================================================"
    echo ""

    ensure_dirs
    update_context_timestamp
    local snapshot_path
    snapshot_path=$(generate_refresh_snapshot)
    check_readme_sync

    echo ""
    echo "============================================================"
    echo "  Context Refresh Complete!"
    echo ""
    echo "  Generated snapshot: ${snapshot_path}"
    echo ""
    echo "  Action items:"
    echo "  1. Review the snapshot and fill in the 'Change Summary' section"
    echo "  2. Update any docs listed in 'Documents to Update'"
    echo "  3. Commit: git add docs/ && git commit -m 'chore: refresh project context'"
    echo "============================================================"
}

main "$@"
