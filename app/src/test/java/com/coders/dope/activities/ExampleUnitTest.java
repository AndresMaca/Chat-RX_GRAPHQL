package com.coders.dope.activities;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testing_optionals() {
        assertEquals(Optional.of("HELLO"), Optional.of("Hello"));
    }

    @Test
    public void test_list() {
        List<String> myList = Stream.of("a", "b").map(String::toUpperCase).collect(Collectors.toList());
        assertEquals(asList("A", "B"), myList);
        List<List<String>> list = Arrays.asList(
                Arrays.asList("a"),
                Arrays.asList("b"));
        System.out.println(list);
        System.out.println(list
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));

    }

    private List<String> asList(String a, String b) {
        List<String> list = new ArrayList<>();
        list.add(a);
        list.add(b);
        return list;
    }

    @Test
    public void testing_Observables() {

        String[] letters = {"a", "b", "c", "d", "e", "f", "g"};
        Observable<String> observable = Observable.fromArray(letters);
        final String[] result = {new String()};
        observable.subscribe(
                i -> result[0] += i,  //OnNext
                Throwable::printStackTrace, //OnError
                () -> result[0] += "_Completed" //OnCompleted
        );
        assertTrue(result[0].equals("abcdefg_Completed"));
    }

    @Test
    public void testing_Optional() {
        String optional = (String) Optional.ofNullable(null).orElse("hola");
        print(optional);
    }

    @Test
    public void testing_orElse() {

        String[] letters = {"a", "b", "c", "d", "e", "f", null, "g"};
        /*
        Stream<String> stringStream =
                asList(Optional.of("a").orElse("rapapa"),
                        (String) Optional.ofNullable(null).
                                orElse("hola")).
                stream().map(String::toUpperCase);
                */
        Stream<String> stringStream = Stream.of(letters).map(s -> Optional.ofNullable(s).orElse("isNull!")).map(String::toUpperCase);
        for (Object item : stringStream.toArray()
                ) {
            print(item.toString());

        }
        //Optional.of(Arrays.asList(letters)).map(strings ->{ print(strings.get(0)); return strings;});
        Stream.of(letters).map(s -> {
            print(s);
            return Optional.of(s).map(String::toUpperCase).orElse("other");
        });

        /*
      Stream.of(letters).map(s -> {
          Optional.of(s).map(String::toUpperCase)
           s.toUpperCase()}) */
    }

    @Test
    public void adding_test() {
        //peek method
        final int[] count = {0};
        Integer[] integers = {1, 2, 3, 4, 5, 6, null};
        Stream.of(integers).map((i) -> Optional.ofNullable(i).orElse(0)).
                peek((s) -> count[0] += s).
                forEach(integer -> print("numero: " + integer));
        print("Suma: " + count[0]);
    }

    @Test
    public void scan_test() {
        String[] letters = {"a", "b", "c"};
        final String[] result = {""};
        Observable.fromArray(letters).scan(new StringBuilder(), StringBuilder::append)
                .subscribe(total -> result[0] += total.toString());
        assertEquals(result[0], "aababc");
    }

    @Test
    public void groupBy_test() {
        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0};
        String[] EVEN = {""};
        String[] ODD = {""};
        Observable.fromArray(numbers).
                groupBy(i -> 0 == (i % 2) ? "EVEN" : "ODD").subscribe(group -> {
            group.subscribe(number -> {
                print(group.getKey());
                print("int: " + number);
                if (group.getKey().equals("EVEN")) {
                    EVEN[0] += number;
                } else {
                    ODD[0] += number;
                }
            });
        });
        print("odds: " + ODD[0]);
        print("EVEN: " + EVEN[0]);

    }

    @Test
    public void filter_test() {
        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0};

        Observable.fromArray(numbers).filter(i -> (i % 2 == 1)).subscribe(i -> print("int: " + i));
    }

    @Test
    public void conditionalOperatorsDIF_test() {
        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0};
        String[] letters = {"a", "b", "c"};

        final String result[] = {""};
        Observable.empty().defaultIfEmpty("Observable is empty ").subscribe(s -> result[0] += s);
        assertEquals("Observable is empty ", result[0]);
    }

    @Test
    public void conditionalOperatorsDIF2_test() {
        String[] letters = {"a", "b", "c"};
        final String result[] = {""};
        Observable.fromArray(letters).defaultIfEmpty("Observable is empty ").first("null!").subscribe(s -> result[0] += s);
        assertEquals("a", result[0]);

    }

    @Test
    public void conditionalOperatorsTW_test() {
        Integer[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0};
        Integer[] sum = {0};
        Observable.fromArray(numbers).takeWhile(i -> i < 6).subscribe(s -> sum[0] += s);
        assertEquals(sum[0], (Integer) 15);
    }

    @Test
    public void connectableObservable() throws InterruptedException {
        String[] result = {""};
        ConnectableObservable<Long> connectableObservable = Observable.interval(200, TimeUnit.MILLISECONDS).publish();
        connectableObservable.subscribe(i -> result[0] += i);
        assertFalse(result[0].equals("01"));
        connectableObservable.connect();
        Thread.sleep(500);
        assertEquals(result[0], "01");
    }

    @Test
    public void singleObservable_test() {
        String[] result = {""};
        Single<String> single = Observable.just("hello").single("s").
                doOnSuccess(i -> result[0] += i).doOnError(error -> {
            throw new RuntimeException(error.getMessage());
        });
        // assertEquals(result[0],"hello");

        single.subscribe();
        assertEquals(result[0], "hello");

    }

    @Test
    public void subjectTest() {
        final Integer[] subscriber1 = {0};
        final Integer[] subscriber2 = {0};

        Observer<Integer> getFirstObserver =
                new Observer<Integer>() {

                    @Override
                    public void onNext(Integer value) {
                        subscriber1[0] += value;
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("error");
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("Subscriber1 completed");
                    }

                    @Override
                    public void onSubscribe(Disposable d) {

                        System.out.println("on subscription completed");

                    }


                };

        Observer<Integer> getSecondObserver = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                System.out.println("on subscription completed");

            }

            @Override
            public void onNext(Integer integer) {
                subscriber2[0] += integer;

            }

            @Override
            public void onError(Throwable e) {
                System.out.println("error");

            }

            @Override
            public void onComplete() {
                System.out.println("Subscriber2 completed");
            }
        };
        PublishSubject<Integer> subject = PublishSubject.create();
        subject.subscribe(getFirstObserver);
        subject.onNext(1);
        subject.onNext(2);
        subject.subscribe(getSecondObserver);
        subject.onNext(3);
        subject.onComplete();
        assertEquals(subscriber1[0] + subscriber2[0], 9);
    }

    @Test
    public void resourceManagement_test() throws InterruptedException {
        String[] result = {""};
        String var;
        var = "dsfsaf";

        Observable dmaskjllsd = Observable.using(() -> {
            Thread.sleep(500);
            return var;
        }, Observable::fromArray, r -> print("dsfaf" + r));
     //   dmaskjllsd.subscribe().dispose();

        Observable<Character> values = Observable.using(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return "Hola";
                    }
                },
                (String r) -> {
                    return Observable.create(o -> {
                        for (Character c : r.toCharArray()) {
                            print(c.toString());
                            o.onNext(c);
                        }
                    });
                },
                r -> print("Disposed: " + r)

        );

        Observable<String> stringObservable = Observable.using(new Callable<String>() {
            @Override
            public String call() {
                print("Call");
                return "hola";
            }
        }, new Function<String, ObservableSource<? extends String>>() {
            @Override
            public ObservableSource<? extends String> apply(String s) throws Exception {
                print("Observable source. ");
                return Observable.fromArray(s);
            }
        }, new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                print("disposed!: " + s);
            }
        });


        stringObservable.subscribe();
    }

    private void print(String textToPrint) {
        System.out.println(textToPrint);

    }
    @Test
    public void returnValue(){
        final String[] result = {""};
        Observable<String> observer = Observable.just("Hello");

        observer.subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                result[0] = s;
            }
        });
        //print(result[0]);
    }

}