const API_URL = 'http://localhost:8080/api/v1';

function getToken() {
    return localStorage.getItem('accessToken');
}

function setTokens(at, rt) {
    localStorage.setItem('accessToken', at);
    localStorage.setItem('refreshToken', rt);
}

function clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
}

function showToast(message, type = 'success') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `${type === 'success' ? '✅' : '❌'} ${message}`;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

async function apiRequest(url, method = 'GET', body = null, isFormData = false) {
    const headers = {};
    if (!isFormData) headers['Content-Type'] = 'application/json';
    const token = getToken();
    if (token) headers['Authorization'] = 'Bearer ' + token;
    const config = { method, headers };
    if (body) config.body = isFormData ? body : JSON.stringify(body);
    const r = await fetch(API_URL + url, config);
    if (r.status === 401) {
        clearTokens();
        return r;
    }
    return r;
}

const STATUS_LABELS = {
    PENDING: 'Не сдано',
    SUBMITTED: 'На проверке',
    GRADED: 'Проверено',
    RETURNED: 'Возвращено'
};

const STATUS_CLASSES = {
    PENDING: 'badge-pending',
    SUBMITTED: 'badge-submitted',
    GRADED: 'badge-graded',
    RETURNED: 'badge-pending'
};

document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const loginForm = document.getElementById('loginForm');

    if (registerForm) {
        registerForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;
            const firstName = document.getElementById('firstName').value.trim();
            const lastName = document.getElementById('lastName').value.trim();
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!firstName) {
                showAlert('Введите имя', 'error');
                return;
            }
            if (!lastName) {
                showAlert('Введите фамилию', 'error');
                return;
            }
            if (!email) {
                showAlert('Введите email', 'error');
                return;
            }
            if (!emailRegex.test(email)) {
                showAlert('Некорректный формат email', 'error');
                return;
            }
            if (!password) {
                showAlert('Введите пароль', 'error');
                return;
            }
            if (password.length < 8) {
                showAlert('Пароль должен быть минимум 8 символов', 'error');
                return;
            }

            const r = await apiRequest('/auth/register', 'POST', { email, password, firstName, lastName });
            const j = await r.json();
            if (r.ok) {
                setTokens(j.accessToken, j.refreshToken);
                window.location.href = '/dashboard';
            } else {
                showAlert(j.message || 'Ошибка регистрации', 'error');
            }
        });
    }

    if (loginForm) {
        loginForm.addEventListener('submit', async function(e) {
            e.preventDefault();
            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;

            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!email) {
                showAlert('Введите email', 'error');
                return;
            }
            if (!emailRegex.test(email)) {
                showAlert('Некорректный формат email', 'error');
                return;
            }
            if (!password) {
                showAlert('Введите пароль', 'error');
                return;
            }

            const r = await apiRequest('/auth/login', 'POST', { email, password });
            if (!r) { showAlert('Ошибка сервера', 'error'); return; }
            const j = await r.json();
            if (r.ok) {
                setTokens(j.accessToken, j.refreshToken);
                window.location.href = '/dashboard';
            } else {
                showAlert(j.message || 'Неверный логин или пароль', 'error');
            }
        });
    }
});

function showAlert(msg, type) {
    const el = document.getElementById('alert');
    if (el) {
        el.textContent = msg;
        el.className = 'alert alert-' + type;
        el.style.display = 'block';
    } else {
        alert(msg);
    }
}

document.querySelectorAll('.tab-header').forEach(header => {
    header.addEventListener('click', function() {
        const tab = this.parentElement;
        const wasActive = tab.classList.contains('active');
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        if (!wasActive) {
            tab.classList.add('active');
            const tabName = this.dataset.tab;
            if (tabName === 'subjects') loadSubjects();
            if (tabName === 'admin') loadAdmin();
            if (tabName === 'stats') loadStudentStats();
        }
    });
});

async function uploadAvatar() {
    const f = document.getElementById('hiddenAvatarFile').files[0];
    if (!f) return;
    const fd = new FormData();
    fd.append('file', f);
    const r = await fetch(API_URL + '/users/me/avatar', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + getToken() },
        body: fd
    });
    if (r.ok) {
        const d = await r.json();
        const av = document.getElementById('avatarLetter');
        av.style.backgroundImage = `url(${d}?t=${Date.now()})`;
        av.style.backgroundSize = 'cover';
        av.style.backgroundPosition = 'center';
        av.textContent = '';
    }
}

async function loadProfile() {
    if (!getToken()) {
        window.location.href = '/login';
        return;
    }
    const r = await apiRequest('/users/me');
    if (!r || !r.ok) return;
    const u = await r.json();
    document.getElementById('navUserName').textContent = u.firstName + ' ' + u.lastName;
    const hour = new Date().getHours();
    let greeting = '';
    let emoji = '';
    if (hour >= 5 && hour < 13) {
        greeting = 'Доброе утро, ';
        emoji = '🌅';
    } else if (hour >= 13 && hour < 17) {
        greeting = 'Добрый день, ';
        emoji = '☀️';
    } else if (hour >= 17 && hour < 24) {
        greeting = 'Добрый вечер, ';
        emoji = '🌙';
    } else {
        greeting = 'Доброй ночи, ';
        emoji = '🌙';
    }
    document.getElementById('profileName').innerHTML = `<span style="color: inherit; filter: none; -webkit-text-fill-color: initial;">${emoji}</span> <span style="background: linear-gradient(135deg, #fff, #8e8ea0); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">${greeting}${u.firstName} ${u.lastName}</span>`;
    document.getElementById('userEmail').textContent = u.email;
    document.getElementById('userRole').textContent = u.role === 'ADMIN' ? 'Администратор' : 'Студент';
    const av = document.getElementById('avatarLetter');
    if (u.avatarUrl) {
        av.style.backgroundImage = `url(${u.avatarUrl})`;
        av.style.backgroundSize = 'cover';
        av.style.backgroundPosition = 'center';
        av.textContent = '';
    } else {
        av.textContent = u.firstName?.charAt(0) || '?';
        av.style.backgroundImage = '';
    }
    document.getElementById('firstName').value = u.firstName || '';
    document.getElementById('lastName').value = u.lastName || '';
    document.getElementById('university').value = u.university || '';
    document.getElementById('major').value = u.major || '';
    document.getElementById('profileGroupName').value = u.groupName || '';
    const isAdmin = u.role === 'ADMIN';
    ['university', 'major', 'profileGroupName'].forEach(id => {
        const el = document.getElementById(id);
        if (!isAdmin) {
            el.readOnly = true;
            el.style.opacity = '0.5';
            el.style.background = '#1a1a23';
            el.style.cursor = 'not-allowed';
        } else {
            el.readOnly = false;
            el.style.opacity = '1';
            el.style.background = '';
            el.style.cursor = '';
        }
    });
    const p = [];
    if (u.university) p.push(u.university);
    if (u.major) p.push(u.major);
    if (u.groupName) p.push(u.groupName);
    document.getElementById('profileDetails').textContent = p.join(', ') || '';
    if (isAdmin) {
        document.getElementById('adminTab').style.display = '';
        document.getElementById('subjectsTab').style.display = 'none';
        document.getElementById('statsTab').style.display = 'none';
    } else {
        document.getElementById('adminTab').style.display = 'none';
        document.getElementById('subjectsTab').style.display = '';
        document.getElementById('statsTab').style.display = '';
    }
}

