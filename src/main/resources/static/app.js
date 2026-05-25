const API = '/api/v1';
const POLL_INTERVAL_MS = 1200;

const encryptForm = document.getElementById('encryptForm');
const decryptForm = document.getElementById('decryptForm');
const plainTextInput = document.getElementById('plainText');
const encryptKeyInput = document.getElementById('encryptKey');
const jobIdInput = document.getElementById('jobId');
const decryptKeyInput = document.getElementById('decryptKey');

const encryptPanel = document.getElementById('encryptPanel');
const decryptPanel = document.getElementById('decryptPanel');
const encryptResult = document.getElementById('encryptResult');
const decryptResult = document.getElementById('decryptResult');
const encryptOutput = document.getElementById('encryptOutput');
const decryptOutput = document.getElementById('decryptOutput');
const encryptStatusPill = document.getElementById('encryptStatusPill');
const decryptStatusPill = document.getElementById('decryptStatusPill');
const credentialsCard = document.getElementById('credentialsCard');
const credentialJobId = document.getElementById('credentialJobId');
const credentialKey = document.getElementById('credentialKey');

const encryptBtn = document.getElementById('encryptBtn');
const decryptBtn = document.getElementById('decryptBtn');
const clearEncryptBtn = document.getElementById('clearEncryptBtn');
const clearDecryptBtn = document.getElementById('clearDecryptBtn');
const goToDecryptBtn = document.getElementById('goToDecryptBtn');
const copyEncryptKeyBtn = document.getElementById('copyEncryptKeyBtn');
const copyJobIdBtn = document.getElementById('copyJobIdBtn');
const copyDecryptKeyBtn = document.getElementById('copyDecryptKeyBtn');
const copyDecryptOutputBtn = document.getElementById('copyDecryptOutputBtn');

const tabs = Array.from(document.querySelectorAll('.tab'));
const toastEl = document.getElementById('toast');

let activeTab = 'encrypt';
let stopPolling = null;
let lastCredentials = { jobId: null, key: null };

function showToast(message) {
    toastEl.textContent = message;
    toastEl.hidden = false;
    toastEl.classList.add('visible');
    clearTimeout(showToast._timer);
    showToast._timer = setTimeout(() => {
        toastEl.classList.remove('visible');
        setTimeout(() => { toastEl.hidden = true; }, 300);
    }, 2200);
}

async function copyText(text, button) {
    if (!text || text === '—') return;
    try {
        await navigator.clipboard.writeText(text);
        if (button) {
            button.classList.add('copied');
            setTimeout(() => button.classList.remove('copied'), 1500);
        }
        showToast('Скопировано');
    } catch {
        showToast('Не удалось скопировать');
    }
}

function copyFromElement(elementId, button) {
    const el = document.getElementById(elementId);
    const text = el?.textContent?.trim() || el?.value?.trim();
    return copyText(text, button);
}

function setLoading(btn, loading) {
    btn.disabled = loading;
    btn.classList.toggle('loading', loading);
    const spinner = btn.querySelector('.btn-spinner');
    if (spinner) spinner.hidden = !loading;
}

function setStatusPill(pill, state, label = state) {
    pill.dataset.state = state;
    pill.textContent = label;
}

function updateCopyButtons() {
    copyEncryptKeyBtn.disabled = !encryptKeyInput.value.trim();
    copyJobIdBtn.disabled = !jobIdInput.value.trim();
    copyDecryptKeyBtn.disabled = !decryptKeyInput.value.trim();
    copyDecryptOutputBtn.disabled = !decryptOutput.value.trim();
}

function showCredentials(jobId, key) {
    lastCredentials = { jobId, key };
    credentialJobId.textContent = jobId;
    credentialKey.textContent = key;
    credentialsCard.classList.remove('hidden');
    sessionStorage.setItem('vigenere_last_job', JSON.stringify({ jobId, key }));
}

function applyCredentialsToDecrypt() {
    if (!lastCredentials.jobId) return;
    jobIdInput.value = lastCredentials.jobId;
    decryptKeyInput.value = lastCredentials.key || '';
    updateCopyButtons();
}

