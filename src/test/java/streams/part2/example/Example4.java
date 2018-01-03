package streams.part2.example;

import lambda.data.Person;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class Example4 {

    private static Stream<Person> getPersonStream() {
        return Stream.of(
                new Person("a", "a", 1),
                new Person("b", "b", 2),
                new Person("c", "c", 3),
                new Person("d", "d", 4),
                new Person("e", "e", 5)
        );
    }

    @Test
    public void incorrectReducePersonsToString() {
        Stream<Person> personStream = getPersonStream();

        String result = personStream.parallel()
                                    .reduce(
                                            "",
                                            (accum, person) -> accum + "\n and " + person.toString(),
                                            (accum1, accum2) -> accum1 + "\n and " + accum2);

        System.out.println(result);
    }

    @Test
    public void incorrectCollectPersonsToString() {
        StringBuilder res = getPersonStream().parallel()
                                             .unordered()
                                             .collect(StringBuilder::new, // synchronization is in Stream
                                                      (builder, person) -> builder.append("\n and ").append(person),
                                                      (builder1, builder2) -> builder1.append("\n and ").append(builder2)
                                             );

        String result = res.toString();

        System.out.println(result);
    }

    @Test
    public void collectPersonToString1() {
        StringJoiner res = getPersonStream().parallel()
                                            .collect(() -> new StringJoiner("\n and "), // synchronization is in Stream
                                                     (joiner, person) -> joiner.add(person.toString()),
                                                     StringJoiner::merge
                                            );

        String result = res.toString();

        System.out.println(result);
    }

    @Test
    public void collectPersonToString2() {
        String result = getPersonStream().parallel()
                .collect(new Collector<Person, StringJoiner, String>() {
                             @Override
                             public Supplier<StringJoiner> supplier() {
                                 return () -> new StringJoiner("\n and ");
                             }

                             @Override
                             public BiConsumer<StringJoiner, Person> accumulator() {
                                 return (joiner, person) -> joiner.add(person.toString());
                             }

                             @Override
                             public BinaryOperator<StringJoiner> combiner() {
                                 return StringJoiner::merge;
                             }

                             @Override
                             public Function<StringJoiner, String> finisher() {
                                 return StringJoiner::toString;
                             }

                             @Override
                             public Set<Characteristics> characteristics() {
                                 return Collections.emptySet();
                             }
                         }
                );

        System.out.println(result);
    }

    @Test
    public void collectPersonToString3() {
        String expected = getPersonStream().parallel()
                                           .map(Object::toString)
                                           .collect(Collectors.joining("\n and "));

        String result = getPersonStream().parallel()
                                         .collect(Collectors.mapping(Object::toString, Collectors.joining("\n and ")));

        System.out.println(result);

        assertEquals(expected, result);
    }
}
