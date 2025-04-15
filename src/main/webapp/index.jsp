<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${sessionScope.user != null}">
        <c:redirect url="dashboard.jsp"/>
    </c:when>
    <c:otherwise>
        <jsp:include page="common/header.jsp">
            <jsp:param name="pageTitle" value="Task Management System | Streamline Your Workflow" />
        </jsp:include>

        <main class="landing-page">
            <section class="hero">
                <div class="container">
                    <div class="hero-content">
                        <h1>Streamline Your Team's Workflow</h1>
                        <p class="hero-subtitle">A powerful platform that helps teams organize, track, and manage tasks with efficiency and precision.</p>
                        <div class="hero-buttons">
                            <a href="register.jsp" class="btn btn-primary btn-lg">Start Free Trial</a>
                            <a href="login.jsp" class="btn btn-secondary btn-lg">Login</a>
                        </div>
                    </div>
                    <div class="hero-image">
                        <img src="assets/img/task-management-hero.png" alt="Task Management Dashboard" class="hero-img">
                    </div>
                </div>
            </section>

            <section class="features">
                <div class="container">
                    <h2 class="section-title">Powerful Features</h2>
                    <p class="section-subtitle">Everything you need to manage your projects efficiently</p>
                    
                    <div class="feature-grid">
                        <div class="feature-card">
                            <div class="feature-icon task-icon"></div>
                            <h3>Task Management</h3>
                            <p>Create, assign, and track tasks with deadlines and priorities. Keep everything organized in one place.</p>
                        </div>
                        <div class="feature-card">
                            <div class="feature-icon project-icon"></div>
                            <h3>Project Organization</h3>
                            <p>Group related tasks into projects for better organization and improved team coordination.</p>
                        </div>
                        <div class="feature-card">
                            <div class="feature-icon team-icon"></div>
                            <h3>Team Collaboration</h3>
                            <p>Work together with real-time updates, comments, and file sharing for seamless teamwork.</p>
                        </div>
                        <div class="feature-card">
                            <div class="feature-icon analytics-icon"></div>
                            <h3>Performance Analytics</h3>
                            <p>Get insights into team productivity and project progress with visual analytics and reports.</p>
                        </div>
                    </div>
                </div>
            </section>
            
            <section class="how-it-works">
                <div class="container">
                    <h2 class="section-title">How It Works</h2>
                    <div class="steps-container">
                        <div class="step">
                            <div class="step-number">1</div>
                            <h3>Create Projects</h3>
                            <p>Set up projects and define their goals, timelines, and team members.</p>
                        </div>
                        <div class="step">
                            <div class="step-number">2</div>
                            <h3>Add Tasks</h3>
                            <p>Break projects down into manageable tasks with clear deadlines.</p>
                        </div>
                        <div class="step">
                            <div class="step-number">3</div>
                            <h3>Collaborate</h3>
                            <p>Work together, share files, and communicate within the platform.</p>
                        </div>
                        <div class="step">
                            <div class="step-number">4</div>
                            <h3>Track Progress</h3>
                            <p>Monitor completion rates and identify bottlenecks with analytics.</p>
                        </div>
                    </div>
                </div>
            </section>

            <section class="cta">
                <div class="container">
                    <div class="cta-content">
                        <h2>Ready to boost your team's productivity?</h2>
                        <p>Join thousands of teams who trust our platform for their project management needs.</p>
                        <a href="register.jsp" class="btn btn-primary btn-lg">Start Free Trial</a>
                    </div>
                </div>
            </section>
        </main>

        <jsp:include page="common/footer.jsp" />
        
        <style>
            /* Landing page specific styles */
            .landing-page {
                background-color: #fafafa;
            }
            
            .hero {
                padding: 80px 0;
                background: linear-gradient(135deg, var(--primary-color) 0%, #2980b9 100%);
                color: white;
                position: relative;
                overflow: hidden;
            }
            
            .hero-subtitle {
                font-size: 1.2rem;
                margin-bottom: 30px;
                max-width: 600px;
            }
            
            .hero .container {
                display: flex;
                align-items: center;
                justify-content: space-between;
            }
            
            .hero-content {
                flex: 1;
                max-width: 600px;
                z-index: 2;
            }
            
            .hero-image {
                flex: 1;
                display: flex;
                justify-content: flex-end;
                z-index: 2;
            }
            
            .hero-img {
                max-width: 100%;
                border-radius: 10px;
                box-shadow: 0 15px 30px rgba(0,0,0,0.2);
            }
            
            .hero h1 {
                font-size: 3rem;
                font-weight: 700;
                margin-bottom: 20px;
                line-height: 1.2;
            }
            
            .btn-lg {
                padding: 12px 24px;
                font-size: 1rem;
                font-weight: 600;
            }
            
            .section-title {
                text-align: center;
                font-size: 2.2rem;
                font-weight: 700;
                margin-bottom: 10px;
                color: var(--dark-color);
            }
            
            .section-subtitle {
                text-align: center;
                font-size: 1.1rem;
                color: #777;
                margin-bottom: 40px;
            }
            
            .features {
                padding: 80px 0;
                background-color: white;
            }
            
            .feature-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                gap: 30px;
                margin-top: 40px;
            }
            
            .feature-card {
                background: white;
                border-radius: 10px;
                box-shadow: 0 5px 15px rgba(0,0,0,0.05);
                padding: 30px;
                transition: transform 0.3s, box-shadow 0.3s;
                text-align: center;
            }
            
            .feature-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 10px 25px rgba(0,0,0,0.1);
            }
            
            .feature-icon {
                width: 70px;
                height: 70px;
                margin: 0 auto 20px;
                background: rgba(52, 152, 219, 0.1);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 28px;
                color: var(--primary-color);
            }
            
            .feature-card h3 {
                margin-bottom: 15px;
                font-weight: 600;
            }
            
            .how-it-works {
                padding: 80px 0;
                background-color: #f5f7fa;
            }
            
            .steps-container {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                gap: 30px;
                margin-top: 40px;
            }
            
            .step {
                text-align: center;
                padding: 30px;
                background: white;
                border-radius: 10px;
                box-shadow: 0 5px 15px rgba(0,0,0,0.05);
            }
            
            .step-number {
                width: 50px;
                height: 50px;
                background: var(--primary-color);
                color: white;
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 1.5rem;
                font-weight: bold;
                margin: 0 auto 20px;
            }
            
            .cta {
                padding: 80px 0;
                background: linear-gradient(135deg, var(--secondary-color) 0%, #27ae60 100%);
                color: white;
                text-align: center;
            }
            
            .cta h2 {
                font-size: 2.5rem;
                font-weight: 700;
                margin-bottom: 20px;
            }
            
            .cta p {
                font-size: 1.2rem;
                margin-bottom: 30px;
                max-width: 700px;
                margin-left: auto;
                margin-right: auto;
            }
            
            @media (max-width: 992px) {
                .hero .container {
                    flex-direction: column;
                    text-align: center;
                }
                
                .hero-content {
                    margin-bottom: 40px;
                }
                
                .hero-buttons {
                    justify-content: center;
                }
                
                .hero-subtitle {
                    margin-left: auto;
                    margin-right: auto;
                }
            }
            
            @media (max-width: 768px) {
                .hero {
                    padding: 60px 0;
                }
                
                .hero h1 {
                    font-size: 2.2rem;
                }
                
                .section-title {
                    font-size: 2rem;
                }
                
                .cta h2 {
                    font-size: 2rem;
                }
            }
        </style>
    </c:otherwise>
</c:choose>