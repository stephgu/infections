import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class ClassTest {
    private String baseSiteVersion = "11";
    private Class testClassroom;

    @Before
    public void setUp() {
        //create 10 students
        List<User> students = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            students.add(new User(i+"", baseSiteVersion));
        }
        User teacher = new User("teacher", baseSiteVersion);
        testClassroom = new Class(teacher, students);
    }

    @Test
    public void testCalculatingPercentageInfected() throws Exception {
        testClassroom.increaseNumStudentsInfected();
        testClassroom.increaseNumStudentsInfected();
        float actual = testClassroom.getPercentageStudentsInfected();
        assertEquals(0.2f, actual );
    }

    @Test
    public void testCalculatingNumAffectedStudents() throws Exception {
        //create another classroom with 5 students
        List<User> students = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            students.add(new User(i+"test", baseSiteVersion));
        }
        User teacher = new User("teacher2", baseSiteVersion);
        Class otherClassroom = new Class(teacher, students);
        //infect the other classroom
        otherClassroom.increaseNumStudentsInfected();
        otherClassroom.increaseNumStudentsInfected();

        //make one of the students from our testclassroom also be a student of this class
        otherClassroom.enrollStudent(testClassroom.getStudents().get(0));
        testClassroom.getConnectedClasses().add(otherClassroom);

        //5 students in other classroom originally, +1 for new enrolled, then -2 for infected = 4
        assertEquals(4, testClassroom.getNumStudentsAffected());

    }

}