/**
 * Frontend Components Test Suite
 * Tests the UI components
 */

describe('ModuleList Component', () => {
  let component;

  beforeEach(() => {
    // Mock API
    global.API = {
      getModules: jest.fn().mockResolvedValue({
        content: [
          {
            id: 1,
            serialNumber: 'SN001',
            speed: '10G',
            wavelength: '850',
            transmissionDistance: 550,
            inboundTime: '2024-01-15T10:00:00Z',
            vendorInfos: [
              { id: 1, vendor: 'Cisco', highSpeedTestRecommended: true }
            ]
          }
        ],
        totalPages: 1,
        totalElements: 1
      }),
      deleteModule: jest.fn().mockResolvedValue({})
    };

    component = new ModuleList();
  });

  test('should initialize with default values', () => {
    expect(component.currentPage).toBe(0);
    expect(component.pageSize).toBe(CONFIG.DEFAULT_PAGE_SIZE);
  });

  test('should load data on initialization', async () => {
    await component.loadData();
    expect(API.getModules).toHaveBeenCalled();
  });

  test('should handle filter changes', () => {
    const filters = component.getFilters();
    expect(filters).toEqual({});
  });

  test('should render table with modules', async () => {
    await component.loadData();
    const tbody = component.container.querySelector('#moduleTableBody');
    expect(tbody.children.length).toBeGreaterThan(0);
  });

  test('should show 编码 column header instead of 序列号', () => {
    const thead = component.container.querySelector('thead');
    expect(thead.innerHTML).toContain('编码');
    expect(thead.innerHTML).not.toContain('序列号');
  });

  test('should not show 型号 column header', () => {
    const thead = component.container.querySelector('thead');
    expect(thead.innerHTML).not.toContain('型号');
  });

  test('should show 速率, 波长, 传输距离 columns', () => {
    const thead = component.container.querySelector('thead');
    expect(thead.innerHTML).toContain('速率');
    expect(thead.innerHTML).toContain('波长');
    expect(thead.innerHTML).toContain('传输距离');
  });

  test('should show 厂家 and 高速重点测试 columns', () => {
    const thead = component.container.querySelector('thead');
    expect(thead.innerHTML).toContain('厂家');
    expect(thead.innerHTML).toContain('高速重点测试');
  });

  test('should support multi-condition search filters', () => {
    const filterSn = component.container.querySelector('#filterSn');
    const filterSpeed = component.container.querySelector('#filterSpeed');
    const filterWavelength = component.container.querySelector('#filterWavelength');
    const filterTransmissionDistance = component.container.querySelector('#filterTransmissionDistance');
    const filterConnectorType = component.container.querySelector('#filterConnectorType');

    expect(filterSn).not.toBeNull();
    expect(filterSpeed).not.toBeNull();
    expect(filterWavelength).not.toBeNull();
    expect(filterTransmissionDistance).not.toBeNull();
    expect(filterConnectorType).not.toBeNull();

    filterSn.value = 'SN001';
    filterSpeed.value = '10G';
    const filters = component.getFilters();
    expect(filters.serialNumber).toBe('SN001');
    expect(filters.speed).toBe('10G');
  });
});

describe('ModuleForm Component', () => {
  let component;

  beforeEach(() => {
    global.API = {
      getModule: jest.fn().mockResolvedValue({
        id: 1,
        serialNumber: 'SN001',
        speed: '10G'
      }),
      createModule: jest.fn().mockResolvedValue({ id: 2 }),
      updateModule: jest.fn().mockResolvedValue({})
    };

    component = new ModuleForm();
  });

  test('should validate required serialNumber field (编码)', () => {
    const errors = component.validate();
    expect(errors).toHaveProperty('serialNumber');
  });

  test('should not require model field (型号 removed)', () => {
    const errors = component.validate();
    expect(errors).not.toHaveProperty('model');
  });

  test('should validate serial number format', () => {
    // Simulate form with invalid serial number
    const form = component.container.querySelector('#moduleForm');
    form.elements.serialNumber.value = 'ABC'; // Too short
    
    const errors = component.validate();
    expect(errors).toHaveProperty('serialNumber');
  });

  test('should use 编码 label instead of 序列号', () => {
    const formHtml = component.container.innerHTML;
    expect(formHtml).toContain('编码');
    expect(formHtml).not.toContain('序列号');
  });

  test('should not contain 型号 required field', () => {
    const modelLabel = component.container.querySelector('label[data-required]');
    // Only serialNumber should be required
    const requiredLabels = component.container.querySelectorAll('label[data-required]');
    const requiredTexts = Array.from(requiredLabels).map(l => l.textContent.trim());
    expect(requiredTexts).not.toContain('型号');
  });

  test('should load module data in edit mode', async () => {
    component.moduleId = 1;
    await component.loadModuleData();
    
    expect(API.getModule).toHaveBeenCalledWith(1);
    expect(component.isEditMode).toBe(true);
  });
});

