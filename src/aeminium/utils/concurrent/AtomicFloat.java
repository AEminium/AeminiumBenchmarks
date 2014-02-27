package aeminium.utils.concurrent;

import static java.lang.Float.floatToIntBits;
import static java.lang.Float.intBitsToFloat;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("serial")
public class AtomicFloat extends Number {

	private AtomicInteger bits;

	public AtomicFloat() {
		this(0f);
	}

	public AtomicFloat(float initialValue) {
		bits = new AtomicInteger(floatToIntBits(initialValue));
	}

	public final boolean compareAndSet(float expect, float update) {
		return bits.compareAndSet(floatToIntBits(expect), floatToIntBits(update));
	}

	public final void set(float newValue) {
		bits.set(floatToIntBits(newValue));
	}

	public final float get() {
		return intBitsToFloat(bits.get());
	}

	public float floatValue() {
		return get();
	}

	public final float getAndSet(float newValue) {
		return intBitsToFloat(bits.getAndSet(floatToIntBits(newValue)));
	}

	public final boolean weakCompareAndSet(float expect, float update) {
		return bits.weakCompareAndSet(floatToIntBits(expect), floatToIntBits(update));
	}

	public final float addAndGet(float delta) {
		return intBitsToFloat(bits.addAndGet(floatToIntBits(delta)));
	}

	public double doubleValue() {
		return (double) floatValue();
	}

	public int intValue() {
		return (int) get();
	}

	public long longValue() {
		return (long) get();
	}

}