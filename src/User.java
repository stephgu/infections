import java.util.ArrayList;
import java.util.List;

public class User {

    private List<Class> teachingClasses;
    private List<Class> takingClasses;
    private String siteVersion;
    private String name; //unique Identifier

    public User(String name, String siteVersion) {
        this.name = name;
        this.siteVersion = siteVersion;
        this.teachingClasses = new ArrayList<>();
        this.takingClasses = new ArrayList<>();
    }

    protected void addClassToTake(Class classToTake ) {
        takingClasses.add(classToTake);
    }

    protected void addClassToTeach(Class classToTeach) {
        teachingClasses.add(classToTeach);
    }

    public List<Class> getTeachingClasses() {
        return teachingClasses;
    }

    public List<Class> getTakingClasses() {
        return takingClasses;
    }

    public List<Class> getAllClasses() {
        List<Class> allClasses = new ArrayList<>();
        allClasses.addAll(takingClasses);
        allClasses.addAll(teachingClasses);
        return allClasses;
    }

    public void setSiteVersion(String newSiteVersion) {
        this.siteVersion = newSiteVersion;
    }

    public String getSiteVersion() {
        return siteVersion;
    }

    public String getName() {
        return name;
    }
}