async function saveProfile() {
    const d = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        university: document.getElementById('university').value,
        major: document.getElementById('major').value,
        groupName: document.getElementById('profileGroupName').value
    };
    await apiRequest('/users/me', 'PUT', d);
    showToast('Данные успешно сохранены!', 'success');
    loadProfile();
}

async function loadSubjects() {
    const grid = document.getElementById('subjectsGrid');
    grid.innerHTML = '';
    grid.dataset.loaded = 'true';
    const r = await apiRequest('/my-subjects');
    if (!r || !r.ok) return;
    const s = await r.json();
    if (!s || s.length === 0) {
        grid.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-text">У вас пока нет заданий</div>
                <div class="empty-state-subtext">Администратор ещё не добавил вас в группу</div>
            </div>`;
        return;
    }
    for (const subject of s) {
        grid.innerHTML += `
            <div style="background:var(--bg-input);padding:12px;border-radius:6px;margin-bottom:8px;">
                <div style="font-weight:500;">${subject.name}</div>
                <div style="color:#8c8c9c;font-size:12px;">${subject.teacherName} · ${subject.totalHours}ч</div>
                <button class="btn btn-secondary btn-sm" style="margin-top:6px;" onclick="loadAssignments('${subject.id}',this)">Задания</button>
                <div class="assigns" style="margin-top:8px;"></div>
            </div>
        `;
    }
}

async function loadAssignments(sid, btn) {
    const c = btn.nextElementSibling;
    if (c.innerHTML) {
        c.innerHTML = '';
        btn.textContent = 'Задания';
        return;
    }
    btn.textContent = 'Скрыть';
    const r = await apiRequest('/subjects/' + sid + '/assignments');
    if (!r || !r.ok) return;
    const data = await r.json();
    const items = data.content || data;
    let totalAssignments = items.length;
    let gradedCount = 0;
    for (const a of items) {
        const statusRes = await apiRequest('/assignments/' + a.id + '/my-status');
        const submissionStatus = statusRes.ok ? (await statusRes.json()).status : 'PENDING';
        if (submissionStatus === 'GRADED') {
            gradedCount++;
        }
    }
    const progress = totalAssignments > 0 ? Math.round((gradedCount / totalAssignments) * 100) : 0;
    c.innerHTML = `
        <div style="margin-bottom:12px; padding-bottom:8px; border-bottom:1px solid #2a2a35;">
            <div style="display:flex; align-items:center; gap:8px; margin-bottom:4px;">
                <span style="font-size:11px; color:#5c5c6e;">Прогресс по предмету</span>
                <span style="font-size:11px; font-weight:500; color:#4a8c5c;">${progress}% (${gradedCount}/${totalAssignments})</span>
            </div>
            <div style="background:var(--bg-input); border-radius:10px; height:6px; overflow:hidden;">
                <div style="width:${progress}%; height:100%; background:linear-gradient(90deg, #4a8c5c, #5da870); border-radius:10px;"></div>
            </div>
        </div>`;
    for (const a of items) {
        const statusRes = await apiRequest('/assignments/' + a.id + '/my-status');
        const submissionStatus = statusRes.ok ? (await statusRes.json()).status : 'PENDING';
        const dl = new Date(a.dueDate).toLocaleString('ru', {
            day: 'numeric',
            month: 'long',
            hour: '2-digit',
            minute: '2-digit'
        });
        let statusText = '';
        let statusClass = '';
        let showButtons = '';
        switch (submissionStatus) {
            case 'SUBMITTED':
                statusText = 'На проверке';
                statusClass = 'badge-submitted';
                showButtons = `<button class="btn-logout btn-sm" onclick="cancelSub('${a.id}')">Отменить</button>`;
                break;
            case 'GRADED':
                statusText = 'Проверено';
                statusClass = 'badge-graded';
                showButtons = `<div style="color:#4a8c5c;font-size:11px;">Оценка: ${a.score || '—'}/100</div>`;
                break;
            case 'RETURNED':
                statusText = 'Возвращено';
                statusClass = 'badge-pending';
                showButtons = `<div style="margin-top:4px;"><label class="btn btn-secondary btn-sm" for="file-${a.id}">Выбрать файл</label>
                              <input type="file" id="file-${a.id}" style="display:none;" onchange="this.previousElementSibling.textContent=this.files[0]?.name||'Выбрать файл'">
                              <button class="btn btn-primary btn-sm" onclick="submitAss('${a.id}')">Отправить снова</button></div>`;
                break;
            default:
                statusText = 'Не сдано';
                statusClass = 'badge-pending';
                showButtons = `<div style="margin-top:4px;"><label class="btn btn-secondary btn-sm" for="file-${a.id}">Выбрать файл</label>
                              <input type="file" id="file-${a.id}" style="display:none;" onchange="this.previousElementSibling.textContent=this.files[0]?.name||'Выбрать файл'">
                              <button class="btn btn-primary btn-sm" onclick="submitAss('${a.id}')">Отправить</button></div>`;
        }
        c.innerHTML += `<div class="ass-item" style="background:#1a1a23;padding:8px;border-radius:4px;margin-bottom:4px;">
            <div style="display:flex;justify-content:space-between;align-items:center;">
                <div><span style="font-size:12px;">${a.title}</span><br><span style="color:#5c5c6e;font-size:10px;">${dl}</span></div>
                <span class="badge ${statusClass}">${statusText}</span>
            </div>
            ${showButtons}
        </div>`;
    }
}

async function submitAss(aid) {
    const fi = document.getElementById('file-' + aid);
    const file = fi?.files[0];
    const fd = new FormData();
    if (file) fd.append('file', file);
    const r = await fetch(API_URL + '/assignments/' + aid + '/submit', {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + getToken() },
        body: file ? fd : null
    });
    if (r && r.ok) {
        showToast('Работа отправлена на проверку!', 'success');
        const btn = document.querySelector(`[onclick="submitAss('${aid}')"]`);
        if (btn) {
            const parent = btn.closest('.ass-item');
            if (parent) {
                parent.querySelector('.badge').textContent = 'На проверке';
                parent.querySelector('.badge').className = 'badge badge-submitted';
                const btnsDiv = parent.querySelector('div:last-child');
                btnsDiv.innerHTML = `<button class="btn-logout btn-sm" onclick="cancelSub('${aid}')">Отменить</button>`;
            }
        }
    } else {
        const error = await r.text();
        showToast('Ошибка: ' + error, 'error');
    }
}

async function cancelSub(aid) {
    const r = await apiRequest('/assignments/' + aid + '/cancel-submit', 'PUT');
    if (r && r.ok) {
        const item = document.querySelector(`[onclick="cancelSub('${aid}')"]`)?.closest('.ass-item');
        if (item) {
            item.querySelector('.badge').textContent = 'Не сдано';
            item.querySelector('.badge').className = 'badge badge-pending';
            item.querySelector('.btn-logout')?.remove();
            item.innerHTML += `<div style="margin-top:4px;"><label class="btn btn-secondary btn-sm" style="cursor:pointer;padding:4px 8px;font-size:11px;" for="file-${a.id}">Выбрать файл</label><input type="file" id="file-${a.id}" style="display:none;" onchange="this.previousElementSibling.textContent=this.files[0]?.name||'Выбрать файл'"><button class="btn btn-primary btn-sm" style="margin-left:4px;" onclick="submitAss('${aid}')">Отправить</button></div>`;
        }
    }
}

async function loadStudents() {
    const r = await apiRequest('/admin/students');
    const s = await r.json();
    document.getElementById('adminContent').innerHTML = '<div style="font-weight:500;margin-bottom:12px;">Студенты</div>' + s.map(s => `<div style="background:var(--bg-input);padding:10px;margin:4px 0;display:flex;justify-content:space-between;"><div>${s.firstName} ${s.lastName}<br><span style="color:#8c8c9c;font-size:11px;">${s.email} · ${s.groupName} · Работ: ${s.totalGrades}</span></div><div><button class="btn btn-secondary btn-sm" onclick="studentStats('${s.id}')">Успеваемость</button><button class="btn btn-secondary btn-sm" onclick="enrollForm('${s.id}')">В группу</button></div></div>`).join('');
}

async function studentStats(uid) {
    const r = await apiRequest('/admin/stats/' + uid);
    const d = await r.json();
    document.getElementById('adminContent').innerHTML = `<button class="btn btn-secondary btn-sm" onclick="loadStudents()">← Назад</button><div style="margin-top:12px;">Средний балл: <b>${d.averageScore}</b> · Оценок: ${d.totalGrades}</div>` + d.grades.map(g => `<div style="background:var(--bg-input);padding:6px;margin:4px 0;">${g.subject}: ${g.assignment} — <b style="color:#4a8c5c;">${g.score}/100</b></div>`).join('');
}

async function enrollForm(uid) {
    const r = await apiRequest('/admin/groups');
    const gs = await r.json();
    document.getElementById('adminContent').innerHTML = '<div style="font-weight:500;">Выбрать группу</div>' + gs.map(g => `<div style="background:var(--bg-input);padding:8px;margin:4px 0;display:flex;justify-content:space-between;">${g.name}<button class="btn btn-primary btn-sm" onclick="enroll('${uid}','${g.id}')">Зачислить</button></div>`).join('');
}

async function enroll(uid, gid) {
    await apiRequest('/admin/enroll', 'POST', { userId: uid, groupId: gid });
    loadStudents();
}

async function loadGroups() {
    const r = await apiRequest('/admin/groups');
    const gs = await r.json();
    document.getElementById('adminContent').innerHTML = '<div style="font-weight:500;">Группы <button class="btn btn-primary btn-sm" onclick="createGroupForm()">+ Создать</button></div>' + gs.map(g => `<div style="background:var(--bg-input);padding:8px;margin:4px 0;"><b>${g.name}</b> · ${g.periodName} · ${g.studentCount} студ. <button class="btn btn-secondary btn-sm" onclick="viewGroup('${g.id}')">Студенты</button></div>`).join('');
}

async function viewGroup(gid) {
    const r = await apiRequest('/admin/groups/' + gid + '/students');
    const ss = await r.json();
    document.getElementById('adminContent').innerHTML = '<button class="btn btn-secondary btn-sm" onclick="loadGroups()">← Назад</button><div style="margin-top:8px;font-weight:500;">Студенты группы</div>' + ss.map(s => `<div style="background:var(--bg-input);padding:6px;margin:4px 0;display:flex;justify-content:space-between;">${s.firstName} ${s.lastName}<button class="btn-logout btn-sm" onclick="removeStudent('${s.id}','${gid}')">Удалить</button></div>`).join('');
}

async function removeStudent(uid, gid) {
    await apiRequest('/admin/unenroll', 'DELETE', { userId: uid, groupId: gid });
    viewGroup(gid);
}

function createGroupForm() {
    document.getElementById('adminContent').innerHTML = `
        <div style="margin-bottom:16px;">
            <div style="font-weight:500;margin-bottom:12px;">Создать группу</div>
            <input id="newGroupName" class="form-input" placeholder="Название группы" autocomplete="off" style="margin-bottom:8px;">
            <input id="newGroupPeriod" class="form-input" placeholder="ID семестра (UUID)" autocomplete="off" style="margin-bottom:12px;">
            <div style="display:flex;gap:8px;">
                <button class="btn btn-primary btn-sm" onclick="createNewGroup()">Создать</button>
                <button class="btn btn-secondary btn-sm" onclick="loadGroups()">Отмена</button>
            </div>
        </div>
    `;
    document.getElementById('newGroupName').value = '';
    document.getElementById('newGroupPeriod').value = '';
}

async function loadSubjectsList() {
    const r = await apiRequest('/admin/subjects');
    const ss = await r.json();
    document.getElementById('adminContent').innerHTML = '<div style="font-weight:500;">Предметы <button class="btn btn-primary btn-sm" onclick="createSubjectForm()">+ Создать</button></div>' + ss.map(s => `<div style="background:var(--bg-input);padding:6px;margin:4px 0;">${s.name} · ${s.teacherName} · ${s.totalHours}ч (${s.groupName})</div>`).join('');
}

function createSubjectForm() {
    document.getElementById('adminContent').innerHTML = `<div>Создать предмет</div><input id="sname" class="form-input" placeholder="Название"><input id="sgroup" class="form-input" placeholder="ID группы"><input id="steacher" class="form-input" placeholder="Преподаватель"><input id="shours" class="form-input" placeholder="Часы" value="100"><button class="btn btn-primary btn-sm" onclick="createSubject()">Создать</button>`;
}

async function createSubject() {
    await apiRequest('/admin/subjects', 'POST', {
        name: document.getElementById('sname').value,
        groupId: document.getElementById('sgroup').value,
        teacherName: document.getElementById('steacher').value,
        totalHours: parseInt(document.getElementById('shours').value),
        weightCoefficient: 0.5
    });
    loadSubjectsList();
}

async function loadAssignmentsList() {
    const r = await apiRequest('/admin/assignments');
    const as = await r.json();
    document.getElementById('adminContent').innerHTML = '<div style="font-weight:500;">Задания <button class="btn btn-primary btn-sm" onclick="createAssignmentForm()">+ Создать</button></div>' + as.map(a => `<div style="background:var(--bg-input);padding:6px;margin:4px 0;">${a.title} · ${a.subjectName} · ${new Date(a.dueDate).toLocaleDateString('ru')} · <b>${STATUS_LABELS[a.status]}</b></div>`).join('');
}

function createAssignmentForm() {
    document.getElementById('adminContent').innerHTML = `<div>Создать задание</div>
        <input id="atitle" class="form-input" placeholder="Название">
        <input id="asubject" class="form-input" placeholder="ID предмета">
        <input type="datetime-local" id="adue" class="form-input">
        <select id="atype" class="form-input">
            <option value="HOMEWORK">Домашнее задание</option>
            <option value="LAB">Лабораторная работа</option>
            <option value="PROJECT">Курсовой проект</option>
            <option value="EXAM">Экзамен</option>
        </select>
        <button class="btn btn-primary btn-sm" onclick="createAssignment()">Создать</button>`;
}

async function createAssignment() {
    await apiRequest('/admin/assignments', 'POST', {
        title: document.getElementById('atitle').value,
        subjectId: document.getElementById('asubject').value,
        dueDate: document.getElementById('adue').value + ':00',
        type: document.getElementById('atype').value,
        weight: 0.33
    });
    loadAssignmentsList();
}

async function loadSubmissions() {
    const r = await apiRequest('/admin/submissions');
    const ss = await r.json();
    console.log('Загружены работы:', ss);
    document.getElementById('adminContent').innerHTML = `
        <div style="font-weight:500;margin-bottom:12px;">Работы на проверке</div>
        ${ss.length ? ss.map(s => `
            <div style="background:var(--bg-input);padding:10px;margin:6px 0;">
                <b>${s.title}</b> · ${s.subjectName}<br>
                Студент: ${s.studentName}
                <div style="margin-top:8px;">
                    <button class="btn btn-primary btn-sm" onclick="gradeWork('${s.assignmentId}','${s.studentId}')">Оценить</button>
                    <button class="btn-logout btn-sm" onclick="rejectWork('${s.assignmentId}','${s.studentId}')">Отклонить</button>
                    <button class="btn btn-secondary btn-sm" onclick="downloadFile('${s.assignmentId}','${s.studentId}')">Скачать</button>
                </div>
            </div>
        `).join('') : '<p style="color:#5c5c6e;">Нет работ</p>'}
    `;
    setActiveAdminButton('submissions');
}

async function gradeWork(aid, sid) {
    const sc = prompt('Оценка (0-100):');
    if (!sc || isNaN(sc) || sc < 0 || sc > 100) return;
    const response = await apiRequest('/admin/grades', 'POST', {
        userId: sid,
        assignmentId: aid,
        score: parseInt(sc)
    });
    if (response.ok) {
        showToast('Работа оценена на ' + sc + ' баллов!', 'success');
        loadSubmissions();
    } else {
        const error = await response.text();
        alert('Ошибка: ' + error);
    }
}

async function rejectWork(aid, sid) {
    if (!confirm('Отклонить работу?')) return;
    const response = await apiRequest('/admin/reject', 'POST', {
        assignmentId: aid,
        userId: sid
    });
    if (response.ok) {
        showToast('Работа отклонена! Студент получит уведомление.', 'success');
        loadSubmissions();
    } else {
        const error = await response.text();
        alert('Ошибка: ' + error);
    }
}

async function downloadFile(aid, sid) {
    const token = getToken();
    if (!token) return;
    try {
        let url = API_URL + '/admin/download/' + aid;
        if (sid) {
            url += '?userId=' + sid;
        }
        const response = await fetch(url, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!response.ok) throw new Error('Ошибка');
        const blob = await response.blob();
        const disposition = response.headers.get('Content-Disposition');
        let filename = 'file';
        if (disposition) {
            const match = disposition.match(/filename\*?=UTF-8''(.+)/);
            if (match) filename = decodeURIComponent(match[1]);
        }
        const urlBlob = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = urlBlob;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(urlBlob);
    } catch (e) {
        alert('Файл не найден');
    }
}

async function loadPeriods() {
    const r = await apiRequest('/admin/periods');
    const ps = await r.json();
    document.getElementById('adminContent').innerHTML = '<div style="font-weight:500;">Семестры <button class="btn btn-primary btn-sm" onclick="createPeriodForm()">+ Создать</button></div>' + ps.map(p => `<div style="background:var(--bg-input);padding:6px;margin:4px 0;display:flex;justify-content:space-between;">${p.name} · ${p.startDate} — ${p.endDate}<button class="btn-logout btn-sm" onclick="delPeriod('${p.id}')">Удалить</button></div>`).join('');
}

function createPeriodForm() {
    document.getElementById('adminContent').innerHTML = `<div>Создать семестр</div><input id="pname" class="form-input" placeholder="Название"><input type="date" id="pstart" class="form-input"><input type="date" id="pend" class="form-input"><button class="btn btn-primary btn-sm" onclick="createPeriod()">Создать</button>`;
}

async function createPeriod() {
    await apiRequest('/admin/periods', 'POST', {
        name: document.getElementById('pname').value,
        startDate: document.getElementById('pstart').value,
        endDate: document.getElementById('pend').value
    });
    loadPeriods();
}

async function delPeriod(pid) {
    await apiRequest('/admin/periods/' + pid, 'DELETE');
    loadPeriods();
}

async function loadStudentStats() {
    const grid = document.getElementById('statsGrid');
    const userResponse = await apiRequest('/users/me');
    const user = await userResponse.json();
    grid.innerHTML = '<p style="color:#5c5c6e;">Загрузка статистики...</p>';
    grid.dataset.loaded = 'true';
    try {
        const statsResponse = await apiRequest('/my-stats');
        if (!statsResponse.ok) throw new Error('Ошибка загрузки');
        const globalStats = await statsResponse.json();
        const subjectsStatsResponse = await apiRequest('/my-subjects-stats');
        if (!subjectsStatsResponse.ok) throw new Error('Ошибка загрузки');
        const subjectsStats = await subjectsStatsResponse.json();
        let html = `
            <div style="text-align: center; margin-bottom: 24px;">
                <div style="font-size: 14px; color: var(--text-muted);">📊 Статистика студента</div>
                <div style="font-size: 28px; font-weight: 600; background: linear-gradient(135deg, #fff, #8e8ea0); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">${user.firstName} ${user.lastName}</div>
            </div>
        `;
        html += `
            <div class="stats-global" style="background:linear-gradient(135deg, #1e1e2a, #141419); border-radius:12px; padding:20px; margin-bottom:24px; width:100%;">
                <div style="font-size:14px; color:#8c8c9c; margin-bottom:4px;">ОБЩИЙ СРЕДНИЙ БАЛЛ</div>
                <div style="font-size:48px; font-weight:600; color:#4a8c5c;">${globalStats.averageScore}</div>
                <div style="font-size:13px; color:#5c5c6e; margin-top:8px;">Всего оценок: ${globalStats.totalGrades}</div>
            </div>
        `;
        subjectsStats.forEach((subj, index) => {
            const avgColor = subj.averageScore >= 80 ? '#4a8c5c' : (subj.averageScore >= 60 ? '#c8b464' : '#c44e4e');
            html += `
                <div class="stats-subject-card" style="background:var(--bg-input); border-radius:12px; padding:16px; margin-bottom:16px; width:100%;">
                    <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px;">
                        <div style="flex:1;">
                            <div style="font-weight:600; font-size:16px;">${subj.subjectName}</div>
                            <div style="font-size:12px; color:#8c8c9c; margin-top:2px;">${subj.teacherName || 'Преподаватель'}</div>
                        </div>
                        <div style="text-align:right;">
                            <div style="font-size:28px; font-weight:600; color:${avgColor};">${subj.averageScore}</div>
                            <div style="font-size:11px; color:#5c5c6e;">Оценок: ${subj.totalGrades}</div>
                        </div>
                        <button class="btn-toggle-grades" data-subject="${subj.subjectId}" style="background:#2a2a35; border:none; color:#a8b3cf; padding:8px 16px; border-radius:8px; cursor:pointer; font-size:13px;">
                            Показать оценки
                        </button>
                    </div>
                    <div id="grades-${subj.subjectId}" style="display:none; margin-top:16px; padding-top:16px; border-top:1px solid #2a2a35;">
                        <div style="font-size:13px; color:#8c8c9c; margin-bottom:12px;">СПИСОК ЗАДАНИЙ:</div>
                        ${subj.assignments.map(a => `
                            <div style="display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid #1f1f2b;">
                                <span style="font-size:13px; flex:1;">${a.assignmentTitle}</span>
                                <span style="font-weight:600; color:#4a8c5c; min-width:60px; text-align:right;">${a.score} / 100</span>
                            </div>
                        `).join('')}
                        ${subj.assignments.length === 0 ? '<div style="padding:10px 0; color:#5c5c6e; font-size:13px; text-align:center;">Нет оценок по этому предмету</div>' : ''}
                        <button class="btn-hide-grades" data-subject="${subj.subjectId}" style="margin-top:12px; background:transparent; border:1px solid #3a3a45; color:#8c8c9c; padding:6px 12px; border-radius:6px; cursor:pointer; font-size:12px;">
                            Скрыть оценки
                        </button>
                    </div>
                </div>
            `;
        });
        if (subjectsStats.length === 0) {
            html += '<div style="text-align:center; padding:60px 20px; color:#5c5c6e; background:var(--bg-input); border-radius:12px;">У вас пока нет оценок</div>';
        }
        grid.innerHTML = html;
        grid.style.display = 'block';
        document.querySelectorAll('.btn-toggle-grades').forEach(btn => {
            btn.addEventListener('click', function() {
                const subjectId = this.getAttribute('data-subject');
                const gradesDiv = document.getElementById(`grades-${subjectId}`);
                if (gradesDiv) {
                    gradesDiv.style.display = 'block';
                    this.style.display = 'none';
                }
            });
        });
        document.querySelectorAll('.btn-hide-grades').forEach(btn => {
            btn.addEventListener('click', function() {
                const subjectId = this.getAttribute('data-subject');
                const gradesDiv = document.getElementById(`grades-${subjectId}`);
                const toggleBtn = document.querySelector(`.btn-toggle-grades[data-subject="${subjectId}"]`);
                if (gradesDiv) {
                    gradesDiv.style.display = 'none';
                    if (toggleBtn) toggleBtn.style.display = 'block';
                }
            });
        });
    } catch (error) {
        console.error(error);
        grid.innerHTML = '<p style="color:#c44e4e;">Ошибка загрузки статистики</p>';
    }
}

function toggleSubjectGrades(subjectId) {
    const gradesDiv = document.getElementById(`grades-${subjectId}`);
    if (gradesDiv) {
        if (gradesDiv.style.display === 'none') {
            gradesDiv.style.display = 'block';
        } else {
            gradesDiv.style.display = 'none';
        }
    }
}

let currentEntity = null;
let currentAction = null;
let currentId = null;
let currentAdminTab = 'students';

function openModal(entity, action, id = null) {
    currentEntity = entity;
    currentAction = action;
    currentId = id;
    if (!document.getElementById('crudModal')) {
        const modal = document.createElement('div');
        modal.id = 'crudModal';
        modal.className = 'modal';
        modal.innerHTML = `
            <div class="modal-content">
                <h3 id="modalTitle">Добавить</h3>
                <div id="modalForm"></div>
                <div class="modal-buttons">
                    <button class="btn btn-secondary" onclick="closeModal()">Отмена</button>
                    <button class="btn btn-primary" onclick="submitModalForm()">Сохранить</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
    }
    document.getElementById('modalTitle').innerText = action === 'add' ? `Добавить ${getEntityName(entity)}` : `Изменить ${getEntityName(entity)}`;
    loadForm(entity, action, id);
    document.getElementById('crudModal').style.display = 'flex';
}

