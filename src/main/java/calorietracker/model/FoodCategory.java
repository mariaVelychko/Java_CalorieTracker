package calorietracker.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "food_category")
public class FoodCategory extends BaseEntity {
    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<FruitProduct> fruits = new ArrayList<>();

    protected FoodCategory() {
    }

    public FoodCategory(long id, String name, String description) {
        super(id, name);
        this.description = description;
    }

    public FoodCategory(long id, String name) {
        this(id, name, null);
    }

    public String getDescription() {
        return description;
    }

    public List<FruitProduct> getFruits() {
        return Collections.unmodifiableList(fruits);
    }

    public void addFruit(FruitProduct fruit) {
        fruits.add(fruit);
        fruit.setCategory(this);
    }

    public void removeFruit(FruitProduct fruit) {
        fruits.remove(fruit);
        fruit.setCategory(null);
    }

    @Override
    public String toString() {
        return super.toString() + (description == null ? "" : " — " + description);
    }
}
