package com.euphoria.lovebeatandroid.data

val loveNotes = arrayOf(
    "Tap to send a love note",
    "Touch to whisper sweet nothings",
    "Press to ignite a spark of romance",
    "Tap to shower affection",
    "Touch to kindle your love's memory",
    "Press to send a virtual embrace",
    "Tap to share a heartfelt moment",
    "Touch to sprinkle love dust",
    "Press to serenade your sweetheart",
    "Tap to blow a digital kiss",
    "Touch to weave a love spell",
    "Press to send butterflies",
    "Tap to pen a love poem",
    "Touch to light up their world",
    "Press to send a gentle caress",
    "Tap to paint their day with love",
    "Touch to compose a love song",
    "Press to send a bouquet of emotions",
    "Tap to dance in their thoughts",
    "Touch to stir romantic memories",
    "Press to send a wave of affection",
    "Tap to write on their heart",
    "Touch to send a tender reminder",
    "Press to ignite passion's flame",
    "Tap to send a love-filled breeze",
    "Touch to weave a tapestry of love",
    "Press to send a romantic whisper",
    "Tap to plant a seed of love",
    "Touch to send a flutter of romance",
    "Press to wrap them in warmth"
)


fun getRandomLoveNote(): String {
    return loveNotes.random()
}