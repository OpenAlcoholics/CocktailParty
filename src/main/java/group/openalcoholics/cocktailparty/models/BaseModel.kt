package group.openalcoholics.cocktailparty.models

interface BaseModel<T : Any> {
    fun withId(id: Int): T
}
