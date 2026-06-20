package edu.franklin.cecas.repository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void testSavePersistsCategory() {
        Category category = new Category();
        category.setCategoryName("Participation");
        category.setDescription("Points for class participation");
        category.setDefaultPoints(10);

        Category savedCategory = categoryRepository.save(category);

        assertThat(savedCategory.getCategoryId()).isNotNull();
        assertThat(savedCategory.getCategoryName()).isEqualTo("Participation");
        assertThat(savedCategory.getDescription()).isEqualTo("Points for class participation");
        assertThat(savedCategory.getDefaultPoints()).isEqualTo(10);
    }

    // Adding an additional test

    @Test
    public void testFindByCategoryName() {
        Category category = new Category();
        category.setCategoryName("Participation");
        category.setDescription("Points for class participation");
        category.setDefaultPoints(10);

        categoryRepository.save(category);

        Optional<Category> result = categoryRepository.findByCategoryNameIgnoreCase("Participation");

        assertThat(result).isPresent();
        assertThat(result.get().getCategoryName()).isEqualTo("Participation");
    }

    @Test
public void testFindAllByIsActiveTrue() {

    Category active1 = new Category();
    active1.setCategoryName("Participation");
    active1.setDescription("Points for participation");
    active1.setDefaultPoints(10);
    active1.setActive(true);

    Category active2 = new Category();
    active2.setCategoryName("Homework");
    active2.setDescription("Homework submissions");
    active2.setDefaultPoints(20);
    active2.setActive(true);

    Category inactive = new Category();
    inactive.setCategoryName("Old Category");
    inactive.setDescription("Should not appear");
    inactive.setDefaultPoints(5);
    inactive.setActive(false);

    categoryRepository.saveAll(List.of(active1, active2, inactive));

    List<Category> results = categoryRepository.findAllByIsActiveTrue();

    // only active ones should be returned
    assertThat(results).hasSize(2);

    assertThat(results)
        .allMatch(Category::isActive);

    assertThat(results)
        .extracting(Category::getCategoryName)
        .containsExactlyInAnyOrder("Participation", "Homework");
}
}
