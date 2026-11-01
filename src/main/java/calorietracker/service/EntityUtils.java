package calorietracker.service;

import java.util.Collection;
import calorietracker.model.BaseEntity;

public final class EntityUtils {
    private EntityUtils() {
    }

    public static void printAll(Collection<? extends BaseEntity> items) {
        for (BaseEntity item : items) {
            System.out.println(item);
        }
    }
}
