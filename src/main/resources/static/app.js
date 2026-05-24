const form = document.getElementById('appForm');
const baseUrlInput = document.getElementById('baseUrl');
const plainTextInput = document.getElementById('plainText');
const encryptKeyInput = document.getElementById('encryptKey');
const jobIdInput = document.getElementById('jobId');
const decryptKeyInput = document.getElementById('decryptKey');
const submitBtn = document.getElementById('submitBtn');
const statusBtn = document.getElementById('statusBtn');
const clearBtn = document.getElementById('clearBtn');
const resultBox = document.getElementById('resultBox');
const currentJobIdEl = document.getElementById('currentJobId');
const statusPill = document.getElementById('statusPill');
const tabs = Array.from(document.querySelectorAll('.tab'));
const encryptMode = document.getElementById('encryptMode');
const decryptMode = document.getElementById('decryptMode');

let activeTab = 'encrypt';
let stopPolling = null;
const DEFAULT_POLL_INTERVAL = 1200;

function normalizeBaseUrl(url) {
    return url.trim().replace(/\/$/, '');
}

function setStatus(state, label = state) {
    statusPill.dataset.state = state;
    statusPill.textContent = label;
}

function setCurrentJobId(jobId = '—') {
    currentJobIdEl.textContent = jobId;
    if (jobId !== '—') jobIdInput.value = jobId;
}

function showResult(value, isError = false) {
    resultBox.value = value || '';
    resultBox.style.color = isError ? 'var(--color-error)' : 'var(--color-text)';
}

function parseMaybeJson(text) {
    try { return text ? JSON.parse(text) : null; } catch { return { message: text }; }
}

async function parseResponse(response) {
    const text = await response.text();
    const data = parseMaybeJson(text);
    if (!response.ok) throw new Error(data?.message || `HTTP ${response.status}`);
    return data;
}

function switchTab(tab) {
    activeTab = tab;
    tabs.forEach((btn) => {
        const active = btn.dataset.tab === tab;
        btn.classList.toggle('active', active);
        btn.setAttribute('aria-selected', String(active));
    });
    encryptMode.classList.toggle('hidden', tab !== 'encrypt');
    decryptMode.classList.toggle('hidden', tab !== 'decrypt');
    submitBtn.textContent = tab === 'encrypt' ? 'Encrypt' : 'Decrypt';
}

function resetForm() {
    plainTextInput.value = '';
    encryptKeyInput.value = '';
    jobIdInput.value = '';
    decryptKeyInput.value = '';
    setCurrentJobId('—');
    setStatus('IDLE');
    showResult('');
    if (typeof stopPolling === 'function') {
        stopPolling();
        stopPolling = null;
    }
}

async function requestStatus(baseUrl, jobId) {
    const response = await fetch(`${baseUrl}/api/v1/jobs/${jobId}`, {
        method: 'GET',
        headers: { Accept: 'application/json' }
    });
    return parseResponse(response);
}

function renderStatus(data) {
    const status = data?.status || 'IDLE';
    setCurrentJobId(data?.jobId || '—');
    setStatus(status);
    if (status === 'COMPLETED') showResult(data?.encryptedText || '');
    else if (status === 'FAILED') showResult(data?.failureReason || 'Request failed', true);
    else if (status === 'PROCESSING') showResult('PROCESSING');
    else if (status === 'RECEIVED') showResult('RECEIVED');
    else showResult('');
}

function startPolling(baseUrl, jobId) {
    if (typeof stopPolling === 'function') stopPolling();
    let cancelled = false;

    const tick = async () => {
        if (cancelled) return;
        try {
            const data = await requestStatus(baseUrl, jobId);
            renderStatus(data);
            if (data.status === 'COMPLETED' || data.status === 'FAILED') return;
            setTimeout(tick, DEFAULT_POLL_INTERVAL);
        } catch (error) {
            setStatus('FAILED');
            showResult(error.message, true);
        }
    };

    tick();
    stopPolling = () => { cancelled = true; };
}

async function handleEncrypt() {
    const baseUrl = normalizeBaseUrl(baseUrlInput.value);
    const text = plainTextInput.value;
    const key = encryptKeyInput.value.trim();
    if (!baseUrl || !text.trim() || !key) throw new Error('Fill text and key');

    setStatus('PROCESSING');
    showResult('PROCESSING');

    const response = await fetch(`${baseUrl}/api/v1/encrypt`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ text, key })
    });

    const data = await parseResponse(response);
    setCurrentJobId(data.jobId || '—');
    setStatus(data.status || 'RECEIVED');
    showResult(data.jobId || '');
    if (data.jobId) startPolling(baseUrl, data.jobId);
}

async function handleDecrypt() {
    const baseUrl = normalizeBaseUrl(baseUrlInput.value);
    const jobId = jobIdInput.value.trim();
    const key = decryptKeyInput.value.trim();
    if (!baseUrl || !jobId || !key) throw new Error('Fill Job ID and key');

    setStatus('PROCESSING');
    showResult('PROCESSING');

    const response = await fetch(`${baseUrl}/api/v1/decrypt/${jobId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify({ key })
    });

    const data = await parseResponse(response);
    setCurrentJobId(data.jobId || jobId);
    setStatus('COMPLETED');
    showResult(data.decryptedText || '');
}

async function handleStatus() {
    const baseUrl = normalizeBaseUrl(baseUrlInput.value);
    const jobId = jobIdInput.value.trim() || currentJobIdEl.textContent.trim();
    if (!baseUrl || !jobId || jobId === '—') throw new Error('Fill Job ID');
    setStatus('PROCESSING');
    const data = await requestStatus(baseUrl, jobId);
    renderStatus(data);
}

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    try {
        if (activeTab === 'encrypt') await handleEncrypt();
        else await handleDecrypt();
    } catch (error) {
        setStatus('FAILED');
        showResult(error.message || 'Request failed', true);
    }
});

statusBtn.addEventListener('click', async () => {
    try {
        await handleStatus();
    } catch (error) {
        setStatus('FAILED');
        showResult(error.message || 'Status failed', true);
    }
});

clearBtn.addEventListener('click', resetForm);
tabs.forEach((btn) => btn.addEventListener('click', () => switchTab(btn.dataset.tab)));

switchTab('encrypt');
setStatus('IDLE');
showResult('');