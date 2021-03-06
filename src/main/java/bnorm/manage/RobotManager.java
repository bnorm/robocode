package bnorm.manage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;

import bnorm.events.EventHandler;
import bnorm.events.RobotFoundEvent;
import bnorm.events.RobotFoundListener;
import bnorm.events.RobotFoundSender;
import bnorm.messages.Message;
import bnorm.messages.MessageHandler;
import bnorm.messages.RobotSnapshotMessage;
import bnorm.robots.IRobot;
import bnorm.robots.IRobotFactory;
import bnorm.robots.IRobotSnapshot;
import bnorm.robots.IRobotSnapshotFactory;
import bnorm.robots.RobotFactory;
import bnorm.robots.RobotSnapshotFactory;
import robocode.Event;
import robocode.Robot;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;

/**
 * The {@link RobotManager} <code>class</code> is used to manage the {@link IRobot} instances that
 * represent all the robots in the battle. The {@link RobotManager} does not distinguish between
 * friend and foe so all the robots are stored together. Robots can be accessed by their name and a
 * collection of all the robots can also be returned.
 *
 * @author Brian Norman
 */
public final class RobotManager implements EventHandler, MessageHandler, RobotFoundSender {

   /**
    * The {@link Robot} <code>class</code> that created the {@link RobotManager} instance.
    */
   private final Robot robot_;

   /**
    * A list of all known robots mapped against their name.
    */
   private final HashMap<String, IRobot> allRobots_;

   /**
    * The robot factory used for creating new robot objects.
    */
   private final IRobotFactory robotFactory_;

   /**
    * The robot snapshot factory used for creating new robot snapshots.
    */
   private final IRobotSnapshotFactory snapshotFactory_;

   /**
    * The collection of listeners to notify if a new robot is found.
    */
   private final Collection<RobotFoundListener> robotFoundListeners;

   /**
    * Returns an instance of the {@link RobotManager} <code>class</code>.
    *
    * @param robot the {@link Robot} object that is requesting the robot manager object.
    * @param robotFactory the {@link RobotFactory} used by the robot manager for creating robot objects.
    * @param snapshotFactory the {@link RobotSnapshotFactory} used by the robot manager for creating robot
    * snapshots.
    * @return an instance.
    * @see #RobotManager(Robot, IRobotFactory, IRobotSnapshotFactory)
    */
   public static RobotManager create(Robot robot, IRobotFactory robotFactory, IRobotSnapshotFactory snapshotFactory) {
      return new RobotManager(robot, robotFactory, snapshotFactory);
   }

   /**
    * Creates an instance of a {@link RobotManager} with the specified arguments.
    *
    * @param robot the {@link Robot} object that is requesting the robot manager object.
    * @param robotFactory the {@link RobotFactory} used by the robot manager for creating robot objects.
    * @param snapshotFactory the {@link RobotSnapshotFactory} used by the robot manager for creating robot
    * snapshots.
    */
   private RobotManager(Robot robot, IRobotFactory robotFactory, IRobotSnapshotFactory snapshotFactory) {
      this.robot_ = robot;
      this.allRobots_ = new HashMap<String, IRobot>();
      this.robotFactory_ = robotFactory;
      this.snapshotFactory_ = snapshotFactory;
      this.robotFoundListeners = new LinkedList<RobotFoundListener>();
   }

   @Override
   public void inEvent(Event event) {
      if (event instanceof ScannedRobotEvent) {
         handleScannedRobotEvent((ScannedRobotEvent) event);
      } else if (event instanceof RobotDeathEvent) {
         handleRobotDeathEvent((RobotDeathEvent) event);
      }
   }

   @Override
   public void inEvents(Iterable<? extends Event> events) {
      for (Event e : events) {
         inEvent(e);
      }
   }

   @Override
   public void inMessage(Message message) {
      if (message instanceof RobotSnapshotMessage) {
         handleRobotSnapshotMessage((RobotSnapshotMessage) message);
      }
   }

   @Override
   public void inMessages(Iterable<? extends Message> messages) {
      for (Message m : messages) {
         inMessage(m);
      }
   }

   /**
    * Processes any {@link ScannedRobotEvent}s that are passed to the {@link RobotManager}. Events
    * are delegated to the method {@link #handleRobotSnapshot(IRobotSnapshot)}. If the specified
    * event is <code>null</code> then a {@link NullPointerException} is thrown.
    *
    * @param event the event to handle.
    * @throws NullPointerException if <code>event</code> is <code>null</code>.
    * @see #handleRobotSnapshot(IRobotSnapshot)
    */
   private void handleScannedRobotEvent(ScannedRobotEvent event) {
      Objects.requireNonNull(event, "ScannedRobotEvent must not be null.");

      handleRobotSnapshot(snapshotFactory_.create(event, robot_));
   }

