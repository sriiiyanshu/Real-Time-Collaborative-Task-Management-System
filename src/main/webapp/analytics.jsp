<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%> <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="common/header.jsp">
  <jsp:param name="pageTitle" value="Analytics" />
</jsp:include>

<jsp:include page="common/navigation.jsp">
  <jsp:param name="active" value="analytics" />
</jsp:include>

<div class="main-container">
  <jsp:include page="common/sidebar.jsp">
    <jsp:param name="sidebarType" value="analytics" />
    <jsp:param name="sidebarTitle" value="Analytics" />
  </jsp:include>

  <main class="main-content">
    <div class="container">
      <div class="section-header">
        <h1>Analytics Dashboard</h1>
      </div>

      <div class="analytics-filters">
        <div class="filter-group">
          <label for="dateRangeFilter">Date Range</label>
          <select id="dateRangeFilter" class="form-control">
            <option value="7">Last 7 days</option>
            <option value="30" selected>Last 30 days</option>
            <option value="90">Last 3 months</option>
            <option value="180">Last 6 months</option>
            <option value="365">Last year</option>
            <option value="custom">Custom range</option>
          </select>
        </div>

        <div class="filter-group" id="customDateRange" style="display: none">
          <div class="date-range-inputs">
            <div class="date-input">
              <label for="startDate">From</label>
              <input type="date" id="startDate" class="form-control" />
            </div>
            <div class="date-input">
              <label for="endDate">To</label>
              <input type="date" id="endDate" class="form-control" />
            </div>
          </div>
        </div>

        <div class="filter-group">
          <label for="projectFilter">Project</label>
          <select id="projectFilter" class="form-control">
            <option value="all">All Projects</option>
            <c:forEach items="${userProjects}" var="project">
              <option value="${project.id}">${project.name}</option>
            </c:forEach>
          </select>
        </div>
      </div>

      <div class="analytics-card-row">
        <div class="card analytics-card">
          <div class="card-body">
            <div class="metric">
              <h2 class="metric-value">${taskCompletionRate}%</h2>
              <p class="metric-label">Task Completion Rate</p>
            </div>
            <div class="metric-icon task-completion-icon"></div>
          </div>
        </div>
        <div class="card analytics-card">
          <div class="card-body">
            <div class="metric">
              <h2 class="metric-value">${completedTasks}</h2>
              <p class="metric-label">Tasks Completed</p>
            </div>
            <div class="metric-icon completed-tasks-icon"></div>
          </div>
        </div>
        <div class="card analytics-card">
          <div class="card-body">
            <div class="metric">
              <h2 class="metric-value">${avgTaskCompletionTime}</h2>
              <p class="metric-label">Avg. Completion Time (days)</p>
            </div>
            <div class="metric-icon time-icon"></div>
          </div>
        </div>
        <div class="card analytics-card">
          <div class="card-body">
            <div class="metric">
              <h2 class="metric-value">${overdueTasksPercentage}%</h2>
              <p class="metric-label">Overdue Tasks</p>
            </div>
            <div class="metric-icon overdue-icon"></div>
          </div>
        </div>
      </div>

      <div class="analytics-section">
        <div class="card">
          <div class="card-header">
            <h2>Task Completion Trend</h2>
          </div>
          <div class="card-body">
            <div class="chart-container">
              <canvas id="taskCompletionChart"></canvas>
            </div>
          </div>
        </div>
      </div>

      <div class="analytics-section row">
        <div class="col-md-6">
          <div class="card">
            <div class="card-header">
              <h2>Task Status Distribution</h2>
            </div>
            <div class="card-body">
              <div class="chart-container">
                <canvas id="taskStatusChart"></canvas>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-6">
          <div class="card">
            <div class="card-header">
              <h2>Task Priority Distribution</h2>
            </div>
            <div class="card-body">
              <div class="chart-container">
                <canvas id="taskPriorityChart"></canvas>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="analytics-section">
        <div class="card">
          <div class="card-header">
            <h2>Team Performance</h2>
          </div>
          <div class="card-body">
            <div class="chart-container">
              <canvas id="teamPerformanceChart"></canvas>
            </div>
          </div>
        </div>
      </div>

      <div class="analytics-section">
        <div class="card">
          <div class="card-header">
            <h2>Time Tracking</h2>
          </div>
          <div class="card-body">
            <div class="chart-container">
              <canvas id="timeTrackingChart"></canvas>
            </div>
            <div class="time-tracking-summary">
              <div class="time-summary-item">
                <span class="summary-label">Total Hours Logged:</span>
                <span class="summary-value">${totalHoursLogged}</span>
              </div>
              <div class="time-summary-item">
                <span class="summary-label">Total Estimated Hours:</span>
                <span class="summary-value">${totalEstimatedHours}</span>
              </div>
              <div class="time-summary-item">
                <span class="summary-label">Accuracy:</span>
                <span class="summary-value ${estimateAccuracy < 80 ? 'text-warning' : 'text-success'}">${estimateAccuracy}%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</div>

