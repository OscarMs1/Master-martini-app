package cl.duoc.mastermartini.ui.screens.home

import cl.duoc.mastermartini.data.model.LearnItem

/** Dos recetas reales del repositorio, elegidas para que ambas se vean en Inicio. */
internal fun featuredSeasonalRecipes(recipes: List<LearnItem>): List<LearnItem> =
    listOf("r-kuchen", "r-bombones").mapNotNull { id -> recipes.firstOrNull { it.id == id } }
