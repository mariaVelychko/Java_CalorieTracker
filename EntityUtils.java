import java.util.Collection;

public final class EntityUtils {
    private EntityUtils() {
    }
    
    public static void printAll(Collection<? extends BaseEntity> items) {
        for (BaseEntity item : items) {
            System.out.println(item);
        }
    }
}