function closeModal() {
    const modal = document.getElementById('crudModal');
    if (modal) modal.style.display = 'none';
    currentEntity = null;
    currentAction = null;
    currentId = null;
}

function getEntityName(entity) {
    const names = {
        'period': 'Семестр',
        'group': 'Группу',
        'subject': 'Предмет',
        'assignment': 'Задание',
        'student': 'Студента'
    };
    return names[entity] || entity;
}

async function loadForm(entity, action, id) {
    const formContainer = document.getElementById('modalForm');
    let html = '';
    if (entity === 'period') {
        html = `
            <div class="form-group"><label>Название семестра</label><input type="text" id="periodName" class="form-input" required></div>
            <div class="form-group"><label>Дата начала</label><input type="date" id="startDate" class="form-input" required></div>
            <div class="form-group"><label>Дата окончания</label><input type="date" id="endDate" class="form-input" required></div>
        `;
        if (action === 'edit' && id) {
            const r = await apiRequest('/admin/periods');
            const periods = await r.json();
            const period = periods.find(p => p.id === id);
            if (period) {
                setTimeout(() => {
                    document.getElementById('periodName').value = period.name;
                    document.getElementById('startDate').value = period.startDate;
                    document.getElementById('endDate').value = period.endDate;
                }, 100);
            }
        }
    } else if (entity === 'group') {
        html = `
            <div class="form-group"><label>Название группы</label><input type="text" id="groupName" class="form-input" required></div>
            <div class="form-group"><label>ID семестра</label><input type="text" id="periodId" class="form-input" required placeholder="a1b2c3d4-e5f6-4a7b-9c8d-1e2f3a4b5c6d"></div>
        `;
        if (action === 'edit' && id) {
            const r = await apiRequest(`/admin/groups/${id}`);
            if (r.ok) {
                const group = await r.json();
                setTimeout(() => {
                    document.getElementById('groupName').value = group.name;
                    document.getElementById('periodId').value = group.periodId;
                }, 100);
            }
        }
    } else if (entity === 'subject') {
        html = `
            <div class="form-group"><label>Название предмета</label><input type="text" id="subjectName" class="form-input" required></div>
            <div class="form-group"><label>ID группы</label><input type="text" id="groupId" class="form-input" required placeholder="b1c2d3e4-f5a6-4b7c-9d8e-2f3a4b5c6d7e"></div>
            <div class="form-group"><label>Преподаватель</label><input type="text" id="teacherName" class="form-input"></div>
            <div class="form-group"><label>Часы</label><input type="number" id="totalHours" class="form-input" value="100"></div>
        `;
        if (action === 'edit' && id) {
            const r = await apiRequest(`/admin/subjects/${id}`);
            if (r.ok) {
                const subject = await r.json();
                setTimeout(() => {
                    document.getElementById('subjectName').value = subject.name;
                    document.getElementById('groupId').value = subject.groupId;
                    document.getElementById('teacherName').value = subject.teacherName || '';
                    document.getElementById('totalHours').value = subject.totalHours || 100;
                }, 100);
            }
        }
    } else if (entity === 'assignment') {
        html = `
            <div class="form-group"><label>Название задания</label><input type="text" id="assignmentTitle" class="form-input" required></div>
            <div class="form-group"><label>ID предмета</label><input type="text" id="subjectId" class="form-input" required placeholder="c1d2e3f4-a5b6-4c7d-9e8f-3a4b5c6d7e8f"></div>
            <div class="form-group"><label>Дедлайн</label><input type="datetime-local" id="dueDate" class="form-input" required></div>
            <div class="form-group"><label>Тип</label>
                <select id="assignmentType" class="form-input">
                    <option value="HOMEWORK">Домашнее задание</option>
                    <option value="LAB">Лабораторная работа</option>
                    <option value="PROJECT">Курсовой проект</option>
                    <option value="EXAM">Экзамен</option>
                </select>
            </div>
        `;
        if (action === 'edit' && id) {
            const r = await apiRequest(`/admin/assignments/${id}`);
            if (r.ok) {
                const assignment = await r.json();
                setTimeout(() => {
                    document.getElementById('assignmentTitle').value = assignment.title;
                    document.getElementById('subjectId').value = assignment.subjectId;
                    const dueDate = assignment.dueDate.replace(' ', 'T').slice(0, 16);
                    document.getElementById('dueDate').value = dueDate;
                    document.getElementById('assignmentType').value = assignment.type || 'HOMEWORK';
                }, 100);
            }
        }
    }
    formContainer.innerHTML = html;
}

