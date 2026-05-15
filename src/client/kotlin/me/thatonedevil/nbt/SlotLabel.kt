package me.thatonedevil.nbt

object SlotLabel {

    private val NAMED_SLOTS = mapOf(
        98 to "Mainhand",
        99 to "Offhand",
        100 to "Feet Armor",
        101 to "Leg Armor",
        102 to "Chest Armor",
        103 to "Head Armor",
        105 to "Wolf/Horse Armor",
        106 to "Saddle/Carpet/Harness",
        400 to "Saddle",
        499 to "Chest Item"
    )

    fun format(slotIndex: Int, showLabels: Boolean): String {
        if (!showLabels) return slotIndex.toString()
        val label = NAMED_SLOTS[slotIndex]
        return if (label != null) "$slotIndex ($label)" else slotIndex.toString()
    }
}
