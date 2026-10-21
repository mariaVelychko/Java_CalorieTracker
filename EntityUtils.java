import java.util.Collection;

public final class EntityUtils {
    private EntityUtils() {
    }

    /**
     * Приймає Collection<? extends BaseEntity> — "читати можна будь-якого
     * нащадка BaseEntity". Якби параметр був Collection<BaseEntity> без
     * wildcard, сюди не можна було б передати List<FruitProduct>: дженерики
     * в Java не коваріантні (List<FruitProduct> — НЕ підтип List<BaseEntity>),
     * хоча сам FruitProduct є підтипом BaseEntity. Wildcard "? extends"
     * дозволяє саме це: приймати список будь-якого конкретного нащадка.
     */
    public static void printAll(Collection<? extends BaseEntity> items) {
        for (BaseEntity item : items) {
            System.out.println(item);
        }
    }
}
