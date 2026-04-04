// Application Configuration
const CONFIG = {
  // API Base URL - 由 nginx 反向代理到 /api/
  API_BASE_URL: '/api',
  
  // Pagination
  DEFAULT_PAGE_SIZE: 20,
  
  // Status mappings
  STATUS_TEXT: {
    'IN_STOCK': '在库',
    'DEPLOYED': '已部署',
    'FAULTY': '故障',
    'UNDER_REPAIR': '维修中',
    'SCRAPPED': '已报废'
  },
  
  // Operation type mappings
  OPERATION_TYPE_TEXT: {
    'INBOUND': '入库',
    'OUTBOUND': '出库',
    'DEPLOY': '部署',
    'RETRIEVE': '收回',
    'MARK_FAULTY': '标记故障',
    'SEND_REPAIR': '送修',
    'RETURN_REPAIR': '维修归还',
    'SCRAP': '报废',
    'UPDATE_INFO': '更新信息'
  }
};