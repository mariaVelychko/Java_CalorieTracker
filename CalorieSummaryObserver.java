public class CalorieSummaryObserver implements MealLogListener {
    private int observedSize;
    private double observedTotal;
    public void onMealAdded(MealAddedEvent event) {
        observedSize = event.getSize(); observedTotal = event.getTotalCalories();
        System.out.printf("[Observer: підсумок] Порцій: %d; всього: %.2f ккал%n", observedSize, observedTotal);
    }
    public int getObservedSize() { return observedSize; }
    public double getObservedTotal() { return observedTotal; }
}
