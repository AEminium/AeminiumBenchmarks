package aeminium.utils.error;

import aeminium.runtime.ErrorHandler;
import aeminium.runtime.Task;

public class PrintErrorHandler implements ErrorHandler {

	@Override
	public void handleTaskException(Task task, Throwable t) {
		t.printStackTrace();
	}

	@Override
	public void handleLockingDeadlock() {
		System.out.println("[AeminiumRT] Found a DEADLOCK.");
	}

	@Override
	public void handleDependencyCycle(Task task) {
		System.out.println("[AeminiumRT] Found a dependency cycle in " + task);
	}

	@Override
	public void handleTaskDuplicatedSchedule(Task task) {
		System.out.println("[AeminiumRT] Duplicated task: " + task);
	}

	@Override
	public void handleInternalError(Error err) {
		err.printStackTrace();
	}

}
