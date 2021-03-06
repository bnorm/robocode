package bnorm.robots;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import bnorm.events.RobotFiredEvent;
import bnorm.events.RobotFiredListener;
import robocode.Rules;

/**
 * An abstract representation of a Robocode robot. Provides base functionality
 * to build upon.
 * <p />
 * See {@link IRobot} for details of the requirements of a robot.
 *
 * @author Brian Norman
 */
class Robot implements IRobot {

   /**
    * The name of the robot.
    */
   private String name;

   /**
    * The map of time series keyed by the match round.
    */
   private Map<Integer, List<IRobotSnapshot>> rounds;

   /**
    * The round time series that was most recently added to.
    */
   private List<IRobotSnapshot> movie;

   /**
    * The most recent snapshot of the current match round. I.e., the last
    * snapshot of the current match round series.
    */
   private IRobotSnapshot recent;

   /**
    * All the listeners to {@link RobotFiredEvent}s.
    */
   private List<RobotFiredListener> robotFiredListeners;

   /**
    * Creates a new robot.
    */
   public Robot() {
      this("");
   }

   /**
    * Creates a new robot with the specified name.
    *
    * @param name the name of the robot.
    */
   public Robot(String name) {
      this.name = name;
      this.rounds = new Hashtable<Integer, List<IRobotSnapshot>>();
      this.movie = new LinkedList<IRobotSnapshot>();
      this.recent = new RobotSnapshot();

      this.robotFiredListeners = new LinkedList<RobotFiredListener>();
   }

   /**
    * Creates a new robot that is a copy of the specified robot.
    *
    * @param robot the robot to copy.
    */
   protected Robot(IRobot robot) {
      this(robot.getName());

      for (Integer i : robot.getRounds()) {
         ListIterator<IRobotSnapshot> movie = robot.getMovie(0, i);
         rounds.put(i, this.movie = new LinkedList<IRobotSnapshot>());
         while (movie.hasNext()) {
            this.movie.add(movie.next());
         }
      }

      recent = robot.getSnapshot();
      if (recent != null) {
         movie = rounds.get(recent.getRound());
      }
   }

   @Override
   public String getName() {
      return name;
   }

   /**
    * {@inheritDoc}
    *
    * @throws NullPointerException if <code>snapshot</code> is null.
    * @throws IllegalArgumentException if the name of <code>snapshot</code>
    * does not match the name of the robot.
    */
   @Override
   public boolean add(IRobotSnapshot snapshot) {
      if (snapshot == null) {
         throw new NullPointerException("IRobotSnapshot must not be null.");
      } else if (!snapshot.getName().equals(name)) {
         throw new IllegalArgumentException(
                 "Name of snapshot must match the name of the robot (" + recent.getName() + " != " + snapshot.getName()
                         + ").");
      }

      movie = rounds.get(snapshot.getRound());
      if (movie == null) {
         rounds.put(snapshot.getRound(), movie = new LinkedList<IRobotSnapshot>());
      }

      int index = getIndex(movie, snapshot.getTime());
      if (index >= 0 && movie.get(index).getTime() == snapshot.getTime()) {
         return false;
      }

      movie.add(index + 1, snapshot);
      recent = movie.get(movie.size() - 1);

      // Did the robot fire a bullet?
      if (index > 0) {
         IRobotSnapshot prev = movie.get(index);
         double timeDiff = snapshot.getTime() - prev.getTime();
         double energyDiff = snapshot.getEnergy() - prev.getEnergy();
         double velocityDiff = snapshot.getVelocity() - prev.getVelocity();
         if (timeDiff == 1 && energyDiff <= -Rules.MIN_BULLET_POWER && energyDiff >= -Rules.MAX_BULLET_POWER
                 && velocityDiff >= -Rules.DECELERATION) {
            // is it ramming someone?
            // has the inactive timer expired
            notify(new RobotFiredEvent(prev, Math.abs(energyDiff)));
         }
      }

      return true;
   }

   @Override
   public IRobotSnapshot getSnapshot() {
      return recent;
   }

   /**
    * {@inheritDoc}
    *
    * @throws IllegalArgumentException if <code>time</code> is less than zero.
    */
   @Override
   public IRobotSnapshot getSnapshot(long time) {
      if (time < 0) {
         throw new IllegalArgumentException("Time must not be less than zero (" + time + ").");
      }

      return getSnapshot(movie, time);
   }

   /**
    * {@inheritDoc}
    *
    * @throws IllegalArgumentException if <code>time</code> is less than zero
    * or if <code>round</code> is less than zero.
    */
   @Override
   public IRobotSnapshot getSnapshot(long time, int round) {
      if (time < 0) {
         throw new IllegalArgumentException("Time must not be less than zero (" + time + ").");
      } else if (round < 0) {
         throw new IllegalArgumentException("Round must not be less than zero (" + round + ").");
      }

      return getSnapshot(rounds.get(round), time);
   }

   @Override
   public ListIterator<IRobotSnapshot> getMovie() {
      return getMovie(movie, 0);
   }

   /**
    * {@inheritDoc}
    *
    * @throws IllegalArgumentException if <code>time</code> is less than zero.
    */
   @Override
   public ListIterator<IRobotSnapshot> getMovie(long time) {
      if (time < 0) {
         throw new IllegalArgumentException("Time must not be less than zero (" + time + ").");
      }

      return getMovie(movie, time);
   }

