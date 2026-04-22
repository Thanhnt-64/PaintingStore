package com.vn.ManageStore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Find artwork by ID with all details (artist, images, categories)
     */
    @Query("SELECT DISTINCT a FROM Artwork a " +
           "LEFT JOIN FETCH a.artist " +
           "LEFT JOIN FETCH a.images " +
           "LEFT JOIN FETCH a.categories " +
           "WHERE a.artworkId = :id")
    Optional<Artwork> findByIdWithDetails(@Param("id") Integer id);

    /**
     * Browse artworks by category with pagination
     */
    @Query("SELECT DISTINCT a FROM Artwork a " +
           "LEFT JOIN a.artist " +
           "LEFT JOIN a.images " +
           "LEFT JOIN a.categories c " +
           "WHERE a.isPublished = true AND c.categoryId = :categoryId " +
           "ORDER BY a.viewCount DESC, a.createdAt DESC")
    Page<Artwork> findByCategoryIdWithDetails(@Param("categoryId") Integer categoryId, Pageable pageable);

    /**
     * Browse artworks by artist with pagination
     */
    @Query("SELECT DISTINCT a FROM Artwork a " +
           "LEFT JOIN a.artist " +
           "LEFT JOIN a.images " +
           "WHERE a.isPublished = true AND a.artistId = :artistId " +
           "ORDER BY a.viewCount DESC, a.createdAt DESC")
    Page<Artwork> findByArtistIdWithDetails(@Param("artistId") Integer artistId, Pageable pageable);

    /**
     * Browse all published artworks with pagination and sorting
     */
    @Query("SELECT DISTINCT a FROM Artwork a " +
           "LEFT JOIN a.artist " +
           "LEFT JOIN a.images " +
           "WHERE a.isPublished = true " +
           "ORDER BY a.viewCount DESC, a.createdAt DESC")
    Page<Artwork> findAllPublishedWithDetails(Pageable pageable);
}
