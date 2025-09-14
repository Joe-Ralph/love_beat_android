package com.euphoria.lovebeatandroid.data

val loveNotes = arrayOf(
    "Tap to Send Love 💖",
    "Send a Vibration 💞",
    "Transmit a Pulse 🫶",
    "Send a Heartbeat ❤️",
    "Deliver a Kiss 😚",
    "Send a Hug 🫂",
    "Transmit Affection 💌",
    "Send a Thought 💝",
    "Deliver a Smile 🥰",
    "Send a Signal 💘",
    "Ping Their Wrist 💑",
    "Send a Spark 💓",
    "Dispatch Love 💕",
    "Nudge Them Gently 👩‍❤️‍👨",
    "Send Warmth 🌷",
    "Deliver a Whisper 💋",
    "Send Butterflies 💐",
    "Issue a Caress 🤱",
    "Beam Love 💗",
    "Radiate Affection 💏"
)


fun getRandomLoveNote(): String {
    return loveNotes.random()
}