package aeminium.runtime.benchmarks.raytracer;

/**************************************************************************
 *                                                                         *
 *             Java Grande Forum Benchmark Suite - Version 2.0             *
 *                                                                         *
 *                            produced by                                  *
 *                                                                         *
 *                  Java Grande Benchmarking Project                       *
 *                                                                         *
 *                                at                                       *
 *                                                                         *
 *                Edinburgh Parallel Computing Centre                      *
 *                                                                         *
 *                email: epcc-javagrande@epcc.ed.ac.uk                     *
 *                                                                         *
 *                 Original version of this code by                        *
 *            Florian Doyon (Florian.Doyon@sophia.inria.fr)                *
 *              and  Wilfried Klauser (wklauser@acm.org)                   *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 1999.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

import java.util.Vector;

@SuppressWarnings("serial")
public class Scene implements java.io.Serializable {
	public final Vector<Light> lights;
	public final Vector<Primitive> objects;
	private View view;

	public Scene() {
		this.lights = new Vector<Light>();
		this.objects = new Vector<Primitive>();
	}

	public void addLight(Light l) {
		this.lights.addElement(l);
	}

	public void addObject(Primitive object) {
		this.objects.addElement(object);
	}

	public void setView(View view) {
		this.view = view;
	}

	public View getView() {
		return this.view;
	}

	public Light getLight(int number) {
		return this.lights.elementAt(number);
	}

	public Primitive getObject(int number) {
		return objects.elementAt(number);
	}

	public int getLights() {
		return this.lights.size();
	}

	public int getObjects() {
		return this.objects.size();
	}

	public void setObject(Primitive object, int pos) {
		this.objects.setElementAt(object, pos);
	}
}
