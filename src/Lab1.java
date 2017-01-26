import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

import java.awt.*;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Lab1 {
    private TSimInterface tsi;
//    private Semaphore s1;
//    private HashMap<Point, Semaphore> hashPoint;
    private Semaphore[] semaphores = new Semaphore[6];;
    public Lab1(Integer speed1, Integer speed2) throws InterruptedException {
        tsi = TSimInterface.getInstance();
//        hashPoint = new HashMap<Point, Semaphore>();
//        addToHashMap();

        for(int i=0; i<6;i++){
            semaphores[i] = new Semaphore(1);
        }

        semaphores[0].acquire(); //upper track, where t1 starts at
        semaphores[5].acquire(); //lower track, where t2 starts at

        try {
//            manually set the switch
            tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
            tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
//            tsi.setSwitch(3,11,TSimInterface.SWITCH_RIGHT);
        } catch (CommandException e) {
            e.printStackTrace();    // or only e.getMessage() for the error
            System.exit(1);
        }

        Thread t1 = new Thread(new Train(Direction.DOWN, speed1, 1));
        Thread t2 = new Thread(new Train(Direction.UP, speed2, 2));
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

//    public void addToHashMap() {
//        Point p1 = new Point(15, 3);   // Start punkt 1 uppe
//        Point p2 = new Point(15, 5);   // Start punkt 2 uppe
//        Point p3 = new Point(14, 11); // Start punkt 3 nere
//        Point p4 = new Point(14, 13); // Start punkt 4 nere
//
//        Semaphore s1 = new Semaphore(1);
//        Semaphore s2 = new Semaphore(1);
//        Semaphore s3 = new Semaphore(1);
//        Semaphore s4 = new Semaphore(1);
//        Semaphore s5 = new Semaphore(1);
//        Semaphore s6 = new Semaphore(1);
//
//        //Fyrkorsningen längst upp
//        hashPoint.put(new Point(6, 6), s1);
//        hashPoint.put(new Point(9, 5), s1);
//        hashPoint.put(new Point(10, 7), s1);
//        hashPoint.put(new Point(10, 8), s1);
//
//        //Path längst upp
//        hashPoint.put(new Point(14, 8), s2);
//
//        //Path höger // Glöm inte att även titta på 14,8 och 13,10. Dessa har redan semaphorer så kan inte läggas till igen.
//        hashPoint.put(new Point(14, 7), s3);
//        hashPoint.put(new Point(13, 9), s3);
//
//        //Path mitten
//        hashPoint.put(new Point(13, 10), s4);
//        hashPoint.put(new Point(6, 10), s4);
//
//        //Path vänster, Gäller även för 3,13 och 6.10 precis som för path höger
//        hashPoint.put(new Point(6, 9), s5);
//        hashPoint.put(new Point(5, 11), s5);
//
//        //Station nedre, av nedre uppe eller nere.
//        hashPoint.put(new Point(3, 13), s6);
//    }

    private enum Direction {
        UP, DOWN
    }

    private class Train implements Runnable {
        private Direction direction;
        private int speed, id;

        public Train(Direction d, int originalSpeed, int id) {
            this.direction = d;
            this.speed = originalSpeed;
            this.id = id;
        }

        private void setSpeed(int speed) {
            try {
                tsi.setSpeed(this.id, speed);
            } catch (CommandException e) {
                e.printStackTrace();
            }
        }

        private void sleep() {
            try {
                Thread.sleep(1000 + (20 * Math.abs(this.speed)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            setSpeed(speed);
            while (true) {
                try {
                    SensorEvent e = tsi.getSensor(this.id);
                    handleEvent(e);

                } catch (CommandException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

        }

        public void handleEvent(SensorEvent e) {
            int x = e.getXpos();
            int y = e.getYpos();

            if(isSensorActive(e)) {
//                if (handleStartStop(e)) {
//                    return;
//                } else if (handleCross(e)) {
//                    return;
//                } else if (handleStationDown(e)) {
//                    return;
//                } else {
//                    handlePath(e);
//                }

                //handle when the train arrives to the upper station
                if (this.direction == Direction.UP && (x == 15 && (y == 3 || y == 5))) {
                    slowDownAndSwitchDirection();

                    //handle when the train arrives to the lower station
                } else if (this.direction == Direction.DOWN && (x == 14 && (y == 11 || y == 13))) {
                    slowDownAndSwitchDirection();

                    //arrives to an intersection 6 6 , 9 5, 10 7, 10 8
                } else if (this.direction == Direction.DOWN && (x == 6 && y == 6)) {
                    handleIntersection(1);
                } else if (this.direction == Direction.DOWN && (x == 9 && y == 5)) {
                    handleIntersection(1);
                } else if (this.direction == Direction.UP && (x == 10 && y == 7)) {
                    handleIntersection(1);
                } else if (this.direction == Direction.UP && (x == 10 && y == 8)) {
                    handleIntersection(1);


//                    arrives to "single path" 14 7 14 8 19 8
                } else if (this.direction == Direction.DOWN && (x == 14 && y == 7)) {
                    stopAtIntersectionTryAcquire(2);
                    setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
                    setSpeed(speed);
//                    SensorEvent e1 = getActiveSensor();
//                    release(semaphores[0]);
//                    handleEvent(e1);
                } else if (this.direction == Direction.DOWN && (x == 14 && y == 8)) {
                    stopAtIntersectionTryAcquire(2);
                    setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                    setSpeed(speed);
//                    SensorEvent e1 = getActiveSensor();
//                    release(semaphores[0]);
//                    handleEvent(e1);
                } else if (this.direction == Direction.UP && (x == 14 && y == 7)) {
                    release(semaphores[2]);

                } else if (this.direction == Direction.UP && (x == 14 && y == 8)) {
                    release(semaphores[2]);

                } else if (this.direction == Direction.UP && (x == 19 && y == 8)) {
                    if (!tryAcquire(semaphores[0])) {
                        setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
                    }else{
                        setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
                    }
                    release(semaphores[3]); //remove comment later when sem 3 is acquired

                } else if (this.direction == Direction.DOWN && (x == 19 && y == 8)){
//                    System.out.println("Semaphore three " +semaphores[3].toString());
                    if (!tryAcquire(semaphores[3])) {
                        System.out.println("Not Acquired 3 ");
                        setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
                    } else {
                        System.out.println("Acquired 3");
                        setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
                    }
                    release(semaphores[0]);

                    //middle right
                }  else if (this.direction == Direction.UP && (x == 13 && y == 9)) {
                    stopAtIntersectionTryAcquire(2);
                    setSpeed(speed);
                    setSwitch(15,9, TSimInterface.SWITCH_RIGHT);
                    SensorEvent e1 = getActiveSensor();
//                    release(semaphores[3]);
                    handleEvent(e1);
                } else if (this.direction == Direction.UP && (x == 13 && y == 10)) {
                    stopAtIntersectionTryAcquire(2);
                    setSpeed(speed);
                    setSwitch(15,9, TSimInterface.SWITCH_LEFT);
                } else if (this.direction == Direction.DOWN && (x == 13 && y == 9)) {
                    release(semaphores[2]);
                } else if (this.direction == Direction.DOWN && (x == 13 && y == 10)) {
                    release(semaphores[2]);


                    //middle left
                } else if (this.direction == Direction.UP && (x == 6 && y == 9)) {
                    release(semaphores[4]);
                } else if (this.direction == Direction.UP && (x == 6 && y == 10)) {
                    release(semaphores[4]);
                } else if (this.direction == Direction.DOWN && (x == 6 && y == 9)) {
                    stopAtIntersectionTryAcquire(4);
                    setSwitch(4,9, TSimInterface.SWITCH_LEFT);
                    setSpeed(speed);
                } else if (this.direction == Direction.DOWN && (x == 6 && y == 10)) {
                    stopAtIntersectionTryAcquire(4);
                    setSwitch(4,9, TSimInterface.SWITCH_RIGHT);
                    setSpeed(speed);

                    //left sensor
                } else if (this.direction == Direction.UP && (x == 1 && y == 10)) {
                    if (!tryAcquire(semaphores[3])) {
                        setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
                    } else {
                        setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
                    }
                    release(semaphores[5]);
                } else if (this.direction == Direction.DOWN && (x == 1 && y == 10)) {
                    if (!tryAcquire(semaphores[5])) {
                        setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
                    } else {
                        setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
                    }
//                    release(semaphores[3]);

                    //last sensors
                }else if (this.direction == Direction.DOWN && (x == 5 && y == 11)) {
                    release(semaphores[4]);
                }else if (this.direction == Direction.DOWN && (x == 3 && y == 13)) {
                    release(semaphores[4]);

                }else if (this.direction == Direction.UP && (x == 5 && y == 11)) {
                    stopAtIntersectionTryAcquire(4);
                    setSwitch(3,11, TSimInterface.SWITCH_LEFT);
                    setSpeed(speed);
                }else if (this.direction == Direction.UP && (x == 3 && y == 13)) {
                    stopAtIntersectionTryAcquire(4);
                    setSwitch(3,11, TSimInterface.SWITCH_RIGHT);
                    setSpeed(speed);
                }

            }
        }

        private void handleIntersection(int i){
            stopAtIntersectionTryAcquire(i);
            setSpeed(speed);
            getActiveSensor();
            release(semaphores[i]);

        }

        private void stopAtIntersectionTryAcquire(int i){
            setSpeed(0);
            acquire(semaphores[i]);
        }

        private SensorEvent getActiveSensor(){
            try {
                SensorEvent next = tsi.getSensor(this.id);
                if(isSensorActive(next)){
                    return next;
                }else{
                    return getActiveSensor();
                }
            } catch (CommandException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

//        private boolean handleStationDown(SensorEvent e) {
//            int y = e.getYpos();
//            int x = e.getXpos();
//
//            Semaphore s = hashPoint.get(new Point(x,y));
//
//            if (x == 6 && y == 9) {
//
//                if (this.direction == Direction.DOWN) {
//                    setSpeed(0);
//                    acquire(s);
//                    setSpeed(speed);
//                    setSwitch(4,9,TSimInterface.SWITCH_RIGHT);
//                    Semaphore whichStation = hashPoint.get(new Point(3,13));
//
//                    if (whichStation.availablePermits() == 0) { // You should go up
//                        //The station farthest down contains a train
//                        setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
//                    } else { // You should go down
//                        acquire(whichStation);
//                        setSwitch(3 , 11, TSimInterface.SWITCH_LEFT);
//                    }
//                } else {
//                    release(s);
//                }
//                return true;
//            }
//
//            if (x == 6 && y == 10) {
//
//                if (this.direction == Direction.DOWN) {
//                    setSpeed(0);
//                    acquire(s);
//                    setSpeed(speed);
//                    setSwitch(4,9,TSimInterface.SWITCH_LEFT);
//                    Semaphore onDownMiddle = hashPoint.get(new Point(13,10));
//                    onDownMiddle.release();
//                    Semaphore whichStation = hashPoint.get(new Point(3,13));
//
//                    if (whichStation.availablePermits() == 0) { // You should go up
//                        //The station farthest down contains a train
//                        setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
//                    } else { // You should go down
//                        acquire(whichStation);
//                        setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
//                    }
//                } else {
//                    s.release();
//                }
//                return true;
//            }
//
//            if (x == 5 && y == 11) {
//                    if (this.direction == Direction.DOWN) {
//                        s.release();
//                    } else {
//                        setSpeed(0);
//                        acquire(s);
//                        setSwitch(3,11, TSimInterface.SWITCH_LEFT);
//                        setSpeed(speed);
//                        Semaphore whichRail = hashPoint.get(new Point(13,10));
//                        if (whichRail.availablePermits() == 0) { // You should go up
//                            //The station farthest down contains a train
//                            setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
//                        } else { // You should go down
//                            acquire(whichRail);
//                            setSwitch(4 , 9, TSimInterface.SWITCH_LEFT);
//                        }
//                    }
//
//            }
//            return false;
//        }

        private void setSwitch(double x, double y, int direction) {
            try {
                tsi.setSwitch((int) x,(int) y,direction);
            } catch (CommandException e1) {
                e1.printStackTrace();
            }
        }

//        private boolean handleStartStop(SensorEvent e) {
//            double y = e.getYpos();
//            double x = e.getXpos();
//            if (x == 15 && (y == 3 || y == 5)) {
//                if (this.direction == Direction.UP) slowDownAndSwitchDirection(e);
//                return true;
//            } else if (x == 14 && (y == 11 || y == 13)) {
//                if (this.direction == Direction.DOWN) slowDownAndSwitchDirection(e);
//                return true;
//            }
//            return false;
//        }

        private void slowDownAndSwitchDirection() {
            setSpeed(0);
            sleep();
            this.speed = -this.speed;
            setSpeed(this.speed);
            if (this.direction == Direction.DOWN) this.direction = Direction.UP;
            else this.direction = Direction.DOWN;
        }

//        private boolean handleCross(SensorEvent e) {
//            int x = e.getXpos();
//            int y = e.getYpos();
//            Semaphore s = hashPoint.get(new Point(x, y));
//
//            if (x == 6 && y == 6 || x == 9 && y == 5) {
//                if (this.direction == Direction.UP) s.release();
//                else acquire(s);
//                return true;
//            }
//
//            if (x == 10 && (y == 7 || y == 8)) {
//                if (this.direction == Direction.DOWN) s.release();
//                else acquire(s);
//                return true;
//            }
//            return false;
//        }

        private void acquire(Semaphore s) {
            try {
                s.acquire();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }

        private boolean tryAcquire(Semaphore s) {
            return s.tryAcquire();
        }

        private void release(Semaphore s) {
            s.release();
        }

//        private void handlePath(SensorEvent e) {
//            int x = e.getXpos();
//            int y = e.getYpos();
//            Semaphore s = hashPoint.get(new Point(x, y));
//            System.out.println("In handlePath. Id: " + this.id + " x:" + x + " y:" + y + " ");
//
//            if (isSensorActive(e)) {
//                if ((x == 14 && y == 7) || x == 13 && y == 9) {
//                    setSpeed(0);
//                    System.out.println("Semaphore acc... for id: " + this.id);
//                    acquire(s);
//                    System.out.println("Semaphore acquired for id: " + this.id);
//                    setSpeed(speed);
//
//                    if(this.direction == Direction.UP){
//                        setSwitch(15,9, TSimInterface.SWITCH_RIGHT);
//                    }else{
//                        setSwitch(15,9, TSimInterface.SWITCH_LEFT);
//
//                    }
//
//                }
//
//                if (direction == Direction.DOWN && (x == 13 && y == 10)) {
//                    System.out.println("Semaphore re.... for id: " + this.id);
//                    Semaphore s1 = hashPoint.get(new Point(13,9));
//                    release(s1);
//                    System.out.println("Semaphore released for id: " + this.id);
//
//                }
//            }
//
//        }

        private boolean isSensorActive(SensorEvent e) {
            return e.getStatus() == 1;
        }

    }

    //6 semaphorer behövs
}