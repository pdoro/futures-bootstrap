package com.pdomingo;

import java.util.concurrent.CompletableFuture;

public class FutureUtils {

	@FunctionalInterface
	public interface Function3<A,B,C, R> {
		R map(A a, B b, C c);
	}

	@FunctionalInterface
	public interface Function4<A,B,C,D, R> {
		R map(A a, B b, C c, D d);
	}

	@FunctionalInterface
	public interface Function5<A,B,C,D,E, R> {
		R map(A a, B b, C c, D d, E e);
	}

	public static <T, A, B, C> CompletableFuture<T> allOf(
			CompletableFuture<A> cfA,
			CompletableFuture<B> cfB,
			CompletableFuture<C> cfC,
			Function3<A,B,C, T> function3) {

		return CompletableFuture.allOf(cfA, cfB, cfC)
				.thenApplyAsync(dummy -> {
					A valA = cfA.join();
					B valB = cfB.join();
					C valC = cfC.join();

					return function3.map(valA, valB, valC);
				});
	}

	public static <T, A, B, C, D> CompletableFuture<T> allOf(
			CompletableFuture<A> cfA,
			CompletableFuture<B> cfB,
			CompletableFuture<C> cfC,
			CompletableFuture<D> cfD,
			Function4<A,B,C,D, T> function4) {

		return CompletableFuture.allOf(cfA, cfB, cfC, cfD)
				.thenApplyAsync(dummy -> {
					A valA = cfA.join();
					B valB = cfB.join();
					C valC = cfC.join();
					D valD = cfD.join();

					return function4.map(valA, valB, valC, valD);
				});
	}

}
