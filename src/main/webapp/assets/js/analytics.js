/**
 * Analytics Module for Real-Time Task Application
 * Handles data visualization, reports, and metrics
 */
document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  // Analytics Module
  const Analytics = {
    // Charts instances
    charts: {},

    // Data cache
    dataCache: {},

    // Date range filters
    dateRange: {
      start: null,
      end: null,
    },

    // Chart color schemes
    colors: {
      primary: ["#4e73df", "#2e59d9", "#1cc88a", "#36b9cc", "#f6c23e", "#e74a3b"],
      secondary: ["rgba(78, 115, 223, 0.8)", "rgba(28, 200, 138, 0.8)", "rgba(54, 185, 204, 0.8)", "rgba(246, 194, 62, 0.8)", "rgba(231, 74, 59, 0.8)", "rgba(78, 115, 223, 0.8)"],
      transparent: ["rgba(78, 115, 223, 0.2)", "rgba(28, 200, 138, 0.2)", "rgba(54, 185, 204, 0.2)", "rgba(246, 194, 62, 0.2)", "rgba(231, 74, 59, 0.2)", "rgba(78, 115, 223, 0.2)"],
    },

    // Initialize analytics module
    init: function () {
      console.log("Analytics module initialized");
      this.setupDateRangePicker();
      this.setupEventListeners();
      this.loadDashboardData();
    },

    // Set up date range picker
    setupDateRangePicker: function () {
      const dateRangePicker = document.getElementById("daterange-picker");
      if (!dateRangePicker) return;

      // Set default range to last 30 days
      const end = new Date();
      const start = new Date();
      start.setDate(start.getDate() - 30);

      this.dateRange.start = start;
      this.dateRange.end = end;

      // Initialize date picker if available
      if (typeof DateRangePicker !== "undefined") {
        new DateRangePicker(
          dateRangePicker,
          {
            startDate: start,
            endDate: end,
            ranges: {
              Today: [new Date(), new Date()],
              Yesterday: [new Date(new Date().setDate(new Date().getDate() - 1)), new Date(new Date().setDate(new Date().getDate() - 1))],
              "Last 7 Days": [new Date(new Date().setDate(new Date().getDate() - 6)), new Date()],
              "Last 30 Days": [new Date(new Date().setDate(new Date().getDate() - 29)), new Date()],
              "This Month": [new Date(new Date().setDate(1)), new Date()],
              "Last Month": [new Date(new Date().setMonth(new Date().getMonth() - 1, 1)), new Date(new Date().setMonth(new Date().getMonth(), 0))],
            },
          },
          (start, end) => {
            this.dateRange.start = start;
            this.dateRange.end = end;
            this.updateDateRangeDisplay();
            this.refreshAllCharts();
          }
        );
      } else {
        // Fallback for date inputs if DateRangePicker is not available
        const startInput = document.getElementById("date-start");
        const endInput = document.getElementById("date-end");

        if (startInput && endInput) {
          startInput.value = this.formatDate(start);
          endInput.value = this.formatDate(end);

          startInput.addEventListener("change", () => {
            this.dateRange.start = new Date(startInput.value);
            this.refreshAllCharts();
          });

          endInput.addEventListener("change", () => {
            this.dateRange.end = new Date(endInput.value);
            this.refreshAllCharts();
          });
        }
      }

      this.updateDateRangeDisplay();
    },

    // Format date for inputs
    formatDate: function (date) {
      return date.toISOString().split("T")[0];
    },

    // Update date range display
    updateDateRangeDisplay: function () {
      const display = document.getElementById("daterange-text");
      if (!display) return;

      const formatOptions = { year: "numeric", month: "short", day: "numeric" };
      const startStr = this.dateRange.start.toLocaleDateString(undefined, formatOptions);
      const endStr = this.dateRange.end.toLocaleDateString(undefined, formatOptions);

      display.textContent = `${startStr} - ${endStr}`;
    },

    // Set up event listeners
    setupEventListeners: function () {
      // Filter change events
      document.querySelectorAll(".filter-select").forEach((filter) => {
        filter.addEventListener("change", () => {
          this.applyFilters();
        });
      });

      // Export buttons
      document.querySelectorAll("[data-export]").forEach((button) => {
        button.addEventListener("click", (event) => {
          const format = button.dataset.export;
          const chartId = button.closest("[data-chart-container]")?.dataset.chartContainer;

          if (chartId) {
            this.exportChart(chartId, format);
          } else {
            this.exportAllCharts(format);
          }
        });
      });

      // Report generation form
      const reportForm = document.getElementById("generate-report-form");
      if (reportForm) {
        reportForm.addEventListener("submit", (event) => {
          event.preventDefault();
          this.generateReport(new FormData(reportForm));
        });
      }

      // Tab switching
      document.querySelectorAll("[data-tab-target]").forEach((tab) => {
        tab.addEventListener("click", () => {
          const targetId = tab.dataset.tabTarget;
          this.switchTab(targetId);

          // Refresh charts in the new tab
          setTimeout(() => {
            this.refreshVisibleCharts();
          }, 100);
        });
      });

      // Chart type toggle
      document.querySelectorAll("[data-chart-type]").forEach((button) => {
        button.addEventListener("click", () => {
          const chartId = button.closest("[data-chart-container]")?.dataset.chartContainer;
          const chartType = button.dataset.chartType;

          if (chartId && chartType) {
            this.switchChartType(chartId, chartType);

            // Update active state for buttons
            button
              .closest(".chart-controls")
              ?.querySelectorAll("[data-chart-type]")
              .forEach((btn) => {
                btn.classList.toggle("active", btn === button);
              });
          }
        });
      });

      // Refresh button clicks
      document.querySelectorAll(".refresh-chart").forEach((button) => {
        button.addEventListener("click", () => {
          const chartId = button.closest("[data-chart-container]")?.dataset.chartContainer;

          if (chartId) {
            this.refreshChart(chartId);
          } else {
            this.refreshAllCharts();
          }
        });
      });

      // Print button
      const printButton = document.getElementById("print-analytics");
      if (printButton) {
        printButton.addEventListener("click", () => {
          window.print();
        });
      }
    },

    // Load dashboard data
    loadDashboardData: function () {
      this.showLoading();

      // Load all required data for charts
      Promise.all([this.fetchData("task-metrics"), this.fetchData("user-activity"), this.fetchData("project-status"), this.fetchData("time-tracking")])
        .then(() => {
          // Initialize all charts once data is loaded
          this.initTaskCompletionChart();
          this.initTaskStatusDistributionChart();
          this.initUserProductivityChart();
          this.initProjectProgressChart();
          this.initTimeTrackingChart();
          this.updateSummaryMetrics();
          this.hideLoading();
        })
        .catch((error) => {
          console.error("Error loading analytics data:", error);
          this.showError("Failed to load analytics data");
          this.hideLoading();
        });
    },

    // Fetch data from API endpoint
    fetchData: function (dataType) {
      const url = `/api/analytics/${dataType}`;
      const params = new URLSearchParams({
        startDate: this.dateRange.start.toISOString(),
        endDate: this.dateRange.end.toISOString(),
      });

      // Add any active filters
      document.querySelectorAll(".filter-select").forEach((filter) => {
        if (filter.value && filter.value !== "all") {
          params.append(filter.name, filter.value);
        }
      });

      return fetch(`${url}?${params.toString()}`)
        .then((response) => {
          if (!response.ok) {
            throw new Error(`Failed to load ${dataType} data`);
          }
          return response.json();
        })
        .then((data) => {
          this.dataCache[dataType] = data;
          return data;
        });
    },

    // Show loading state
    showLoading: function () {
      document.querySelectorAll(".chart-container").forEach((container) => {
        container.classList.add("loading");
        const loadingEl = container.querySelector(".chart-loading");
        if (loadingEl) {
          loadingEl.style.display = "flex";
        }
      });
    },

    // Hide loading state
    hideLoading: function () {
      document.querySelectorAll(".chart-container").forEach((container) => {
        container.classList.remove("loading");
        const loadingEl = container.querySelector(".chart-loading");
        if (loadingEl) {
          loadingEl.style.display = "none";
        }
      });
    },

    // Show error message
    showError: function (message, containerId) {
      if (containerId) {
        const container = document.getElementById(containerId);
        if (container) {
          const errorEl = container.querySelector(".chart-error");
          if (errorEl) {
            errorEl.textContent = message;
            errorEl.style.display = "block";
          }
        }
      } else {
        // Global error message
        const errorContainer = document.getElementById("analytics-error");
        if (errorContainer) {
          errorContainer.textContent = message;
          errorContainer.style.display = "block";

          setTimeout(() => {
            errorContainer.style.display = "none";
          }, 5000);
        }
      }
    },

    // Task Completion Over Time Chart
    initTaskCompletionChart: function () {
      const ctx = document.getElementById("task-completion-chart");
      if (!ctx) return;

      const data = this.dataCache["task-metrics"];
      if (!data || !data.completionTrend) {
        this.showError("Task completion data not available", "task-completion-container");
        return;
      }

      const labels = data.completionTrend.map((item) => item.date);
      const completedTasks = data.completionTrend.map((item) => item.completed);
      const createdTasks = data.completionTrend.map((item) => item.created);

      this.charts["task-completion"] = new Chart(ctx, {
        type: "line",
        data: {
          labels: labels,
          datasets: [
            {
              label: "Completed Tasks",
              data: completedTasks,
              backgroundColor: this.colors.transparent[0],
              borderColor: this.colors.primary[0],
              borderWidth: 2,
              pointBackgroundColor: this.colors.primary[0],
              tension: 0.3,
              fill: true,
            },
            {
              label: "Created Tasks",
              data: createdTasks,
              backgroundColor: this.colors.transparent[1],
              borderColor: this.colors.primary[2],
              borderWidth: 2,
              pointBackgroundColor: this.colors.primary[2],
              tension: 0.3,
              fill: true,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "top",
            },
            tooltip: {
              mode: "index",
              intersect: false,
            },
          },
          scales: {
            x: {
              grid: {
                display: false,
              },
            },
            y: {
              beginAtZero: true,
              ticks: {
                precision: 0,
              },
            },
          },
        },
      });
    },

    // Task Status Distribution Chart
    initTaskStatusDistributionChart: function () {
      const ctx = document.getElementById("task-status-chart");
      if (!ctx) return;

      const data = this.dataCache["task-metrics"];
      if (!data || !data.statusDistribution) {
        this.showError("Task status data not available", "task-status-container");
        return;
      }

      const labels = data.statusDistribution.map((item) => item.status);
      const values = data.statusDistribution.map((item) => item.count);

      this.charts["task-status"] = new Chart(ctx, {
        type: "doughnut",
        data: {
          labels: labels,
          datasets: [
            {
              data: values,
              backgroundColor: this.colors.primary,
              borderWidth: 1,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "right",
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  const total = context.dataset.data.reduce((a, b) => a + b, 0);
                  const percentage = Math.round((context.raw / total) * 100);
                  return `${context.label}: ${context.raw} (${percentage}%)`;
                },
              },
            },
          },
          cutout: "65%",
        },
      });
    },

    // User Productivity Chart
    initUserProductivityChart: function () {
      const ctx = document.getElementById("user-productivity-chart");
      if (!ctx) return;

      const data = this.dataCache["user-activity"];
      if (!data || !data.userProductivity) {
        this.showError("User productivity data not available", "user-productivity-container");
        return;
      }

      const users = data.userProductivity.map((item) => item.name);
      const completedTasks = data.userProductivity.map((item) => item.tasksCompleted);

      this.charts["user-productivity"] = new Chart(ctx, {
        type: "bar",
        data: {
          labels: users,
          datasets: [
            {
              label: "Completed Tasks",
              data: completedTasks,
              backgroundColor: this.colors.secondary,
              borderWidth: 0,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false,
            },
          },
          scales: {
            x: {
              grid: {
                display: false,
              },
            },
            y: {
              beginAtZero: true,
              ticks: {
                precision: 0,
              },
            },
          },
        },
      });
    },

    // Project Progress Chart
    initProjectProgressChart: function () {
      const ctx = document.getElementById("project-progress-chart");
      if (!ctx) return;

      const data = this.dataCache["project-status"];
      if (!data || !data.projects) {
        this.showError("Project progress data not available", "project-progress-container");
        return;
      }

      const projects = data.projects.map((item) => item.name);
      const progress = data.projects.map((item) => item.completionPercentage);
      const remaining = data.projects.map((item) => 100 - item.completionPercentage);

      this.charts["project-progress"] = new Chart(ctx, {
        type: "horizontalBar",
        data: {
          labels: projects,
          datasets: [
            {
              label: "Completed",
              data: progress,
              backgroundColor: this.colors.primary[2],
              borderWidth: 0,
            },
            {
              label: "Remaining",
              data: remaining,
              backgroundColor: this.colors.transparent[2],
              borderWidth: 0,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "top",
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  return `${context.dataset.label}: ${context.raw}%`;
                },
              },
            },
          },
          scales: {
            x: {
              stacked: true,
              beginAtZero: true,
              max: 100,
              ticks: {
                callback: function (value) {
                  return value + "%";
                },
              },
            },
            y: {
              stacked: true,
              grid: {
                display: false,
              },
            },
          },
        },
      });
    },

    // Time Tracking Chart
    initTimeTrackingChart: function () {
      const ctx = document.getElementById("time-tracking-chart");
      if (!ctx) return;

      const data = this.dataCache["time-tracking"];
      if (!data || !data.timeRecords) {
        this.showError("Time tracking data not available", "time-tracking-container");
        return;
      }

      const categories = data.timeRecords.map((item) => item.category);
      const timeSpent = data.timeRecords.map((item) => item.hoursSpent);

      this.charts["time-tracking"] = new Chart(ctx, {
        type: "pie",
        data: {
          labels: categories,
          datasets: [
            {
              data: timeSpent,
              backgroundColor: this.colors.primary,
              borderWidth: 1,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: "right",
            },
            tooltip: {
              callbacks: {
                label: function (context) {
                  const total = context.dataset.data.reduce((a, b) => a + b, 0);
                  const percentage = Math.round((context.raw / total) * 100);
                  return `${context.label}: ${context.raw}h (${percentage}%)`;
                },
              },
            },
          },
        },
      });
    },

    // Update summary metrics at the top of the dashboard
    updateSummaryMetrics: function () {
      const taskData = this.dataCache["task-metrics"];
      const userData = this.dataCache["user-activity"];
      const projectData = this.dataCache["project-status"];

      if (taskData) {
        // Update task metrics
        this.updateMetricCard("total-tasks", taskData.totalTasks);
        this.updateMetricCard("completed-tasks", taskData.completedTasks);
        this.updateMetricCard("completion-rate", `${taskData.completionRate}%`);
        this.updateMetricCard("overdue-tasks", taskData.overdueTasks);
      }

      if (userData) {
        // Update user metrics
        this.updateMetricCard("active-users", userData.activeUsers);
        this.updateMetricCard("avg-tasks-per-user", userData.avgTasksPerUser);
      }

      if (projectData) {
        // Update project metrics
        this.updateMetricCard("active-projects", projectData.activeProjects);
        this.updateMetricCard("avg-project-completion", `${projectData.avgCompletionRate}%`);
      }
    },

    // Update a single metric card
    updateMetricCard: function (id, value) {
      const element = document.getElementById(id);
      if (element) {
        element.textContent = value;

        // Animate number counting
        this.animateCounter(element, value);
      }
    },

    // Animate counter for metric cards
    animateCounter: function (element, endValue) {
      if (typeof endValue !== "number") {
        // Handle percentage or other string values
        if (typeof endValue === "string" && endValue.indexOf("%") > -1) {
          endValue = parseFloat(endValue);
          const startValue = 0;
          const duration = 1000; // 1 second
          const startTime = performance.now();

          const updateCounter = (currentTime) => {
            const elapsedTime = currentTime - startTime;
            if (elapsedTime < duration) {
              const value = Math.round(startValue + (endValue - startValue) * (elapsedTime / duration));
              element.textContent = `${value}%`;
              requestAnimationFrame(updateCounter);
            } else {
              element.textContent = `${endValue}%`;
            }
          };

          requestAnimationFrame(updateCounter);
        }
        return;
      }

      const startValue = 0;
      const duration = 1000; // 1 second
      const startTime = performance.now();

      const updateCounter = (currentTime) => {
        const elapsedTime = currentTime - startTime;
        if (elapsedTime < duration) {
          const value = Math.round(startValue + (endValue - startValue) * (elapsedTime / duration));
          element.textContent = value;
          requestAnimationFrame(updateCounter);
        } else {
          element.textContent = endValue;
        }
      };

      requestAnimationFrame(updateCounter);
    },

    // Switch between tabs
    switchTab: function (tabId) {
      // Hide all tabs
      document.querySelectorAll(".tab-content").forEach((tab) => {
        tab.classList.remove("active");
      });

      // Deactivate all tab buttons
      document.querySelectorAll("[data-tab-target]").forEach((button) => {
        button.classList.remove("active");
      });

      // Show selected tab
      const targetTab = document.getElementById(tabId);
      if (targetTab) {
        targetTab.classList.add("active");
      }

      // Activate selected tab button
      const activeButton = document.querySelector(`[data-tab-target="${tabId}"]`);
      if (activeButton) {
        activeButton.classList.add("active");
      }
    },

    // Switch chart type
    switchChartType: function (chartId, newType) {
      const chart = this.charts[chartId];
      if (!chart) return;

      const currentData = chart.data;
      const currentOptions = chart.options;

      // Destroy current chart
      chart.destroy();

      // Special case handling for different chart types
      if ((newType === "bar" || newType === "horizontalBar") && (chart.config.type === "line" || chart.config.type === "radar")) {
        // Remove line specific options
        currentData.datasets.forEach((dataset) => {
          dataset.tension = 0;
          dataset.fill = false;
        });
      }

      if (newType === "line" && (chart.config.type === "bar" || chart.config.type === "horizontalBar")) {
        // Add line specific options
        currentData.datasets.forEach((dataset) => {
          dataset.tension = 0.3;
          dataset.fill = false;
        });
      }

      // Create new chart with the same data but different type
      const ctx = document.getElementById(`${chartId}-chart`);
      this.charts[chartId] = new Chart(ctx, {
        type: newType,
        data: currentData,
        options: currentOptions,
      });
    },

    // Apply all filters and refresh charts
    applyFilters: function () {
      this.refreshAllCharts();
    },

    // Refresh a specific chart
    refreshChart: function (chartId) {
      // Show loading indicator for this chart only
      const container = document.querySelector(`[data-chart-container="${chartId}"]`);
      if (container) {
        container.classList.add("loading");
        const loadingEl = container.querySelector(".chart-loading");
        if (loadingEl) {
          loadingEl.style.display = "flex";
        }
      }

      // Determine the data type needed for this chart
      let dataType;
      switch (chartId) {
        case "task-completion":
        case "task-status":
          dataType = "task-metrics";
          break;
        case "user-productivity":
          dataType = "user-activity";
          break;
        case "project-progress":
          dataType = "project-status";
          break;
        case "time-tracking":
          dataType = "time-tracking";
          break;
        default:
          dataType = chartId;
      }

      // Fetch updated data
      this.fetchData(dataType)
        .then(() => {
          // Update chart with new data
          const chart = this.charts[chartId];
          if (!chart) return;

          switch (chartId) {
            case "task-completion":
              this.updateTaskCompletionChart();
              break;
            case "task-status":
              this.updateTaskStatusChart();
              break;
            case "user-productivity":
              this.updateUserProductivityChart();
              break;
            case "project-progress":
              this.updateProjectProgressChart();
              break;
            case "time-tracking":
              this.updateTimeTrackingChart();
              break;
          }

          // Hide loading indicator
          if (container) {
            container.classList.remove("loading");
            const loadingEl = container.querySelector(".chart-loading");
            if (loadingEl) {
              loadingEl.style.display = "none";
            }
          }
        })
        .catch((error) => {
          console.error(`Error refreshing ${chartId} chart:`, error);
          this.showError(`Failed to refresh ${chartId} chart`, `${chartId}-container`);

          // Hide loading indicator
          if (container) {
            container.classList.remove("loading");
            const loadingEl = container.querySelector(".chart-loading");
            if (loadingEl) {
              loadingEl.style.display = "none";
            }
          }
        });
    },

    // Update task completion chart with new data
    updateTaskCompletionChart: function () {
      const chart = this.charts["task-completion"];
      if (!chart) return;

      const data = this.dataCache["task-metrics"];
      if (!data || !data.completionTrend) return;

      chart.data.labels = data.completionTrend.map((item) => item.date);
      chart.data.datasets[0].data = data.completionTrend.map((item) => item.completed);
      chart.data.datasets[1].data = data.completionTrend.map((item) => item.created);

      chart.update();
    },

    // Update task status chart with new data
    updateTaskStatusChart: function () {
      const chart = this.charts["task-status"];
      if (!chart) return;

      const data = this.dataCache["task-metrics"];
      if (!data || !data.statusDistribution) return;

      chart.data.labels = data.statusDistribution.map((item) => item.status);
      chart.data.datasets[0].data = data.statusDistribution.map((item) => item.count);

      chart.update();
    },

    // Update user productivity chart with new data
    updateUserProductivityChart: function () {
      const chart = this.charts["user-productivity"];
      if (!chart) return;

      const data = this.dataCache["user-activity"];
      if (!data || !data.userProductivity) return;

      chart.data.labels = data.userProductivity.map((item) => item.name);
      chart.data.datasets[0].data = data.userProductivity.map((item) => item.tasksCompleted);

      chart.update();
    },

    // Update project progress chart with new data
    updateProjectProgressChart: function () {
      const chart = this.charts["project-progress"];
      if (!chart) return;

      const data = this.dataCache["project-status"];
      if (!data || !data.projects) return;

      chart.data.labels = data.projects.map((item) => item.name);
      chart.data.datasets[0].data = data.projects.map((item) => item.completionPercentage);
      chart.data.datasets[1].data = data.projects.map((item) => 100 - item.completionPercentage);

      chart.update();
    },

    // Update time tracking chart with new data
    updateTimeTrackingChart: function () {
      const chart = this.charts["time-tracking"];
      if (!chart) return;

      const data = this.dataCache["time-tracking"];
      if (!data || !data.timeRecords) return;

      chart.data.labels = data.timeRecords.map((item) => item.category);
      chart.data.datasets[0].data = data.timeRecords.map((item) => item.hoursSpent);

      chart.update();
    },

    // Refresh all charts
    refreshAllCharts: function () {
      this.showLoading();

      Promise.all([this.fetchData("task-metrics"), this.fetchData("user-activity"), this.fetchData("project-status"), this.fetchData("time-tracking")])
        .then(() => {
          // Update all charts
          this.updateTaskCompletionChart();
          this.updateTaskStatusChart();
          this.updateUserProductivityChart();
          this.updateProjectProgressChart();
          this.updateTimeTrackingChart();
          this.updateSummaryMetrics();
          this.hideLoading();
        })
        .catch((error) => {
          console.error("Error refreshing analytics data:", error);
          this.showError("Failed to refresh analytics data");
          this.hideLoading();
        });
    },

    // Refresh only visible charts (for performance)
    refreshVisibleCharts: function () {
      // Get active tab
      const activeTab = document.querySelector(".tab-content.active");
      if (!activeTab) return;

      // Find charts in the active tab
      const chartContainers = activeTab.querySelectorAll("[data-chart-container]");
      chartContainers.forEach((container) => {
        const chartId = container.dataset.chartContainer;
        if (chartId) {
          this.refreshChart(chartId);
        }
      });
    },

    // Export a chart as image
    exportChart: function (chartId, format) {
      const chart = this.charts[chartId];
      if (!chart) return;

      if (format === "png" || format === "jpg") {
        const link = document.createElement("a");
        link.download = `${chartId}-chart.${format}`;
        link.href = chart.toBase64Image(`image/${format}`, 1.0);
        link.click();
      } else if (format === "csv" || format === "excel") {
        // Export data as CSV
        this.exportChartDataAsCsv(chart, chartId);
      }
    },

    // Export chart data as CSV
    exportChartDataAsCsv: function (chart, chartId) {
      const csvRows = [];

      // Add header row
      const headers = ["Label"];
      chart.data.datasets.forEach((dataset) => {
        headers.push(dataset.label || "Value");
      });
      csvRows.push(headers.join(","));

      // Add data rows
      chart.data.labels.forEach((label, i) => {
        const row = [label];
        chart.data.datasets.forEach((dataset) => {
          row.push(dataset.data[i]);
        });
        csvRows.push(row.join(","));
      });

      // Create CSV content
      const csvContent = "data:text/csv;charset=utf-8," + csvRows.join("\n");

      // Create download link
      const encodedUri = encodeURI(csvContent);
      const link = document.createElement("a");
      link.setAttribute("href", encodedUri);
      link.setAttribute("download", `${chartId}-data.csv`);
      document.body.appendChild(link);

      // Trigger download
      link.click();

      // Clean up
      document.body.removeChild(link);
    },

    // Export all charts
    exportAllCharts: function (format) {
      for (const chartId in this.charts) {
        this.exportChart(chartId, format);
      }
    },

    // Generate a comprehensive report
    generateReport: function (formData) {
      const reportType = formData.get("report-type");
      const reportFormat = formData.get("report-format");

      // Show generating report message
      const reportStatus = document.getElementById("report-status");
      if (reportStatus) {
        reportStatus.textContent = "Generating report...";
        reportStatus.classList.add("show");
      }

      // Build request parameters
      const params = new URLSearchParams({
        reportType: reportType,
        format: reportFormat,
        startDate: this.dateRange.start.toISOString(),
        endDate: this.dateRange.end.toISOString(),
      });

      // Add any filters
      document.querySelectorAll(".filter-select").forEach((filter) => {
        if (filter.value && filter.value !== "all") {
          params.append(filter.name, filter.value);
        }
      });

      // Request report generation
      fetch(`/api/analytics/reports/generate?${params.toString()}`)
        .then((response) => {
          if (!response.ok) {
            throw new Error("Failed to generate report");
          }

          // Check if direct download or returning a URL
          const contentType = response.headers.get("content-type");
          if (contentType && contentType.includes("application/json")) {
            return response.json();
          } else {
            return response.blob().then((blob) => {
              // Create object URL for direct download
              const url = window.URL.createObjectURL(blob);
              return { downloadUrl: url, directDownload: true, blob: blob };
            });
          }
        })
        .then((data) => {
          // Update status message
          if (reportStatus) {
            reportStatus.textContent = "Report generated successfully!";
            setTimeout(() => {
              reportStatus.classList.remove("show");
            }, 3000);
          }

          // Handle download
          if (data.directDownload) {
            // Determine filename from content type
            let filename = `report_${new Date().toISOString().slice(0, 10)}`;
            switch (reportFormat) {
              case "pdf":
                filename += ".pdf";
                break;
              case "excel":
                filename += ".xlsx";
                break;
              case "csv":
                filename += ".csv";
                break;
              default:
                filename += ".pdf";
            }

            const a = document.createElement("a");
            a.href = data.downloadUrl;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(data.downloadUrl);
          } else {
            // Open report in new window or download from URL
            window.open(data.downloadUrl, "_blank");
          }
        })
        .catch((error) => {
          console.error("Error generating report:", error);

          if (reportStatus) {
            reportStatus.textContent = "Failed to generate report";
            reportStatus.classList.add("error");
            setTimeout(() => {
              reportStatus.classList.remove("show", "error");
            }, 3000);
          }

          this.showError("Failed to generate report");
        });
    },
  };

  // Initialize analytics module
  Analytics.init();

  // Make Analytics module available globally
  window.Analytics = Analytics;
});
