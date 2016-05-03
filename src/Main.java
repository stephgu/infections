import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String startSiteVersion = "00000";
        String newSiteVersion = "00001";

        //creating class 1 and adding the teacher, then students
        User teacher1 = new User("aa", startSiteVersion);

        String[] studentNames = {"a", "b", "c", "d", "e", "f", "g"};
        List<User> class1Students = new ArrayList<>();
        for (int i = 0; i < studentNames.length; i++) {
            User user = new User(studentNames[i], startSiteVersion);
            class1Students.add(user);
        }
        Class class1 = new Class(teacher1, class1Students);
        class1.prettyPrintClass();

        //creating class 2 and adding another teacher, then student a from class 1
        User teacher2 = new User("bb", startSiteVersion);
        List<User> class2Students = new ArrayList<>();
        class2Students.add(class1Students.get(0));
        Class class2 = new Class(teacher2, class2Students);
        class2.prettyPrintClass();

        //creating class 3 from user c and adding new students
        User teacher3 = class1Students.get(2);
        List<User> class3Students = new ArrayList<>();
        class3Students.add(class1Students.get(3));
        class3Students.add(new User("z", startSiteVersion));
        class3Students.add(new User("y", startSiteVersion));
        Class class3 = new Class(teacher3, class3Students);
        class3.prettyPrintClass();

        //setting up the test environment
        System.out.println("\nStarting test!");
        TestEnvironment testEnv = new TestEnvironment(startSiteVersion, true);
        testEnv.addClass(class1);
        testEnv.addClass(class2);
        testEnv.addClass(class3);

        //running total infection
        User randUser = testEnv.getRandomNotInfectedUser();
        System.out.println("\nRandom user: " + randUser.getName());
//        testEnv.totalInfection(randUser, newSiteVersion);
        testEnv.limitedInfection(randUser, newSiteVersion, 0.25f);
        testEnv.prettyPrintInfectedUsers();
    }
}
