package cl.duoc.mastermartini
import org.junit.Test
import org.junit.Assert.*
import cl.duoc.mastermartini.data.local.LearningState
import cl.duoc.mastermartini.data.repository.FakeContentRepository
class LearningTest {
 @Test fun progressCountsOnlyValidModules() {
  val s=LearningState(modules=setOf("a:0","a:2","a:99","b:1"))
  assertEquals(2f/3f,s.progress("a",3),0.001f)
  assertEquals(0f,s.progress("a",0),0f)
  assertEquals(0f,s.progress("c",3),0f)
 }
 @Test fun catalogReferencesResolve() {
  val r=FakeContentRepository()
  r.learnItems().forEach { item -> item.productIds.forEach { assertNotNull(r.product(it)) } }
  r.services().mapNotNull { it.learnId }.forEach { assertNotNull(r.learnItem(it)) }
 }
}
