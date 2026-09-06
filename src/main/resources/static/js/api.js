/**
 * Yearly Goal Tracker - REST API Client Module
 */
const API_BASE = '/api/v1';
const TOKEN_KEY = 'ygt_access_token';

export const tokenStorage = {
  getToken: () => localStorage.getItem(TOKEN_KEY),
  setToken: (token) => localStorage.setItem(TOKEN_KEY, token),
  clearToken: () => localStorage.removeItem(TOKEN_KEY),
};

async function request(endpoint, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  const token = tokenStorage.getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(`${API_BASE}${endpoint}`, config);

    // If 401 Unauthorized, notify session expired
    if (response.status === 401) {
      tokenStorage.clearToken();
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
    }

    const result = await response.json();

    if (!response.ok || !result.success) {
      const errorMsg = result.message || `API Error: ${response.status}`;
      throw new Error(errorMsg);
    }
    return result.data;
  } catch (err) {
    console.error(`Fetch failed on [${options.method || 'GET'}] ${endpoint}:`, err);
    throw err;
  }
}

export const api = {
  // Auth
  auth: {
    signup: (data) => request('/auth/signup', { method: 'POST', body: JSON.stringify(data) }),
    login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
    getMe: () => request('/auth/me'),
  },

  // Users
  getUsers: () => request('/users'),
  createUser: (data) => request('/users', { method: 'POST', body: JSON.stringify(data) }),
  getUser: (id) => request(`/users/${id}`),

  // Goals
  getGoals: (userId = null, category = null, status = null) => {
    const params = new URLSearchParams();
    if (userId) params.append('userId', userId);
    if (category) params.append('category', category);
    if (status) params.append('status', status);
    const qs = params.toString();
    return request(`/goals${qs ? '?' + qs : ''}`);
  },
  getGoalDetail: (id) => request(`/goals/${id}`),
  createGoal: (data) => request('/goals', { method: 'POST', body: JSON.stringify(data) }),
  updateGoal: (id, data) => request(`/goals/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  updateGoalStatus: (id, status) => request(`/goals/${id}/status?status=${status}`, { method: 'PATCH' }),
  deleteGoal: (id) => request(`/goals/${id}`, { method: 'DELETE' }),

  // SubTasks
  getSubTasks: (goalId) => request(`/goals/${goalId}/sub-tasks`),
  createSubTask: (goalId, data) => request(`/goals/${goalId}/sub-tasks`, { method: 'POST', body: JSON.stringify(data) }),
  deleteSubTask: (id) => request(`/sub-tasks/${id}`, { method: 'DELETE' }),

  // CheckIns
  createCheckIn: (subTaskId, data) => request(`/sub-tasks/${subTaskId}/check-ins`, { method: 'POST', body: JSON.stringify(data) }),
  getCheckIns: (subTaskId) => request(`/sub-tasks/${subTaskId}/check-ins`),
  updateCheckIn: (id, data) => request(`/check-ins/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteCheckIn: (id) => request(`/check-ins/${id}`, { method: 'DELETE' }),

  // Dashboard / Analytics
  dashboard: {
    getSummary: () => request('/dashboard/summary'),
    getHeatmap: (days = 105) => request(`/dashboard/heatmap?days=${days}`),
  },

  // Notes
  notes: {
    getAll: (type = null, goalId = null) => {
      const params = new URLSearchParams();
      if (type) params.append('type', type);
      if (goalId) params.append('goalId', goalId);
      const qs = params.toString();
      return request(`/notes${qs ? '?' + qs : ''}`);
    },
    create: (data) => request('/notes', { method: 'POST', body: JSON.stringify(data) }),
    update: (id, data) => request(`/notes/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
    delete: (id) => request(`/notes/${id}`, { method: 'DELETE' }),
  },

  // AI
  ai: {
    suggestSubTasks: (data) => request('/ai/suggest-subtasks', { method: 'POST', body: JSON.stringify(data) }),
  },
};
