package com.pdomingo;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class App  {

    public static void main( String[] args ) throws Exception {
        staticMethods();
        callbacks();
        asynchronousCallback();
        getVsJoin();
        chainingCompletableFuture();
        exceptions();
    }

    public static void staticMethods() throws ExecutionException, InterruptedException {

        Instant start, stop;

        // CompletableFuture that does not return anything.
        // The execution is started immediately and scheduled in a jvm thread
        CompletableFuture<Void> voidCF = CompletableFuture.runAsync(() -> {
            System.out.println("This line does not return nothing and is executed asynchronously");
        });

        // CompletableFuture that returns a type.
        // The execution is started immediately and scheduled in a jvm thread
        CompletableFuture<String> stringCF = CompletableFuture.supplyAsync(() -> {
            System.out.println("This supplies a String asynchronously");
            return "Hello world";
        });

        // CompletableFuture with type preset
        CompletableFuture.completedFuture(1);

        // Will execute all the operations in parallel
        // The completable future will yield in 4 sec
        start = Instant.now();
        CompletableFuture<Void> allOf = CompletableFuture.allOf(
                CompletableFuture.runAsync(() -> slowVoidTask(1000)),
                CompletableFuture.runAsync(() -> slowVoidTask(2000)),
                CompletableFuture.runAsync(() -> slowVoidTask(3000))
        );
        allOf.get();
        stop = Instant.now();
        System.out.println("All the operations finished in " + Duration.between(start, stop));

        // Will execute all the operations in parallel
        // The completable future will yield in 4 sec
        start = Instant.now();
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(
                CompletableFuture.runAsync(() -> slowVoidTask(1000)),
                CompletableFuture.runAsync(() -> slowVoidTask(2000)),
                CompletableFuture.runAsync(() -> slowVoidTask(3000))
        );
        anyOf.get();
        stop = Instant.now();
        System.out.println("One operation finished in " + Duration.between(start, stop));
    }

    private static void callbacks() {

        CompletableFuture.completedFuture(1)
                .thenAccept(value -> {
                    System.out.println("Consumes value " + value + " and don't return anything");
                });

        CompletableFuture.completedFuture(1)
                .thenRun(() -> {
                    System.out.println("Method thenRun does not have access to the computed value");
                });

        CompletableFuture.completedFuture(1)
                .thenApply(value -> {
                    System.out.println("Consumes value " + value + " and return something else");
                    return "Values is " + value;
                });
    }

    private static void asynchronousCallback() throws ExecutionException, InterruptedException {

        System.out.println("------ Asynchronous callbacks ------");

        CompletableFuture.supplyAsync(() -> {
            System.out.println("> supplyAsync executed from " + Thread.currentThread());
            return 1;
        }).thenAcceptAsync(value -> {
            System.out.println("> thenAcceptAsync executed from " + Thread.currentThread());
            System.out.println("Consumes value " + value + " and don't return anything");
        }).get();

        separate();

        CompletableFuture.supplyAsync(() -> {
            System.out.println("> supplyAsync executed from " + Thread.currentThread());
            return 1;
        }).thenRunAsync(() -> {
            System.out.println("> thenRunAsync " + Thread.currentThread());
            System.out.println("Method thenRunAsync does not have access to the computed value");
        }).get();

        separate();

        CompletableFuture.supplyAsync(() -> {
            System.out.println("> supplyAsync executed from " + Thread.currentThread());
            return 1;
        }).thenApplyAsync(value -> {
            System.out.println("> thenApplyAsync executed from " + Thread.currentThread());
            System.out.println("Consumes value " + value + " and return something else");
            return "Values is " + value;
        }).get();

        separate();
    }

    private static void getVsJoin() {

        CompletableFuture<Integer> dummyCF1 = CompletableFuture.completedFuture(1);

        try {
            // May throw checked exceptions
            Integer value = dummyCF1.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        CompletableFuture<Integer> dummyCF2 = CompletableFuture.completedFuture(1);
        // May throw CancellationException or CompletionException. Both RuntimeExceptions
        Integer value = dummyCF2.join();
    }

    private static void chainingCompletableFuture() {

        // ThenCompose
        // Future2 depends upon Future1
        future1().thenCompose(value -> future2(value));

        // ThenCombine
        // Future1 and Future2 are independent
        // A second param is passed here and will be called then both futures finish
        future1().thenCombine(
                future2("Again"),
                (future1Value, future2Value) -> future1Value + " " + future2Value
        );
    }

    private static CompletableFuture<String> future1() { return CompletableFuture.completedFuture("World"); }
    private static CompletableFuture<String> future2(String str) { return CompletableFuture.completedFuture("Hello " + str); }

    private static void exceptions() {

        CompletableFuture<Object> failingFuture = CompletableFuture.supplyAsync(() -> {
            throw new RuntimeException("error!");
        });

        failingFuture.exceptionally(ex -> {
            System.out.println("The exceptions is of type " + ex.getClass().getSimpleName());
            return null;
        });

        failingFuture.copy()
                .exceptionallyCompose(ex -> {
                    System.out.println("The exceptions is of type " + ex.getClass().getSimpleName());
                    return fallbackCompletable();
                });

        failingFuture.copy()
                .handle((value, ex) -> {
                    System.out.println("The value is " + value);
                    System.out.println("The exceptions is " + ex);

                    return value;
                });
    }

    private static void slowVoidTask(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static int slowYieldTask(int millis) {
        try {
            Thread.sleep(millis);
            return 1;
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static CompletableFuture<Object> fallbackCompletable() {
        return CompletableFuture.supplyAsync(Object::new);
    }

    private static void separate() {
        System.out.println("-".repeat(36));
    }
}
