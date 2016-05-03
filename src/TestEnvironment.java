import java.util.*;

public class TestEnvironment {

    private HashSet<User> allUsersSet;
    private List<User> allUsersList;
    private List<Class> allClasses;
    private HashSet<User> infectedUsers;
    private String baseSiteVersion;
    private boolean debug;
    private float delta;
    private float infectThreshold;
    private int hardLimitFactor;

    public TestEnvironment(String baseSiteVersion, boolean debug) {
        this.baseSiteVersion = baseSiteVersion;
        this.allClasses = new ArrayList<>();
        this.allUsersSet = new HashSet<>();
        this.allUsersList = new ArrayList<>();
        this.debug = debug;
        this.infectedUsers = new HashSet<>();
        this.delta = 0.05f;
        this.infectThreshold = 0.5f;
        this.hardLimitFactor = 2;
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

    public float getHardLimit(float targetRange) {
        return targetRange + delta * hardLimitFactor;
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

    public void totalInfection(User firstInfectedUser, String newSiteVersion) {
        Queue<User> allUsersToInfect = new LinkedList<>();
        allUsersToInfect.add(firstInfectedUser);

        while (allUsersToInfect.size() > 0) {
            User userToInfect = allUsersToInfect.remove();
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

    public void limitedInfection(User firstInfectedUser, String newSiteVersion, float targetPercentage) {
        Comparator<Class> classInfectedComparator = new ClassInfectedComparator();
        Queue<Class> maxHeap = new PriorityQueue<Class>(10, classInfectedComparator.reversed());
        maxHeap.addAll(firstInfectedUser.getAllClasses());
        while (!maxHeap.isEmpty() || !isPercentageInfectedWithinTargetRange(targetPercentage)) {
            //stop if we get too far off the target
            if (getTotalPercentageInfected() > getHardLimit(targetPercentage)) {
                return;
            }
            // if it's empty but still hasn't hit the target percentage yet, then choose another random user
            if (maxHeap.isEmpty()) {
                User notInfectedUser = getRandomNotInfectedUser();
                maxHeap.addAll(notInfectedUser.getAllClasses());
            }
            Class classToInfect = maxHeap.poll();
            //if you are in the target range its time to be picky
            if (isPercentageInfectedWithinTargetRange(targetPercentage)) {
                if (classToInfect.getPercentageStudentsInfected() > infectThreshold) {
                    if (!classToInfect.isCompletelyInfected()) { // just a check for performance
                        List<Class> allConnectedClasses = infect(classToInfect, newSiteVersion);
                        maxHeap.addAll(allConnectedClasses);
                    }
                } else { //if we passed the threshold, it means there are no more items in the queue that are greater than the threshold
                    return; //so it's time to stop infecting, since we're within the target anyways
                }
            } else { //otherwise just infect that classroom
                if (!classToInfect.isCompletelyInfected()) {
                    List<Class> allConnectedClasses = infect(classToInfect, newSiteVersion);
                    maxHeap.addAll(allConnectedClasses);
                }
            }
        }
    }

    private List<Class> infect(Class classToInfect, String newSiteVersion) {
        List<Class> allConnectedClasses = new ArrayList<>();
        for (User user : classToInfect.getAllUsers()) {
            if (!user.getSiteVersion().equals(newSiteVersion)) {
                user.setSiteVersion(newSiteVersion);
                infectedUsers.add(user);
                classToInfect.increaseNumStudentsInfected();
                allConnectedClasses.addAll(user.getAllClasses());
            }
        }
        return allConnectedClasses;
    }

    private float getTotalPercentageInfected() {
        return (float) infectedUsers.size() / (float) allUsersList.size();
    }

    private boolean isPercentageInfectedWithinTargetRange(float targetPercentage) {
        return Math.abs(targetPercentage - getTotalPercentageInfected()) < delta;
    }

    public class ClassInfectedComparator implements Comparator<Class>
    {
        @Override
        public int compare(Class x, Class y)
        {
            if (x.getPercentageStudentsInfected() < y.getPercentageStudentsInfected())
            {
                return -1;
            }
            if (x.getPercentageStudentsInfected() > y.getPercentageStudentsInfected())
            {
                return 1;
            }
            return 0;
        }
    }
}
