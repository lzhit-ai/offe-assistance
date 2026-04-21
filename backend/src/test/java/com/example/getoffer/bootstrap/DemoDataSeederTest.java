package com.example.getoffer.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.getoffer.repository.ArticleRepository;
import com.example.getoffer.repository.FavoriteRepository;
import com.example.getoffer.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class DemoDataSeederTest {

    @Autowired
    private DemoDataSeeder demoDataSeeder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @BeforeEach
    void setUp() {
        favoriteRepository.deleteAll();
        articleRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void seedIfEmptyCreatesStarterUsersArticlesAndFavorites() {
        demoDataSeeder.seedIfEmpty();

        assertEquals(3, userRepository.count());
        assertEquals(4, articleRepository.count());
        assertEquals(4, favoriteRepository.count());
        assertTrue(userRepository.existsByUsername("demo_admin"));
        assertTrue(userRepository.existsByUsername("frontend_offer"));
        assertTrue(userRepository.existsByUsername("backend_offer"));
        assertTrue(articleRepository.findCategoryCounts().size() >= 2);
    }

    @Test
    void seedIfEmptyIsIdempotent() {
        demoDataSeeder.seedIfEmpty();
        demoDataSeeder.seedIfEmpty();

        assertEquals(3, userRepository.count());
        assertEquals(4, articleRepository.count());
        assertEquals(4, favoriteRepository.count());
    }
}
