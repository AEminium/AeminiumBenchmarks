package aeminium.runtime.benchmarks.moldyn;

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

public class AeMolDyn extends MolDyn {
	int i, j, k, lg, mdsize, move, mm;

	double l, rcoff, rcoffs, side, sideh, hsq, hsq2, vel;
	double a, r, sum, tscale, sc, ekin, ek, ts, sp;
	double den = 0.83134;
	double tref = 0.722;
	double h = 0.064;
	double vaver, vaverh, rand;
	double etot, temp, pres, rp;
	double u1, u2, v1, v2, s;

	int ijk, npartm, PARTSIZE, iseed, tint;
	int irep = 10;
	int istop = 19;
	int iprint = 10;
	int movemx = 50;

	random randnum;

	int threshold;
	Runtime rt;

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		final int iterations;
		if (be.args.length > 0) {
			iterations = Integer.parseInt(be.args[0]);
		} else {
			iterations = MolDyn.DEFAULT_ITERATIONS;
		}
		final int size;
		if (be.args.length > 1) {
			size = Integer.parseInt(be.args[1]);
		} else {
			size = MolDyn.DEFAULT_SIZE;
		}
		final int threshold;
		if (be.args.length > 2) {
			threshold = Integer.parseInt(be.args[2]);
		} else {
			threshold = MolDyn.DEFAULT_THRESHOLD;
		}

		Runtime rt = Factory.getRuntime();
		rt.addErrorHandler(new PrintErrorHandler());

