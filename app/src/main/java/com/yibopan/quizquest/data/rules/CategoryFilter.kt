package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.Category

/**
 * Stage 2 — age-appropriate content for secondary-school learners
 * (Lecture 9: if we target people under 18, content must be safe for them).
 *
 * Only academic Open Trivia DB categories become worlds. Verify the ids against
 * https://opentdb.com/api_category.php when implementing.
 */
object CategoryFilter {

    /** 9 General Knowledge, 17 Science & Nature, 18 Computers, 19 Mathematics, 22 Geography, 23 History, 27 Animals. */
    val ALLOWED_CATEGORY_IDS: Set<Int> = setOf(9, 17, 18, 19, 22, 23, 27)

    /** Keeps only allowed categories, preserving their order. */
    fun filterAllowed(categories: List<Category>): List<Category> {
        return categories.filter { it.id in ALLOWED_CATEGORY_IDS }
    }

    /** Checks whether a single category ID is allowed. */
    fun isAllowed(categoryId: Int): Boolean = categoryId in ALLOWED_CATEGORY_IDS
}