async function submitModalForm() {
    if (currentAction === 'add') {
        await addEntity(currentEntity);
    } else if (currentAction === 'edit') {
        await updateEntity(currentEntity, currentId);
    }
    closeModal();
    refreshCurrentTab();
}

async function addEntity(entity) {
    if (entity === 'period') {
        const data = {
            name: document.getElementById('periodName').value,
            startDate: document.getElementById('startDate').value,
            endDate: document.getElementById('endDate').value
        };
        await apiRequest('/admin/periods', 'POST', data);
    } else if (entity === 'group') {
        const data = {
            name: document.getElementById('groupName').value,
            periodId: document.getElementById('periodId').value
        };
        await apiRequest('/admin/groups', 'POST', data);
    } else if (entity === 'subject') {
        const data = {
            name: document.getElementById('subjectName').value,
            groupId: document.getElementById('groupId').value,
            teacherName: document.getElementById('teacherName').value,
            totalHours: parseInt(document.getElementById('totalHours').value),
            weightCoefficient: 0.5
        };
        await apiRequest('/admin/subjects', 'POST', data);
    } else if (entity === 'assignment') {
        const data = {
            title: document.getElementById('assignmentTitle').value,
            subjectId: document.getElementById('subjectId').value,
            dueDate: document.getElementById('dueDate').value + ':00',
            type: document.getElementById('assignmentType').value,
            weight: 0.33
        };
        await apiRequest('/admin/assignments', 'POST', data);
    }
}

