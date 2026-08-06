let stompClient = null;
let username = "";

const joinModal = document.querySelector("#joinModal");
const chatPage = document.querySelector("#chat-page");

const usernameForm = document.querySelector("#usernameForm");
const messageForm = document.querySelector("#messageForm");

const usernameInput = document.querySelector("#username");
const messageInput = document.querySelector("#message");

const messages = document.querySelector("#messages");

const status = document.querySelector("#connection-status");
const spinner = document.querySelector("#spinner");

const themeBtn = document.querySelector("#themeBtn");
const disconnectBtn = document.querySelector("#disconnectBtn");

const myAvatar = document.querySelector("#myAvatar");
const currentUsername = document.querySelector("#currentUsername");

usernameForm.addEventListener("submit", connect);
messageForm.addEventListener("submit", sendMessage);
disconnectBtn.addEventListener("click", disconnect);
themeBtn.addEventListener("click", toggleTheme);

/* ===========================
   THEME
=========================== */

function applyTheme(theme) {
    if (theme === "dark") {
        document.body.classList.add("dark");
        themeBtn.textContent = "☀️";
    } else {
        document.body.classList.remove("dark");
        themeBtn.textContent = "🌙";
    }
}

function toggleTheme() {
    const next = document.body.classList.contains("dark") ? "light" : "dark";
    localStorage.setItem("chat-theme", next);
    applyTheme(next);
}

applyTheme(localStorage.getItem("chat-theme") || "light");

/* ===========================
   CONNECTION
=========================== */

function connect(event) {

    event.preventDefault();

    username = usernameInput.value.trim();

    if (username === "") {
        return;
    }

    spinner.classList.remove("hidden");
    status.textContent = "Connecting to WebSocket...";

    const socket = new SockJS("/ws");

    stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, onConnected, onError);

}

function onConnected() {

    spinner.classList.add("hidden");
    status.textContent = "";

    joinModal.classList.add("hidden");
    chatPage.classList.remove("hidden");

    currentUsername.textContent = username;
    myAvatar.textContent = username.charAt(0).toUpperCase();

    stompClient.subscribe("/topic/public", onMessageReceived);

    stompClient.send(
        "/app/chat.addUser",
        {},
        JSON.stringify({
            sender: username,
            type: "JOIN"
        })
    );
}

function onError() {

    spinner.classList.add("hidden");
    status.textContent = "❌ Unable to connect. Please try again.";

}

function disconnect() {

    if (stompClient !== null) {
        stompClient.disconnect();
    }

    stompClient = null;
    username = "";

    messages.innerHTML = "";
    usernameInput.value = "";
    status.textContent = "";
    spinner.classList.add("hidden");

    chatPage.classList.add("hidden");
    joinModal.classList.remove("hidden");
}

/* ===========================
   MESSAGES
=========================== */

function sendMessage(event) {

    event.preventDefault();

    const content = messageInput.value.trim();

    if (content === "" || !stompClient) {
        return;
    }

    stompClient.send(
        "/app/chat.sendMessage",
        {},
        JSON.stringify({
            sender: username,
            content: content,
            type: "CHAT"
        })
    );

    messageInput.value = "";

}

function formatTime() {
    return new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
}

function onMessageReceived(payload) {

    const message = JSON.parse(payload.body);

    const wrapper = document.createElement("div");
    wrapper.classList.add("message-wrapper");

    if (message.type === "JOIN" || message.type === "LEAVE") {

        wrapper.classList.add("system");

        const bubble = document.createElement("div");
        bubble.classList.add("message");
        bubble.textContent = message.type === "JOIN"
            ? `${message.sender} joined the chat.`
            : `${message.sender} left the chat.`;

        wrapper.appendChild(bubble);

    } else {

        const isOwn = message.sender === username;
        wrapper.classList.add(isOwn ? "own-message" : "other-message");

        if (!isOwn) {
            const avatar = document.createElement("div");
            avatar.classList.add("msg-avatar");
            avatar.textContent = message.sender.charAt(0).toUpperCase();
            wrapper.appendChild(avatar);
        }

        const bubble = document.createElement("div");
        bubble.classList.add("message");

        if (!isOwn) {
            const sender = document.createElement("span");
            sender.classList.add("sender");
            sender.textContent = message.sender;
            bubble.appendChild(sender);
        }

        const content = document.createElement("span");
        content.classList.add("content");
        content.textContent = message.content;
        bubble.appendChild(content);

        const time = document.createElement("span");
        time.classList.add("time");
        time.textContent = formatTime();
        bubble.appendChild(time);

        wrapper.appendChild(bubble);
    }

    messages.appendChild(wrapper);

    messages.scrollTop = messages.scrollHeight;
}
