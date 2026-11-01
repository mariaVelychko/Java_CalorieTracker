package calorietracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Objects;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "fruit_product")
public class FruitProduct extends MealProduct {
    @Column(name = "sugar_grams", nullable = false, precision = 5, scale = 2)
    @JdbcTypeCode(SqlTypes.NUMERIC)
    private double sugarGrams;

    @Column(name = "fresh", nullable = false)
    private boolean fresh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private FoodCategory category;

    @Transient
    private Long categoryId;

    protected FruitProduct() {
    }

    public FruitProduct(long id, String name, int weightGrams,
                        double caloriesPer100g,
                        double proteinPer100g,
                        String mealType,
                        double sugarGrams) {
        this(id, name, weightGrams, caloriesPer100g, proteinPer100g,
             mealType, sugarGrams, true);
    }

    public FruitProduct(long id, String name, int weightGrams,
                        double caloriesPer100g,
                        double proteinPer100g,
                        String mealType,
                        double sugarGrams,
                        boolean fresh) {
        this(id, name, weightGrams, caloriesPer100g, proteinPer100g,
             mealType, sugarGrams, fresh, null);
    }

    public FruitProduct(long id, String name, int weightGrams,
                        double caloriesPer100g,
                        double proteinPer100g,
                        String mealType,
                        double sugarGrams,
                        boolean fresh,
                        Long categoryId) {
        super(id, name, weightGrams, caloriesPer100g, proteinPer100g, mealType);
        this.sugarGrams = sugarGrams;
        this.fresh = fresh;
        this.categoryId = categoryId;
    }

    @Override
    public String getFoodDescription() {
        return "Фрукт із вмістом цукру " + sugarGrams + " г";
    }

    @Override
    public String toString() {
        return super.toString() + ", цукор: " + sugarGrams + " г";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FruitProduct other)) return false;
        return super.equals(o)
                && Double.compare(sugarGrams, other.sugarGrams) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sugarGrams);
    }

    public double getSugarGrams() {
        return sugarGrams;
    }

    public boolean isFresh() {
        return fresh;
    }

    public Long getCategoryId() {
        return category != null ? Long.valueOf(category.getId()) : categoryId;
    }

    public FoodCategory getCategory() {
        return category;
    }

    void setCategory(FoodCategory category) {
        this.category = category;
    }

    public FruitProduct withCategory(Long newCategoryId) {
        return new FruitProduct(getId(), getName(), getWeightGrams(), getCaloriesPer100g(),
                getProteinPer100g(), getMealType(), sugarGrams, fresh, newCategoryId);
    }
}