async function updateEntity(entity, id) {
    if (entity === 'period') {
        const data = {
            name: document.getElementById('periodName').value,
            startDate: document.getElementById('startDate').value,
            endDate: document.getElementById('endDate').value
        };
        await apiRequest(`/admin/periods/${id}`, 'PUT', data);
    } else if (entity === 'group') {
        const data = {
            name: document.getElementById('groupName').value,
            periodId: document.getElementById('periodId').value
        };
        await apiRequest(`/admin/groups/${id}`, 'PUT', data);
    } else if (entity === 'subject') {
        const data = {
            name: document.getElementById('subjectName').value,
            groupId: document.getElementById('groupId').value,
            teacherName: document.getElementById('teacherName').value,
            totalHours: parseInt(document.getElementById('totalHours').value)
        };
        await apiRequest(`/admin/subjects/${id}`, 'PUT', data);
    } else if (entity === 'assignment') {
        const data = {
            title: document.getElementById('assignmentTitle').value,
            subjectId: document.getElementById('subjectId').value,
            dueDate: document.getElementById('dueDate').value + ':00',
            type: document.getElementById('assignmentType').value
        };
        await apiRequest(`/admin/assignments/${id}`, 'PUT', data);
    }
}

async function deleteEntity(entity, id, name) {
    if (!confirm(`Вы уверены, что хотите удалить ${name}?`)) return;
    let url = '';
    if (entity === 'period') url = `/admin/periods/${id}`;
    else if (entity === 'group') url = `/admin/groups/${id}`;
    else if (entity === 'subject') url = `/admin/subjects/${id}`;
    else if (entity === 'assignment') url = `/admin/assignments/${id}`;
    else if (entity === 'student') url = `/admin/users/${id}`;
    try {
        const response = await apiRequest(url, 'DELETE');
        if (response.ok) {
            refreshCurrentTab();
        } else {
            const error = await response.json();
            showToast('Ошибка удаления: ' + (error.error || error.message), 'error');
        }
    } catch (error) {
        alert('Ошибка удаления: ' + error.message);
    }
}

