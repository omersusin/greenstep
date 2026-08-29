package io.greenstep.data.economy

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Forest
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import io.greenstep.R

data class Achievement(
    val id: String,
    val titleRes: Int,
    val icon: ImageVector,
    val unlocked: Boolean = false,
    val progress: Float = 0f
)

object Achievements {
    val all: List<Achievement> = listOf(
        Achievement("first_step", R.string.ach_first_step, Icons.Outlined.DirectionsWalk),
        Achievement("streak_3", R.string.ach_streak_3, Icons.Outlined.LocalFireDepartment),
        Achievement("streak_7", R.string.ach_streak_7, Icons.Outlined.WbSunny),
        Achievement("streak_30", R.string.ach_streak_30, Icons.Outlined.EmojiEvents),
        Achievement("steps_10k", R.string.ach_steps_10k, Icons.Outlined.Terrain),
        Achievement("steps_100k", R.string.ach_steps_100k, Icons.Outlined.Flag),
        Achievement("green_sprout", R.string.ach_green_sprout, Icons.Outlined.Spa),
        Achievement("green_forest", R.string.ach_green_forest, Icons.Outlined.Forest),
        Achievement("eco_warrior", R.string.ach_eco_warrior, Icons.Outlined.Eco),
        Achievement("heart_steps", R.string.ach_heart_steps, Icons.Outlined.Favorite),
        Achievement("zen_master", R.string.ach_zen_master, Icons.Outlined.SelfImprovement),
        Achievement("tree_planter", R.string.ach_tree_planter, Icons.Outlined.Park),
    )
}
