import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static junit.framework.TestCase.assertEquals;

public class TestEnvironmentTest {
    private String baseSiteVersion = "11";
    private Class testClassroom;
    private TestEnvironment testEnv;
    private int numStudents = 100000;

    @Before
    public void setUp() {
        //create 10 students
        List<User> students = new ArrayList<>();
        for (int i = 0; i < numStudents; i++) {
            students.add(new User(i+"", baseSiteVersion));
        }
        User teacher = new User("teacher", baseSiteVersion);
        testClassroom = new Class(teacher, students);

        testEnv = new TestEnvironment(baseSiteVersion, true);
        testEnv.addClass(testClassroom);
    }

    @Test
    public void testSetAllConnected() throws Exception {
        //create another classroom with 5 students
        List<User> students = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            students.add(new User(i+"test", baseSiteVersion));
        }
        User teacher = new User("teacher2", baseSiteVersion);
        Class otherClassroom = new Class(teacher, students);

        //make 2 of the students from our testclassroom also be a students of this class
        otherClassroom.enrollStudent(testClassroom.getStudents().get(0));
        otherClassroom.enrollStudent(testClassroom.getStudents().get(1));

        testEnv.addClass(otherClassroom);
        testEnv.setAllConnectedClasses();
        //there should only be 1 connected class total
        assertEquals(1, testClassroom.getConnectedClasses().size());
        assertEquals(1, otherClassroom.getConnectedClasses().size());
    }

    @Test
    public void testSetAllConnectedNoneConnected() throws Exception {
        //create another classroom with 5 students
        List<User> students = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            students.add(new User(i+"test", baseSiteVersion));
        }
        User teacher = new User("teacher2", baseSiteVersion);
        Class otherClassroom = new Class(teacher, students);

        testEnv.addClass(otherClassroom);
        testEnv.setAllConnectedClasses();
        //there should only be 0 connected class total
        assertEquals(0, testClassroom.getConnectedClasses().size());
        assertEquals(0, otherClassroom.getConnectedClasses().size());
    }

    @Test
    public void testGetAffectedThreshold() throws Exception {
        //numStudents * 0.005
        assertEquals((int) (numStudents * 0.005), testEnv.getAffectedThreshold());
    }

    @Test
    public void testTotalPercentInfected() throws Exception {
        for (int i = 0; i < numStudents * 0.24; i++) {
            testEnv.getInfectedUsers().add(testClassroom.getStudents().get(i));
        }
        assertEquals(true, 0.24-testEnv.getTotalPercentageInfected() < 0.01);
    }

    @Test
    public void testIsPercentInfectedWithinTarget() throws Exception {
        for (int i = 0; i < numStudents * 0.21; i++) {
            testEnv.getInfectedUsers().add(testClassroom.getStudents().get(i));
        }
        assertEquals(true, testEnv.isPercentageInfectedWithinTargetRange(0.25f));
    }

    @Test
    public void testOneClassroomInfect() throws Exception {
        //create another classroom with 5 students
        List<User> students = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            students.add(new User(i+"test", baseSiteVersion));
        }
        User teacher = new User("teacher2", baseSiteVersion);
        Class otherClassroom = new Class(teacher, students);

        //make 2 of the students from our testclassroom also be a students of this class
        otherClassroom.enrollStudent(testClassroom.getStudents().get(0));
        otherClassroom.enrollStudent(testClassroom.getStudents().get(1));

        testEnv.addClass(otherClassroom);
        testEnv.setAllConnectedClasses();
        //
        Queue toInfectQueue = new LinkedList<>();
        testEnv.infect(testClassroom, "12", toInfectQueue);

        assertEquals(true, testClassroom.isCompletelyInfected());
        assertEquals(false, otherClassroom.isCompletelyInfected());
        assertEquals(1, toInfectQueue.size());
    }


    @Test
    public void testTotalInfect() throws Exception {
        //create another classroom with 5 students
        List<User> students = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            students.add(new User(i+"test", baseSiteVersion));
        }
        User teacher = new User("teacher2", baseSiteVersion);
        Class otherClassroom = new Class(teacher, students);

        //make 2 of the students from our testclassroom also be a students of this class
        otherClassroom.enrollStudent(testClassroom.getStudents().get(0));
        otherClassroom.enrollStudent(testClassroom.getStudents().get(1));

        testEnv.addClass(otherClassroom);

        //
        testEnv.totalInfection("12");
        assertEquals(1.0f, testEnv.getTotalPercentageInfected());
    }

    @Test
    public void testLimitedInfection() throws Exception {
        //create special test environment
        TestEnvironment limitTestEnv = new TestEnvironment(baseSiteVersion, true);
        List<Class> randomClasses = createRandomClasses(1000);
        jumble(randomClasses);

        for (Class rc : randomClasses) {
            limitTestEnv.addClass(rc);
        }
        limitTestEnv.limitedInfection("12", 0.25f);

        int completelyInfectedClass = 0;
        int smallInfectedClass = 0;
        int badlyInfectedClass = 0;
        for (Class c : limitTestEnv.getAllClasses()) {
            if (c.isCompletelyInfected()) {
                completelyInfectedClass += 1;
            } else if (c.getPercentageStudentsInfected() < 0.005) {
                smallInfectedClass += 1;
            } else {
                badlyInfectedClass += 1;
            }
        }

        float percentCompletelyInfectedClasses = (float) completelyInfectedClass / (float) limitTestEnv.getAllClasses().size();
        float percentSmallInfectedClasses = (float) smallInfectedClass / (float) limitTestEnv.getAllClasses().size();
        float percentBadlyInfectedClasses = (float) badlyInfectedClass / (float) limitTestEnv.getAllClasses().size();
        System.out.println("percent completely infected classes: " + percentCompletelyInfectedClasses);
        System.out.println("percent minor infected classes: " + percentSmallInfectedClasses);
        System.out.println("percent badly infected classes: " + percentBadlyInfectedClasses);
        System.out.println("percent of total infected users: " + limitTestEnv.getTotalPercentageInfected());
        //I noticed usually the badly infected classes is very very low (aka all classes above 0.5% infection are low)
        //Majority of classes only have a couple students infected
        //Hit very close to the target percentage of 25%
        //Time to run, < 2 seconds
    }

    private List<Class> createRandomClasses(int numTotalClasses) {
        List<Class> allClasses = new ArrayList<>();
        Random random = new Random();
        int maxClassSize = 1000;
        for (int i = 0; i < numTotalClasses; i++) {
            int classSize = random.nextInt(maxClassSize);
            List<User> students = new ArrayList<>(classSize);
            for (int j = 0; j < classSize; j++) {
                students.add(new User(j+","+i+"student", baseSiteVersion));
            }
            Class classroom = new Class( new User(i+"teacher", baseSiteVersion), students);
            allClasses.add(classroom);
        }
        return allClasses;
    }

    /**
     * Basically draws a bunch of random student/teacher relationships between random classes
     * @param classes
     */
    private void jumble(List<Class> classes) {
        Random random = new Random();
        for (Class classroom : classes) {
            int anotherClassroomIndex = random.nextInt(classes.size());
            Class anotherClassroom = classes.get(anotherClassroomIndex);
            int numRogueStudents = random.nextInt(classroom.getAllUsers().size());
            int numRogueTeachers = random.nextInt(classroom.getAllUsers().size()/4+1);
            for (int rs = 0; rs < numRogueStudents; rs++) {
                anotherClassroom.enrollStudent(classroom.getAllUsers().get(rs));
            }
            anotherClassroom.enrollTeacher(classroom.getAllUsers().get(numRogueTeachers));
        }
    }
}