<jsp:include page="common/footer.jsp" />

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
  document.addEventListener('DOMContentLoaded', function() {
      // Date range filter functionality
      const dateRangeFilter = document.getElementById('dateRangeFilter');
      const customDateRange = document.getElementById('customDateRange');

      dateRangeFilter.addEventListener('change', function() {
          if (this.value === 'custom') {
              customDateRange.style.display = 'block';
          } else {
              customDateRange.style.display = 'none';
              // Reload analytics data with selected date range
              loadAnalyticsData(this.value);
          }
      });

      // Initialize charts with data from server
      initializeCharts();

      // Filter change event listeners
      document.getElementById('projectFilter').addEventListener('change', function() {
          loadAnalyticsData();
      });

      function initializeCharts() {
          // Sample data - would be replaced with actual data from the server
          initTaskCompletionChart();
          initTaskStatusChart();
          initTaskPriorityChart();
          initTeamPerformanceChart();
          initTimeTrackingChart();
      }

      function loadAnalyticsData(dateRange) {
          // AJAX call to fetch analytics data based on filters
          // This would update the charts with new data
          console.log("Loading analytics data for date range:", dateRange);
      }

      function initTaskCompletionChart() {
          const ctx = document.getElementById('taskCompletionChart').getContext('2d');
          const taskCompletionChart = new Chart(ctx, {
              type: 'line',
              data: {
                  labels: ${taskCompletionDates},
                  datasets: [{
                      label: 'Tasks Completed',
                      data: ${taskCompletionCounts},
                      backgroundColor: 'rgba(78, 115, 223, 0.2)',
                      borderColor: 'rgba(78, 115, 223, 1)',
                      borderWidth: 2,
                      tension: 0.3
                  }]
              },
              options: {
                  responsive: true,
                  maintainAspectRatio: false,
                  scales: {
                      y: {
                          beginAtZero: true,
                          ticks: {
                              precision: 0
                          }
                      }
                  }
              }
          });
      }

      function initTaskStatusChart() {
          const ctx = document.getElementById('taskStatusChart').getContext('2d');
          const taskStatusChart = new Chart(ctx, {
              type: 'doughnut',
              data: {
                  labels: ${taskStatusLabels},
                  datasets: [{
                      data: ${taskStatusCounts},
                      backgroundColor: [
                          '#4e73df',
                          '#1cc88a',
                          '#36b9cc',
                          '#f6c23e',
                          '#e74a3b'
                      ],
                      borderWidth: 1
                  }]
              },
              options: {
                  responsive: true,
                  maintainAspectRatio: false
              }
          });
      }

      function initTaskPriorityChart() {
          const ctx = document.getElementById('taskPriorityChart').getContext('2d');
          const taskPriorityChart = new Chart(ctx, {
              type: 'doughnut',
              data: {
                  labels: ${taskPriorityLabels},
                  datasets: [{
                      data: ${taskPriorityCounts},
                      backgroundColor: [
                          '#e74a3b',
                          '#f6c23e',
                          '#1cc88a'
                      ],
                      borderWidth: 1
                  }]
              },
              options: {
                  responsive: true,
                  maintainAspectRatio: false
              }
          });
      }

      function initTeamPerformanceChart() {
          const ctx = document.getElementById('teamPerformanceChart').getContext('2d');
          const teamPerformanceChart = new Chart(ctx, {
              type: 'bar',
              data: {
                  labels: ${teamMemberNames},
                  datasets: [{
                      label: 'Tasks Completed',
                      data: ${teamMemberTaskCounts},
                      backgroundColor: 'rgba(78, 115, 223, 0.8)'
                  }]
              },
              options: {
                  responsive: true,
                  maintainAspectRatio: false,
                  scales: {
                      y: {
                          beginAtZero: true,
                          ticks: {
                              precision: 0
                          }
                      }
                  }
              }
          });
      }

      function initTimeTrackingChart() {
          const ctx = document.getElementById('timeTrackingChart').getContext('2d');
          const timeTrackingChart = new Chart(ctx, {
              type: 'bar',
              data: {
                  labels: ${projectNames},
                  datasets: [
                      {
                          label: 'Estimated Hours',
                          data: ${projectEstimatedHours},
                          backgroundColor: 'rgba(54, 185, 204, 0.8)'
                      },
                      {
                          label: 'Actual Hours',
                          data: ${projectActualHours},
                          backgroundColor: 'rgba(28, 200, 138, 0.8)'
                      }
                  ]
              },
              options: {
                  responsive: true,
                  maintainAspectRatio: false,
                  scales: {
                      y: {
                          beginAtZero: true
                      }
                  }
              }
          });
      }
  });
</script>
