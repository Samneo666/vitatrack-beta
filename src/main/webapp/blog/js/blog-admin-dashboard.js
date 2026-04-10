// Dashboard JavaScript
const API_BASE = '/api';

// Fetch dashboard data
function loadDashboard() {
    fetch(`${API_BASE}/admin/dashboard`, {
        credentials: 'include'
    })
    .then(response => response.json())
    .then(result => {
        if (!result.success) {
            console.error('Failed to load dashboard:', result.errMsg);
            // Redirect to login if unauthorized
            if (result.errMsg && result.errMsg.includes('未提供存取權杖')) {
                window.location.href = '/api/oauth/google/login';
            }
            return;
        }
        
        const data = result.data;
        
        // Show stats and charts containers (hidden by default)
        document.querySelector('.stats-container')?.classList.remove('d-none');
        document.querySelectorAll('.charts-container').forEach(el => el.classList.remove('d-none'));
        
        // Update summary stats
        updateStats(data);
        
        // Render charts
        renderCategoryChart(data.categoryStats);
        renderDailyTrendChart(data.dailyTrend);
        renderMonthlyTrendChart(data.monthlyTrend);
        renderTrafficChart(data.trafficSources);
    })
    .catch(error => {
        console.error('Error loading dashboard:', error);
    });
}

// Update summary statistics with count-up animation
function updateStats(data) {
    // Start all counters simultaneously
    animateCounter('total-views', data.totalViews, 700);
    animateCounter('total-likes', data.totalLikes, 700);
    animateCounter('total-shares', data.totalShares, 700);
    
    // Show all growth indicators after animation finishes
    setTimeout(() => {
        updateGrowth('views-growth', data.viewsGrowth);
        updateGrowth('likes-growth', data.likesGrowth);
        updateGrowth('shares-growth', data.sharesGrowth);
    }, 700);
}

// Count-up animation for numbers
function animateCounter(elementId, targetValue, duration) {
    const element = document.getElementById(elementId);
    const startValue = 0;
    const startTime = performance.now();
    
    function update(currentTime) {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        // Ease-out effect for smooth deceleration
        const easeOut = 1 - Math.pow(1 - progress, 3);
        const currentValue = Math.floor(startValue + (targetValue - startValue) * easeOut);
        
        element.textContent = formatNumber(currentValue);
        
        if (progress < 1) {
            requestAnimationFrame(update);
        } else {
            element.textContent = formatNumber(targetValue);
        }
    }
    
    requestAnimationFrame(update);
}

function updateGrowth(elementId, growth) {
    const span = document.getElementById(elementId);
    const container = document.getElementById(elementId + '-container');
    const icon = container.querySelector('i');
    const value = Math.abs(growth).toFixed(1);
    const isPositive = growth >= 0;
    
    // Update text
    span.textContent = `${value}% ${isPositive ? '本週增長' : '本週減少'}`;
    
    // Update icon
    icon.className = isPositive ? 'ri-arrow-up-line' : 'ri-arrow-down-line';
    
    // Update color
    container.style.color = isPositive ? '#54d3c2' : '#f90c4c';
    
    // Fade in growth indicator
    container.style.transition = 'opacity 0.3s ease';
    container.style.opacity = '1';
}

function formatNumber(num) {
    return num.toLocaleString('zh-TW');
}

// Render category distribution pie chart
function renderCategoryChart(categories) {
    Highcharts.chart('categoryChart', {
        chart: { 
            type: 'pie',
            height: 300
        },
        title: { text: null },
        credits: { enabled: false },
        plotOptions: {
            pie: {
                innerSize: '60%',
                dataLabels: {
                    enabled: true,
                    format: '{point.name}: {point.percentage:.0f}%',
                    style: {
                        fontSize: '13px',
                        fontWeight: '500',
                        color: '#313b50'
                    }
                }
            }
        },
        series: [{
            name: '文章數',
            data: categories.map((c, i) => ({
                name: c.category,
                y: c.articleCount,
                color: ['#54d3c2', '#3a4ee5', '#ffd783', '#f90c4c', '#9966ff'][i % 5]
            }))
        }]
    });
}

