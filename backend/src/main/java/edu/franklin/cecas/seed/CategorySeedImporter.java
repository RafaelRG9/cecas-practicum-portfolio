package edu.franklin.cecas.seed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.repository.CategoryRepository;

@Component
public class CategorySeedImporter {
    private final CategoryRepository categoryRepository;

    public CategorySeedImporter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategorySeedImportResult importCategories(List<CategorySeedRow> rows) {
        List<Category> categories = categoryRepository.findAll();

        Map<String, Category> existingByName = new HashMap<>();
        Set<String> currentCategories = new HashSet<>();
        List<Category> toInsert = new ArrayList<>();
        List<Category> toUpdate = new ArrayList<>();

        int inserted = 0;
        int updated = 0;
        int unchanged = 0;
        int reactivated = 0;
        int deactivated = 0;

        for (Category category : categories) {
            existingByName.put(normalizeCategoryName(category.getCategoryName()), category);
        }

        for (CategorySeedRow row : rows) {
            String categoryName = normalizeCategoryName(row.categoryName());
            currentCategories.add(categoryName);

            Category existingCategory = existingByName.get(categoryName);

            if (existingCategory == null) {
                Category category = new Category();
                category.setCategoryName(row.categoryName().trim());
                category.setDescription(row.description());
                category.setDefaultPoints(row.defaultPoints());
                category.setActive(true);
                toInsert.add(category);
                inserted++;
            } else if (existingCategory != null && existingCategory.isActive() == false) {
                existingCategory.setActive(true);
                existingCategory.setCategoryName(row.categoryName().trim());
                existingCategory.setDescription(row.description());
                existingCategory.setDefaultPoints(row.defaultPoints());
                toUpdate.add(existingCategory);
                reactivated++;
            } else {
                boolean changed = false;

                String csvDisplayName = row.categoryName().trim();

                if (!existingCategory.getCategoryName().equals(csvDisplayName)) {
                    existingCategory.setCategoryName(csvDisplayName);
                    changed = true;
                }

                if (!existingCategory.getDescription().equals(row.description())) {
                    existingCategory.setDescription(row.description());
                    changed = true;
                }

                if (!existingCategory.getDefaultPoints().equals(row.defaultPoints())) {
                    existingCategory.setDefaultPoints(row.defaultPoints());
                    changed = true;
                }

                if (changed) {
                    toUpdate.add(existingCategory);
                    updated++;
                } else {
                    unchanged++;
                }
            }
        }

        for (Category category : categories) {
            String normalizedDbName = normalizeCategoryName(category.getCategoryName());

            if (!currentCategories.contains(normalizedDbName) && category.isActive()) {
                category.setActive(false);
                toUpdate.add(category);
                deactivated++;
            }
        }

        categoryRepository.saveAll(toInsert);
        categoryRepository.saveAll(toUpdate);

        return new CategorySeedImportResult(inserted, updated, unchanged, reactivated, deactivated);
    }

    private String normalizeCategoryName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
