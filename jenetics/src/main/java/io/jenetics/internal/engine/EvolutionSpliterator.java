/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmail.com)
 */
package io.jenetics.internal.engine;

import static java.util.Objects.requireNonNull;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.jenetics.Gene;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStart;

/**
 * @author <a href="mailto:franz.wilhelmstoetter@gmail.com">Franz Wilhelmstötter</a>
 * @since 3.0
 * @version 4.1
 */
public final class EvolutionSpliterator<
	G extends Gene<?, G>,
	C extends Comparable<? super C>
>
	implements Spliterator<EvolutionResult<G, C>>
{

	private final Supplier<EvolutionStart<G, C>> _start;
	private final Function<? super EvolutionStart<G, C>, EvolutionResult<G, C>> _evolution;

	private long _estimate;
	private EvolutionStart<G, C> _next = null;

	private EvolutionSpliterator(
		final Supplier<EvolutionStart<G, C>> start,
		final Function<? super EvolutionStart<G, C>, EvolutionResult<G, C>> evolution,
		final long estimate
	) {
		_evolution = requireNonNull(evolution);
		_start = requireNonNull(start);
		_estimate = estimate;
	}

	public EvolutionSpliterator(
		final Supplier<EvolutionStart<G, C>> start,
		final Function<? super EvolutionStart<G, C>, EvolutionResult<G, C>> evolution
	) {
		this(start, evolution, Long.MAX_VALUE);
	}

	@Override
	public boolean
	tryAdvance(final Consumer<? super EvolutionResult<G, C>> action) {
		if (_next == null) {
			_next = _start.get();
		}

		final EvolutionResult<G, C> result = _evolution.apply(_next);
		action.accept(result);
		_next = result.next();
		return true;
	}

	@Override
	public Spliterator<EvolutionResult<G, C>> trySplit() {
		return _estimate > 0
			? new EvolutionSpliterator<>(_start, _evolution, _estimate >>>= 1)
			: null;
	}

	@Override
	public long estimateSize() {
		return _estimate;
	}

	@Override
	public int characteristics() {
		return NONNULL | IMMUTABLE | ORDERED;
	}

}
