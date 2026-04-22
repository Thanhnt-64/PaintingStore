package com.vn.ManageStore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.vn.ManageStore.domain.Artwork;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Integer> {

    /**
     * Find all featured artworks (is_featured = true)
     */
    List<Artwork> findByIsFeaturedTrue();

    /**
     * Find featured artworks with eager loading of artist and images
     */
    @Query("SELECT DISTINCT a FROM Artwork a " +
           "LEFT JOIN FETCH a.artist " +
           "LEFT JOIN FETCH a.images " +
           "WHERE a.isFeatured = true " +
           "ORDER BY a.viewCount DESC, a.createdAt DESC")
    List<Artwork> findFeaturedWithDetails();
}
