package br.com.casaclara.domain

import br.com.casaclara.domain.content.CozinhaGuides
import br.com.casaclara.domain.content.Library
import br.com.casaclara.domain.content.Tips
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JourneyAndLibraryTest {
    @Test fun jornada_por_situacao() {
        for (s in Situation.values()) {
            val p = Journey.path(s, null)
            assertEquals("perfil", p.first().id)
            assertEquals(p.size, p.map { it.id }.toSet().size)
        }
        val j0 = Journey.compute(Snapshot(), Situation.JUNTANDO, null, emptySet(), emptySet())
        assertEquals("perfil", j0.next?.step?.id)
        val j1 = Journey.compute(Snapshot(onboarded = true, livingCostDone = true), Situation.JUNTANDO, null, emptySet(), emptySet())
        assertEquals("renda", j1.next?.step?.id)
        val j2 = Journey.compute(Snapshot(onboarded = true), Situation.JUNTANDO, null, setOf("custo"), setOf("renda"))
        assertEquals("gastos", j2.next?.step?.id)
        assertTrue(j2.steps.first { it.step.id == "custo" }.manual)
    }

    @Test fun objetivo_puxa_passos_para_frente() {
        val p = Journey.path(Situation.SOZINHO, MainGoal.CASA).map { it.id }
        assertEquals("perfil", p[0])
        assertEquals(Journey.STEPS[p[1]]!!.area, Area.CASA)
        assertEquals(Journey.path(Situation.SOZINHO, null).size, p.size)
    }

    @Test fun conquistas() {
        assertTrue(Achievements.unlocked(Snapshot()).isEmpty())
        val u = Achievements.unlocked(Snapshot(onboarded = true, expenses = 12, guidesRead = 6))
        assertTrue("primeiros-passos" in u && "de-olho" in u && "leitor" in u)
        assertFalse("biblioteca" in u)
        assertEquals(Achievements.ALL.size, Achievements.ALL.map { it.id }.toSet().size)
    }

    @Test fun biblioteca_completa_e_consistente() {
        val g = Library.GUIDES
        assertTrue("pelo menos 90 guias", g.size >= 90)
        assertEquals("ids únicos", g.size, g.map { it.id }.toSet().size)
        assertEquals("títulos únicos", g.size, g.map { it.title }.toSet().size)
        for (c in Library.CATEGORIES) assertTrue("categoria ${c.id} com 7+ guias", Library.byCategory(c.id).size >= 7)
        for (x in g) {
            assertTrue("${x.id}: categoria válida", Library.CATEGORIES.any { it.id == x.category })
            assertTrue("${x.id}: problema", x.problem.length >= 60)
            assertTrue("${x.id}: 3+ passos", x.steps.size >= 3)
            assertTrue("${x.id}: checklist", x.checklist.isNotEmpty())
            assertTrue("${x.id}: relacionados", Library.related(x).isNotEmpty())
            for (r in x.related) assertTrue("${x.id}: relacionado $r existe", Library.BY_ID.containsKey(r))
            assertTrue("${x.id}: tempo", x.minutes in 1..15)
        }
        val textos = g.flatMap { it.steps }
        assertEquals("sem passos repetidos", textos.size, textos.toSet().size)
    }

    @Test fun busca() {
        assertEquals("vazamento-gas", Library.search("vazamento gas").first().id)
        assertTrue(Library.search("caução").any { it.id == "contrato-garantias" })
        assertTrue(Library.search("x").isEmpty())
    }

    @Test fun receitas_e_dicas() {
        assertTrue(CozinhaGuides.RECIPES.size >= 8)
        CozinhaGuides.RECIPES.forEach { assertTrue(it.steps.size >= 3 && it.ingredients.size >= 3) }
        assertEquals(Tips.ofDay(0), Tips.ofDay(Tips.ALL.size.toLong()))
        assertTrue(Tips.ofDay(-1).isNotEmpty())
    }
}
