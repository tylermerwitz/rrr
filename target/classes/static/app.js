const API_BASE = '/api/game';

const state = {
    playerId: null
};

const screens = ['login', 'hub', 'mirror', 'equipment', 'shop', 'floor'];

function showScreen(name) {
    for (const s of screens) {
        document.getElementById('screen-' + s).classList.toggle('hidden', s !== name);
    }
}

function showError(message) {
    const banner = document.getElementById('error-banner');
    banner.textContent = message;
    banner.classList.remove('hidden');
}

function clearError() {
    document.getElementById('error-banner').classList.add('hidden');
}

// The narrator's turns hit the LLM, which can take several seconds (especially when a
// single upstream provider is rate-limiting and we retry). Show an overlay so the player
// can tell the backend is actively working rather than stalled.
function showLlmLoading() {
    document.getElementById('llm-loading').classList.remove('hidden');
}

function hideLlmLoading() {
    document.getElementById('llm-loading').classList.add('hidden');
}

async function api(path, method = 'GET', body) {
    clearError();
    const res = await fetch(API_BASE + path, {
        method,
        headers: body ? { 'Content-Type': 'application/json' } : undefined,
        body: body ? JSON.stringify(body) : undefined
    });

    const text = await res.text();
    const data = text ? JSON.parse(text) : null;

    if (!res.ok) {
        const message = (data && data.error) ? data.error : `Request failed (${res.status})`;
        showError(message);
        throw new Error(message);
    }

    return data;
}

function updateTopbar(meta, run) {
    document.getElementById('topbar').classList.remove('hidden');
    document.getElementById('stat-name').textContent = meta.name;
    document.getElementById('stat-deaths').textContent = meta.totalDeaths;
    document.getElementById('stat-regression').textContent = meta.regressionPoints;
    document.getElementById('stat-arousal').textContent = meta.arousalPoints;
    document.getElementById('stat-coins').textContent = meta.coins;
    document.getElementById('stat-floor').textContent = run.currentFloor;
    document.getElementById('stat-location').textContent = run.location;
    document.getElementById('stat-humiliation').textContent = run.humiliation;
    document.getElementById('stat-bladder').textContent = run.bladderPercent.toFixed(1);
    document.getElementById('stat-bowel').textContent = run.bowelPercent.toFixed(1);

    const wetEl = document.getElementById('stat-wetness');
    const messEl = document.getElementById('stat-mess');
    wetEl.textContent = wetnessLabel(run.diaperWetness);
    messEl.textContent = messLabel(run.diaperMessiness);
    wetEl.classList.toggle('stat-soiled', run.diaperWetness > 0);
    messEl.classList.toggle('stat-soiled', run.diaperMessiness > 0);
}

// Describe how saturated the worn diaper is. Wetness accrues ~34 per wetting accident.
function wetnessLabel(wetness) {
    if (!wetness || wetness <= 0) return 'Dry';
    if (wetness < 68) return 'Damp';
    if (wetness < 136) return 'Wet';
    if (wetness < 204) return 'Soaked';
    return 'Saturated';
}

// Describe how soiled the worn diaper is. Messiness accrues ~50 per messing accident.
function messLabel(messiness) {
    if (!messiness || messiness <= 0) return 'Clean';
    if (messiness < 100) return 'Messy';
    if (messiness < 200) return 'Soiled';
    return 'Filthy';
}

function renderState(data) {
    updateTopbar(data.meta, data.run);

    if (data.options) {
        showScreen('hub');
    } else {
        renderFloor(data);
        showScreen('floor');
    }
}

async function refreshState() {
    const data = await api(`/state/${state.playerId}`);
    renderState(data);
}

/* ---------- Login ---------- */

document.getElementById('btn-start').addEventListener('click', async () => {
    const id = Number(document.getElementById('input-player-id').value);
    if (!id || id < 1) {
        showError('Enter a valid player ID');
        return;
    }
    try {
        const data = await api('/start', 'POST', { playerId: id });
        state.playerId = id;
        localStorage.setItem('rrr_player_id', String(id));
        renderState(data);
    } catch (e) {
        // error already shown
    }
});

document.getElementById('btn-switch-player').addEventListener('click', () => {
    state.playerId = null;
    document.getElementById('topbar').classList.add('hidden');
    showScreen('login');
});

/* ---------- Hub ---------- */

document.getElementById('btn-hub-mirror').addEventListener('click', loadMirror);
document.getElementById('btn-hub-equipment').addEventListener('click', loadEquipment);
document.getElementById('btn-hub-shop').addEventListener('click', loadShop);
document.getElementById('btn-hub-enter').addEventListener('click', async () => {
    showLlmLoading();
    try {
        const data = await api('/hub/enter', 'POST', { playerId: state.playerId });
        renderState(data);
    } catch (e) {
        // error already shown
    } finally {
        hideLlmLoading();
    }
});

