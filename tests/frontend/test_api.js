/**
 * Frontend API Client Test Suite
 * Tests the API client in js/api.js
 */

global.fetch = jest.fn();

describe('API', () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  describe('request', () => {
    test('should make successful GET request', async () => {
      const mockData = { id: 1, name: 'Test' };
      fetch.mockResolvedValueOnce({
        ok: true,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => mockData
      });

      const result = await API.request('/test');
      
      expect(fetch).toHaveBeenCalledWith(
        '/api/test',
        expect.objectContaining({
          headers: expect.objectContaining({
            'Content-Type': 'application/json'
          })
        })
      );
      expect(result).toEqual(mockData);
    });

    test('should handle error responses', async () => {
      fetch.mockResolvedValueOnce({
        ok: false,
        status: 400,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ error: 'Bad request' })
      });

      await expect(API.request('/test')).rejects.toThrow();
    });
  });

  describe('getModules', () => {
    test('should call modules endpoint with query params', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ content: [], totalElements: 0 })
      });

      await API.getModules({ page: 0, size: 20, status: 'IN_STOCK' });
      
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/modules?'),
        expect.any(Object)
      );
    });
  });

  describe('getModule', () => {
    test('should call single module endpoint', async () => {
      const mockModule = { id: 1, serialNumber: 'SN123' };
      fetch.mockResolvedValueOnce({
        ok: true,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => mockModule
      });

      const result = await API.getModule(1);
      
      expect(fetch).toHaveBeenCalledWith(
        '/api/modules/1',
        expect.any(Object)
      );
      expect(result).toEqual(mockModule);
    });
  });

  describe('createModule', () => {
    test('should send POST request with data', async () => {
      const moduleData = { serialNumber: 'SN123', model: 'TEST' };
      fetch.mockResolvedValueOnce({
        ok: true,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ id: 1, ...moduleData })
      });

      await API.createModule(moduleData);
      
      expect(fetch).toHaveBeenCalledWith(
        '/api/modules',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify(moduleData)
        })
      );
    });
  });

  describe('updateModule', () => {
    test('should send PUT request with data', async () => {
      const moduleData = { model: 'UPDATED' };
      fetch.mockResolvedValueOnce({
        ok: true,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ id: 1, ...moduleData })
      });

      await API.updateModule(1, moduleData);
      
      expect(fetch).toHaveBeenCalledWith(
        '/api/modules/1',
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify(moduleData)
        })
      );
    });
  });

  describe('deleteModule', () => {
    test('should send DELETE request', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        status: 200,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({})
      });

      await API.deleteModule(1);
      
      expect(fetch).toHaveBeenCalledWith(
        '/api/modules/1',
        expect.objectContaining({
          method: 'DELETE'
        })
      );
    });

    test('should handle 204 no content response', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        status: 204,
        headers: new Headers(),
        text: async () => ''
      });

      const result = await API.deleteModule(1);
      expect(result).toBeNull();
    });
  });

  describe('getHistories', () => {
    test('should call histories endpoint', async () => {
      fetch.mockResolvedValueOnce({
        ok: true,
        headers: new Headers({ 'content-type': 'application/json' }),
        json: async () => ({ content: [], totalElements: 0 })
      });

      await API.getHistories({ page: 0, size: 20 });
      
      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/api/histories?'),
        expect.any(Object)
      );
    });
  });
});
