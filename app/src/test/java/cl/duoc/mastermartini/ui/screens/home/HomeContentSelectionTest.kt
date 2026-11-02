package cl.duoc.mastermartini.ui.screens.home

import cl.duoc.mastermartini.data.repository.FakeContentRepository
import cl.duoc.mastermartini.data.model.LearnKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeContentSelectionTest {
    private val recipes = FakeContentRepository().learnItems().filter { it.kind == LearnKind.RECIPE }

    @Test fun `seasonal recipes are two different items`() {
        val result = featuredSeasonalRecipes(recipes)
        assertEquals(listOf("r-kuchen", "r-bombones"), result.map { it.id })
    }

    @Test fun `seasonal recipes do not invent missing content`() {
        assertTrue(featuredSeasonalRecipes(emptyList()).isEmpty())
    }
}
