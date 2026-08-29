package io.greenstep.data.challenge

enum class ChallengeType(val label: String) {
    STEP("Step"),
    DAILY_GOAL("Daily Goal"),
    DISTANCE("Distance"),
    ECO_ADVENTURE("Eco Adventure")
}

enum class ChallengeDuration(val weeks: Int, val label: String) {
    ONE(1, "1 wk"),
    TWO(2, "2 wks"),
    THREE(3, "3 wks"),
    FOUR(4, "4 wks")
}

data class EcoCheckpoint(
    val km: Float,
    val title: String,
    val emoji: String
)

data class Challenge(
    val id: String,
    val title: String,
    val type: ChallengeType = ChallengeType.STEP,
    val duration: ChallengeDuration = ChallengeDuration.ONE,
    val daysLeft: Int,
    val progress: Float,
    val participants: Int,
    val rewardCoins: Int = 100,
    val description: String = "",
    val checkpoints: List<EcoCheckpoint> = emptyList(),
    val isEcoAdventure: Boolean = type == ChallengeType.ECO_ADVENTURE
)

val ecoAdventureCheckpoints = listOf(
    EcoCheckpoint(0f, "Start — Rio", "🌿"),
    EcoCheckpoint(30f, "Canopy Camp", "🦜"),
    EcoCheckpoint(60f, "River Crossing", "🛶"),
    EcoCheckpoint(90f, "Ancient Kapok", "🌳"),
    EcoCheckpoint(120f, "Heart of Amazon", "🌎")
)

val sampleEcoAdventure = Challenge(
    id = "eco_amazon",
    title = "Amazon Trail 120 km → plant 10 trees",
    type = ChallengeType.ECO_ADVENTURE,
    duration = ChallengeDuration.FOUR,
    daysLeft = 21,
    progress = 0.34f,
    participants = 892,
    rewardCoins = 500,
    description = "Walk 120 km together — every finisher plants 10 real trees. Collect postcards at checkpoints!",
    checkpoints = ecoAdventureCheckpoints
)
