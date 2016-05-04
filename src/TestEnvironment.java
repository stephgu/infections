import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * This is the environment which holds all the information for testing a new feature to the website,
 * such as all registered users, classes, ect.
 * It also has the two main testing methods: total infection and limited infection
 */
public class TestEnvironment {

    private HashSet<User> allUsersSet; //A set of all the users in the system
    private List<User> allUsersList; //A list of all the users in the system (needed to get access to ordered user)
    private List<Class> allClasses; //A list of all classes registered in the system
    private HashSet<User> infectedUsers; //A list of all users that have been infected in the system
    private String baseSiteVersion; //The site version that every user starts with prior to infection
    private boolean debug; //a test variable to help with developer testing
    private float delta; //this delta determines an acceptable range around the target percentage of the population that we want to hit
    private float affectedThresholdFactor; //this factor is intended to be * with total population, get to the max amount of affected people we will tolerate
    private int sizeLimit; //this is the max number of classes per "connected" calculation that we do. this factor is here for performance reasons
    private int numRetries; //this is the max number of retries we do when picking a random user while the queue is empty

    public TestEnvironment(String baseSiteVersion, boolean debug) {
        this.baseSiteVersion = baseSiteVersion;
        this.allClasses = new ArrayList<>();
        this.allUsersSet = new HashSet<>();
        this.allUsersList = new ArrayList<>();
        this.debug = debug;
        this.infectedUsers = new HashSet<>();
        //the following are default values, and can of course be tweaked
        this.delta = 0.05f;
        this.affectedThresholdFactor = 0.005f;
        this.sizeLimit = 10000;
        this.numRetries = 3;
    }

    public void addClass(Class classToAdd) {
        allClasses.add(classToAdd);
        for (User student : classToAdd.getStudents()) {
            if (allUsersSet.add(student)) {
                allUsersList.add(student);
            }
        }
        if (allUsersSet.add(classToAdd.getTeacher())) {
            allUsersList.add(classToAdd.getTeacher());
        }
    }

