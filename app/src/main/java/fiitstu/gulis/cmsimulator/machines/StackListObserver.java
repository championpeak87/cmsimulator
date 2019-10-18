package fiitstu.gulis.cmsimulator.machines;

/**
 * An interface for notifying an adapter when a new stack is created
 *
 * Created by Jakub Sedlář on 03.01.2018.
 */
public interface StackListObserver {
    void notifyDataSetChanged();
}
