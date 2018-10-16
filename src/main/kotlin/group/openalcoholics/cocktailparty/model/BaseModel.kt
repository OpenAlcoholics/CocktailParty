package group.openalcoholics.cocktailparty.model

interface BaseModel<T : Any> {
    fun withId(id: Int): T
}
