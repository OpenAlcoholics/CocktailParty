@file:JvmName("HelloWorld")

package group.openalcoholics.cocktailparty

/**
 * Prints "Hello, world!".
 * @param args the application arguments
 */
fun main(args: Array<String>) {
    println(HelloWorldSupplier().get())
}
