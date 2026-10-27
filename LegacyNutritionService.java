public final class LegacyNutritionService {
    public int classifyServing(double energyKilojoules, double proteinMilligrams) {
        if (energyKilojoules > 2928.8) return 2;
        if (proteinMilligrams >= 30000) return 1;
        return 0;
    }
}
