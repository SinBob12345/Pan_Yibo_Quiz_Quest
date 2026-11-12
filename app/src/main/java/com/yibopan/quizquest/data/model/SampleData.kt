package com.yibopan.quizquest.data.model

/**
 * Hard-coded sample data for @Preview functions and the Stage 1 placeholder screens
 * (like the hard-coded pets list in Lecture 4). ViewModels stop using it once the
 * repositories are real (Stages 5–6).
 */
object SampleData {

    val worlds: List<Category> = listOf(
        Category(id = 9, name = "General Knowledge"),
        Category(id = 17, name = "Science & Nature"),
        Category(id = 18, name = "Science: Computers"),
        Category(id = 19, name = "Science: Mathematics"),
        Category(id = 22, name = "Geography"),
        Category(id = 23, name = "History"),
        Category(id = 27, name = "Animals"),
    )

    fun worldName(categoryId: Int): String = worlds.firstOrNull { it.id == categoryId }?.name.orEmpty()
}