function switchTab(tab) {
    activeTab = tab;
    tabs.forEach((btn) => {
        const active = btn.dataset.tab === tab;
        btn.classList.toggle('active', active);
        btn.setAttribute('aria-selected', String(active));
    });
    encryptPanel.classList.toggle('hidden', tab !== 'encrypt');
    encryptPanel.hidden = tab !== 'encrypt';
    decryptPanel.classList.toggle('hidden', tab !== 'decrypt');
    decryptPanel.hidden = tab !== 'decrypt';
}

function parseMaybeJson(text) {
    try {
        return text ? JSON.parse(text) : null;
    } catch {
        return { message: text };
    }
}

async function parseResponse(response) {
    const text = await response.text();
    const data = parseMaybeJson(text);
    if (!response.ok) {
        throw new Error(data?.message || `Ошибка ${response.status}`);
    }
    return data;
}

async function apiGet(path) {
    const response = await fetch(`${API}${path}`, {
        method: 'GET',
        headers: { Accept: 'application/json' }
    });
    return parseResponse(response);
}

async function apiPost(path, body) {
    const response = await fetch(`${API}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify(body)
    });
    return parseResponse(response);
}

function renderEncryptStatus(data) {
    const status = data?.status || 'IDLE';
    setStatusPill(encryptStatusPill, status);
    goToDecryptBtn.disabled = status !== 'COMPLETED';

    if (status === 'COMPLETED') {
        encryptOutput.value = data?.encryptedText || '';
        encryptOutput.classList.remove('is-error');
        if (lastCredentials.jobId) {
            showCredentials(lastCredentials.jobId, lastCredentials.key);
        }
    } else if (status === 'FAILED') {
        encryptOutput.value = data?.failureReason || 'Ошибка шифрования';
        encryptOutput.classList.add('is-error');
        credentialsCard.classList.add('hidden');
    } else if (status === 'PROCESSING' || status === 'RECEIVED') {
        encryptOutput.value = 'Ожидание…';
        encryptOutput.classList.remove('is-error');
    }
}

function startPolling(jobId) {
    if (typeof stopPolling === 'function') stopPolling();
    let cancelled = false;

    const tick = async () => {
        if (cancelled) return;
        try {
            const data = await apiGet(`/jobs/${jobId}`);
            renderEncryptStatus(data);
            if (data.status === 'COMPLETED' || data.status === 'FAILED') return;
            setTimeout(tick, POLL_INTERVAL_MS);
        } catch (error) {
            setStatusPill(encryptStatusPill, 'FAILED');
            encryptOutput.value = error.message;
            encryptOutput.classList.add('is-error');
        }
    };

    tick();
    stopPolling = () => { cancelled = true; };
}

async function handleEncrypt(event) {
    event.preventDefault();

    const text = plainTextInput.value;
    const key = encryptKeyInput.value.trim();
    if (!text.trim()) throw new Error('Введите текст');
    if (!key) throw new Error('Введите ключ');

    if (typeof stopPolling === 'function') {
        stopPolling();
        stopPolling = null;
    }

    setLoading(encryptBtn, true);
    encryptResult.classList.remove('hidden');
    credentialsCard.classList.add('hidden');
    setStatusPill(encryptStatusPill, 'PROCESSING');
    encryptOutput.value = 'Отправка…';
    encryptOutput.classList.remove('is-error');

    try {
        const data = await apiPost('/encrypt', { text, key });
        const jobId = data.jobId;
        lastCredentials = { jobId, key };

        setStatusPill(encryptStatusPill, data.status || 'RECEIVED');
        encryptOutput.value = 'Обработка…';

        if (jobId) {
            showCredentials(jobId, key);
            goToDecryptBtn.disabled = true;
            startPolling(jobId);
        }
    } finally {
        setLoading(encryptBtn, false);
    }
}

async function handleDecrypt(event) {
    event.preventDefault();

    const jobId = jobIdInput.value.trim();
    const key = decryptKeyInput.value.trim();
    if (!jobId) throw new Error('Введите Job ID');
    if (!key) throw new Error('Введите ключ');

    setLoading(decryptBtn, true);
    decryptResult.classList.remove('hidden');
    setStatusPill(decryptStatusPill, 'PROCESSING');
    decryptOutput.value = 'Расшифровка…';
    decryptOutput.classList.remove('is-error');

    try {
        const data = await apiPost(`/decrypt/${jobId}`, { key });
        setStatusPill(decryptStatusPill, 'COMPLETED');
        decryptOutput.value = data.decryptedText || '';
        copyDecryptOutputBtn.disabled = !decryptOutput.value;
    } catch (error) {
        setStatusPill(decryptStatusPill, 'FAILED');
        decryptOutput.value = error.message;
        decryptOutput.classList.add('is-error');
    } finally {
        setLoading(decryptBtn, false);
    }
}

function resetEncrypt() {
    if (typeof stopPolling === 'function') {
        stopPolling();
        stopPolling = null;
    }
    plainTextInput.value = '';
    encryptKeyInput.value = '';
    encryptResult.classList.add('hidden');
    credentialsCard.classList.add('hidden');
    encryptOutput.value = '';
    setStatusPill(encryptStatusPill, 'IDLE', '—');
    lastCredentials = { jobId: null, key: null };
    updateCopyButtons();
}

function resetDecrypt() {
    jobIdInput.value = '';
    decryptKeyInput.value = '';
    decryptResult.classList.add('hidden');
    decryptOutput.value = '';
    setStatusPill(decryptStatusPill, 'IDLE', '—');
    updateCopyButtons();
}

function goToDecrypt() {
    applyCredentialsToDecrypt();
    switchTab('decrypt');
    decryptPanel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function restoreSession() {
    try {
        const raw = sessionStorage.getItem('vigenere_last_job');
        if (!raw) return;
        const { jobId, key } = JSON.parse(raw);
        if (jobId && key) {
            lastCredentials = { jobId, key };
        }
    } catch { /* ignore */ }
}

encryptForm.addEventListener('submit', (e) => {
    handleEncrypt(e).catch((err) => {
        setStatusPill(encryptStatusPill, 'FAILED');
        encryptOutput.value = err.message;
        encryptOutput.classList.add('is-error');
        encryptResult.classList.remove('hidden');
    });
});

decryptForm.addEventListener('submit', (e) => {
    handleDecrypt(e).catch((err) => {
        setStatusPill(decryptStatusPill, 'FAILED');
        decryptOutput.value = err.message;
        decryptOutput.classList.add('is-error');
        decryptResult.classList.remove('hidden');
    });
});

clearEncryptBtn.addEventListener('click', resetEncrypt);
clearDecryptBtn.addEventListener('click', resetDecrypt);
goToDecryptBtn.addEventListener('click', goToDecrypt);

tabs.forEach((btn) => {
    btn.addEventListener('click', () => switchTab(btn.dataset.tab));
});

copyEncryptKeyBtn.addEventListener('click', () => copyText(encryptKeyInput.value.trim(), copyEncryptKeyBtn));
copyJobIdBtn.addEventListener('click', () => copyText(jobIdInput.value.trim(), copyJobIdBtn));
copyDecryptKeyBtn.addEventListener('click', () => copyText(decryptKeyInput.value.trim(), copyDecryptKeyBtn));
copyDecryptOutputBtn.addEventListener('click', () => copyText(decryptOutput.value, copyDecryptOutputBtn));

document.querySelectorAll('.copy-btn').forEach((btn) => {
    btn.addEventListener('click', () => copyFromElement(btn.dataset.copyTarget, btn));
});

encryptKeyInput.addEventListener('input', updateCopyButtons);
jobIdInput.addEventListener('input', updateCopyButtons);
decryptKeyInput.addEventListener('input', updateCopyButtons);

restoreSession();
switchTab('encrypt');
updateCopyButtons();