document.querySelectorAll('.back-btn').forEach(btn => {
    btn.addEventListener('click', async () => {
        try {
            await refreshState();
        } catch (e) {
            // error already shown
        }
    });
});

/* ---------- Mirror ---------- */

async function loadMirror() {
    try {
        const data = await api(`/hub/mirror/${state.playerId}`);
        updateTopbar(data.meta, data.run);

        const items = data.equippedItems.length
            ? data.equippedItems.map(e => `${e.name} (${e.type})`).join(', ')
            : 'nothing';

        document.getElementById('mirror-content').innerHTML = `
            <p>${data.meta.name} stares back, ${data.meta.totalDeaths} death(s) deep into the realm.</p>
            <ul class="item-list">
                <li><span>Wearing</span><span>${items}</span></li>
                <li><span>Flat Defense</span><span>${data.totalFlatDefense}</span></li>
                <li><span>Speed Modifier</span><span>${data.totalSpeedModifier}</span></li>
                <li><span>Bladder Modifier</span><span>${data.totalBladderModifier}</span></li>
                <li><span>Bowel Modifier</span><span>${data.totalBowelModifier}</span></li>
            </ul>
        `;
        showScreen('mirror');
    } catch (e) {
        // error already shown
    }
}

/* ---------- Equipment ---------- */

async function loadEquipment() {
    try {
        const data = await api(`/hub/equipment/${state.playerId}`);
        renderEquipmentScreen(data);
        showScreen('equipment');
    } catch (e) {
        // error already shown
    }
}

function renderEquipmentScreen(data) {
    const equippedIds = new Set(data.equippedEquipment.map(e => e.id));
    const unlockedNotEquipped = data.unlockedEquipment.filter(e => !equippedIds.has(e.id));

    const equippedList = document.getElementById('equipped-list');
    equippedList.innerHTML = data.equippedEquipment.length
        ? data.equippedEquipment.map(e => equipmentRow(e, 'Unequip')).join('')
        : '<li><span class="muted">nothing equipped</span></li>';

    const unlockedList = document.getElementById('unlocked-list');
    unlockedList.innerHTML = unlockedNotEquipped.length
        ? unlockedNotEquipped.map(e => equipmentRow(e, 'Equip')).join('')
        : '<li><span class="muted">nothing else unlocked</span></li>';

    attachItemActions();
}

function equipmentRow(equipment, actionLabel) {
    return `
        <li data-equipment-id="${equipment.id}" data-action="${actionLabel}">
            <span>${equipment.name} <span class="item-meta">(${equipment.type})</span></span>
            <button class="row-action-btn">${actionLabel}</button>
        </li>
    `;
}

function attachItemActions() {
    document.querySelectorAll('#equipped-list .row-action-btn, #unlocked-list .row-action-btn').forEach(btn => {
        btn.addEventListener('click', async (ev) => {
            const li = ev.target.closest('li');
            const equipmentId = Number(li.dataset.equipmentId);
            const action = li.dataset.action;
            try {
                const data = action === 'Equip'
                    ? await api('/hub/equipment/equip', 'POST', { playerId: state.playerId, equipmentId })
                    : await api('/hub/equipment/unequip', 'POST', { playerId: state.playerId, equipmentId });
                renderEquipmentScreen(data);
            } catch (e) {
                // error already shown
            }
        });
    });
}

/* ---------- Shop ---------- */

async function loadShop() {
    try {
        const data = await api(`/hub/shop/${state.playerId}`);
        renderShopScreen(data);
        showScreen('shop');
    } catch (e) {
        // error already shown
    }
}

function renderShopScreen(data) {
    document.getElementById('shop-coins').textContent = data.coins;

    const list = document.getElementById('shop-list');
    list.innerHTML = data.listings.length
        ? data.listings.map(listing => {
            const canBuy = !listing.owned && listing.eligible && data.coins >= listing.unlockCost;
            const status = listing.owned ? 'owned' : (!listing.eligible ? 'tier locked' : `${listing.unlockCost} coins`);
            return `
                <li data-equipment-id="${listing.equipmentId}">
                    <span>${listing.name} <span class="item-meta">(${listing.type}${listing.legendary ? ', legendary' : ''}) — ${status}</span></span>
                    <button class="buy-btn" ${canBuy ? '' : 'disabled'}>${listing.owned ? 'Owned' : 'Buy'}</button>
                </li>
            `;
        }).join('')
        : '<li><span class="muted">the shop is empty</span></li>';

    list.querySelectorAll('.buy-btn').forEach(btn => {
        btn.addEventListener('click', async (ev) => {
            const equipmentId = Number(ev.target.closest('li').dataset.equipmentId);
            try {
                const updated = await api('/hub/shop/purchase', 'POST', { playerId: state.playerId, equipmentId });
                renderShopScreen(updated);
            } catch (e) {
                // error already shown
            }
        });
    });
}

