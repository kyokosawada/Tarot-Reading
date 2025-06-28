package com.example.tarot.util

import android.content.Context
import androidx.annotation.DrawableRes
import com.example.tarot.R

object ImageResourceMapper {

    // Get card back image resource
    @DrawableRes
    fun getCardBackResource(): Int = R.drawable.card_back

    // Map card names to drawable resource IDs
    @DrawableRes
    fun getCardImageResource(imageName: String): Int {
        // Convert database image name to actual resource name by adding "tarot_" prefix
        val resourceName = "tarot_$imageName"

        return when (resourceName) {
            // Major Arcana
            "tarot_the_fool" -> R.drawable.tarot_the_fool
            "tarot_the_magician" -> R.drawable.tarot_the_magician
            "tarot_the_high_priestess" -> R.drawable.tarot_the_high_priestess
            "tarot_the_empress" -> R.drawable.tarot_the_empress
            "tarot_the_emperor" -> R.drawable.tarot_the_emperor
            "tarot_the_hierophant" -> R.drawable.tarot_the_hierophant
            "tarot_the_lovers" -> R.drawable.tarot_the_lovers
            "tarot_the_chariot" -> R.drawable.tarot_the_chariot
            "tarot_strength" -> R.drawable.tarot_strength
            "tarot_the_hermit" -> R.drawable.tarot_the_hermit
            "tarot_wheel_of_fortune" -> R.drawable.tarot_wheel_of_fortune
            "tarot_justice" -> R.drawable.tarot_justice
            "tarot_the_hanged_man" -> R.drawable.tarot_the_hanged_man
            "tarot_death" -> R.drawable.tarot_death
            "tarot_temperance" -> R.drawable.tarot_temperance
            "tarot_the_devil" -> R.drawable.tarot_the_devil
            "tarot_the_tower" -> R.drawable.tarot_the_tower
            "tarot_the_star" -> R.drawable.tarot_the_star
            "tarot_the_moon" -> R.drawable.tarot_the_moon
            "tarot_the_sun" -> R.drawable.tarot_the_sun
            "tarot_judgement" -> R.drawable.tarot_judgement
            "tarot_the_world" -> R.drawable.tarot_the_world

            // Cups
            "tarot_ace_of_cups" -> R.drawable.tarot_ace_of_cups
            "tarot_two_of_cups" -> R.drawable.tarot_two_of_cups
            "tarot_three_of_cups" -> R.drawable.tarot_three_of_cups
            "tarot_four_of_cups" -> R.drawable.tarot_four_of_cups
            "tarot_five_of_cups" -> R.drawable.tarot_five_of_cups
            "tarot_six_of_cups" -> R.drawable.tarot_six_of_cups
            "tarot_seven_of_cups" -> R.drawable.tarot_seven_of_cups
            "tarot_eight_of_cups" -> R.drawable.tarot_eight_of_cups
            "tarot_nine_of_cups" -> R.drawable.tarot_nine_of_cups
            "tarot_ten_of_cups" -> R.drawable.tarot_ten_of_cups
            "tarot_page_of_cups" -> R.drawable.tarot_page_of_cups
            "tarot_knight_of_cups" -> R.drawable.tarot_knight_of_cups
            "tarot_queen_of_cups" -> R.drawable.tarot_queen_of_cups
            "tarot_king_of_cups" -> R.drawable.tarot_king_of_cups

            // Pentacles  
            "tarot_ace_of_pentacles" -> R.drawable.tarot_ace_of_pentacles
            "tarot_two_of_pentacles" -> R.drawable.tarot_two_of_pentacles
            "tarot_three_of_pentacles" -> R.drawable.tarot_three_of_pentacles
            "tarot_four_of_pentacles" -> R.drawable.tarot_four_of_pentacles
            "tarot_five_of_pentacles" -> R.drawable.tarot_five_of_pentacles
            "tarot_six_of_pentacles" -> R.drawable.tarot_six_of_pentacles
            "tarot_seven_of_pentacles" -> R.drawable.tarot_seven_of_pentacles
            "tarot_eight_of_pentacles" -> R.drawable.tarot_eight_of_pentacles
            "tarot_nine_of_pentacles" -> R.drawable.tarot_nine_of_pentacles
            "tarot_ten_of_pentacles" -> R.drawable.tarot_ten_of_pentacles
            "tarot_page_of_pentacles" -> R.drawable.tarot_page_of_pentacles
            "tarot_knight_of_pentacles" -> R.drawable.tarot_knight_of_pentacles
            "tarot_queen_of_pentacles" -> R.drawable.tarot_queen_of_pentacles
            "tarot_king_of_pentacles" -> R.drawable.tarot_king_of_pentacles

            // Swords
            "tarot_ace_of_swords" -> R.drawable.tarot_ace_of_swords
            "tarot_two_of_swords" -> R.drawable.tarot_two_of_swords
            "tarot_three_of_swords" -> R.drawable.tarot_three_of_swords
            "tarot_four_of_swords" -> R.drawable.tarot_four_of_swords
            "tarot_five_of_swords" -> R.drawable.tarot_five_of_swords
            "tarot_six_of_swords" -> R.drawable.tarot_six_of_swords
            "tarot_seven_of_swords" -> R.drawable.tarot_seven_of_swords
            "tarot_eight_of_swords" -> R.drawable.tarot_eight_of_swords
            "tarot_nine_of_swords" -> R.drawable.tarot_nine_of_swords
            "tarot_ten_of_swords" -> R.drawable.tarot_ten_of_swords
            "tarot_page_of_swords" -> R.drawable.tarot_page_of_swords
            "tarot_knight_of_swords" -> R.drawable.tarot_knight_of_swords
            "tarot_queen_of_swords" -> R.drawable.tarot_queen_of_swords
            "tarot_king_of_swords" -> R.drawable.tarot_king_of_swords

            // Wands
            "tarot_ace_of_wands" -> R.drawable.tarot_ace_of_wands
            "tarot_two_of_wands" -> R.drawable.tarot_two_of_wands
            "tarot_three_of_wands" -> R.drawable.tarot_three_of_wands
            "tarot_four_of_wands" -> R.drawable.tarot_four_of_wands
            "tarot_five_of_wands" -> R.drawable.tarot_five_of_wands
            "tarot_six_of_wands" -> R.drawable.tarot_six_of_wands
            "tarot_seven_of_wands" -> R.drawable.tarot_seven_of_wands
            "tarot_eight_of_wands" -> R.drawable.tarot_eight_of_wands
            "tarot_nine_of_wands" -> R.drawable.tarot_nine_of_wands
            "tarot_ten_of_wands" -> R.drawable.tarot_ten_of_wands
            "tarot_page_of_wands" -> R.drawable.tarot_page_of_wands
            "tarot_knight_of_wands" -> R.drawable.tarot_knight_of_wands
            "tarot_queen_of_wands" -> R.drawable.tarot_queen_of_wands
            "tarot_king_of_wands" -> R.drawable.tarot_king_of_wands

            // Fallback for unknown cards - use card back as placeholder
            else -> R.drawable.card_back
        }
    }

    // Alternative method using reflection (more flexible but slower)
    @DrawableRes
    fun getCardImageResourceDynamic(context: Context, imageName: String): Int {
        return context.resources.getIdentifier(
            imageName,
            "drawable",
            context.packageName
        ).takeIf { it != 0 } ?: R.drawable.ic_launcher_foreground
    }
}