   /**
    * Processes any {@link RobotDeathEvent}s that are passed to the {@link RobotManager}. If the
    * specified event is <code>null</code> then a {@link NullPointerException} is thrown.
    *
    * @param event the event to handle.
    * @throws NullPointerException if <code>event</code> is <code>null</code>.
    */
   private void handleRobotDeathEvent(RobotDeathEvent event) {
      Objects.requireNonNull(event, "RobotDeathEvent must not be null.");

      IRobot r = allRobots_.get(event.getName());
      if (r != null) {
         r.add(snapshotFactory_.create(event, r.getSnapshot()));
      }
   }

   /**
    * Processes any {@link RobotSnapshotMessage}s that are passed to the {@link RobotManager}.
    * Events are delegated to the method {@link #handleRobotSnapshot(IRobotSnapshot)}. If the
    * specified message is <code>null</code> then a {@link NullPointerException} is thrown.
    *
    * @param message the message to handle.
    * @throws NullPointerException if <code>message</code> is <code>null</code>.
    * @see #handleRobotSnapshot(IRobotSnapshot)
    */
   private void handleRobotSnapshotMessage(RobotSnapshotMessage message) {
      Objects.requireNonNull(message, "RobotSnapshotMessage must not be null.");

      handleRobotSnapshot(message.getSnapshot());
   }

   /**
    * Processes the specified {@link IRobotSnapshot}. If the specified snapshot is <code>null</code>
    * then a {@link NullPointerException} is thrown. If the snapshot has the same name as the robot
    * that is processing the snapshot then the snapshot is skipped and not processed.
    *
    * @param snapshot the robot snapshot to handle.
    * @throws NullPointerException if <code>snapshot</code> is <code>null</code>.
    */
   private void handleRobotSnapshot(IRobotSnapshot snapshot) {
      Objects.requireNonNull(snapshot, "IRobotSnapshot must not be null.");
      if (snapshot.getName().equals(robot_.getName())) {
         // Skip snapshot of myself
         return;
      }

      String name = snapshot.getName();
      if (allRobots_.containsKey(name)) {
         allRobots_.get(name).add(snapshot);
      } else {
         allRobots_.put(name, robotFactory_.create(snapshot, robot_));
         notifyRobotFound(new RobotFoundEvent(allRobots_.get(name)));
      }
   }

   /**
    * Returns the robot with the specified name. If the manager does not contain the specified name
    * then a blank enemy is return. This blank enemy is died and has everything set to defaults.
    *
    * @param name the name of the robot to find.
    * @return the robot with the specified name.
    */
   public IRobot getRobot(String name) {
      IRobot r = allRobots_.get(name);
      if (r == null) {
         r = robotFactory_.createEnemy();
      }
      return r;
   }

   /**
    * Returns all the robots this manager contains. This is returned as a collection. There is no
    * distinction between teammate and enemy and no guarantee about their position in the
    * collection.
    *
    * @return all the robots.
    */
   public Collection<IRobot> getRobots() {
      return allRobots_.values();
   }

   /**
    * Returns the the robot that is the least of all the robots using the comparator. The comparator
    * should be designed to provide some tolerance towards the robot that was chosen last time.
    * <p>
    * Note: If two robots are equal, the robot that is earlier in the list is chosen.
    *
    * @param comparator the comparator used to find the "smallest" robot.
    * @return the "smallest" robot.
    */
   public IRobot getRobot(Comparator<IRobot> comparator) {
      IRobot r = null;
      for (IRobot temp : getRobots()) {
         if (r == null || comparator.compare(r, temp) > 0) {
            r = temp;
         }
      }
      return r != null ? r : robotFactory_.createEnemy();
   }

   @Override
   public void addRobotFoundListener(RobotFoundListener listener) {
      robotFoundListeners.add(listener);
   }

   @Override
   public void removeRobotFoundListener(RobotFoundListener listener) {
      robotFoundListeners.remove(listener);
   }

   private void notifyRobotFound(RobotFoundEvent e) {
      for (RobotFoundListener l : robotFoundListeners) {
         l.handleRobotFound(e);
      }
   }
}