		while (!be.stop()) {
			be.start();
			rt.init();
			Task tmain = rt.createNonBlockingTask(new Body() {
				@Override
				public void execute(Runtime rt, Task current) throws Exception {
					AeMolDyn sim = new AeMolDyn();
					sim.initialise(iterations, size, threshold, rt);
					sim.run();
				}
			}, Hints.LARGE);
			rt.schedule(tmain, Runtime.NO_PARENT, Runtime.NO_DEPS);
			rt.shutdown();
			be.end();
		}
	}

	public void initialise(int iter, int size, int threshold, Runtime rt) {
		movemx = iter;
		mm = size;
		PARTSIZE = mm * mm * mm * 4;
		mdsize = PARTSIZE;
		one = new particle[mdsize];
		l = LENGTH;

		side = Math.pow((mdsize / den), 0.3333333);
		rcoff = mm / 4.0;

		a = side / mm;
		sideh = side * 0.5;
		hsq = h * h;
		hsq2 = hsq * 0.5;
		npartm = mdsize - 1;
		rcoffs = rcoff * rcoff;
		tscale = 16.0 / (1.0 * mdsize - 1.0);
		vaver = 1.13 * Math.sqrt(tref / 24.0);
		vaverh = vaver * h;

		iseed = 0;
		v1 = 0.0;
		v2 = 0.0;

		randnum = new random(iseed, v1, v2);

		this.threshold = threshold;
		this.rt = rt;

	}

	public void run() {

		/* Particle Generation */

		final AeMolDyn store = this;
		final Task gen = ForTask.createFor(rt, new Range(0, mm), new ForBody<Integer>() {

			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				int ijk, lg, j, k;
				for (lg = 0; lg <= 1; lg++) {
					for (j = 0; j < mm; j++) {
						for (k = 0; k < mm; k++) {
							ijk = k + j * mm + i * mm * mm + lg * mm * mm * mm;
							one[ijk] = new particle((i * a + lg * a * 0.5), (j * a + lg * a * 0.5), (k * a), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, store);
						}
					}
				}
				for (lg = 1; lg <= 2; lg++) {
					for (j = 0; j < mm; j++) {
						for (k = 0; k < mm; k++) {
							ijk = k + j * mm + i * mm * mm + (lg + 1) * mm * mm * mm;
							one[ijk] = new particle((i * a + (2 - lg) * a * 0.5), (j * a + (lg - 1) * a * 0.5), (k * a + a * 0.5), 0.0, 0.0, 0.0, 0.0, 0.0,
									0.0, store);
						}
					}
				}
			}

		}, Runtime.NO_HINTS);
		rt.schedule(gen, Runtime.NO_PARENT, Runtime.NO_DEPS);

		Task xvel = ForTask.createFor(rt, new Range(0, mdsize / 2), new ForBody<Integer>() {

			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				double r = randnum.seed();
				r = randnum.seed();
				one[2 * i].xvelocity = r * randnum.v1;
				one[2 * i + 1].xvelocity = r * randnum.v2;
			}

		}, Runtime.NO_HINTS);
		rt.schedule(xvel, Runtime.NO_PARENT, Arrays.asList(gen));
		Task yvel = ForTask.createFor(rt, new Range(0, mdsize / 2), new ForBody<Integer>() {

			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				double r = randnum.seed();
				one[2 * i].yvelocity = r * randnum.v1;
				one[2 * i + 1].yvelocity = r * randnum.v2;
			}

		}, Runtime.NO_HINTS);
		rt.schedule(yvel, Runtime.NO_PARENT, Arrays.asList(gen));
		Task zvel = ForTask.createFor(rt, new Range(0, mdsize / 2), new ForBody<Integer>() {

			@Override
			public void iterate(Integer i, Runtime rt, Task current) {
				double r = randnum.seed();
				one[2 * i].zvelocity = r * randnum.v1;
				one[2 * i + 1].zvelocity = r * randnum.v2;
			}

		}, Runtime.NO_HINTS);
		rt.schedule(zvel, Runtime.NO_PARENT, Arrays.asList(gen));

		/* velocity scaling */

		Task other_vel = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws Exception {
				/* velocity scaling */

				ekin = 0.0;
				sp = 0.0;

				for (i = 0; i < mdsize; i++) {
					sp = sp + one[i].xvelocity;
				}
				sp = sp / mdsize;

				for (i = 0; i < mdsize; i++) {
					one[i].xvelocity = one[i].xvelocity - sp;
					ekin = ekin + one[i].xvelocity * one[i].xvelocity;
				}

				sp = 0.0;
				for (i = 0; i < mdsize; i++) {
					sp = sp + one[i].yvelocity;
				}
				sp = sp / mdsize;

				for (i = 0; i < mdsize; i++) {
					one[i].yvelocity = one[i].yvelocity - sp;
					ekin = ekin + one[i].yvelocity * one[i].yvelocity;
				}

				sp = 0.0;
				for (i = 0; i < mdsize; i++) {
					sp = sp + one[i].zvelocity;
				}
				sp = sp / mdsize;

				for (i = 0; i < mdsize; i++) {
					one[i].zvelocity = one[i].zvelocity - sp;
					ekin = ekin + one[i].zvelocity * one[i].zvelocity;
				}

				ts = tscale * ekin;
				sc = h * Math.sqrt(tref / ts);

				Task up = ForTask.createFor(rt, new Range(0, mdsize), new ForBody<Integer>() {

					@Override
					public void iterate(Integer i, Runtime rt, Task current) {
						one[i].xvelocity = one[i].xvelocity * sc;
						one[i].yvelocity = one[i].yvelocity * sc;
						one[i].zvelocity = one[i].zvelocity * sc;
					}

				}, Runtime.NO_HINTS);
				rt.schedule(up, current, Runtime.NO_DEPS);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(other_vel, Runtime.NO_PARENT, Arrays.asList(xvel, yvel, zvel));

		/* MD simulation */

		Task previous = other_vel;
		move = 0;
		for (move = 0; move < movemx; move++) {
			epot = 0.0;
			vir = 0.0;

			/* move the particles and update velocities */
			Task movet = ForTask.createFor(rt, new Range(0, mdsize), new ForBody<Integer>() {

				@Override
				public void iterate(Integer i, Runtime rt, Task current) {
					one[i].domove(side);
				}

			}, Runtime.NO_HINTS);
			rt.schedule(movet, Runtime.NO_PARENT, Arrays.asList(previous));

			/* compute forces */
			Task force = ForTask.createFor(rt, new Range(0, mdsize), new ForBody<Integer>() {

				@Override
				public void iterate(Integer i, Runtime rt, Task current) {
					one[i].force(side, rcoff, mdsize, i);
				}

			}, Runtime.NO_HINTS);
			rt.schedule(force, Runtime.NO_PARENT, Arrays.asList(movet));

			/* update force arrays */

			Task forceupdate = rt.createNonBlockingTask(new Body() {

				@Override
				public void execute(Runtime rt, Task current) throws Exception {
					sum = 0.0;

					for (i = 0; i < mdsize; i++) {
						sum = sum + one[i].mkekin(hsq2); /*
														 * scale forces, update
														 * velocities
														 */
					}

					ekin = sum / hsq;

					vel = 0.0;
					count = 0.0;

					for (i = 0; i < mdsize; i++) {
						vel = vel + one[i].velavg(vaverh, h); /* average velocity */
					}

					vel = vel / h;

					/* tmeperature scale if required */

					if ((move < istop) && (((move + 1) % irep) == 0)) {
						sc = Math.sqrt(tref / (tscale * ekin));
						for (i = 0; i < mdsize; i++) {
							one[i].dscal(sc, 1);
						}
						ekin = tref / tscale;
					}

					/* sum to get full potential energy and virial */

					if (((move + 1) % iprint) == 0) {
						ek = 24.0 * ekin;
						epot = 4.0 * epot;
						etot = ek + epot;
						temp = tscale * ekin;
						pres = den * 16.0 * (ekin - vir) / mdsize;
						vel = vel / mdsize;
						rp = (count / mdsize) * 100.0;
					}
				}
			}, Hints.LARGE);
			rt.schedule(forceupdate, Runtime.NO_PARENT, Arrays.asList(force));
			previous = forceupdate;
		}
	}
}
