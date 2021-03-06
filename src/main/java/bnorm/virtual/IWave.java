package bnorm.virtual;

/**
 * Interface for a wave that exists on the Robocode battlefield. This wave has
 * a starting coordinate, a speed, and a starting time. Given a specified time,
 * we can see where this wave is on the battlefield and how far it has
 * traveled.
 *
 * @author Brian Norman
 */
public interface IWave extends IPoint {

   /**
    * Returns the speed of the wave in pixels/tick.
    *
    * @return the speed of the wave in pixels/tick.
    */
   double getVelocity();

   /**
    * Returns the time the wave started.
    *
    * @return the time the wave started.
    */
   long getTime();

   /**
    * Returns whether the wave is active at the specified time.
    *
    * @param currentTime time to test activity.
    * @return if the wave is active.
    */
   boolean isActive(long currentTime);

   /**
    * Returns whether the wave is active at the specified time and with the
    * specified target.
    *
    * @param currentTime time to test activity.
    * @param target the target of the wave.
    * @return if the wave is active.
    */
   public boolean isActive(long currentTime, IPoint target);

   /**
    * Returns the distance the wave has traveled from where it started as of
    * the specified time.
    *
    * @param currentTime the time at which to calculate the distance.
    * @return the distance the wave has traveled.
    */
   double dist(long currentTime);

   /**
    * Returns the squared distance the wave has traveled from where it started
    * as of the specified time.
    *
    * @param currentTime the time at which to calculate the squared distance.
    * @return the squared distance the wave has traveled.
    */
   double distSq(long currentTime);

}