    public List<Class> getAllClasses() {
        return allClasses;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDelta(float newDelta) {
        delta = newDelta;
    }

    public void setAffectedThresholdFactor( float newFactor) {
        affectedThresholdFactor = newFactor;
    }

    public void setSizeLimit(int limit) {
        sizeLimit = limit;
    }

    public void setNumRetries(int retries) {
        numRetries = retries;
    }

    public List<User> getAllUsersList() {
        return allUsersList;
    }

    public void prettyPrintInfectedUsers() {
        System.out.println("Infected users are:");
        for (User infectedUser : infectedUsers) {
            System.out.println(infectedUser.getName());
        }
    }

    /**
     * Picks a random user, checks if they are infected. If yes, return. if no, try again.
     * Use this to ensure a non deterministic user base who will test the features
     * @return a random user that is not infected
     */
    public User getRandomNotInfectedUser() {
        Random random = new Random();
        boolean isUserInfected = true;
        User pickedUser = null;
        while (isUserInfected) {
            int randomNum = random.nextInt(allUsersSet.size());
            pickedUser = allUsersList.get(randomNum);
            if (!infectedUsers.contains(pickedUser)) {
                isUserInfected = false;
            }
        }
        return pickedUser;
    }

    /**
     * Given a user, infected all users within any degree of connection to that user
     * @param firstInfectedUser
     * @param newSiteVersion
     */
    public void totalInfection(User firstInfectedUser, String newSiteVersion) {
        //use bfs to avoid stack limitations of dfs
        Queue<User> allUsersToInfect = new LinkedList<>();
        allUsersToInfect.add(firstInfectedUser);

        while (!allUsersToInfect.isEmpty()) {
            User userToInfect = allUsersToInfect.poll();
            if (!userToInfect.getSiteVersion().equals(newSiteVersion)) {
                userToInfect.setSiteVersion(newSiteVersion);
                if (isDebug()) {
                    infectedUsers.add(userToInfect);
                }
                for (Class classUserTeaches : userToInfect.getTeachingClasses()) {
                    allUsersToInfect.addAll(classUserTeaches.getStudents());
                }
                for (Class classUserTakes : userToInfect.getTakingClasses()) {
                    allUsersToInfect.add(classUserTakes.getTeacher());
                }
            }
        }
    }

    public boolean hitTarget(float targetPercentage) {
        return getTotalPercentageInfected() >= targetPercentage;
    }

    public int getAffectedThreshold() {
        return (int) (affectedThresholdFactor * allUsersList.size());
    }

    public boolean meetsRequirements(Class randomClass, float targetPercentage) {
        if (isPercentageInfectedWithinTargetRange(targetPercentage)) {
            return randomClass.getNumStudentsAffected() < getAffectedThreshold();
        } else {
            return randomClass.getConnectedClasses().size() > sizeLimit;
        }
    }

    public void limitedInfection(User firstInfectedUser, String newSiteVersion, float targetPercentage) {
        boolean forceInfect = false;
        Queue<Class> toInfectQueue = new LinkedList<>();
        toInfectQueue.addAll(firstInfectedUser.getAllClasses());

        //don't stop if the queue isn't empty or we haven't hit the target yet
        while (!toInfectQueue.isEmpty() || !hitTarget(targetPercentage)) {
            //stop if we hit or surpass the target
            if (hitTarget(targetPercentage)) {
                return;
            }
            // if we get here, means the queue is empty but the target hasn't been hit so we need to chose another class
            if (toInfectQueue.isEmpty()) {
                Class randomClass = null;
                //tries to randomly pick a class that meets the requirements <numRetries> number of times
                for (int i = 0; i < numRetries; i++) {
                    randomClass = getRandomNotInfectedUser().getAllClasses().get(0);
                    if (meetsRequirements(randomClass, targetPercentage)) {
                        toInfectQueue.add(randomClass);
                        break;
                    }
                }
                //if the queue is still empty then all the users we randomly chose did not meet the requirements
                //but lets just use it anyways (so we don't get stuck) and force infect that user
                if (toInfectQueue.isEmpty()) {
                    toInfectQueue.add(randomClass);
                    forceInfect = true;
                }
            }
            Class classToInfect = toInfectQueue.poll();
            //if forceinfect is true, it means it will disregard any heuristics and just infect it to avoid
            //the case in which we go on a never ending loop because all nodes in the system are "bad" according
            //to the heuristic
            if (forceInfect) {
                infect(classToInfect, newSiteVersion, toInfectQueue);
                forceInfect = false;
            } else {
                //the size limit serves two purposes:
                //1) calculating 'getNumStudentsAffected' is expensive and the runtime is O(num of connected classes)
                //   so we want to avoid nodes that will be very expensive
                //2) we probably don't want to infect a person that will touch > 10k classes anyways
                if (classToInfect.getConnectedClasses().size() < sizeLimit) {
                    // if we are nearing our target percentage, we want to start being picky about the type of classes we choose to infect
                    // we only want to infect classes that will AFFECT a small amount of people,
                    // where "AFFECT" means the number of NON-infected students in all of the surrounding connected classes
                    //(aka this is the number of people who will be affected by this classroom getting infected)
                    if (isPercentageInfectedWithinTargetRange(targetPercentage)) {
                        int numAffected = classToInfect.getNumStudentsAffected();
                        if (numAffected < getAffectedThreshold()) {
                            infect(classToInfect, newSiteVersion, toInfectQueue);
                        }
                    } else {
                        infect(classToInfect, newSiteVersion, toInfectQueue);
                    }
                }
            }
        }
    }

    /**
     * Will completely infect the classToInfect students and teacher with the given site version.
     * Adds the connected classes to the queue
     * @param classToInfect
     * @param newSiteVersion
     * @return Will return a list of all classes that users in the class are connected to
     */
    private void infect(Class classToInfect, String newSiteVersion, Queue toInfectQueue) {
        for (User user : classToInfect.getAllUsers()) {
            if (!user.getSiteVersion().equals(newSiteVersion)) {
                user.setSiteVersion(newSiteVersion);
                infectedUsers.add(user);
                classToInfect.increaseNumStudentsInfected();
            }
        }
        toInfectQueue.addAll(classToInfect.getConnectedClasses());
    }

    private float getTotalPercentageInfected() {
        return (float) infectedUsers.size() / (float) allUsersList.size();
    }

    /**
     * Calculates whether the total percentage of infected users is within some delta of the target percentage
     * @param targetPercentage
     * @return
     */
    private boolean isPercentageInfectedWithinTargetRange(float targetPercentage) {
        return Math.abs(targetPercentage - getTotalPercentageInfected()) < delta;
    }
}
