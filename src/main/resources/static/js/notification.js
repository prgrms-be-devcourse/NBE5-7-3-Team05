// notification.js
import Toastify from 'https://cdn.jsdelivr.net/npm/toastify-js/src/toastify-es.js';

export async function setupNotification() {
    try {
        const res = await fetch('/users/me');
        if (!res.ok) throw new Error("인증 필요");

        const response = await res.json();
        const userId = response.data;

        const eventSource = new EventSource(`/api/notifications/subscribe/${userId}`);
        console.log(`✅ SSE 연결 시도 중: userId = ${userId}`);

        eventSource.addEventListener("connect", (event) => {
            console.log("✅ SSE 연결 성공:", event.data);
        });

        eventSource.addEventListener("notification", (event) => {
            const data = JSON.parse(event.data);
            console.log("🔔 알림 도착:", data);
            Toastify({
                text: `📬 ${data.content}`,
                duration: 4000,
                gravity: "top",
                position: "right",
                backgroundColor: "linear-gradient(to right, #6366F1, #3B82F6)", // gradient
                style: {
                    fontSize: "15px",
                    borderRadius: "8px",
                    boxShadow: "0 4px 6px rgba(0,0,0,0.1)"
                },
                close: true
            }).showToast();
        });

        eventSource.onerror = (e) => {
            console.error("❌ SSE 연결 오류:", e);
            eventSource.close();
        };
    } catch (e) {
        console.error("알림 설정 실패:", e);
    }
}
