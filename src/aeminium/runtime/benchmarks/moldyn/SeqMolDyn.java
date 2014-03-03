package aeminium.runtime.benchmarks.moldyn;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqMolDyn extends MolDyn {

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

	public static void main(String[] args) {
		Benchmark be = new Benchmark(args);
		int iterations = MolDyn.DEFAULT_ITERATIONS;
		if (be.args.length > 0) {
			iterations = Integer.parseInt(be.args[0]);
		}
		int size = MolDyn.DEFAULT_SIZE;
		if (be.args.length > 1) {
			size = Integer.parseInt(be.args[1]);
		}

		while (!be.stop()) {
			be.start();
			SeqMolDyn sim = new SeqMolDyn();
			sim.initialise(iterations, size);
			sim.run();
			be.end();
		}
	}

	public void initialise(int iter, int size) {
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

	}

	public void run() {

		ijk = 0;
		for (lg = 0; lg <= 1; lg++) {
			for (i = 0; i < mm; i++) {
				for (j = 0; j < mm; j++) {
					for (k = 0; k < mm; k++) {
						one[ijk] = new particle((i * a + lg * a * 0.5), (j * a + lg * a * 0.5), (k * a), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, this);
						ijk = ijk + 1;
					}
				}
			}
		}
		for (lg = 1; lg <= 2; lg++) {
			for (i = 0; i < mm; i++) {
				for (j = 0; j < mm; j++) {
					for (k = 0; k < mm; k++) {
						one[ijk] = new particle((i * a + (2 - lg) * a * 0.5), (j * a + (lg - 1) * a * 0.5), (k * a + a * 0.5), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
								this);
						ijk = ijk + 1;
					}
				}
			}
		}

		/* Initialise velocities */

		iseed = 0;
		v1 = 0.0;
		v2 = 0.0;

		randnum = new random(iseed, v1, v2);

		for (i = 0; i < mdsize; i += 2) {
			r = randnum.seed();
			one[i].xvelocity = r * randnum.v1;
			one[i + 1].xvelocity = r * randnum.v2;
		}

		for (i = 0; i < mdsize; i += 2) {
			r = randnum.seed();
			one[i].yvelocity = r * randnum.v1;
			one[i + 1].yvelocity = r * randnum.v2;
		}

		for (i = 0; i < mdsize; i += 2) {
			r = randnum.seed();
			one[i].zvelocity = r * randnum.v1;
			one[i + 1].zvelocity = r * randnum.v2;
		}

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

		for (i = 0; i < mdsize; i++) {
			one[i].xvelocity = one[i].xvelocity * sc;
			one[i].yvelocity = one[i].yvelocity * sc;
			one[i].zvelocity = one[i].zvelocity * sc;
		}
		/* MD simulation */
		move = 0;
		for (move = 0; move < movemx; move++) {// System.out.println("move: "+move);
			for (i = 0; i < mdsize; i++) {
				one[i].domove(side); /* move the particles and update velocities */
			}

			epot = 0.0;
			vir = 0.0;

			for (i = 0; i < mdsize; i++) {
				one[i].force(side, rcoff, mdsize, i); /* compute forces */
			}
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
	}

}