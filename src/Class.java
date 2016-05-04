import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Class {

    private List<User> students;
    private User teacher;
    private int numStudentsInfected;
    private Set<Class> connectedClasses;

    public Class(User teacher) {
        this.students = new ArrayList<>();
        this.numStudentsInfected = 0;
        this.connectedClasses = new HashSet<>();
        enrollTeacher(teacher);
    }

    public Class(User teacher, List<User> students) {
        this(teacher);
        for (User student : students) {
            enrollStudent(student);
        }
    }

    public boolean isCompletelyInfected() {
        return numStudentsInfected >= students.size();
    }

    public void increaseNumStudentsInfected() {
        numStudentsInfected++;
    }

    public float getPercentageStudentsInfected() {
        return (float) numStudentsInfected / (float) students.size();
    }

    public int getNumStudentsInfected() {
        return numStudentsInfected;
    }

    public int getNumStudentsNotInfected() {
        return students.size() - numStudentsInfected;
    }

    public int getNumStudentsAffected() {
        int totalAffected = 0;
        for (Class connectedClass : connectedClasses) {
            totalAffected += connectedClass.getNumStudentsNotInfected();
        }
        return totalAffected;
    }

    public Set<Class> getConnectedClasses() {
        return connectedClasses;
    }

    public List<User> getStudents() {
        return students;
    }

    public User getTeacher() {
        return teacher;
    }

    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        allUsers.add(teacher);
        allUsers.addAll(students);
        return allUsers;
    }

    public void enrollStudent(User student) {
        students.add(student);
        student.addClassToTake(this);
        connectedClasses.addAll(student.getAllClasses());
    }

    public void enrollTeacher(User newTeacher) {
        teacher = newTeacher;
        newTeacher.addClassToTeach(this);
        connectedClasses.addAll(teacher.getAllClasses());
    }

    public void prettyPrintClass() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("The teacher of the class is: \t%s\n", teacher.getName()));
        sb.append("The students in the class are: \n");
        for (User student : students) {
            sb.append("\t" + student.getName() + "\n");
        }

        System.out.print(sb.toString());
    }
}
