/**
 * Yearly Goal Tracker - State Management Module
 */
export const state = {
  users: [],
  currentUser: null,
  currentYear: 2026,
  categoryFilter: 'ALL',
  statusFilter: 'ALL',
  searchKeyword: '',
  goals: [], // GoalDetailResponse objects
  subTasks: [], // flattened subtasks with goal meta
  selectedGoalId: null,
  selectedGoalTitle: '',
  listeners: [],

  subscribe(listener) {
    this.listeners.push(listener);
  },

  notify() {
    this.listeners.forEach((listener) => listener(this));
  },

  setCurrentUser(user) {
    this.currentUser = user;
    this.notify();
  },

  setCategoryFilter(category) {
    this.categoryFilter = category;
    this.notify();
  },

  setStatusFilter(status) {
    this.statusFilter = status;
    this.notify();
  },

  setSearchKeyword(keyword) {
    this.searchKeyword = keyword;
    this.notify();
  },

  setYear(year) {
    this.currentYear = year;
    this.notify();
  },

  setSelectedGoal(goalId, goalTitle) {
    this.selectedGoalId = goalId;
    this.selectedGoalTitle = goalTitle || '';
    this.notify();
  },

  setGoals(goals) {
    this.goals = goals;
    // Flatten subTasks
    const flattened = [];
    goals.forEach((goal) => {
      if (goal.subTasks && goal.subTasks.length > 0) {
        goal.subTasks.forEach((st) => {
          flattened.push({
            ...st,
            goalTitle: goal.title,
            goalCategory: goal.category,
            goalId: goal.id,
          });
        });
      }
    });
    this.subTasks = flattened;
    this.notify();
  },
};
