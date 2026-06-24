package edu.franklin.cecas.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.repository.CategoryRepository;
import edu.franklin.cecas.support.MySqlDataJpaTest;

@MySqlDataJpaTest
@Import(CategorySeedImporter.class)
public class CategorySeedImporterTest {

    @Autowired
    private CategorySeedImporter importer;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category createCategory(String categoryName, String description,
            int defaultPoints, boolean active) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        category.setDescription(description);
        category.setDefaultPoints(defaultPoints);
        category.setActive(active);
        return categoryRepository.save(category);
    }

    private Category findCategoryByNameOrThrow(String name) {
        return categoryRepository.findByCategoryNameIgnoreCase(name).orElseThrow();
    }

    /**
     * Verifies that a new category is inserted in the database.
     */
    @Test
    void testImportCategoriesInsertsNewCategory() {
        CategorySeedImportResult result = importer.importCategories(List.of(
                new CategorySeedRow("Participation", "Answering a question", 10)));

        List<Category> categories = categoryRepository.findAll();

        assertThat(categories).hasSize(1);

        Category savedCategory = categories.get(0);

        assertThat(savedCategory.getCategoryName()).isEqualTo("Participation");
        assertThat(savedCategory.getDescription()).isEqualTo("Answering a question");
        assertThat(savedCategory.getDefaultPoints()).isEqualTo(10);
        assertThat(savedCategory.isActive()).isTrue();
        assertThat(savedCategory.getCategoryId()).isNotNull();

        assertThat(result).isEqualTo(new CategorySeedImportResult(1, 0, 0, 0, 0));
    }

    /**
     * Verifies that a matching inactive category is reactivated and counted only as
     * reactivated.
     */
    @Test
    void testReactivatesInactiveCategoryWhenRestored() {
        Category existing = createCategory(
                "seminar attendance",
                "Old description",
                5,
                false);

        List<CategorySeedRow> rows = List.of(
                new CategorySeedRow("Seminar Attendance", "Updated description", 10));

        CategorySeedImportResult result = importer.importCategories(rows);

        Category updated = findCategoryByNameOrThrow("Seminar Attendance");

        assertEquals(existing.getCategoryId(), updated.getCategoryId());
        assertTrue(updated.isActive());
        assertEquals("Seminar Attendance", updated.getCategoryName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(10, updated.getDefaultPoints());

        assertEquals(0, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(0, result.unchanged());
        assertEquals(1, result.reactivated());
        assertEquals(0, result.deactivated());
    }

    /**
     * Verifies that a capitalization-only name change updates the existing category
     * rather than creating a new row.
     */
    @Test
    void testCapitalizationOnlyChangeUpdatesExistingCategory() {
        Category existing = createCategory(
                "seminar attendance",
                "Approved seminar attendance",
                5,
                true);

        List<CategorySeedRow> rows = List.of(
                new CategorySeedRow("Seminar Attendance", "Approved seminar attendance", 5));

        CategorySeedImportResult result = importer.importCategories(rows);

        Category updated = findCategoryByNameOrThrow("Seminar Attendance");

        assertEquals(existing.getCategoryId(), updated.getCategoryId());
        assertEquals(1, categoryRepository.findAll().size());
        assertEquals("Seminar Attendance", updated.getCategoryName());

        assertEquals(0, result.inserted());
        assertEquals(1, result.updated());
        assertEquals(0, result.unchanged());
        assertEquals(0, result.reactivated());
        assertEquals(0, result.deactivated());
    }

    /**
     * Verifies that description and default points are updated for an existing
     * active category.
     */
    @Test
    void testUpdatesDescriptionAndDefaultPointsForExistingActiveCategory() {
        Category existing = createCategory(
                "Seminar Attendance",
                "Old description",
                5,
                true);

        List<CategorySeedRow> rows = List.of(
                new CategorySeedRow("Seminar Attendance", "New description", 12));

        CategorySeedImportResult result = importer.importCategories(rows);

        Category updated = findCategoryByNameOrThrow("Seminar Attendance");

        assertEquals(existing.getCategoryId(), updated.getCategoryId());
        assertEquals("New description", updated.getDescription());
        assertEquals(12, updated.getDefaultPoints());

        assertEquals(0, result.inserted());
        assertEquals(1, result.updated());
        assertEquals(0, result.unchanged());
        assertEquals(0, result.reactivated());
        assertEquals(0, result.deactivated());
    }

    /**
     * Verifies that an existing active category with matching values is counted as
     * unchanged.
     */
    @Test
    void testCountsExistingActiveCategoryAsUnchangedWhenValuesMatch() {
        createCategory(
                "Seminar Attendance",
                "Approved seminar attendance",
                5,
                true);

        List<CategorySeedRow> rows = List.of(
                new CategorySeedRow("Seminar Attendance", "Approved seminar attendance", 5));

        CategorySeedImportResult result = importer.importCategories(rows);

        assertEquals(1, categoryRepository.findAll().size());

        assertEquals(0, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(1, result.unchanged());
        assertEquals(0, result.reactivated());
        assertEquals(0, result.deactivated());
    }

    /**
     * Verifies that active categories missing from the seed data are marked
     * inactive rather than deleted.
     */
    @Test
    void testDeactivatesActiveCategoriesMissingFromSeedData() {
        createCategory(
                "Seminar Attendance",
                "Approved seminar attendance",
                5,
                true);

        createCategory(
                "Workshop Participation",
                "Approved workshop participation",
                8,
                true);

        List<CategorySeedRow> rows = List.of(
                new CategorySeedRow("Seminar Attendance", "Approved seminar attendance", 5));

        CategorySeedImportResult result = importer.importCategories(rows);

        Category seminar = findCategoryByNameOrThrow("Seminar Attendance");
        Category workshop = findCategoryByNameOrThrow("Workshop Participation");

        assertTrue(seminar.isActive());
        assertFalse(workshop.isActive());
        assertEquals(2, categoryRepository.findAll().size());

        assertEquals(0, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(1, result.unchanged());
        assertEquals(0, result.reactivated());
        assertEquals(1, result.deactivated());
    }

    /**
     * Verifies that categories already inactive are not counted as deactivated
     * again when absent from the seed data.
     */
    @Test
    void testDoesNotCountAlreadyInactiveCategoryAsDeactivatedAgain() {
        createCategory(
                "Old Category",
                "Old inactive category",
                3,
                false);

        List<CategorySeedRow> rows = List.of(
                new CategorySeedRow("Seminar Attendance", "Approved seminar attendance", 5));

        CategorySeedImportResult result = importer.importCategories(rows);

        Category oldCategory = findCategoryByNameOrThrow("Old Category");
        Category seminar = findCategoryByNameOrThrow("Seminar Attendance");

        assertFalse(oldCategory.isActive());
        assertTrue(seminar.isActive());

        assertEquals(1, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(0, result.unchanged());
        assertEquals(0, result.reactivated());
        assertEquals(0, result.deactivated());
    }

    /**
     * Verifies that changing a category name to a different normalized match key
     * creates a new category and deactivates the previously active one.
     */
    @Test
    void testMeaningfulNameChangeCreatesNewCategoryAndDeactivatesOldCategory() {
        Category oldCategory = createCategory(
                "Seminar Attendance",
                "Approved seminar attendance",
                5,
                true);

        List<CategorySeedRow> rows = List.of(
                new CategorySeedRow("Conference Attendance", "Approved conference attendance", 10));

        CategorySeedImportResult result = importer.importCategories(rows);

        Category inactiveOldCategory = findCategoryByNameOrThrow("Seminar Attendance");
        Category newCategory = findCategoryByNameOrThrow("Conference Attendance");

        assertEquals(2, categoryRepository.findAll().size());
        assertEquals(oldCategory.getCategoryId(), inactiveOldCategory.getCategoryId());
        assertFalse(inactiveOldCategory.isActive());
        assertTrue(newCategory.isActive());
        assertTrue(!oldCategory.getCategoryId().equals(newCategory.getCategoryId()));

        assertEquals(1, result.inserted());
        assertEquals(0, result.updated());
        assertEquals(0, result.unchanged());
        assertEquals(0, result.reactivated());
        assertEquals(1, result.deactivated());
    }

    /**
     * Verifies that re-importing the same category seed data does not create
     * duplicate categories.
     */
    @Test
    void testImportCategoriesDoesNotCreateDuplicatesWhenReRunWithSameData() {
        List<CategorySeedRow> rows = List.of(
                new CategorySeedRow("Seminar Attendance", "Approved seminar attendance", 5));

        CategorySeedImportResult firstResult = importer.importCategories(rows);
        CategorySeedImportResult secondResult = importer.importCategories(rows);

        assertEquals(1, categoryRepository.findAll().size());

        assertEquals(1, firstResult.inserted());
        assertEquals(0, firstResult.updated());
        assertEquals(0, firstResult.unchanged());
        assertEquals(0, firstResult.reactivated());
        assertEquals(0, firstResult.deactivated());

        assertEquals(0, secondResult.inserted());
        assertEquals(0, secondResult.updated());
        assertEquals(1, secondResult.unchanged());
        assertEquals(0, secondResult.reactivated());
        assertEquals(0, secondResult.deactivated());
    }
}
