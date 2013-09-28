package aeminium.runtime.benchmarks.nbody;

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

public class AeNBody {
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
		final int advance_t;
		if (be.args.length > 2) {
			advance_t = Integer.parseInt(be.args[2]);
		} else {
			advance_t = NBody.ADVANCE_THRESHOLD;
		}

		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());
		
		final AeNBodySystem bodies = new AeNBodySystem(NBody.generateRandomBodies(size, 1L), rt);
		
		if (be.verbose)
			System.out.printf("%.9f\n", bodies.energy());
		
		be.start();
		rt.init();
		
		Task t = ForTask.createFor(rt, new Range(n), new ForBody<Integer>() {
			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				bodies.advance(0.01, advance_t, current);
			}
		}, Hints.NO_DEPENDENTS);
		rt.schedule(t, Runtime.NO_PARENT, Runtime.NO_DEPS);
		
		rt.shutdown();
		be.end();
		
		if (be.verbose)
			System.out.printf("%.9f\n", bodies.energy());
			
	}
}


class AeNBodySystem extends NBodySystem {
	
	final protected Runtime runtime;

	public AeNBodySystem(NBody[] data, Runtime rt) {
		super(data);
		runtime = rt;
	}

	public void advance(final double dt, final int advance_t, Task parent) {
		Body b = new AeAdvanceBody(runtime, bodies, 0, bodies.length, dt, advance_t);
		Task advance = runtime.createNonBlockingTask(b, (short) (Hints.RECURSION & Hints.LARGE));
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
		runtime.schedule(apply, parent, Runtime.NO_DEPS);
	}
}


class AeAdvanceBody implements Body {
	final protected Runtime runtime;
	final double dt;
	final NBody[] bodies;
	final int start;
	final int end;
	final int threshold;
	
	public AeAdvanceBody(Runtime rt, NBody[] bodies, int st, int end, double dt, int advance_t) {
		runtime = rt;
		this.dt = dt;
		this.threshold = advance_t;
		this.start = st;
		this.end = end;
		this.bodies = bodies;
	}

	@Override
	public void execute(Runtime rt, Task current) throws Exception {
		if (Benchmark.useThreshold ? end-start < threshold : !rt.parallelize(current)) {
			advance();
		} else {
			int mid = (end - start) / 4 + start;
			
			Body b = new AeAdvanceBody(runtime, bodies, start, mid, dt, threshold);
			Task t1 = runtime.createNonBlockingTask(b, (short) (Hints.RECURSION & Hints.LARGE));
			runtime.schedule(t1, current, Runtime.NO_DEPS);
			
			Body b2 = new AeAdvanceBody(runtime, bodies, mid, end, dt, threshold);
			Task t2 = runtime.createNonBlockingTask(b2, (short) (Hints.RECURSION & Hints.LARGE));
			runtime.schedule(t2, current, Runtime.NO_DEPS);
		}
	}
	
	private void advance() {
		for (int i = start; i < end; ++i) {
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
				
				synchronized (body) {
					body.vx += dx * iBody.mass * mag;
					body.vy += dy * iBody.mass * mag;
					body.vz += dz * iBody.mass * mag;
				}
			}
		}
	}
}
