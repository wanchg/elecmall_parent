import com.alibaba.nacos.common.util.Md5Utils;

public class PasswordTest {
    public static void main(String[] args) {
        String md5 = Md5Utils.getMD5("111111".getBytes());
        System.out.println(md5);
    }
}
