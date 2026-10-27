package com.example.data

import com.example.model.AccentType
import com.example.model.CareActionLog
import com.example.model.ChatMessage
import com.example.model.DailyRhythmBlock
import com.example.model.LifeQuest
import com.example.model.MemoryItem
import com.example.model.PresenceMode
import com.example.model.TamagotchiMood
import com.example.model.TamagotchiState
import com.example.model.MetaAiConfig
import com.example.model.MuseLifeAdvice
import com.example.model.MusePillarAdvice
import com.example.model.LifeSyncIntegrations
import com.example.model.ThoughtStep
import com.example.model.ThoughtStream
import com.example.model.VitalityCheckin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SerenityRepository {

    private val _tamagotchiState = MutableStateFlow(
        TamagotchiState(
            name = "Lumi",
            evolution = "Luminous Luminary (Stage 3)",
            level = 4,
            xp = 740,
            xpForNextLevel = 1000,
            hydration = 0.85f,
            focus = 0.90f,
            innerPeace = 0.82f,
            physicalEnergy = 0.78f,
            connection = 0.80f,
            mood = TamagotchiMood.RADIANT,
            speech = "I feel so radiant today! You've nurtured our mind and body beautifully.",
            equippedAccessory = "Celestial Halo",
            isSleeping = false,
            totalPets = 42
        )
    )
    val tamagotchiState: StateFlow<TamagotchiState> = _tamagotchiState.asStateFlow()

    private val _careLogs = MutableStateFlow<List<CareActionLog>>(initialCareLogs())
    val careLogs: StateFlow<List<CareActionLog>> = _careLogs.asStateFlow()

    private val _lifeQuests = MutableStateFlow<List<LifeQuest>>(initialLifeQuests())
    val lifeQuests: StateFlow<List<LifeQuest>> = _lifeQuests.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(initialMessages())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryItem>>(initialMemories())
    val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    private val _selectedMode = MutableStateFlow(PresenceMode.MINDFUL_GUIDE)
    val selectedMode: StateFlow<PresenceMode> = _selectedMode.asStateFlow()

    private val _auraIntensity = MutableStateFlow(1.0f)
    val auraIntensity: StateFlow<Float> = _auraIntensity.asStateFlow()

    private val _vitalityCheckin = MutableStateFlow(
        VitalityCheckin(
            clarityScore = 91,
            emotionalEquilibrium = 88,
            restorativePeace = 84,
            moodWord = "Radiant Equilibrium"
        )
    )
    val vitalityCheckin: StateFlow<VitalityCheckin> = _vitalityCheckin.asStateFlow()

    private val museService = MuseService()

    private val _metaAiConfig = MutableStateFlow(
        MetaAiConfig(
            token = "",
            isConnected = false,
            lastSyncedAt = null
        )
    )
    val metaAiConfig: StateFlow<MetaAiConfig> = _metaAiConfig.asStateFlow()

    private val _latestMuseAdvice = MutableStateFlow<MuseLifeAdvice?>(null)
    val latestMuseAdvice: StateFlow<MuseLifeAdvice?> = _latestMuseAdvice.asStateFlow()

    private val _integrations = MutableStateFlow(LifeSyncIntegrations())
    val integrations: StateFlow<LifeSyncIntegrations> = _integrations.asStateFlow()

    fun toggleGoogleHealth() {
        val curr = _integrations.value
        val newState = !curr.googleHealthConnected
        _integrations.value = curr.copy(googleHealthConnected = newState)
        if (newState) {
            val currState = _tamagotchiState.value
            _tamagotchiState.value = currState.copy(
                physicalEnergy = 0.92f,
                hydration = 0.88f,
                speech = "Google Health connected! 7,840 steps & 7.4 hrs sleep synced to my vitality! 🌿"
            )
        }
    }

    fun toggleFacebook() {
        val curr = _integrations.value
        val newState = !curr.facebookConnected
        _integrations.value = curr.copy(
            facebookConnected = newState,
            facebookStats = if (newState) "Synced: 4 friend interactions & community circles" else "Connect to sync close friends & social connection vitals"
        )
        if (newState) {
            val currState = _tamagotchiState.value
            _tamagotchiState.value = currState.copy(
                connection = 0.90f,
                speech = "Facebook connected! Close friend connections blooming in my heart aura! 💖"
            )
        }
    }

    fun toggleFinanceApp() {
        val curr = _integrations.value
        val newState = !curr.financeAppConnected
        _integrations.value = curr.copy(financeAppConnected = newState)
        if (newState) {
            val currState = _tamagotchiState.value
            _tamagotchiState.value = currState.copy(
                innerPeace = (currState.innerPeace + 0.10f).coerceAtMost(1.0f),
                speech = "Finance app connected! Mindful budget is on track—financial serenity established. 💎"
            )
        }
    }

    fun toggleTasksApp() {
        val curr = _integrations.value
        val newState = !curr.tasksAppConnected
        _integrations.value = curr.copy(tasksAppConnected = newState)
        if (newState) {
            val currState = _tamagotchiState.value
            _tamagotchiState.value = currState.copy(
                focus = (currState.focus + 0.12f).coerceAtMost(1.0f),
                speech = "Tasks app connected! 4 completed tasks powered my focus flow sprint! 🎯"
            )
        }
    }

    fun updateMetaAiToken(newToken: String) {
        val trimmed = newToken.trim()
        _metaAiConfig.value = _metaAiConfig.value.copy(
            token = trimmed,
            isConnected = trimmed.isNotEmpty()
        )
    }

    suspend fun sendAllInfoToMuse(): MuseLifeAdvice {
        val advice = museService.sendLifeTelemetryToMuse(
            metaAiToken = _metaAiConfig.value.token,
            tamagotchiState = _tamagotchiState.value,
            quests = _lifeQuests.value,
            recentLogs = _careLogs.value,
            valuesAndMemories = _memories.value,
            integrations = _integrations.value
        )
        _latestMuseAdvice.value = advice
        _metaAiConfig.value = _metaAiConfig.value.copy(
            isConnected = true,
            lastSyncedAt = advice.timestamp
        )

        // Automatically share Muse's life prescription into Dialogue chat
        val museMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = false,
            content = "✨ **Muse Superintelligence Life Prescription:**\n\n${advice.headline}\n\n${advice.overallDiagnosis}\n\n💡 **Prescription for ${_tamagotchiState.value.name}:** ${advice.tamagotchiPrescription}",
            timestamp = advice.timestamp,
            thoughtSteps = advice.pillars.map {
                ThoughtStep("${it.pillar} (${it.score}%)", it.recommendation)
            },
            suggestedPrompts = advice.pillars.map { it.suggestedAction }.take(3),
            category = "Muse Life Intelligence"
        )
        _messages.value = _messages.value + museMessage

        return advice
    }

    fun nurtureWater() {
        val curr = _tamagotchiState.value
        val newHydration = (curr.hydration + 0.18f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                hydration = newHydration,
                speech = "Ahhh, crystal water! My luminous aura is shimmering with vitality!"
            ),
            actionName = "Glass of Pure Water",
            detail = "+18% Hydration",
            xpGained = 20
        )
    }

    fun nurtureBreathe() {
        val curr = _tamagotchiState.value
        val newPeace = (curr.innerPeace + 0.20f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                innerPeace = newPeace,
                speech = "Inhaling peace with you... our thoughts feel spacious and unhurried."
            ),
            actionName = "3-Minute Breath Reset",
            detail = "+20% Inner Peace",
            xpGained = 25
        )
    }

    fun nurtureWalk() {
        val curr = _tamagotchiState.value
        val newEnergy = (curr.physicalEnergy + 0.20f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                physicalEnergy = newEnergy,
                speech = "Moving our body unclogs stagnant energy. I feel light on my feet!"
            ),
            actionName = "Movement & Sunlight Walk",
            detail = "+20% Physical Energy",
            xpGained = 30
        )
    }

    fun nurtureFocus() {
        val curr = _tamagotchiState.value
        val newFocus = (curr.focus + 0.16f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                focus = newFocus,
                speech = "Deep focus locked in! One mindful task at a time."
            ),
            actionName = "Completed Focus Sprint",
            detail = "+16% Mind Flow",
            xpGained = 25
        )
    }

    fun nurtureConnect() {
        val curr = _tamagotchiState.value
        val newConn = (curr.connection + 0.20f).coerceAtMost(1.0f)
        applyCareAction(
            updatedState = curr.copy(
                connection = newConn,
                speech = "Connection is warmth! Connecting with loved ones anchors our soul."
            ),
            actionName = "Heartfelt Connection",
            detail = "+20% Connection",
            xpGained = 25
        )
    }

    fun petTamagotchi() {
        val curr = _tamagotchiState.value
        val newPets = curr.totalPets + 1
        val newPeace = (curr.innerPeace + 0.04f).coerceAtMost(1.0f)
        val petPhrases = listOf(
            "*chirp!* Your touch fills my aura with warm sparkles! ✨",
            "I love being your life companion. We're thriving together! 💖",
            "*purr* Thank you for looking after yourself—and me! 🌸",
            "A gentle caress! My core feels balanced and happy. 🌿"
        )
        val randomSpeech = petPhrases[newPets % petPhrases.size]
        applyCareAction(
            updatedState = curr.copy(
                totalPets = newPets,
                innerPeace = newPeace,
                speech = randomSpeech
            ),
            actionName = "Affection & Petting",
            detail = "Gentle loving attention",
            xpGained = 10
        )
    }

    fun toggleSleepMode() {
        val curr = _tamagotchiState.value
        val newSleep = !curr.isSleeping
        val speech = if (newSleep) {
            "Resting our eyes in gentle twilight... Sweet dreams, Maya. 🌙"
        } else {
            "Awake and refreshed! Ready to craft a radiant day with you! ☀️"
        }
        val mood = if (newSleep) TamagotchiMood.SLEEPY else TamagotchiMood.RADIANT
        _tamagotchiState.value = curr.copy(
            isSleeping = newSleep,
            speech = speech,
            mood = mood
        )
    }

    fun equipAccessory(accessory: String) {
        _tamagotchiState.value = _tamagotchiState.value.copy(
            equippedAccessory = accessory,
            speech = "Wearing my $accessory! Doesn't it look radiant? ✨"
        )
    }

    fun toggleQuest(questId: String) {
        var completedQuest: LifeQuest? = null
        _lifeQuests.value = _lifeQuests.value.map { quest ->
            if (quest.id == questId) {
                val newState = !quest.isCompleted
                if (newState) completedQuest = quest
                quest.copy(isCompleted = newState)
            } else quest
        }

        completedQuest?.let { quest ->
            val curr = _tamagotchiState.value
            applyCareAction(
                updatedState = curr.copy(
                    speech = "Quest accomplished: ${quest.title}! Our life score just blossomed! 🌸"
                ),
                actionName = "Completed Quest: ${quest.title}",
                detail = quest.impact,
                xpGained = 40
            )
        }
    }

    private fun applyCareAction(
        updatedState: TamagotchiState,
        actionName: String,
        detail: String,
        xpGained: Int
    ) {
        var newXp = updatedState.xp + xpGained
        var newLevel = updatedState.level
        var newEvolution = updatedState.evolution
        val xpThreshold = updatedState.xpForNextLevel

        if (newXp >= xpThreshold) {
            newLevel += 1
            newXp -= xpThreshold
            newEvolution = when (newLevel) {
                1 -> "Sprout Spirit (Stage 1)"
                2 -> "Radiant Wisp (Stage 2)"
                3 -> "Luminous Bloom (Stage 3)"
                4 -> "Harmonic Luminary (Stage 4)"
                else -> "Celestial Ascendant (Stage 5)"
            }
        }

        val reevaluatedMood = evaluateMood(updatedState)

        _tamagotchiState.value = updatedState.copy(
            level = newLevel,
            xp = newXp,
            evolution = newEvolution,
            mood = reevaluatedMood
        )

        val newLog = CareActionLog(
            id = UUID.randomUUID().toString(),
            actionName = actionName,
            detail = detail,
            timestamp = currentTimeFormatted(),
            xpGained = xpGained
        )
        _careLogs.value = listOf(newLog) + _careLogs.value.take(15)
    }

    private fun evaluateMood(state: TamagotchiState): TamagotchiMood {
        if (state.isSleeping) return TamagotchiMood.SLEEPY
        if (state.hydration < 0.40f) return TamagotchiMood.THIRSTY
        if (state.innerPeace < 0.45f) return TamagotchiMood.SLEEPY
        if (state.focus < 0.45f) return TamagotchiMood.RESTLESS
        val avg = (state.hydration + state.focus + state.innerPeace + state.physicalEnergy + state.connection) / 5f
        return when {
            avg >= 0.80f -> TamagotchiMood.RADIANT
            else -> TamagotchiMood.BALANCED
        }
    }

    fun setPresenceMode(mode: PresenceMode) {
        _selectedMode.value = mode
    }

    fun setAuraIntensity(intensity: Float) {
        _auraIntensity.value = intensity
    }

    fun logVitalityCheckin(clarity: Int, equilibrium: Int, peace: Int, mood: String) {
        _vitalityCheckin.value = VitalityCheckin(clarity, equilibrium, peace, mood)
    }

    fun getThoughtStreams(): List<ThoughtStream> = listOf(
        ThoughtStream(
            id = "1",
            title = "Morning Cognitive Attunement",
            tag = "Clarity & Rhythm",
            summary = "Your life tamagotchi is glowing at ${tamagotchiState.value.overallLifeScore}% vitality. A focused sprint will trigger its next stage evolution.",
            prompt = "How can I nurture Lumi's focus today?",
            accentType = AccentType.CORAL
        ),
        ThoughtStream(
            id = "2",
            title = "Equilibrium & Micro-Pauses",
            tag = "Vitality",
            summary = "Lumi responds warmly to 4-minute sensory resets. Your parasympathetic equilibrium feeds its aura directly.",
            prompt = "Let's do a 3-minute mindful breath reset for Lumi and me.",
            accentType = AccentType.MINT
        ),
        ThoughtStream(
            id = "3",
            title = "Architecture of Your Creative Seed",
            tag = "Insight",
            summary = "Connecting your project intention 'Harmonic Living' with your reading notes on adaptive tranquility.",
            prompt = "Synthesize my recent thoughts on Harmonic Living.",
            accentType = AccentType.AMBER
        )
    )

    fun getDailyRhythm(): List<DailyRhythmBlock> = listOf(
        DailyRhythmBlock(
            time = "08:00 AM",
            title = "Gentle Awakening & Attunement",
            description = "Natural light, emotional grounding, intention setting",
            isCurrent = false
        ),
        DailyRhythmBlock(
            time = "09:30 AM – 12:00 PM",
            title = "Deep Synthesis Window",
            description = "Uninterrupted creative flow, high associative capacity",
            isCurrent = true
        ),
        DailyRhythmBlock(
            time = "01:30 PM",
            title = "Mindful Reset & Nutrition",
            description = "Sensory walk, screen pause, gentle tea ritual",
            isCurrent = false
        ),
        DailyRhythmBlock(
            time = "03:30 PM – 05:00 PM",
            title = "Collaborative Resonance",
            description = "Light communications, synthesis reviews, memory logging",
            isCurrent = false
        ),
        DailyRhythmBlock(
            time = "08:30 PM",
            title = "Luminous Wind-down",
            description = "Reflective journaling, warm aura breathing, restorative rest",
            isCurrent = false
        )
    )

    fun addMemory(title: String, content: String, category: String, isPinned: Boolean = false) {
        val newItem = MemoryItem(
            id = UUID.randomUUID().toString(),
            title = title,
            content = content,
            category = category,
            date = "Today",
            isPinned = isPinned
        )
        _memories.value = listOf(newItem) + _memories.value
    }

    fun toggleMemoryPin(id: String) {
        _memories.value = _memories.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun sendMessage(userText: String) {
        val currentTime = currentTimeFormatted()
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            content = userText,
            timestamp = currentTime
        )
        val currentList = _messages.value.toMutableList()
        currentList.add(userMsg)

        val companionReply = generateCompanionReply(userText, _selectedMode.value)
        currentList.add(companionReply)

        _messages.value = currentList
    }

    private fun currentTimeFormatted(): String {
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
    }

    private fun generateCompanionReply(input: String, mode: PresenceMode): ChatMessage {
        val lower = input.lowercase()
        val timeStr = currentTimeFormatted()
        val id = UUID.randomUUID().toString()
        val tamagotchi = _tamagotchiState.value

        return when {
            lower.contains("how are you") || lower.contains("tamagotchi") || lower.contains("lumi") -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "I reflect your life flow! Right now our Life Score is ${tamagotchi.overallLifeScore}%. My hydration is at ${(tamagotchi.hydration * 100).toInt()}%, and our inner peace is at ${(tamagotchi.innerPeace * 100).toInt()}%. Every healthy choice you make directly blooms in my aura! ✨",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Sensory Life Telemetry", "Synthesizing 5 core vital meters: Hydration, Focus, Peace, Energy, Connection"),
                        ThoughtStep("Symbiotic Reflection", "Mapping user wellness directly into Tamagotchi aura bloom")
                    ),
                    suggestedPrompts = listOf("How can I boost your energy?", "Let's do a breath reset", "Give me a healthy quest"),
                    category = "Life Tamagotchi State"
                )
            }
            lower.contains("breath") || lower.contains("calm") || lower.contains("reset") -> {
                nurtureBreathe()
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "Inhale softly with me for 4 counts... feel the warm expansion in your chest. Hold gently for 2... and exhale with complete release. Look at my aura blooming in soft mint and coral! (+25 XP)",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Sensing Heart Rate & Rhythm", "Detecting shift toward restorative parasympathetic activation"),
                        ThoughtStep("Pacing Cadence", "Modulating visual aura to gentle 4-second breath expansion"),
                        ThoughtStep("Harmony Lock", "Grounding attention in the present moment")
                    ),
                    suggestedPrompts = listOf("Do another breath cycle", "I feel more grounded now", "What should I focus on next?"),
                    category = "Mindful Reset"
                )
            }
            lower.contains("water") || lower.contains("drink") -> {
                nurtureWater()
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "Delicious! Thank you for hydrating us. Water clears brain fog and restores cellular fluidity. (+18% Hydration • +20 XP)",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Hydration Boost", "Updating life vital to ${(tamagotchi.hydration * 100).toInt()}%")
                    ),
                    suggestedPrompts = listOf("Check my life score", "What's my next quest?", "Start a focus sprint"),
                    category = "Life Care"
                )
            }
            lower.contains("creative") || lower.contains("focus") || lower.contains("priorit") || lower.contains("project") -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "When we look across your intentions, the core essence is clarity of purpose over volume. Let's isolate the single pivot that creates the most effortless leverage today. Where does your curiosity naturally feel warmest right now?",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Context Retrieval", "Checking stored intentions for 'Harmonic Flow' and active projects"),
                        ThoughtStep("Cognitive Friction Analysis", "Identifying high-leverage vs distracting micro-tasks"),
                        ThoughtStep("Synthesis", "Formulating non-demanding, warm inquiry to unblock intuition")
                    ),
                    suggestedPrompts = listOf("Refining my project outline", "Help me organize 3 key steps", "Synthesize my weekly intentions"),
                    category = "Strategic Synthesis"
                )
            }
            else -> {
                ChatMessage(
                    id = id,
                    isFromUser = false,
                    content = "I hear you deeply. Underneath the words, there is a clear intuitive direction forming. When you give yourself permission to step back and observe without pressure, the solution illuminates naturally. Shall we explore this thought together?",
                    timestamp = timeStr,
                    thoughtSteps = listOf(
                        ThoughtStep("Empathetic Semantic Parsing", "Attuning to underlying emotional valence and reflective tone"),
                        ThoughtStep("Memory Synthesis", "Cross-referencing ongoing themes of balance and intentional living"),
                        ThoughtStep("Radiant Formulation", "Crafting spacious, supportive answer")
                    ),
                    suggestedPrompts = listOf("Tell me more about this", "How does this relate to my goals?", "Save this reflection in Vault"),
                    category = "Insight Reflection"
                )
            }
        }
    }

    private fun initialCareLogs(): List<CareActionLog> = listOf(
        CareActionLog("c1", "Morning Hydration", "+18% Hydration", "8:15 AM", 20),
        CareActionLog("c2", "Sunlight & Fresh Air Walk", "+20% Physical Energy", "8:45 AM", 30),
        CareActionLog("c3", "Mindful Intention Setting", "+20% Inner Peace", "9:05 AM", 25)
    )

    private fun initialLifeQuests(): List<LifeQuest> = listOf(
        LifeQuest("q1", "Drink 2 tall glasses of water", "+20% Hydration • +30 XP", "Physical", true),
        LifeQuest("q2", "Complete 45-min deep focus sprint", "+25% Mind Flow • +40 XP", "Mind", false),
        LifeQuest("q3", "3-Minute mindful breath pause", "+20% Inner Peace • +25 XP", "Spirit", false),
        LifeQuest("q4", "Send a warm message to a loved one", "+25% Connection • +35 XP", "Social", false),
        LifeQuest("q5", "Gentle spine stretch & shoulder roll", "+15% Physical • +20 XP", "Physical", false)
    )

    private fun initialMessages(): List<ChatMessage> = listOf(
        ChatMessage(
            id = "m1",
            isFromUser = false,
            content = "Hello, Maya! I am Lumi, your life companion spirit. My vitality, aura glow, and joy directly reflect how well you are treating yourself today. Right now our Life Score is 83%—we are radiant!",
            timestamp = "9:00 AM",
            thoughtSteps = listOf(
                ThoughtStep("Tamagotchi Life Sync", "Reading current life balance: 83% harmonious"),
                ThoughtStep("Aura Calibration", "Rendering Stage 3 Luminous Luminary with warm coral bloom"),
                ThoughtStep("Presence Readiness", "Attuned to Mindful Companion state")
            ),
            suggestedPrompts = listOf(
                "How can I nurture you right now?",
                "Let's review our life vitals",
                "Take a 3-minute mindful pause"
            ),
            category = "Life Tamagotchi Attunement"
        ),
        ChatMessage(
            id = "m2",
            isFromUser = true,
            content = "I want to make sure I don't get overwhelmed with too many tasks today.",
            timestamp = "9:02 AM"
        ),
        ChatMessage(
            id = "m3",
            isFromUser = false,
            content = "When you feel overwhelmed, my aura dims slightly. Let's protect both of us: pick just one single focal task this morning, hydrate, and let me hold the rest of your mental load. I am right here with you.",
            timestamp = "9:03 AM",
            thoughtSteps = listOf(
                ThoughtStep("Cognitive Load Assessment", "User experiencing mild task fragmentation"),
                ThoughtStep("Boundary Architecture", "Protecting attention container from multi-task scatter"),
                ThoughtStep("Emotional Resonation", "Offering unconditional reassurance and mental offloading")
            ),
            suggestedPrompts = listOf(
                "Log a water break for Lumi",
                "Define my single focal task",
                "Start a quiet focus timer"
            ),
            category = "Cognitive Protection"
        )
    )

    private fun initialMemories(): List<MemoryItem> = listOf(
        MemoryItem(
            id = "v1",
            title = "Unforced Effortlessness",
            content = "The best work emerges when I am relaxed, grounded, and unhurried. Quality of attention creates speed, not rush.",
            category = "Values",
            date = "Oct 24",
            isPinned = true
        ),
        MemoryItem(
            id = "v2",
            title = "Harmonic Living Project",
            content = "Synthesizing intuitive design systems, warm spatial audio, and emotional companion intelligence.",
            category = "Projects",
            date = "Yesterday",
            isPinned = true
        ),
        MemoryItem(
            id = "v3",
            title = "Morning Silence Window",
            content = "Keep the first 30 minutes after waking screen-free to allow subconscious creative integration.",
            category = "Intentions",
            date = "Oct 22",
            isPinned = false
        ),
        MemoryItem(
            id = "v4",
            title = "Equilibrium Insight",
            content = "Tension in the shoulders is a signal to pause and take 3 deep belly breaths, not to push harder.",
            category = "Reflections",
            date = "Oct 20",
            isPinned = false
        )
    )
}
