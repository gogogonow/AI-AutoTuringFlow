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
            model: 'SFP-10G',
            vendor: 'Cisco',
            speed: '10G',
            status: 'IN_STOCK',
            inboundTime: '2024-01-15T10:00:00Z'
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
});

describe('ModuleForm Component', () => {
  let component;

  beforeEach(() => {
    global.API = {
      getModule: jest.fn().mockResolvedValue({
        id: 1,
        serialNumber: 'SN001',
        model: 'TEST',
        vendor: 'Cisco'
      }),
      createModule: jest.fn().mockResolvedValue({ id: 2 }),
      updateModule: jest.fn().mockResolvedValue({})
    };

    component = new ModuleForm();
  });

  test('should validate required fields', () => {
    const errors = component.validate();
    expect(errors).toHaveProperty('serialNumber');
    expect(errors).toHaveProperty('model');
    expect(errors).toHaveProperty('vendor');
  });

  test('should validate serial number format', () => {
    // Simulate form with invalid serial number
    const form = component.container.querySelector('#moduleForm');
    form.elements.serialNumber.value = 'ABC'; // Too short
    form.elements.model.value = 'Test';
    form.elements.vendor.value = 'Test';
    
    const errors = component.validate();
    expect(errors).toHaveProperty('serialNumber');
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
        model: 'SFP-10G',
        vendor: 'Cisco',
        status: 'IN_STOCK'
      }),
      getModuleHistory: jest.fn().mockResolvedValue({
        content: [
          {
            operationType: 'INBOUND',
            operationTime: '2024-01-15T10:00:00Z',
            operator: 'admin',
            nextStatus: 'IN_STOCK'
          }
        ]
      }),
      deleteModule: jest.fn().mockResolvedValue({})
    };

    component = new ModuleDetails({ id: 1 });
  });

  test('should load module data and history', async () => {
    await component.loadData();
    
    expect(API.getModule).toHaveBeenCalledWith(1);
    expect(API.getModuleHistory).toHaveBeenCalledWith(1);
  });

  test('should render status actions based on current status', () => {
    const actions = component.renderStatusActions('IN_STOCK');
    expect(actions).toContain('deploy');
    expect(actions).toContain('scrap');
  });

  test('should render timeline with history items', () => {
    const history = [
      {
        operationType: 'INBOUND',
        operationTime: '2024-01-15T10:00:00Z',
        operator: 'admin',
        nextStatus: 'IN_STOCK'
      }
    ];
    
    const timeline = component.renderTimeline(history);
    expect(timeline).toContain('timeline-item');
    expect(timeline).toContain('入库');
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