/* ---------- Floor ---------- */

// A floor is one continuous encounter: the player only ever reads the narration and
// clicks one of the three choices, which submits the next action.
function renderFloor(data) {
    document.getElementById('floor-death-banner').classList.add('hidden');
    document.getElementById('floor-live').classList.remove('hidden');
    document.getElementById('floor-number').textContent = data.run.currentFloor;

    renderOutcome(data.outcome);
    renderNarration(data.narration, data.phase);
}

function renderOutcome(outcome) {
    const panel = document.getElementById('outcome-panel');
    if (!outcome) {
        panel.classList.add('hidden');
        return;
    }
    panel.classList.remove('hidden');
    const itemText = outcome.itemReward ? ` · found ${outcome.itemReward}!` : '';
    const changeText = outcome.diaperChanged ? ' · you were changed into a fresh diaper.' : '';
    document.getElementById('outcome-summary').textContent =
        `${outcome.success ? 'Success' : 'Failure'} · Humiliation +${outcome.humiliationAdded} · ` +
        `Regression +${outcome.regressionAdded} · Arousal +${outcome.arousalAdded}` +
        accidentLabel(outcome) + itemText + changeText;
}

// Spell out exactly what kind of accident the turn produced, instead of a generic "accident".
function accidentLabel(outcome) {
    if (outcome.wetted && outcome.messed) return ' · you wet AND messed your diaper!';
    if (outcome.wetted) return ' · you wet your diaper!';
    if (outcome.messed) return ' · you messed your diaper!';
    return '';
}

function renderNarration(narration, phase) {
    const text = document.getElementById('narration-text');
    const choicesEl = document.getElementById('narration-choices');

    if (!narration) {
        text.textContent = 'The encounter is beginning...';
        text.classList.add('muted');
        choicesEl.innerHTML = '';
        return;
    }

    text.textContent = narration.narration;
    text.classList.remove('muted');

    // A resolution beat (how the last action played out) carries no choices — give the player a
    // Continue button to advance to the next encounter instead.
    if (phase === 'RESOLUTION') {
        choicesEl.innerHTML = '<button class="choice-btn" id="btn-continue-encounter">Continue</button>';
        document.getElementById('btn-continue-encounter')
            .addEventListener('click', continueEncounter);
        return;
    }

    const choices = narration.choices || [];
    choicesEl.innerHTML = choices
        .map((c, i) => {
            const tag = c.category ? `${c.riskLevel} · ${c.category}` : c.riskLevel;
            return `<button class="choice-btn" data-idx="${i}">${c.text} <span class="item-meta">(${tag})</span></button>`;
        })
        .join('');

    choicesEl.querySelectorAll('.choice-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const choice = choices[Number(btn.dataset.idx)];
            takeAction(choice.eventId, choice.actionType);
        });
    });
}

async function takeAction(eventId, actionType) {
    showLlmLoading();
    try {
        const data = await api('/action', 'POST', {
            playerId: state.playerId,
            eventId,
            actionType
        });

        updateTopbar(data.meta, data.run);

        if (data.phase === 'DEAD') {
            // The turn broke the player. Show how they came undone, then Continue wakes them in the hub.
            const deathText = (data.narration && data.narration.narration)
                ? data.narration.narration
                : 'You wake up back in the Hub.';
            document.getElementById('death-narration-text').textContent = deathText;
            document.getElementById('floor-live').classList.add('hidden');
            document.getElementById('floor-death-banner').classList.remove('hidden');
            showScreen('floor');
            return;
        }

        renderFloor(data);
        showScreen('floor');
    } catch (e) {
        // error already shown
    } finally {
        hideLlmLoading();
    }
}

// Advance from a resolution screen to the next encounter's scene + choices.
async function continueEncounter() {
    showLlmLoading();
    try {
        const data = await api('/continue', 'POST', { playerId: state.playerId });
        updateTopbar(data.meta, data.run);
        renderFloor(data);
        showScreen('floor');
    } catch (e) {
        // error already shown
    } finally {
        hideLlmLoading();
    }
}

document.getElementById('btn-continue-after-death').addEventListener('click', async () => {
    try {
        await refreshState();
    } catch (e) {
        // error already shown
    }
});

/* ---------- Init ---------- */

(function init() {
    const saved = localStorage.getItem('rrr_player_id');
    if (saved) {
        document.getElementById('input-player-id').value = saved;
    }
    showScreen('login');
})();
