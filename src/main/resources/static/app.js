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