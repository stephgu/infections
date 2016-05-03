import java.util.ArrayList;
import java.util.List;

public class Class {

    private List<User> students;
    private User teacher;
    private int numStudentsInfected;

    public Class(User teacher) {
        teachClass(teacher);
        this.students = new ArrayList<>();
        numStudentsInfected = 0;
    }

    public Class(User teacher, List<User> students) {
        this(teacher);
        for (User student : students) {
            enrollClass(student);
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

    public void enrollClass(User student) {
        students.add(student);
        student.addClassToTake(this);
    }

    public void teachClass(User newTeacher) {
        teacher = newTeacher;
        newTeacher.addClassToTeach(this);
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