describe('ModuleDetails Component', () => {
  let component;

  beforeEach(() => {
    global.API = {
      getModule: jest.fn().mockResolvedValue({
        id: 1,
        serialNumber: 'SN001',
        speed: '10G',
        wavelength: '850',
        transmissionDistance: 550
      }),
      getModuleHistory: jest.fn().mockResolvedValue({
        content: [
          {
            operationType: 'INBOUND',
            operationTime: '2024-01-15T10:00:00Z',
            operator: 'admin'
          }
        ]
      }),
      getVendorInfos: jest.fn().mockResolvedValue([
        {
          id: 1,
          vendor: 'Cisco',
          highSpeedTestRecommended: true,
          coveredBoards: 'Board-A,Board-B',
          testReportLink: 'https://example.com/report1,https://example.com/report2'
        }
      ]),
      deleteModule: jest.fn().mockResolvedValue({})
    };

    component = new ModuleDetails({ id: 1 });
  });

  test('should load module data and history', async () => {
    await component.loadData();
    
    expect(API.getModule).toHaveBeenCalledWith(1);
    expect(API.getModuleHistory).toHaveBeenCalledWith(1);
    expect(API.getVendorInfos).toHaveBeenCalledWith(1);
  });

  test('should render timeline with history items', () => {
    const history = [
      {
        operationType: 'INBOUND',
        operationTime: '2024-01-15T10:00:00Z',
        operator: 'admin'
      }
    ];
    
    const timeline = component.renderTimeline(history);
    expect(timeline).toContain('timeline-item');
    expect(timeline).toContain('入库');
  });

  test('should render vendor info with multi-row covered boards and test reports', () => {
    const vendorInfos = [
      {
        id: 1,
        vendor: 'Cisco',
        highSpeedTestRecommended: true,
        coveredBoards: 'Board-A,Board-B',
        testReportLink: 'https://example.com/report1,https://example.com/report2'
      }
    ];
    
    const html = component.renderVendorInfoTable(vendorInfos);
    // Should contain both boards as separate entries
    expect(html).toContain('Board-A');
    expect(html).toContain('Board-B');
    // Should contain rowspan for multi-row display
    expect(html).toContain('rowspan');
  });

  test('should use 编码 label in details', async () => {
    await component.loadData();
    const detailsHtml = component.container.innerHTML;
    expect(detailsHtml).toContain('编码');
    expect(detailsHtml).not.toContain('>序列号<');
  });

  test('should not show 型号 in details', async () => {
    await component.loadData();
    const detailsHtml = component.container.innerHTML;
    expect(detailsHtml).not.toContain('>型号<');
  });
});

describe('HistoryList Component', () => {
  let component;

  beforeEach(() => {
    global.API = {
      getHistories: jest.fn().mockResolvedValue({
        content: [
          {
            operationType: 'INBOUND',
            operationTime: '2024-01-15T10:00:00Z',
            moduleSerialNumber: 'SN001',
            moduleModel: 'SFP-10G',
            operator: 'admin',
            nextStatus: 'IN_STOCK'
          }
        ],
        totalPages: 1,
        totalElements: 1
      })
    };

    component = new HistoryList();
  });

  test('should load history data on initialization', async () => {
    await component.loadData();
    expect(API.getHistories).toHaveBeenCalled();
  });

  test('should render history table with items', async () => {
    await component.loadData();
    const tbody = component.container.querySelector('#historyTableBody');
    expect(tbody.children.length).toBeGreaterThan(0);
  });

  test('should handle pagination', () => {
    component.totalPages = 5;
    component.renderPagination();
    
    const pagination = component.container.querySelector('#historyPagination');
    expect(pagination.children.length).toBeGreaterThan(0);
  });
});

describe('App Component', () => {
  let app;

  beforeEach(() => {
    document.body.innerHTML = '';
    app = new App();
  });

  test('should initialize header and sidebar', () => {
    expect(app.header).toBeDefined();
    expect(app.sidebar).toBeDefined();
  });

  test('should show page based on route', () => {
    app.showPage('list');
    expect(app.currentPage).toBe('list');
    expect(app.currentComponent).toBeInstanceOf(ModuleList);
  });

  test('should handle hash change', () => {
    window.location.hash = '#/create';
    app.handleHashChange();
    expect(app.currentPage).toBe('create');
  });

  test('should show error for invalid page', () => {
    app.showPage('invalid');
    expect(app.mainContent.innerHTML).toContain('页面不存在');
  });
});
