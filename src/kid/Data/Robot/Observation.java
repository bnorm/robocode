package kid.Data.Robot;

import robocode.Robot;
import kid.Utils;
import kid.Data.PatternMatching.*;

public class Observation {

    private PolarPattern POLAR_PATTERN;
    private LatVelPattern GF_PATTERN;

    private double GF;
    private double GFWieght;

    private double X;
    private double Y;

    private double ANGLE_TO_ENEMY;
    private double ANGLE_TO_ME;

    private double DIST;

    private double ENERGY;
    private double DELTA_ENERGY;

    private double HEADING;
    private double DELTA_HEADING;

    private double VELOCITY;
    private double DELTA_VELOCITY;
    private double TIME_OF_ACCEL;
    private double TIME_OF_DECCEL;
    private double ADVANCING_VELOCITY;
    private double LATERAL_VELOCITY;

    private long TIME;
    private int DELTA_TIME;

    public Observation(EnemyData e, Robot MyRobot) {
        X = e.getX();
        Y = e.getY();
        ANGLE_TO_ME = Utils.getAngle(MyRobot.getX(), MyRobot.getY(), X, Y);
        ANGLE_TO_ENEMY = Utils.getAngle(X, Y, MyRobot.getX(), MyRobot.getY());

        DIST = Utils.getDist(MyRobot.getX(), MyRobot.getY(), X, Y);

        ENERGY = e.getEnergy();
        DELTA_ENERGY = e.getDeltaEnergy();

        HEADING = e.getHeading();
        DELTA_HEADING = e.getDeltaHeading();

        VELOCITY = e.getVelocity();
        DELTA_VELOCITY = e.getTime() - e.getDeltaVelocity();
        TIME_OF_ACCEL = e.getTime() - e.getTimeSinceAccel();
        TIME_OF_DECCEL = e.getTimeSinceDeccel();
        ADVANCING_VELOCITY = VELOCITY * -Utils.cos(HEADING - ANGLE_TO_ME);
        LATERAL_VELOCITY = VELOCITY * Utils.sin(HEADING - ANGLE_TO_ME);

        TIME = e.getTime();
        DELTA_TIME = e.getDeltaTime();
    }

    public Observation(Robot MyRobot, EnemyData e) {
        X = MyRobot.getX();
        Y = MyRobot.getY();
        ANGLE_TO_ENEMY = Utils.getAngle(MyRobot.getX(), MyRobot.getY(), X, Y);
        ANGLE_TO_ME = Utils.getAngle(X, Y, MyRobot.getX(), MyRobot.getY());

        DIST = Utils.getDist(e.getX(), e.getY(), X, Y);

        ENERGY = MyRobot.getEnergy();
        // DELTA_ENERGY = MyRobot.getDeltaEnergy();

        HEADING = MyRobot.getHeading();
        // DELTA_HEADING = MyRobot.getDeltaHeading();

        VELOCITY = MyRobot.getVelocity();
        double angle = Utils.getAngle(e.getX(), e.getY(), X, Y);
        ADVANCING_VELOCITY = VELOCITY * -Utils.cos(HEADING - angle);
        LATERAL_VELOCITY = VELOCITY * Utils.sin(HEADING - angle);

        TIME = MyRobot.getTime();
        // DELTA_TIME = MyRobot.getDeltaTime();
    }

    public Observation(TeammateData MyRobot, EnemyData e) {
        X = MyRobot.getX();
        Y = MyRobot.getY();
        ANGLE_TO_ENEMY = Utils.getAngle(MyRobot.getX(), MyRobot.getY(), X, Y);
        ANGLE_TO_ME = Utils.getAngle(X, Y, MyRobot.getX(), MyRobot.getY());

        DIST = Utils.getDist(e.getX(), e.getY(), X, Y);

        ENERGY = MyRobot.getEnergy();
        // DELTA_ENERGY = MyRobot.getDeltaEnergy();

        HEADING = MyRobot.getHeading();
        // DELTA_HEADING = MyRobot.getDeltaHeading();

        VELOCITY = MyRobot.getVelocity();
        double angle = Utils.getAngle(e.getX(), e.getY(), X, Y);
        ADVANCING_VELOCITY = VELOCITY * -Utils.cos(HEADING - angle);
        LATERAL_VELOCITY = VELOCITY * Utils.sin(HEADING - angle);

        TIME = MyRobot.getTime();
        // DELTA_TIME = MyRobot.getDeltaTime();
    }

    public void setGFHit(double GF) {
        this.GF = GF;
        GFWieght = 1;
    }

    public void setGFHit(double GF, double GFWieght) {
        this.GF = GF;
        this.GFWieght = GFWieght;
    }

    public void setGFPattern(LatVelPattern p) {
        GF_PATTERN = p;
    }

    public LatVelPattern getGFPattern() {
        return GF_PATTERN;
    }

    public void setPolarPattern(PolarPattern p) {
        POLAR_PATTERN = p;
    }

    public PolarPattern getPolarPattern() {
        return POLAR_PATTERN;
    }

    public double getGF() {
        return GF;
    }

    public double getGFWieght() {
        return GFWieght;
    }

    public double getDist() {
        return DIST;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public double getAngleToEnemy() {
        return ANGLE_TO_ENEMY;
    }

    public double getAngleFromEnemy() {
        return ANGLE_TO_ME;
    }

    public double getEnergy() {
        return ENERGY;
    }

    public double getDeltaEnergy() {
        return DELTA_ENERGY;
    }

    public double getHeading() {
        return HEADING;
    }

    public double getDeltaHeading() {
        return DELTA_HEADING;
    }

    public double getVelocity() {
        return VELOCITY;
    }

    public double getDeltaVelocity() {
        return DELTA_VELOCITY;
    }

    public double getTimeSinceAccel() {
        return TIME - TIME_OF_ACCEL;
    }

    public double getTimeSinceDeccel() {
        return TIME - TIME_OF_DECCEL;
    }

    public double getLateralVelocity() {
        return LATERAL_VELOCITY;
    }

    public double getAdvancingVelocity() {
        return ADVANCING_VELOCITY;
    }


    public long getTime() {
        return TIME;
    }

    public int getDeltaTime() {
        return DELTA_TIME;
    }
}
