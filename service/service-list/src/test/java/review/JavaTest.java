package review;



public class JavaTest {

    public static void main(String[] args) {
        User user = new User();
        user.setName("123");
        Student student = new Student();
        student.setName(user.getName());
        user.setName("123"+456);
        System.out.println(student);
    }
}