// Render daily trend dual-axis chart (用戶趨勢)
function renderDailyTrendChart(trend) {
    Highcharts.chart('userTrendChart', {
        chart: { 
            type: 'column',
            height: 300
        },
        title: { text: null },
        credits: { enabled: false },
        legend: {
            enabled: true,
            align: 'center',
            verticalAlign: 'top'
        },
        xAxis: { 
            categories: trend.map(t => t.date),
            labels: {
                style: { 
                    fontSize: '11px',
                    color: '#777' 
                }
            }
        },
        yAxis: [
            { 
                title: { text: null },
                labels: { 
                    format: '{value}',
                    style: { 
                        fontSize: '12px',
                        color: '#777' 
                    } 
                }
            },
            { 
                title: { text: null },
                labels: { 
                    format: '{value}',
                    style: { 
                        fontSize: '12px',
                        color: '#777' 
                    } 
                },
                opposite: true
            }
        ],
        plotOptions: {
            column: {
                borderRadius: 4
            }
        },
        series: [
            {
                name: '瀏覽',
                data: trend.map(t => t.views),
                yAxis: 0,
                color: '#3a4ee5'
            },
            {
                name: '按讚',
                data: trend.map(t => t.likes),
                yAxis: 1,
                color: '#54d3c2'
            },
            {
                name: '分享',
                data: trend.map(t => t.shares),
                yAxis: 1,
                color: '#ffd783'
            }
        ]
    });
}

// Render monthly trend area chart (瀏覽趨勢)
function renderMonthlyTrendChart(trend) {
    Highcharts.chart('viewTrendChart', {
        chart: { 
            type: 'area',
            height: 300
        },
        title: { text: null },
        credits: { enabled: false },
        xAxis: { 
            categories: trend.map(t => t.month),
            labels: { 
                rotation: 0,
                style: { 
                    fontSize: '11px',
                    color: '#777' 
                } 
            }
        },
        yAxis: {
            title: { text: null },
            labels: {
                format: '{value}',
                style: {
                    fontSize: '12px',
                    color: '#777'
                }
            }
        },
        legend: { enabled: false },
        plotOptions: {
            area: {
                fillColor: {
                    linearGradient: { x1: 0, y1: 0, x2: 0, y2: 1 },
                    stops: [
                        [0, 'rgba(58, 78, 229, 0.3)'],
                        [1, 'rgba(58, 78, 229, 0.05)']
                    ]
                },
                lineWidth: 2,
                marker: {
                    enabled: false,
                    radius: 4,
                    states: {
                        hover: {
                            enabled: true
                        }
                    }
                }
            }
        },
        series: [{
            name: '瀏覽數',
            data: trend.map(t => t.views),
            color: '#3a4ee5'
        }]
    });
}

// Render traffic sources bar chart (流量來源)
function renderTrafficChart(sources) {
    // Safety check
    if (!sources || sources.length === 0) {
        console.error('No traffic sources data available');
        document.getElementById('trafficSourceChart').innerHTML = '<p style="text-align:center;padding:50px;color:#999;">暫無數據</p>';
        return;
    }
    
    Highcharts.chart('trafficSourceChart', {
        chart: { 
            type: 'bar',
            height: 300,
            spacingBottom: 40,
            marginRight: 50
        },
        title: { text: null },
        credits: { enabled: false },
        xAxis: { 
            categories: sources.map(s => s.sourceType),
            labels: {
                style: {
                    fontSize: '11px',
                    color: '#777'
                }
            }
        },
        yAxis: {
            title: { text: null },
            labels: {
                formatter: function() {
                    return this.value.toLocaleString();
                },
                style: {
                    fontSize: '12px',
                    color: '#777'
                }
            },
            tickAmount: 5
        },
        legend: { enabled: false },
        plotOptions: {
            bar: {
                borderRadius: 4,
                dataLabels: {
                    enabled: true,
                    formatter: function() {
                        return this.y.toLocaleString();
                    },
                    align: 'left',
                    inside: false,
                    x: 10,
                    overflow: 'allow',
                    crop: false,
                    style: {
                        fontSize: '11px',
                        fontWeight: '600',
                        color: '#313b50',
                        textOutline: 'none'
                    }
                }
            }
        },
        series: [{
            name: '訪問次數',
            data: sources.map((s, i) => ({
                y: s.visitCount,
                color: ['#3a4ee5', '#54d3c2', '#ffd783', '#f90c4c', '#9966ff', '#ff6384'][i % 6]
            }))
        }]
    });
}

// Load dashboard on page load (after auth check)
document.addEventListener('DOMContentLoaded', () => {
    requireAuth().then(user => {
        if (user) {
            loadDashboard();
        }
    });
});
