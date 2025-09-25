const state = { user: null, projects: [], tasks: {}, socket: null, activeProject: null };

async function fetchJSON(url, options) {
	const res = await fetch(url, Object.assign({ headers: { 'Content-Type': 'application/json' } }, options));
	if (!res.ok) throw new Error('Request failed');
	return res.json();
}

function connectSocket(projectId) {
	if (state.socket) state.socket.close();
	const proto = location.protocol === 'https:' ? 'wss' : 'ws';
	const ws = new WebSocket(`${proto}://${location.host}${location.pathname.replace(/\/[^/]*$/, '')}/ws/tasks/${projectId}`);
	ws.onmessage = (ev) => {
		try {
			const msg = JSON.parse(ev.data);
			if (msg.type === 'task_update') {
				loadTasks(projectId);
				loadAnalytics(projectId);
			}
		} catch {}
	};
	state.socket = ws;
}

async function loadProjects() {
	const data = await fetchJSON('api/projects');
	state.projects = data.projects || [];
	const list = document.getElementById('projectList');
	list.innerHTML = '';
	state.projects.forEach(p => {
		const li = document.createElement('li');
		li.textContent = p.name;
		li.onclick = () => selectProject(p);
		list.appendChild(li);
	});
}

async function loadTasks(project) {
	const projectId = typeof project === 'object' ? project.id : project;
	const data = await fetchJSON(`api/tasks?projectId=${projectId}`);
	state.tasks[projectId] = data.tasks || [];
	['todo','in_progress','done'].forEach(status => {
		const col = document.getElementById(status === 'todo' ? 'todoCol' : status === 'in_progress' ? 'inProgressCol' : 'doneCol');
		col.innerHTML = '';
		state.tasks[projectId].filter(t => t.status === status).forEach(t => {
			const div = document.createElement('div');
			div.className = 'task';
			div.draggable = true;
			div.dataset.taskId = t.id;
			div.innerHTML = `<div class="title">${t.title}</div><div class="meta">#${t.id}</div>`;
			div.addEventListener('dragstart', handleDragStart);
			col.appendChild(div);
		});
	});
	document.getElementById('taskBoard').classList.remove('hidden');
}

async function loadAnalytics(projectId) {
	try {
		const data = await fetchJSON(`api/analytics?projectId=${projectId}`);
		updateAnalyticsChart(data.counts);
	} catch (e) {
		console.error('Failed to load analytics:', e);
	}
}

function updateAnalyticsChart(counts) {
	const canvas = document.getElementById('chart1');
	if (!canvas) return;
	const ctx = canvas.getContext('2d');
	canvas.width = 400;
	canvas.height = 200;
	
	const data = [
		{ label: 'To Do', value: counts.todo || 0, color: '#ef4444' },
		{ label: 'In Progress', value: counts.in_progress || 0, color: '#f59e0b' },
		{ label: 'Done', value: counts.done || 0, color: '#22c55e' }
	];
	
	const total = data.reduce((sum, d) => sum + d.value, 0);
	if (total === 0) {
		ctx.fillStyle = '#6b7280';
		ctx.font = '14px system-ui';
		ctx.textAlign = 'center';
		ctx.fillText('No tasks yet', canvas.width/2, canvas.height/2);
		return;
	}
	
	let currentAngle = 0;
	data.forEach(d => {
		const sliceAngle = (d.value / total) * 2 * Math.PI;
		ctx.beginPath();
		ctx.moveTo(canvas.width/2, canvas.height/2);
		ctx.arc(canvas.width/2, canvas.height/2, 80, currentAngle, currentAngle + sliceAngle);
		ctx.closePath();
		ctx.fillStyle = d.color;
		ctx.fill();
		currentAngle += sliceAngle;
	});
}

function selectProject(p) {
	state.activeProject = p;
	document.getElementById('projectTitle').textContent = p.name;
	connectSocket(p.id);
	loadTasks(p);
	loadAnalytics(p.id);
}

function handleDragStart(e) {
	e.dataTransfer.setData('text/plain', e.target.dataset.taskId);
}

function handleDragOver(e) {
	e.preventDefault();
}

function handleDrop(e) {
	e.preventDefault();
	const taskId = e.dataTransfer.getData('text/plain');
	const newStatus = e.currentTarget.dataset.status;
	if (state.activeProject) {
		updateTaskStatus(taskId, newStatus);
	}
}

async function updateTaskStatus(taskId, status) {
	try {
		const formData = new FormData();
		formData.append('action', 'update_status');
		formData.append('taskId', taskId);
		formData.append('status', status);
		formData.append('projectId', state.activeProject.id);
		
		await fetch('api/tasks', {
			method: 'POST',
			body: formData
		});
	} catch (e) {
		console.error('Failed to update task status:', e);
	}
}

function showCreateTaskModal() {
	const title = prompt('Task title:');
	if (!title) return;
	const description = prompt('Task description (optional):') || '';
	createTask(title, description);
}

async function createTask(title, description) {
	if (!state.activeProject) return;
	try {
		const formData = new FormData();
		formData.append('action', 'create');
		formData.append('projectId', state.activeProject.id);
		formData.append('title', title);
		formData.append('description', description);
		
		await fetch('api/tasks', {
			method: 'POST',
			body: formData
		});
	} catch (e) {
		console.error('Failed to create task:', e);
	}
}

async function init() {
	await loadProjects();
	
	// Add drag and drop listeners
	document.querySelectorAll('.column').forEach(col => {
		col.addEventListener('dragover', handleDragOver);
		col.addEventListener('drop', handleDrop);
	});
	
	// Add create task button
	document.getElementById('newProject').addEventListener('click', () => {
		const name = prompt('Project name:');
		if (name) createProject(name);
	});
	
	// Add create task button to each column
	document.querySelectorAll('.column').forEach(col => {
		const btn = document.createElement('button');
		btn.textContent = '+ Add Task';
		btn.className = 'add-task-btn';
		btn.onclick = showCreateTaskModal;
		col.appendChild(btn);
	});
}

async function createProject(name) {
	try {
		const formData = new FormData();
		formData.append('action', 'create');
		formData.append('name', name);
		formData.append('description', '');
		
		await fetch('api/projects', {
			method: 'POST',
			body: formData
		});
		await loadProjects();
	} catch (e) {
		console.error('Failed to create project:', e);
	}
}

init();
