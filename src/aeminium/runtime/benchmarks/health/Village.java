package aeminium.runtime.benchmarks.health;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Village {
	public int id;
	public int level;
	public int seed;
	public List<Village> children = new ArrayList<Village>();
	public Village root;
	public List<Patient> population = new ArrayList<Patient>();
	public Hosp hosp = new Hosp();
	
	public void tick() {
		this.check_patients_inside();
		this.check_patients_assess();
		this.check_patients_waiting();
		this.check_patients_realloc();
		this.check_patients_population();
	}


	public void check_patients_population() {
		List<Patient> rem = new ArrayList<Patient>(); 
		for (Patient p : this.population) {
			Random r = new Random(p.seed);
			if (r.nextDouble() < Health.sim_get_sick_p) {
				rem.add(p);
				put_in_hosp(p);
			}
		}
		this.population.remove(rem);
	}

	public void check_patients_inside() {
		List<Patient> rem = new ArrayList<Patient>(); 
		for (Patient p : this.hosp.inside) {
			p.time_left--;
			if (p.time_left == 0) {
				this.hosp.free_personnel++;
				rem.add(p);
				this.population.add(p);
			}
		}
		this.hosp.inside.remove(rem);
	}

	public void check_patients_assess() {
		List<Patient> rem = new ArrayList<Patient>(); 
		for (Patient p : this.hosp.assess) {
			p.time_left--;
			if (p.time_left == 0) {
				Random random = new Random(p.seed);
				if (random.nextDouble() < Health.sim_convalescence_p) {
					if (random.nextDouble() > Health.sim_realloc_p || this.level == Health.sim_level) {
						rem.add(p);
						this.hosp.inside.add(p);
						p.time_left = Health.sim_convalescence_time;
						p.time += p.time_left;
					} else {
						this.hosp.free_personnel++;
						rem.add(p);
						this.root.hosp.assess.add(p);
					}
				} else {
					this.hosp.free_personnel++;
					rem.add(p);
					this.population.add(p);
				}
			}
		}	
		this.hosp.assess.remove(rem);
	}

	public void check_patients_waiting() {
		List<Patient> rem = new ArrayList<Patient>(); 
		for (Patient p : this.hosp.waiting) {
			if (this.hosp.free_personnel > 0) {
				this.hosp.free_personnel--;
				p.time_left = Health.sim_assess_time;
				p.time += p.time_left;
				rem.add(p);
				this.hosp.assess.add(p);
			} else {
				p.time++;
			}
		}
		this.hosp.waiting.remove(rem);
	}

	public void check_patients_realloc() {
		Patient s = null;
		for (Patient p : this.hosp.realloc) {
			if (s == null || p.id < s.id) s = p;
		}
		if (s != null) {
			this.hosp.realloc.remove(s);
			put_in_hosp(s);
		}
	}

	public void put_in_hosp(Patient p) {
		Hosp hosp = this.hosp;
		p.hosps_visited++;
		if (hosp.free_personnel > 0) {
			hosp.free_personnel--;
			hosp.assess.add(p);
			p.time_left = Health.sim_assess_time;
			p.time += p.time_left;
		} else {
			hosp.waiting.add(p);
		}

	}

}
