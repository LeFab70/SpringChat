let stompClient = null;
let username = "";

const usernamePage = document.querySelector("#username-page");
const chatPage = document.querySelector("#chat-page");

const usernameForm = document.querySelector("#usernameForm");
const messageForm = document.querySelector("#messageForm");

const usernameInput = document.querySelector("#username");
const messageInput = document.querySelector("#message");

const messages = document.querySelector("#messages");

const status = document.querySelector("#connection-status");

usernameForm.addEventListener("submit", connect);
messageForm.addEventListener("submit", sendMessage);

function connect(event){

    event.preventDefault();

    username = usernameInput.value.trim();

    if(username === ""){
        return;
    }

    status.innerHTML = "⏳ Connecting to WebSocket...";

    const socket = new SockJS("/ws");

    stompClient = Stomp.over(socket);

    stompClient.connect({}, onConnected, onError);

}

function onConnected() {

    usernamePage.classList.add("hidden");

    chatPage.classList.remove("hidden");

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

function onError(){

    status.innerHTML="❌ Unable to connect.";

}

function sendMessage(event){

    event.preventDefault();

    const content = messageInput.value.trim();

    if(content==="" || !stompClient){
        return;
    }

    stompClient.send(

        "/app/chat.sendMessage",

        {},

        JSON.stringify({

            sender:username,

            content:content,

            type:"CHAT"

        })

    );

    messageInput.value="";

}

function onMessageReceived(payload) {

    const message = JSON.parse(payload.body);

    const wrapper = document.createElement("div");

    wrapper.classList.add("message-wrapper");

    const div = document.createElement("div");

    div.classList.add("message");

    if (message.type === "JOIN") {

        wrapper.classList.add("system");

        div.innerHTML = `<em>${message.sender} joined the chat.</em>`;

    } else {

        if (message.sender === username) {
            wrapper.classList.add("own-message");
        } else {
            wrapper.classList.add("other-message");
        }

        div.innerHTML =
            `<strong>${message.sender}</strong><br>${message.content}`;
    }

    wrapper.appendChild(div);

    messages.appendChild(wrapper);

    messages.scrollTop = messages.scrollHeight;
}