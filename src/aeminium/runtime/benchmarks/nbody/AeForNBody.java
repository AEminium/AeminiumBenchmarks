package aeminium.runtime.benchmarks.nbody;

import java.util.Arrays;

import aeminium.runtime.Body;
import aeminium.runtime.Hints;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.benchmarks.helpers.Benchmark;
import aeminium.runtime.helpers.loops.ForBody;
import aeminium.runtime.helpers.loops.ForTask;
import aeminium.runtime.helpers.loops.Range;
import aeminium.runtime.implementations.Factory;
import aeminium.utils.error.PrintErrorHandler;

public class AeForNBody {
	public static void main(String[] args) {

		Benchmark be = new Benchmark(args);
		final int n;
		if (be.args.length > 0) {
			n = Integer.parseInt(be.args[0]);
		} else {
			n = NBody.DEFAULT_ITERATIONS;
		}
		final int size;
		if (be.args.length > 1) {
			size = Integer.parseInt(be.args[1]);
		} else {
			size = NBody.DEFAULT_SIZE;
		}

		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());

		while (!be.stop()) {
			final AeForNBodySystem bodies = new AeForNBodySystem(NBody.generateRandomBodies(size, 1L), rt);
			if (be.verbose) System.out.printf("%.9f\n", bodies.energy());
			be.start();
			rt.init();
			Task tmain = rt.createNonBlockingTask(new Body() {
				@Override
				public void execute(Runtime rt, Task current) throws Exception {
					Task t = ForTask.createFor(rt, new Range(n), new ForBody<Integer>() {
						@Override
						public void iterate(Integer i, Runtime rt, Task current) {
							bodies.advance(0.01, current);
						}
					}, Hints.NO_DEPENDENTS);
					rt.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
				}
			}, Hints.LOOPS);
			rt.schedule(tmain, Runtime.NO_PARENT, Runtime.NO_DEPS);
			rt.shutdown();
			be.end();

			if (be.verbose) System.out.printf("%.9f\n", bodies.energy());
		}
	}
}

class AeForNBodySystem extends NBodySystem {

	protected Runtime runtime;

	public AeForNBodySystem(NBody[] data, Runtime rt) {
		super(data);
		runtime = rt;
	}

	public void advance(final double dt, Task parent) {
		Task advance = ForTask.createFor(runtime, new Range(bodies.length), new ForBody<Integer>() {
			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				NBody iBody = bodies[i];
				for (int j = i + 1; j < bodies.length; ++j) {
					final NBody body = bodies[j];
					double dx = iBody.x - body.x;
					double dy = iBody.y - body.y;
					double dz = iBody.z - body.z;

					double dSquared = dx * dx + dy * dy + dz * dz;
					double distance = Math.sqrt(dSquared);
					double mag = dt / (dSquared * distance);

					iBody.vx -= dx * body.mass * mag;
					iBody.vy -= dy * body.mass * mag;
					iBody.vz -= dz * body.mass * mag;

					body.vx += dx * iBody.mass * mag;
					body.vy += dy * iBody.mass * mag;
					body.vz += dz * iBody.mass * mag;
				}
			}
		}, Hints.LARGE);
		runtime.schedule(advance, parent, Runtime.NO_DEPS);

		Task apply = ForTask.createFor(runtime, new Range(bodies.length), new ForBody<Integer>() {
			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				NBody body = bodies[i];
				body.x += dt * body.vx;
				body.y += dt * body.vy;
				body.z += dt * body.vz;
			}
		}, Hints.LARGE);
		runtime.schedule(apply, parent, Arrays.asList(advance));
	}
}
