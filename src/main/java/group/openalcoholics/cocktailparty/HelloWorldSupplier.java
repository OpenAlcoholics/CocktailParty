package group.openalcoholics.cocktailparty;

import java.util.function.Supplier;

final class HelloWorldSupplier implements Supplier<String> {

    @Override
    public String get() {
        return "Hello, world!";
    }
}