   /**
    * {@inheritDoc}
    *
    * @throws IllegalArgumentException if <code>time</code> is less than zero
    * or if <code>round</code> is less than zero.
    */
   @Override
   public ListIterator<IRobotSnapshot> getMovie(long time, int round) {
      if (time < 0) {
         throw new IllegalArgumentException("Time must not be less than zero (" + time + ").");
      } else if (round < 0) {
         throw new IllegalArgumentException("Round must not be less than zero (" + round + ").");
      }

      return getMovie(rounds.get(round), time);
   }

   @Override
   public Set<Integer> getRounds() {
      return rounds.keySet();
   }

   @Override
   public void addRobotFiredListener(RobotFiredListener listener) {
      robotFiredListeners.add(listener);
   }

   @Override
   public void removeRobotFiredListener(RobotFiredListener listener) {
      robotFiredListeners.remove(listener);
   }

   /**
    * Notifies all the RobotFiredEvent listeners.
    *
    * @param event the RobotFiredEvent.
    */
   protected void notify(RobotFiredEvent event) {
      for (RobotFiredListener l : robotFiredListeners) {
         l.handleRobotFired(event);
      }
   }

   /**
    * Returns the index of the snapshot that matches the specified time in the
    * specified series. The series is assumed to be sorted. If the exact time
    * does not appear in the series, then the index of the greatest time that
    * is still less than the specified time is return.
    * <p>
    * Properties:<br>
    * 1) If <code>getIndex(movie,time=t)</code> returns a snapshot for which
    * the time is equal to <code>t</code>, then the snapshot at index
    * <code>getIndex(movie,time=t-1)+1</code> will be the same snapshot.<br>
    * 2) If <code>getIndex(movie,time=t)</code> returns a snapshot for which
    * the time is less than <code>t</code>, then the snapshot at index
    * <code>getIndex(movie,time=t-1)+1</code> will be the snapshot with the
    * smallest time that is still greater than the time <code>t</code>.
    *
    * @param movie the series to search.
    * @param time the time to search for.
    * @return the index of the time, rounding down.
    * @throws IllegalArgumentException if <code>movie</code> is null or if
    * <code>time</code> is less than zero.
    */
   protected static int getIndex(List<IRobotSnapshot> movie, long time) {
      if (movie == null) {
         throw new NullPointerException("List must not be null.");
      } else if (time < 0) {
         throw new IllegalArgumentException("Time must not be less than zero (" + time + ").");
      } else if (movie.size() == 0) {
         return -1;
      }

      int index = movie.size() - 1;

      long headDiff = Math.abs(time - movie.get(0).getTime());
      long tailDiff = Math.abs(time - movie.get(movie.size() - 1).getTime());

      if (headDiff < tailDiff) {
         index = -1;
         ListIterator<? extends IRobotSnapshot> iter = movie.listIterator();
         while (iter.hasNext() && iter.next().getTime() <= time) {
            index++;
         }
      } else {
         ListIterator<? extends IRobotSnapshot> iter = movie.listIterator(movie.size());
         while (iter.hasPrevious() && iter.previous().getTime() > time) {
            index--;
         }
      }

      return index;
   }

   /**
    * Returns an iterator over the specified list starting at the specified
    * time. If the specified list is <code>null</code> then an empty iterator
    * is returned. If the exact time does not appear in the series, then the
    * index of the greatest time that is still less than the specified time is
    * used.
    *
    * @param movie the list series to be played as a movie.
    * @param time the time when the movie starts.
    * @return an iterator over the specified list starting at the specified
    *         time.
    * @throws IllegalArgumentException if <code>time</code> is less than zero.
    */
   protected static ListIterator<IRobotSnapshot> getMovie(List<IRobotSnapshot> movie, long time) {
      if (time < 0) {
         throw new IllegalArgumentException("Time must not be less than zero (" + time + ").");
      } else if (movie == null) {
         return new LinkedList<IRobotSnapshot>().listIterator();
      }

      if (time == 0) {
         return movie.listIterator();
      } else {
         // See doc of getIndex(List, long) for why the following works.
         int index = getIndex(movie, time - 1);
         return movie.listIterator(Math.min(index + 1, movie.size()));
      }
   }

   /**
    * Returns the snapshot at the specified time from the specified list. If
    * the specified list is empty or <code>null</code> then a blank robot
    * snapshot is returned. If the exact time does not appear in the series,
    * then the snapshot of the greatest time that is still less than the
    * specified time is returned. If a time before all snapshots in the series
    * is specified, then the first snapshot in the series is returned.
    *
    * @param movie the series to search.
    * @param time the time to search for.
    * @return the snapshot at the specified time from the specified list.
    * @throws IllegalArgumentException if <code>time</code> is less than zero.
    */
   protected static IRobotSnapshot getSnapshot(List<IRobotSnapshot> movie, long time) {
      if (time < 0) {
         throw new IllegalArgumentException("Time must not be less than zero (" + time + ").");
      } else if (movie == null || movie.size() == 0) {
         return new RobotSnapshot();
      }

      int index = getIndex(movie, time);
      return movie.get(Math.max(0, index));
   }

}
