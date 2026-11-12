package com.yibopan.quizquest.data.rules

import com.yibopan.quizquest.data.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [CategoryFilter] ensuring school-appropriate category filtering. */
class CategoryFilterTest {

    @Test
    fun `ALLOWED_CATEGORY_IDS contains exactly the 7 school-appropriate categories`() {
        val expected = setOf(9, 17, 18, 19, 22, 23, 27)
        assertEquals(7, CategoryFilter.ALLOWED_CATEGORY_IDS.size)
        assertEquals(expected, CategoryFilter.ALLOWED_CATEGORY_IDS)
    }

    @Test
    fun `filterAllowed filters out non-academic categories and preserves order`() {
        val input = listOf(
            Category(9, "General Knowledge"),
            Category(10, "Entertainment: Books"),
            Category(17, "Science & Nature"),
            Category(21, "Sports"),
            Category(23, "History"),
            Category(26, "Celebrities"),
            Category(27, "Animals"),
        )

        val filtered = CategoryFilter.filterAllowed(input)

        val expected = listOf(
            Category(9, "General Knowledge"),
            Category(17, "Science & Nature"),
            Category(23, "History"),
            Category(27, "Animals"),
        )
        assertEquals(expected, filtered)
    }

    @Test
    fun `filterAllowed returns empty list when no categories match`() {
        val input = listOf(
            Category(10, "Entertainment: Books"),
            Category(11, "Entertainment: Film"),
            Category(21, "Sports"),
        )

        val filtered = CategoryFilter.filterAllowed(input)

        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `filterAllowed handles empty list`() {
        val filtered = CategoryFilter.filterAllowed(emptyList())
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `isAllowed returns true for academic categories and false for others`() {
        assertTrue(CategoryFilter.isAllowed(9))
        assertTrue(CategoryFilter.isAllowed(17))
        assertTrue(CategoryFilter.isAllowed(18))
        assertTrue(CategoryFilter.isAllowed(19))
        assertTrue(CategoryFilter.isAllowed(22))
        assertTrue(CategoryFilter.isAllowed(23))
        assertTrue(CategoryFilter.isAllowed(27))

        assertFalse(CategoryFilter.isAllowed(10))
        assertFalse(CategoryFilter.isAllowed(11))
        assertFalse(CategoryFilter.isAllowed(21))
        assertFalse(CategoryFilter.isAllowed(26))
        assertFalse(CategoryFilter.isAllowed(999))
    }
}
