/**
 * Оцінка порції за вмістом білка. Перевизначає default-метод shortLabel().
 */
public class ProteinAdviceStrategy extends AbstractAdviceStrategy {
    private static final double HIGH_PROTEIN = 30.0;
    private static final double LOW_PROTEIN = 15.0;

    @Override
    public String evaluate(Product product) {
        double protein = product.getTotalProtein();
        if (protein >= HIGH_PROTEIN) {
            return format("Білок", "Високобілкова порція.");
        } else if (protein >= LOW_PROTEIN) {
            return format("Білок", "Помірно білкова порція.");
        }
        return format("Білок", "Низькобілкова порція.");
    }

    @Override
    public String shortLabel() {
        return "Білкова оцінка";
    }
}
