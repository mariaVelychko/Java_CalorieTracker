package calorietracker.observer;

public class ConsoleMealObserver implements MealLogListener {
    public void onMealAdded(MealAddedEvent event) {
        System.out.println("[Observer: журнал] Додано: " + event.getProduct().toReportLine());
    }
}
