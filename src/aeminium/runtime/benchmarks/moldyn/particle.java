package aeminium.runtime.benchmarks.moldyn;

class particle {
	public double xcoord, ycoord, zcoord;
	public double xvelocity, yvelocity, zvelocity;
	public double xforce, yforce, zforce;
	MolDyn store;

	public particle(double xcoord, double ycoord, double zcoord, double xvelocity, double yvelocity, double zvelocity, double xforce, double yforce,
			double zforce, MolDyn store) {
		this.xcoord = xcoord;
		this.ycoord = ycoord;
		this.zcoord = zcoord;
		this.xvelocity = xvelocity;
		this.yvelocity = yvelocity;
		this.zvelocity = zvelocity;
		this.xforce = xforce;
		this.yforce = yforce;
		this.zforce = zforce;
		this.store = store;
	}

	public void domove(double side) {
		xcoord = xcoord + xvelocity + xforce;
		ycoord = ycoord + yvelocity + yforce;
		zcoord = zcoord + zvelocity + zforce;

		if (xcoord < 0) {
			xcoord = xcoord + side;
		}
		if (xcoord > side) {
			xcoord = xcoord - side;
		}
		if (ycoord < 0) {
			ycoord = ycoord + side;
		}
		if (ycoord > side) {
			ycoord = ycoord - side;
		}
		if (zcoord < 0) {
			zcoord = zcoord + side;
		}
		if (zcoord > side) {
			zcoord = zcoord - side;
		}

		xvelocity = xvelocity + xforce;
		yvelocity = yvelocity + yforce;
		zvelocity = zvelocity + zforce;

		xforce = 0.0;
		yforce = 0.0;
		zforce = 0.0;
	}

	public void force(double side, double rcoff, int mdsize, int x) {
		double sideh;
		double rcoffs;

		double xx, yy, zz, xi, yi, zi, fxi, fyi, fzi;
		double rd, rrd, rrd2, rrd3, rrd4, rrd6, rrd7, r148;
		double forcex, forcey, forcez;

		int i;

		sideh = 0.5 * side;
		rcoffs = rcoff * rcoff;

		xi = xcoord;
		yi = ycoord;
		zi = zcoord;
		fxi = 0.0;
		fyi = 0.0;
		fzi = 0.0;

		for (i = x + 1; i < mdsize; i++) {
			xx = xi - store.one[i].xcoord;
			yy = yi - store.one[i].ycoord;
			zz = zi - store.one[i].zcoord;

			if (xx < (-sideh)) {
				xx = xx + side;
			}
			if (xx > (sideh)) {
				xx = xx - side;
			}
			if (yy < (-sideh)) {
				yy = yy + side;
			}
			if (yy > (sideh)) {
				yy = yy - side;
			}
			if (zz < (-sideh)) {
				zz = zz + side;
			}
			if (zz > (sideh)) {
				zz = zz - side;
			}

			rd = xx * xx + yy * yy + zz * zz;

			if (rd <= rcoffs) {
				rrd = 1.0 / rd;
				rrd2 = rrd * rrd;
				rrd3 = rrd2 * rrd;
				rrd4 = rrd2 * rrd2;
				rrd6 = rrd2 * rrd4;
				rrd7 = rrd6 * rrd;
				store.epot = store.epot + (rrd6 - rrd3);
				r148 = rrd7 - 0.5 * rrd4;
				store.vir = store.vir - rd * r148;
				forcex = xx * r148;
				fxi = fxi + forcex;
				store.one[i].xforce = store.one[i].xforce - forcex;
				forcey = yy * r148;
				fyi = fyi + forcey;
				store.one[i].yforce = store.one[i].yforce - forcey;
				forcez = zz * r148;
				fzi = fzi + forcez;
				store.one[i].zforce = store.one[i].zforce - forcez;
				store.interactions++;
			}
		}

		xforce = xforce + fxi;
		yforce = yforce + fyi;
		zforce = zforce + fzi;
	}

	public double mkekin(double hsq2) {
		double sumt = 0.0;

		xforce = xforce * hsq2;
		yforce = yforce * hsq2;
		zforce = zforce * hsq2;

		xvelocity = xvelocity + xforce;
		yvelocity = yvelocity + yforce;
		zvelocity = zvelocity + zforce;

		sumt = (xvelocity * xvelocity) + (yvelocity * yvelocity) + (zvelocity * zvelocity);
		return sumt;
	}

	public double velavg(double vaverh, double h) {
		double velt;
		double sq;

		sq = Math.sqrt(xvelocity * xvelocity + yvelocity * yvelocity + zvelocity * zvelocity);

		if (sq > vaverh) {
			store.count = store.count + 1.0;
		}

		velt = sq;
		return velt;
	}

	public void dscal(double sc, int incx) {
		xvelocity = xvelocity * sc;
		yvelocity = yvelocity * sc;
		zvelocity = zvelocity * sc;
	}
}