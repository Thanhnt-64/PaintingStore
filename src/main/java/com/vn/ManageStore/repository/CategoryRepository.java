package com.vn.ManageStore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.vn.ManageStore.domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * Find category by slug
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Find category by ID with eager loading of parent and children
     */
    @Query("SELECT c FROM Category c " +
           "LEFT JOIN FETCH c.parent " +
           "LEFT JOIN FETCH c.children " +
           "WHERE c.categoryId = :id")
    Optional<Category> findByIdWithHierarchy(@Param("id") Integer id);
}
