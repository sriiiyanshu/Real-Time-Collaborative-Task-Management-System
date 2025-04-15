/**
 * Chart utility functions for Real-Time Task Application
 * Extends and customizes Chart.js functionality
 */
document.addEventListener("DOMContentLoaded", function () {
  "use strict";

  // Custom chart configuration and utilities
  window.ChartUtils = {
    // Default colors for consistent styling
    colors: {
      primary: "#3498db",
      secondary: "#2ecc71",
      danger: "#e74c3c",
      warning: "#f39c12",
      info: "#1abc9c",
      dark: "#34495e",
      light: "#ecf0f1",
      // Additional colors for datasets
      dataColors: ["#3498db", "#2ecc71", "#e74c3c", "#f39c12", "#9b59b6", "#1abc9c", "#34495e", "#95a5a6"],
    },

    // Default chart options for consistent styling
    defaultOptions: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: "bottom",
          labels: {
            boxWidth: 12,
            padding: 15,
          },
        },
        tooltip: {
          backgroundColor: "rgba(0, 0, 0, 0.7)",
          padding: 10,
          titleFont: {
            size: 14,
          },
          bodyFont: {
            size: 13,
          },
        },
      },
      animation: {
        duration: 1000,
        easing: "easeOutQuart",
      },
    },

    // Create line chart with default styling
    createLineChart: function (ctx, data, options = {}) {
      return new Chart(ctx, {
        type: "line",
        data: this.applyDefaultColors(data),
        options: this.mergeOptions(options, {
          scales: {
            y: {
              beginAtZero: true,
              grid: {
                color: "rgba(0, 0, 0, 0.05)",
              },
            },
            x: {
              grid: {
                display: false,
              },
            },
          },
        }),
      });
    },

    // Create bar chart with default styling
    createBarChart: function (ctx, data, options = {}) {
      return new Chart(ctx, {
        type: "bar",
        data: this.applyDefaultColors(data),
        options: this.mergeOptions(options, {
          scales: {
            y: {
              beginAtZero: true,
              grid: {
                color: "rgba(0, 0, 0, 0.05)",
              },
            },
            x: {
              grid: {
                display: false,
              },
            },
          },
        }),
      });
    },

    // Create doughnut chart with default styling
    createDoughnutChart: function (ctx, data, options = {}) {
      return new Chart(ctx, {
        type: "doughnut",
        data: this.applyDefaultColors(data),
        options: this.mergeOptions(options, {
          cutout: "70%",
          plugins: {
            legend: {
              position: "bottom",
            },
          },
        }),
      });
    },

    // Create pie chart with default styling
    createPieChart: function (ctx, data, options = {}) {
      return new Chart(ctx, {
        type: "pie",
        data: this.applyDefaultColors(data),
        options: this.mergeOptions(options, {}),
      });
    },

    // Apply default colors to datasets if not specified
    applyDefaultColors: function (data) {
      if (data.datasets) {
        data.datasets.forEach((dataset, index) => {
          if (!dataset.backgroundColor) {
            const colorIndex = index % this.colors.dataColors.length;
            dataset.backgroundColor = this.isMultiColorType(dataset.type) ? this.colors.dataColors : this.colors.dataColors[colorIndex];

            if (!this.isMultiColorType(dataset.type)) {
              dataset.borderColor = dataset.backgroundColor;
            }
          }
        });
      }
      return data;
    },

    // Check if chart type needs multiple colors (like pie/doughnut)
    isMultiColorType: function (type) {
      return ["pie", "doughnut", "polarArea"].includes(type);
    },

    // Deep merge options with defaults
    mergeOptions: function (options, additionalDefaults = {}) {
      const merged = JSON.parse(JSON.stringify(this.defaultOptions));

      // Merge additional defaults
      this.deepMerge(merged, additionalDefaults);

      // Merge user options
      this.deepMerge(merged, options);

      return merged;
    },

    // Helper function for deep merging objects
    deepMerge: function (target, source) {
      for (const key in source) {
        if (source[key] instanceof Object && key in target) {
          this.deepMerge(target[key], source[key]);
        } else {
          target[key] = source[key];
        }
      }
      return target;
    },

    // Format large numbers for better readability
    formatNumber: function (number) {
      if (number >= 1000000) {
        return (number / 1000000).toFixed(1) + "M";
      } else if (number >= 1000) {
        return (number / 1000).toFixed(1) + "K";
      }
      return number.toString();
    },

    // Create a stacked bar chart
    createStackedBarChart: function (ctx, data, options = {}) {
      return new Chart(ctx, {
        type: "bar",
        data: this.applyDefaultColors(data),
        options: this.mergeOptions(options, {
          scales: {
            y: {
              beginAtZero: true,
              stacked: true,
            },
            x: {
              stacked: true,
            },
          },
        }),
      });
    },

    // Create a horizontal bar chart
    createHorizontalBarChart: function (ctx, data, options = {}) {
      return new Chart(ctx, {
        type: "bar",
        data: this.applyDefaultColors(data),
        options: this.mergeOptions(options, {
          indexAxis: "y",
          scales: {
            x: {
              beginAtZero: true,
              grid: {
                color: "rgba(0, 0, 0, 0.05)",
              },
            },
            y: {
              grid: {
                display: false,
              },
            },
          },
        }),
      });
    },

    // Create a radar chart
    createRadarChart: function (ctx, data, options = {}) {
      return new Chart(ctx, {
        type: "radar",
        data: this.applyDefaultColors(data),
        options: this.mergeOptions(options, {
          elements: {
            line: {
              borderWidth: 2,
            },
            point: {
              radius: 3,
            },
          },
        }),
      });
    },

    // Update chart with new data with animation
    updateChart: function (chart, newData) {
      // Update dataset values while preserving colors
      newData.datasets.forEach((dataset, i) => {
        if (chart.data.datasets[i]) {
          const backgroundColor = chart.data.datasets[i].backgroundColor;
          const borderColor = chart.data.datasets[i].borderColor;

          Object.assign(chart.data.datasets[i], dataset);

          // Preserve colors if not specified in new data
          if (!dataset.backgroundColor) {
            chart.data.datasets[i].backgroundColor = backgroundColor;
          }
          if (!dataset.borderColor) {
            chart.data.datasets[i].borderColor = borderColor;
          }
        }
      });

      chart.data.labels = newData.labels;
      chart.update();
    },

    // Generate a random but visually pleasant color
    randomColor: function () {
      return `hsl(${Math.floor(Math.random() * 360)}, 70%, 60%)`;
    },

    // Create a gradient background for a dataset
    createGradient: function (ctx, startColor, endColor) {
      const gradient = ctx.createLinearGradient(0, 0, 0, 400);
      gradient.addColorStop(0, startColor);
      gradient.addColorStop(1, endColor);
      return gradient;
    },
  };

  // Register global chart defaults
  if (window.Chart) {
    Chart.defaults.font.family = "'Segoe UI', Tahoma, Geneva, Verdana, sans-serif";
    Chart.defaults.color = "#2c3e50";
    Chart.defaults.plugins.tooltip.titleColor = "#ffffff";
    Chart.defaults.plugins.tooltip.bodyColor = "#ffffff";
  }
});
