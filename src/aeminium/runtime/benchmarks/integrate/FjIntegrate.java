/**
 * Copyright (c) 2010-11 The AEminium Project (see AUTHORS file)
 * 
 * This file is part of Plaid Programming Language.
 *
 * Plaid Programming Language is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 *  Plaid Programming Language is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plaid Programming Language.  If not, see <http://www.gnu.org/licenses/>.
 */

package aeminium.runtime.benchmarks.integrate;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import aeminium.runtime.benchmarks.helpers.Benchmark;

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
* published by the Free Software Foundation.
*
* This code is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

/*
 * @test
 * @bug 6865571
 * @summary Numerical Integration using fork/join
 * @run main Integrate reps=1 forkPolicy=dynamic
 * @run main Integrate reps=1 forkPolicy=serial
 * @run main Integrate reps=1 forkPolicy=fork
 */

/**
 * Sample program using Gaussian Quadrature for numerical integration.
 * This version uses a simplified hardwired function.  Inspired by a
 * <A href="http://www.cs.uga.edu/~dkl/filaments/dist.html">
 * Filaments</A> demo program.
 */
public final class FjIntegrate {

    public static void main(String[] args) throws Exception {     
        Benchmark be = new Benchmark(args);
        int procs = Runtime.getRuntime().availableProcessors();
        
        be.start();
        ForkJoinPool g = new ForkJoinPool(procs);
		double a = DQuad.computeArea(g, Integrate.start, Integrate.end);
		g.shutdown();
		be.end();
		if (be.verbose) {
			System.out.println("Integral: " + a);
		}
    }


    // Sequential version
    public static final class SQuad extends RecursiveAction {
		private static final long serialVersionUID = 2280172350754386752L;

		static double computeArea(ForkJoinPool pool, double l, double r) {
            SQuad q = new SQuad(l, r, 0);
            pool.invoke(q);
            return q.area;
        }

        final double left;       // lower bound
        final double right;      // upper bound
        double area;

        public SQuad(double l, double r, double a) {
            this.left = l; this.right = r; this.area = a;
        }

        public final void compute() {
            double l = left;
            double r = right;
            
            area = recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r, area);
        }

        static final double recEval(double l, double r, double fl,
                                    double fr, double a) {
            double h = (r - l) * 0.5;
            double c = l + h;
            double fc = (c * c + 1.0) * c;
            double hh = h * 0.5;
            double al = (fl + fc) * hh;
            double ar = (fr + fc) * hh;
            double alr = al + ar;
            if (Math.abs(alr - a) <= Integrate.errorTolerance)
                return alr;
            else
                return recEval(c, r, fc, fr, ar) + recEval(l, c, fl, fc, al);
        }

    }

    //....................................

    // ForkJoin version
    public static final class FQuad extends RecursiveAction {
		private static final long serialVersionUID = -6277723543198168418L;

		static double computeArea(ForkJoinPool pool, double l, double r) {
            FQuad q = new FQuad(l, r, 0);
            pool.invoke(q);
            return q.area;
        }

        final double left;       // lower bound
        final double right;      // upper bound
        double area;

        public FQuad(double l, double r, double a) {
            this.left = l; this.right = r; this.area = a;
        }

        public final void compute() {
            double l = left;
            double r = right;
            area = recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r, area);
        }

        public static final double recEval(double l, double r, double fl,
                                    double fr, double a) {
            double h = (r - l) * 0.5;
            double c = l + h;
            double fc = (c * c + 1.0) * c;
            double hh = h * 0.5;
            double al = (fl + fc) * hh;
            double ar = (fr + fc) * hh;
            double alr = al + ar;
            if (Math.abs(alr - a) <= Integrate.errorTolerance)
                return alr;
            if (Math.abs(alr - a) <= Integrate.threshold) {
				// Threshold for task
				return SQuad.recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r, a);
			}
            FQuad q = new FQuad(l, c, al);
            q.fork();
            ar = recEval(c, r, fc, fr, ar);
            if (!q.tryUnfork()) {
            	q.quietlyJoin();
                return ar + q.area;
            }
            return ar + recEval(l, c, fl, fc, al);
        }

    }

    // ...........................

    // Version using on-demand Fork
    public static final class DQuad extends RecursiveAction {
		private static final long serialVersionUID = -4117817369633015698L;

		static double computeArea(ForkJoinPool pool, double l, double r) {
            DQuad q = new DQuad(l, r, 0);
            pool.invoke(q);
            return q.area;
        }

        final double left;       // lower bound
        final double right;      // upper bound
        double area;

        DQuad(double l, double r, double a) {
            this.left = l; this.right = r; this.area = a;
        }

        public final void compute() {
            double l = left;
            double r = right;
            area = recEval(l, r, (l * l + 1.0) * l, (r * r + 1.0) * r, area);
        }

        static final double recEval(double l, double r, double fl,
                                     double fr, double a) {
             double h = (r - l) * 0.5;
             double c = l + h;
             double fc = (c * c + 1.0) * c;
             double hh = h * 0.5;
             double al = (fl + fc) * hh;
             double ar = (fr + fc) * hh;
             double alr = al + ar;
             if (Math.abs(alr - a) <= Integrate.errorTolerance)
                 return alr;
             DQuad q = null;
             if (getSurplusQueuedTaskCount() <= 3)
                 (q = new DQuad(l, c, al)).fork();
             ar = recEval(c, r, fc, fr, ar);
             if (q != null && !q.tryUnfork()) {
                 q.quietlyJoin();
                 return ar + q.area;
             }
        	 return ar + recEval(l, c, fl, fc, al);
        }
    }
}