function refreshCurrentTab() {
    if (currentAdminTab === 'groups') loadGroups();
    else if (currentAdminTab === 'subjects') loadSubjectsList();
    else if (currentAdminTab === 'periods') loadPeriods();
    else if (currentAdminTab === 'assignments') loadAssignmentsList();
    else if (currentAdminTab === 'submissions') loadSubmissions();
    else loadStudents();
}

loadAdmin = async function() {
    document.getElementById('adminPanel').innerHTML = `
        <div style="display:flex;gap:8px;flex-wrap:wrap;margin-bottom:16px;">
            <button id="btnStudents" class="btn btn-secondary btn-sm" onclick="loadStudents()">Студенты</button>
            <button id="btnGroups" class="btn btn-secondary btn-sm" onclick="loadGroups()">Группы</button>
            <button id="btnSubjects" class="btn btn-secondary btn-sm" onclick="loadSubjectsList()">Предметы</button>
            <button id="btnSubmissions" class="btn btn-secondary btn-sm" onclick="loadSubmissions()">Проверка</button>
            <button id="btnPeriods" class="btn btn-secondary btn-sm" onclick="loadPeriods()">Семестры</button>
            <button id="btnAssignments" class="btn btn-secondary btn-sm" onclick="loadAssignmentsList()">Задания</button>
        </div>
        <div id="adminContent" style="background:var(--bg-card);border-radius:8px;padding:16px;"></div>`;
    if (currentAdminTab === 'groups') loadGroups();
    else if (currentAdminTab === 'subjects') loadSubjectsList();
    else if (currentAdminTab === 'periods') loadPeriods();
    else if (currentAdminTab === 'assignments') loadAssignmentsList();
    else if (currentAdminTab === 'submissions') loadSubmissions();
    else loadStudents();
    setActiveAdminButton(currentAdminTab);
};

