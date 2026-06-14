package edu.franklin.cecas.repository;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;


import edu.franklin.cecas.domain.Category;
import edu.franklin.cecas.support.MySqlDataJpaTest;

// @MySqlDataJpaTest
// public class CategoryRepositoryTest {
    
//     @Autowired
//     private CategoryRepository categoryRepository;

//     @Test
//     @Disabled("Enable once CategoryRepository is implemented")
//     public void testSavePersistsCategory() {
//         Category category = new Category();
//         category.setCategoryName("Participation");
//         category.setDescription("Points for class participation");
//         category.setDefaultPoints(10);

//         Category savedCategory = categoryRepository.save(category);

//         assertThat(savedCategory.getCategoryId()).isNotNull();
//         assertThat(savedCategory.getCategoryName()).isEqualTo("Participation");
//         assertThat(savedCategory.getDescription()).isEqualTo("Points for class participation");
//         assertThat(savedCategory.getDefaultPoints()).isEqualTo(10);
//     }
// }
