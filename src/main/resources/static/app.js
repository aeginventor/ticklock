document.addEventListener('DOMContentLoaded', () => {
    loadEvents();
});

// 이벤트 목록 로드
async function loadEvents() {
    const tbody = document.getElementById('eventsBody');
    tbody.innerHTML = '<tr><td colspan="5" class="empty">로딩 중...</td></tr>';

    try {
        const response = await fetch('/api/events');
        if (!response.ok) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty">이벤트를 불러올 수 없습니다</td></tr>';
            return;
        }

        const events = await response.json();

        if (events.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="empty">이벤트가 없습니다</td></tr>';
            return;
        }

        const rows = events.map(event => {
            const status = event.remainingSeats === 0 
                ? '<span class="status-sold-out">매진</span>' 
                : '<span class="status-available">판매중</span>';
            return `
                <tr>
                    <td>${event.id}</td>
                    <td>${event.name}</td>
                    <td>${event.totalSeats}</td>
                    <td>${event.remainingSeats}</td>
                    <td>${status}</td>
                </tr>
            `;
        });

        tbody.innerHTML = rows.join('');
    } catch (e) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty">오류: ' + e.message + '</td></tr>';
    }
}

async function createEvent() {
    const name = document.getElementById('eventName').value.trim();
    const totalSeats = parseInt(document.getElementById('totalSeats').value);

    if (!name) {
        alert('이벤트 이름을 입력하세요');
        return;
    }

    if (isNaN(totalSeats) || totalSeats <= 0) {
        alert('올바른 좌석 수를 입력하세요');
        return;
    }

    try {
        const response = await fetch('/api/events', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, totalSeats })
        });

        if (response.ok) {
            document.getElementById('eventName').value = '';
            document.getElementById('totalSeats').value = '100';
            await loadEvents();
            alert('이벤트 생성 완료!');
        } else {
            const errorText = await response.text();
            alert('이벤트 생성 실패: ' + errorText);
        }
    } catch (e) {
        alert('오류: ' + e.message);
    }
}

// 동시성 테스트
async function runConcurrencyTest() {
    const eventId = document.getElementById('testEventId').value;
    const lockType = document.getElementById('lockType').value;
    const count = parseInt(document.getElementById('concurrentRequests').value);

    if (!eventId) {
        alert('이벤트 ID를 입력하세요');
        return;
    }

    const btn = document.getElementById('testBtn');
    const resultDiv = document.getElementById('testResult');

    btn.disabled = true;
    btn.textContent = '테스트 중...';
    resultDiv.classList.remove('hidden');

    document.getElementById('successCount').textContent = '0';
    document.getElementById('failCount').textContent = '0';
    document.getElementById('elapsed').textContent = '측정 중...';
    document.getElementById('finalSeats').textContent = '-';

    const startTime = Date.now();
    let successCount = 0;
    let failCount = 0;

    // 동시에 모든 요청 실행
    const promises = [];
    for (let i = 0; i < count; i++) {
        promises.push(
            fetch(`/api/events/${eventId}/purchase/${lockType}`, { method: 'POST' })
                .then(res => res.json())
                .then(result => {
                    if (result.success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                    // 실시간 업데이트
                    document.getElementById('successCount').textContent = successCount;
                    document.getElementById('failCount').textContent = failCount;
                })
                .catch(() => {
                    failCount++;
                    document.getElementById('failCount').textContent = failCount;
                })
        );
    }

    await Promise.all(promises);

    const elapsed = Date.now() - startTime;
    document.getElementById('elapsed').textContent = elapsed + 'ms';

    // 최종 잔여석 조회
    try {
        const response = await fetch(`/api/events/${eventId}`);
        if (response.ok) {
            const event = await response.json();
            document.getElementById('finalSeats').textContent = event.remainingSeats;

            // 목록 새로고침
            loadEvents();
        }
    } catch (e) {
        // 무시
    }

    btn.disabled = false;
    btn.textContent = '테스트 실행';
}