function setActiveAdminButton(tab) {
    const buttons = ['btnStudents', 'btnGroups', 'btnSubjects', 'btnSubmissions', 'btnPeriods', 'btnAssignments'];
    buttons.forEach(id => {
        const btn = document.getElementById(id);
        if (btn) {
            btn.style.background = 'transparent';
            btn.style.color = 'var(--text-secondary)';
            btn.style.border = '1px solid var(--border)';
        }
    });
    let activeId = '';
    if (tab === 'students') activeId = 'btnStudents';
    else if (tab === 'groups') activeId = 'btnGroups';
    else if (tab === 'subjects') activeId = 'btnSubjects';
    else if (tab === 'submissions') activeId = 'btnSubmissions';
    else if (tab === 'periods') activeId = 'btnPeriods';
    else if (tab === 'assignments') activeId = 'btnAssignments';
    const activeBtn = document.getElementById(activeId);
    if (activeBtn) {
        activeBtn.style.background = '#2a2a35';
        activeBtn.style.color = '#ececf0';
        activeBtn.style.border = '1px solid #3a3a45';
    }
}

loadGroups = async function() {
    currentAdminTab = 'groups';
    const r = await apiRequest('/admin/groups');
    const gs = await r.json();
    document.getElementById('adminContent').innerHTML =
        '<div style="font-weight:500;margin-bottom:12px;">Группы <button class="btn btn-primary btn-sm" style="margin-left:10px;" onclick="openModal(\'group\',\'add\')">+ Добавить</button></div>' +
        gs.map(g => `<div style="background:var(--bg-input);padding:10px;margin:4px 0;display:flex;justify-content:space-between;align-items:center;">
            <div><b>${g.name || 'Без названия'}</b> · ${g.periodName} · ${g.studentCount} студ.</div>
            <div style="display:flex;gap:6px;">
                <button class="btn btn-secondary btn-sm" onclick="viewGroup('${g.id}')">Студенты</button>
                <button class="btn btn-secondary btn-sm" onclick="openModal('group','edit','${g.id}')">✏️</button>
                <button class="btn-logout btn-sm" onclick="deleteEntity('group','${g.id}','${g.name || 'группу'}')">🗑️</button>
            </div>
        </div>`).join('');
    setActiveAdminButton('groups');
};

loadSubjectsList = async function() {
    currentAdminTab = 'subjects';
    const r = await apiRequest('/admin/subjects');
    const ss = await r.json();
    document.getElementById('adminContent').innerHTML =
        '<div style="font-weight:500;margin-bottom:12px;">Предметы <button class="btn btn-primary btn-sm" style="margin-left:10px;" onclick="openModal(\'subject\',\'add\')">+ Добавить</button></div>' +
        ss.map(s => `<div style="background:var(--bg-input);padding:10px;margin:4px 0;display:flex;justify-content:space-between;align-items:center;">
            <div>${s.name} · ${s.teacherName} · ${s.totalHours}ч (${s.groupName})</div>
            <div style="display:flex;gap:6px;">
                <button class="btn btn-secondary btn-sm" onclick="openModal('subject','edit','${s.id}')">✏️</button>
                <button class="btn-logout btn-sm" onclick="deleteEntity('subject','${s.id}','${s.name}')">🗑️</button>
            </div>
        </div>`).join('');
    setActiveAdminButton('subjects');
};

loadPeriods = async function() {
    currentAdminTab = 'periods';
    const r = await apiRequest('/admin/periods');
    const ps = await r.json();
    document.getElementById('adminContent').innerHTML =
        '<div style="font-weight:500;margin-bottom:12px;">Семестры <button class="btn btn-primary btn-sm" style="margin-left:10px;" onclick="openModal(\'period\',\'add\')">+ Добавить</button></div>' +
        ps.map(p => `<div style="background:var(--bg-input);padding:10px;margin:4px 0;display:flex;justify-content:space-between;align-items:center;">
            <div>${p.name} · ${p.startDate} — ${p.endDate}</div>
            <div style="display:flex;gap:6px;">
                <button class="btn btn-secondary btn-sm" onclick="openModal('period','edit','${p.id}')">✏️</button>
                <button class="btn-logout btn-sm" onclick="deleteEntity('period','${p.id}','${p.name}')">🗑️</button>
            </div>
        </div>`).join('');
    setActiveAdminButton('periods');
};

loadAssignmentsList = async function() {
    currentAdminTab = 'assignments';
    const r = await apiRequest('/admin/assignments');
    const as = await r.json();
    document.getElementById('adminContent').innerHTML =
        '<div style="font-weight:500;margin-bottom:12px;">Задания <button class="btn btn-primary btn-sm" style="margin-left:10px;" onclick="openModal(\'assignment\',\'add\')">+ Добавить</button></div>' +
        as.map(a => `<div style="background:var(--bg-input);padding:10px;margin:4px 0;display:flex;justify-content:space-between;align-items:center;">
            <div>${a.title} · ${a.subjectName} · ${new Date(a.dueDate).toLocaleDateString('ru')}</div>
            <div style="display:flex;gap:6px;">
                <button class="btn btn-secondary btn-sm" onclick="openModal('assignment','edit','${a.id}')">✏️</button>
                <button class="btn-logout btn-sm" onclick="deleteEntity('assignment','${a.id}','${a.title}')">🗑️</button>
            </div>
        </div>`).join('');
    setActiveAdminButton('assignments');
};

loadStudents = async function() {
    currentAdminTab = 'students';
    const r = await apiRequest('/admin/students');
    const s = await r.json();
    document.getElementById('adminContent').innerHTML =
        '<div style="font-weight:500;margin-bottom:12px;">Студенты</div>' +
        s.map(s => `<div style="background:var(--bg-input);padding:10px;margin:4px 0;display:flex;justify-content:space-between;align-items:center;">
            <div>${s.firstName} ${s.lastName}<br><span style="color:#8c8c9c;font-size:11px;">${s.email} · ${s.groupName}</span></div>
            <div style="display:flex;gap:6px;">
                <button class="btn btn-secondary btn-sm" onclick="studentStats('${s.id}')">Успеваемость</button>
                <button class="btn btn-secondary btn-sm" onclick="enrollForm('${s.id}')">В группу</button>
                <button class="btn-logout btn-sm" onclick="deleteEntity('student','${s.id}','${s.firstName} ${s.lastName}')">🗑️</button>
            </div>
        </div>`).join('');
    setActiveAdminButton('students');
};

loadSubmissions = async function() {
    currentAdminTab = 'submissions';
    const r = await apiRequest('/admin/submissions');
    const ss = await r.json();
    console.log('Загружены работы:', ss);
    document.getElementById('adminContent').innerHTML = `
        <div style="font-weight:500;margin-bottom:12px;">Работы на проверке</div>
        ${ss.length ? ss.map(s => `
            <div style="background:var(--bg-input);padding:10px;margin:6px 0;">
                <b>${s.title}</b> · ${s.subjectName}<br>
                Студент: ${s.studentName}
                <div style="margin-top:8px;">
                    <button class="btn btn-primary btn-sm" onclick="gradeWork('${s.assignmentId}','${s.studentId}')">Оценить</button>
                    <button class="btn-logout btn-sm" onclick="rejectWork('${s.assignmentId}','${s.studentId}')">Отклонить</button>
                    <button class="btn btn-secondary btn-sm" onclick="downloadFile('${s.assignmentId}','${s.studentId}')">Скачать</button>
                </div>
            </div>
        `).join('') : '<p style="color:#5c5c6e;">Нет работ</p>'}
    `;
    setActiveAdminButton('submissions');
};

viewGroup = async function(gid) {
    const r = await apiRequest('/admin/groups/' + gid + '/students');
    const ss = await r.json();
    document.getElementById('adminContent').innerHTML =
        '<button class="btn btn-secondary btn-sm" onclick="loadGroups()">← Назад к группам</button>' +
        '<div style="margin-top:12px;font-weight:500;">Студенты группы</div>' +
        ss.map(s => `<div style="background:var(--bg-input);padding:10px;margin:4px 0;display:flex;justify-content:space-between;align-items:center;">
            ${s.firstName} ${s.lastName}
            <button class="btn-logout btn-sm" onclick="removeStudent('${s.id}','${gid}')">Удалить</button>
        </div>`).join('');
};

studentStats = async function(uid) {
    const r = await apiRequest('/admin/stats/' + uid);
    const d = await r.json();
    const userResponse = await apiRequest('/users/me');
    const allUsers = await apiRequest('/admin/students');
    const students = await allUsers.json();
    const student = students.find(s => s.id === uid);
    document.getElementById('adminContent').innerHTML = `
        <button class="btn btn-secondary btn-sm" onclick="loadStudents()">← Назад к студентам</button>
        <div style="margin-top: 20px; background: linear-gradient(135deg, #1e1e2a, #141419); border-radius: 16px; padding: 20px; margin-bottom: 20px;">
            <div style="font-size: 13px; color: var(--text-muted);">Успеваемость студента</div>
            <div style="font-size: 24px; font-weight: 600; background: linear-gradient(135deg, #fff, #8e8ea0); -webkit-background-clip: text; -webkit-text-fill-color: transparent;">${student ? student.firstName + ' ' + student.lastName : 'Студент'}</div>
            <div style="margin-top: 12px;">
                <span class="badge badge-graded">Средний балл: ${d.averageScore}</span>
                <span class="badge badge-submitted" style="margin-left: 8px;">Оценок: ${d.totalGrades}</span>
            </div>
        </div>
        <div style="background:var(--bg-input); border-radius: 16px; padding: 16px;">
            <div style="font-weight:500; margin-bottom: 12px;">Список оценок:</div>
            ${d.grades.map(g => `
                <div style="display:flex; justify-content:space-between; align-items:center; padding: 10px 0; border-bottom:1px solid var(--border);">
                    <div>
                        <div style="font-weight:500;">${g.subject}</div>
                        <div style="font-size:12px; color:var(--text-muted);">${g.assignment}</div>
                    </div>
                    <div style="font-size:20px; font-weight:600; color:#4a8c5c;">${g.score} / 100</div>
                </div>
            `).join('')}
            ${d.grades.length === 0 ? '<div style="text-align:center; padding: 40px; color: var(--text-muted);">Нет оценок</div>' : ''}
        </div>
    `;
};

enrollForm = async function(uid) {
    const r = await apiRequest('/admin/groups');
    const gs = await r.json();
    document.getElementById('adminContent').innerHTML = `
        <button class="btn btn-secondary btn-sm" onclick="loadStudents()">← Назад к студентам</button>
        <div style="margin-top:12px;font-weight:500;">Выбрать группу</div>
        <div id="enrollError" style="color:#c44e4e; margin-bottom:10px; display:none; background:rgba(196,78,78,0.1); padding:8px 12px; border-radius:8px;"></div>
        ${gs.map(g => `
            <div style="background:var(--bg-input);padding:10px;margin:4px 0;display:flex;justify-content:space-between;align-items:center;">
                ${g.name}
                <button class="btn btn-primary btn-sm" onclick="enrollToGroup('${uid}','${g.id}')">Зачислить</button>
            </div>
        `).join('')}
    `;
};

async function enrollToGroup(uid, gid) {
    const response = await apiRequest('/admin/enroll', 'POST', { userId: uid, groupId: gid });
    const errorDiv = document.getElementById('enrollError');
    if (response.ok) {
        showToast('Студент зачислен в группу!', 'success');
        loadStudents();
    } else {
        const errorText = await response.text();
        if (errorDiv) {
            errorDiv.textContent = errorText || 'Ошибка зачисления';
            errorDiv.style.display = 'block';
            setTimeout(() => {
                errorDiv.style.display = 'none';
            }, 4000);
        } else {
            showToast(errorText || 'Ошибка зачисления', 'error');
        }
    }
}

async function createNewGroup() {
    const name = document.getElementById('newGroupName').value.trim();
    const periodId = document.getElementById('newGroupPeriod').value.trim();
    if (!name) {
        showToast('Введите название группы', 'error');
        return;
    }
    if (!periodId) {
        alert('Введите ID семестра');
        return;
    }
    console.log('Создаем группу:', { name, periodId });
    const response = await apiRequest('/admin/groups', 'POST', {
        name: name,
        periodId: periodId
    });
    if (response.ok) {
        loadGroups();
    } else {
        const error = await response.json();
        alert('Ошибка: ' + (error.message || error.error || 'Неизвестная ошибка'));
    }
}

function logout() {
    clearTokens();
    window.location.href = '/';
}

if (window.location.pathname === '/dashboard') loadProfile();